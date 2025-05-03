package org.face_recognition;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.ArrayList;
import java.util.List;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

//Класс отвечает за шифрование и дешифрование данных

public class KeyGet {
    // Константа для размера блока данных при шифровании
    private static final int BLOCK_SIZE = 1024;

    // Статический блок для добавления провайдера BouncyCastle
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Инициализирует и возвращает объект Cipher для указанного режима, ключа, алгоритма и вектора инициализации
    private static Cipher getCipher(int mode, String key, String alg, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), alg);
        Cipher cipher = Cipher.getInstance(alg + "/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(mode, keySpec, ivSpec);
        return cipher;
    }

    // Шифрует изображение поблочно и сохраняет результат в файл
    public static void encryptImage(String inputFilePath, String outputFilePath, String key, String alg) {
        try {
            byte[] imageData = Files.readAllBytes(new File(inputFilePath).toPath()); // Читает изображение в массив байтов
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                // Генерирует случайный вектор инициализации (IV) и записывает его в начало файла
                byte[] iv = new byte[16];
                SecureRandom.getInstanceStrong().nextBytes(iv);
                fos.write(iv);

                Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, alg, iv); // Инициализирует шифр для шифрования

                // Шифрует данные поблочно
                int blockCount = (int) Math.ceil((double) imageData.length / BLOCK_SIZE);
                for (int i = 0; i < blockCount; i++) {
                    int start = i * BLOCK_SIZE;
                    int length = Math.min(BLOCK_SIZE, imageData.length - start);
                    byte[] block = new byte[length];
                    System.arraycopy(imageData, start, block, 0, length);

                    byte[] encryptedBlock = cipher.update(block);
                    if (encryptedBlock != null) {
                        fos.write(encryptedBlock); // Записывает зашифрованный блок в файл
                    }
                }

                byte[] finalBlock = cipher.doFinal(); // Завершает шифрование
                fos.write(finalBlock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Шифрует массив байтов изображения и сохраняет результат в файл
    public static void encryptImageIcon(byte[] imageData, String outputFilePath, String key, String alg) {
        try {
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                // Генерирует случайный IV и записывает его в начало файла
                byte[] iv = new byte[16];
                SecureRandom.getInstanceStrong().nextBytes(iv);
                fos.write(iv);

                Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, alg, iv); // Инициализирует шифр

                // Шифрует данные поблочно
                int blockCount = (int) Math.ceil((double) imageData.length / BLOCK_SIZE);
                for (int i = 0; i < blockCount; i++) {
                    int start = i * BLOCK_SIZE;
                    int length = Math.min(BLOCK_SIZE, imageData.length - start);
                    byte[] block = new byte[length];
                    System.arraycopy(imageData, start, block, 0, length);

                    byte[] encryptedBlock = cipher.update(block);
                    if (encryptedBlock != null) {
                        fos.write(encryptedBlock);
                    }
                }

                byte[] finalBlock = cipher.doFinal();
                fos.write(finalBlock); // Записывает финальный зашифрованный блок
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Возвращает расширение файла или "Неизвестный", если расширение отсутствует
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex == -1) ? "Неизвестный" : name.substring(lastIndex + 1);
    }

    // Перешифровывает файл с использованием нового ключа и алгоритма
    public static void reencryptFileBlocks(
            String oldFilePath,
            String oldAlg,
            String oldKey,
            String newKey,
            String newAlg,
            String newFilePath
    ) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        try (
                FileInputStream fis = new FileInputStream(oldFile);
                FileOutputStream fos = new FileOutputStream(newFile)
        ) {
            // Читает IV из старого файла
            byte[] oldIv = new byte[16];
            fis.read(oldIv);

            // Читает длину и байты расширения файла
            int extLength = fis.read();
            byte[] extBytes = new byte[extLength];
            fis.read(extBytes);

            // Генерирует новый IV и записывает его в новый файл
            byte[] newIv = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(newIv);
            fos.write(newIv);

            // Записывает расширение файла в новый файл
            fos.write(extLength);
            fos.write(extBytes);

            // Преобразует коды алгоритмов в их названия
            String algorithmOld = switch (oldAlg.toLowerCase()) {
                case "1a" -> "AES";
                case "1b" -> "Twofish";
                case "1c" -> "Serpent";
                default -> throw new NoSuchAlgorithmException("Unknown algorithm code: " + oldAlg);
            };

            String algorithmNew = switch (newAlg.toLowerCase()) {
                case "1a" -> "AES";
                case "1b" -> "Twofish";
                case "1c" -> "Serpent";
                default -> throw new NoSuchAlgorithmException("Unknown algorithm code: " + newAlg);
            };

            // Инициализирует шифры для расшифровки и шифрования
            Cipher decryptCipher = getCipher(Cipher.DECRYPT_MODE, oldKey, algorithmOld, oldIv);
            Cipher encryptCipher = getCipher(Cipher.ENCRYPT_MODE, newKey, algorithmNew, newIv);

            // Поблочно расшифровывает и зашифровывает данные
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] block = Arrays.copyOfRange(buffer, 0, bytesRead);
                byte[] decryptedBlock = decryptCipher.update(block);
                if (decryptedBlock != null) {
                    byte[] encryptedBlock = encryptCipher.update(decryptedBlock);
                    if (encryptedBlock != null) {
                        fos.write(encryptedBlock);
                    }
                }
            }

            // Обрабатывает финальные блоки
            byte[] finalDecrypted = decryptCipher.doFinal();
            byte[] finalEncrypted = encryptCipher.doFinal(finalDecrypted);
            fos.write(finalEncrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Шифрует файл с сохранением его расширения
    static void encryptFile(String inputFilePath, String outputFilePath, String key, String alg) {
        File inputFile = new File(inputFilePath);
        String fileExtension = getFileExtension(inputFile); // Получает расширение файла

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            // Генерирует и записывает IV
            byte[] iv = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            fos.write(iv);

            // Записывает длину и байты расширения
            byte[] extBytes = fileExtension.getBytes();
            fos.write(extBytes.length);
            fos.write(extBytes);

            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, alg, iv); // Инициализирует шифр

            // Шифрует файл поблочно
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] block = Arrays.copyOfRange(buffer, 0, bytesRead);
                byte[] encryptedBlock = cipher.update(block);
                if (encryptedBlock != null) {
                    fos.write(encryptedBlock);
                }
            }

            byte[] finalBlock = cipher.doFinal();
            fos.write(finalBlock); // Записывает финальный блок
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Генерирует пару ключей RSA
    static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // Шифрует аудиоданные и сохраняет их в файл
    static void saveEncryptedAudio(byte[] audioData, String filename, String key, String alg) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // Генерирует и записывает IV
            byte[] iv = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            fos.write(iv);

            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, alg, iv); // Инициализирует шифр

            // Шифрует аудио поблочно
            int blockCount = (int) Math.ceil((double) audioData.length / BLOCK_SIZE);
            for (int i = 0; i < blockCount; i++) {
                int start = i * BLOCK_SIZE;
                int length = Math.min(BLOCK_SIZE, audioData.length - start);
                byte[] block = Arrays.copyOfRange(audioData, start, start + length);
                byte[] encryptedBlock = cipher.update(block);
                if (encryptedBlock != null) {
                    fos.write(encryptedBlock);
                }
            }
            byte[] finalBlock = cipher.doFinal();
            fos.write(finalBlock); // Записывает финальный блок
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Шифрует текст и записывает его в файл
    static void encryptAndWriteToFile(String algorithm, String provider, String keyString, String text, String filename) throws Exception {
        byte[] inputData = text.getBytes();
        List<byte[]> blocks = splitIntoBlocks(inputData, 4096); // Разбивает текст на блоки

        // Создает директории для файла, если они не существуют
        File file = new File(filename);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию: " + parentDir.getAbsolutePath());
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (byte[] block : blocks) {
                fos.write(encryptBlock(algorithm, provider, keyString, block)); // Шифрует и записывает каждый блок
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Шифрует блок данных с использованием указанного алгоритма
    static byte[] encryptBlock(String algorithm, String provider, String keyString, byte[] block) throws Exception {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyString), algorithm);
        Cipher cipher = (provider == null) ? Cipher.getInstance(algorithm + "/ECB/PKCS5Padding") : Cipher.getInstance(algorithm + "/ECB/PKCS5Padding", provider);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(block);
    }

    // Разбивает данные на блоки указанного размера
    private static List<byte[]> splitIntoBlocks(byte[] data, int blockSize) {
        List<byte[]> blocks = new ArrayList<>();
        for (int i = 0; i < data.length; i += blockSize) {
            int len = Math.min(blockSize, data.length - i);
            byte[] block = new byte[len];
            System.arraycopy(data, i, block, 0, len);
            blocks.add(block);
        }
        return blocks;
    }

    // Генерирует ключ AES
    static String generationKeyAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGenAES = KeyGenerator.getInstance("AES");
        String keyAES = Base64.getEncoder().encodeToString(keyGenAES.generateKey().getEncoded());
        return keyAES;
    }

    // Генерирует ключ для Twofish или Serpent
    static String generationKeyTwoFishAndSerpent(String alg, String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator keyGenTwofishAndSepent = KeyGenerator.getInstance(alg, str);
        String keyTwofishAndSepent = Base64.getEncoder().encodeToString(keyGenTwofishAndSepent.generateKey().getEncoded());
        return keyTwofishAndSepent;
    }

    // Кодирует ключ в строку Base64
    static String encodeKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Декодирует публичный ключ RSA из строки Base64
    static PublicKey decodePublicKeyFromString(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    // Шифрует данные с использованием публичного ключа RSA
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Расшифровывает данные с использованием приватного ключа RSA
    static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    // Декодирует приватный ключ RSA из строки Base64
    static PrivateKey decodePrivateKeyFromString(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
