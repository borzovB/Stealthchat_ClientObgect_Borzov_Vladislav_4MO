package org.face_recognition;

// Класс EncryptionAccaunt предоставляет методы для криптографических операций, включая хеширование паролей с использованием Argon2,
// шифрование и расшифровку данных с применением алгоритмов AES, RSA и ChaCha20, а также генерацию случайных кодов и ключей
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.Random;
import java.security.SecureRandom;

public class EncryptionAccaunt {

    // Хеширует строку с использованием алгоритма Argon2
    public static String Argon2(String text) {
        // Создает экземпляр Argon2
        Argon2 argon2 = Argon2Factory.create();
        try {
            // Параметры Argon2: итерации, объем памяти (КБ), параллелизм
            int iterations = 3;
            int memory = 65536;
            int parallelism = 100;
            // Хеширует текст и возвращает результат
            String hash = argon2.hash(iterations, memory, parallelism, text.toCharArray());
            return hash;
        } finally {
            // Очищает массив символов для безопасности
            argon2.wipeArray(text.toCharArray());
        }
    }

    // Расшифровывает данные с использованием AES
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES"); // Инициализирует шифр AES
        cipher.init(Cipher.DECRYPT_MODE, key); // Настраивает режим расшифровки
        byte[] decodedData = Base64.getDecoder().decode(encryptedData); // Декодирует входные данные из Base64
        byte[] decryptedData = cipher.doFinal(decodedData); // Выполняет расшифровку
        return new String(decryptedData); // Возвращает расшифрованный текст
    }

    // Шифрует данные с использованием AES
    static String encryptWithAES(String data, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES"); // Инициализирует шифр AES
        cipher.init(Cipher.ENCRYPT_MODE, aesKey); // Настраивает режим шифрования
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8")); // Шифрует данные
        return Base64.getEncoder().encodeToString(encryptedBytes); // Возвращает зашифрованные данные в Base64
    }

    // Генерирует случайный код длиной 32 символа
    static String generateCode() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[24];
        random.nextBytes(randomBytes); // Генерирует случайные байты
        // Кодирует в Base64 и обрезает до 32 символов
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, 32);
    }

    // Возвращает случайный код алгоритма шифрования (1a, 1b или 1c)
    public static String getRandomString() {
        String[] options = {"1a", "1b", "1c"};
        Random random = new Random();
        int index = random.nextInt(options.length);
        return options[index];
    }

    // Генерирует симметричный ключ AES длиной 128 бит
    static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // Устанавливает длину ключа
        return keyGenerator.generateKey(); // Возвращает сгенерированный ключ
    }

    // Шифрует данные с использованием RSA
    static String encryptWithRSA(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA"); // Инициализирует шифр RSA
        cipher.init(Cipher.ENCRYPT_MODE, publicKey); // Настраивает режим шифрования
        byte[] encryptedBytes = cipher.doFinal(data); // Шифрует данные
        return Base64.getEncoder().encodeToString(encryptedBytes); // Возвращает результат в Base64
    }

    // Шифрует текст с использованием ChaCha20
    String chaha20Encript(String password, String originalText) throws Exception {
        byte[] salt = generateSalt(); // Генерирует случайную соль
        byte[] key = generateKeyFromPasswordChacha20(password, salt); // Генерирует ключ из пароля и соли
        byte[] nonce = generateNonce(password); // Генерирует nonce из пароля
        // Шифрует текст
        String encryptedText = Base64.getEncoder().encodeToString(encryptChacha20(originalText.getBytes(), key, nonce));
        // Объединяет соль, nonce и зашифрованный текст
        String storedData = Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(nonce) + ":" + encryptedText;
        return storedData;
    }

    // Генерирует случайную соль для ChaCha20
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt); // Заполняет массив случайными байтами
        return salt;
    }

    // Генерирует ключ для ChaCha20 из пароля и соли
    public static byte[] generateKeyFromPasswordChacha20(String password, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Использует SHA-256
        digest.update(salt); // Добавляет соль
        return digest.digest(password.getBytes()); // Возвращает хеш пароля
    }

    // Генерирует 12-байтовый nonce для ChaCha20 из пароля
    public static byte[] generateNonce(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] passwordHash = digest.digest(password.getBytes());
        byte[] nonce = new byte[12];
        System.arraycopy(passwordHash, 0, nonce, 0, 12); // Копирует первые 12 байт хеша
        return nonce;
    }

    // Выполняет шифрование с использованием ChaCha20
    public static byte[] encryptChacha20(byte[] plainText, byte[] key, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("ChaCha20"); // Инициализирует шифр ChaCha20
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20"); // Создает спецификацию ключа
        ChaCha20ParameterSpec paramSpec = new ChaCha20ParameterSpec(nonce, 0); // Устанавливает nonce
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec); // Настраивает режим шифрования
        byte[] encryptedBytes = cipher.doFinal(plainText); // Шифрует данные
        return encryptedBytes;
    }

    // Выполняет расшифровку с использованием ChaCha20
    public static byte[] decryptChaha20(byte[] decodedBytes, byte[] key, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("ChaCha20"); // Инициализирует шифр ChaCha20
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20"); // Создает спецификацию ключа
        ChaCha20ParameterSpec paramSpec = new ChaCha20ParameterSpec(nonce, 0); // Устанавливает nonce
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec); // Настраивает режим расшифровки
        byte[] decryptedBytes = cipher.doFinal(decodedBytes); // Расшифровывает данные
        return decryptedBytes;
    }

    // Расшифровывает данные, зашифрованные с использованием ChaCha20
    String chaha20Decrypt(String password, String storedData) throws Exception {
        // Разделяет хранимые данные на соль, nonce и зашифрованный текст
        String[] parts = storedData.split(":");
        byte[] storedSalt = Base64.getDecoder().decode(parts[0]);
        byte[] storedNonce = Base64.getDecoder().decode(parts[1]);
        String storedCipherText = parts[2];
        byte[] storedCipher = Base64.getDecoder().decode(storedCipherText);
        // Генерирует ключ из пароля и соли
        byte[] storedKey = generateKeyFromPasswordChacha20(password, storedSalt);
        // Расшифровывает данные
        String decryptedText = new String(decryptChaha20(storedCipher, storedKey, storedNonce));
        return decryptedText;
    }
}
