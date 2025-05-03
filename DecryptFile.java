package org.face_recognition;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Base64;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import javax.swing.Timer;
import java.io.*;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import java.awt.*;

// Класс DecryptFile предоставляет методы для расшифровки файлов, включая текстовые сообщения,
// изображения и аудиофайлы, с поддержкой различных алгоритмов шифрования (AES, Twofish, Serpent)
public class DecryptFile {

    // Регистрирует провайдер BouncyCastle для поддержки Twofish и Serpent
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Выбирает алгоритм шифрования для текстовых данных и возвращает расшифрованный текст
    public static String choosing_cipher(String alg, String nameFile, String key) throws Exception {

        String result = null;

        switch (alg){
            case "1a": {

                result = new String(readAndDecryptFile("AES", null, key, nameFile));

                break;
            }

            case "1b": {

                result = new String(readAndDecryptFile("Twofish", "BC", key, nameFile));

                break;
            }

            case "1c": {

                result = new String(readAndDecryptFile("Serpent", "BC", key, nameFile));

                break;
            }

        }

        return  result;

    }

    // Расшифровывает файл и сохраняет его с использованием JFileChooser
    private static void decryptFile(File inputFile, String alg, String key) {
        try (FileInputStream fis = new FileInputStream(inputFile)) {

            // Читаем IV
            byte[] iv = new byte[16];
            fis.read(iv);

            // Читаем длину и само расширение файла
            int extLength = fis.read();
            byte[] extBytes = new byte[extLength];
            fis.read(extBytes);
            String fileExtension = new String(extBytes);

            // Используем JFileChooser для выбора местоположения и имени для сохранения расшифрованного файла
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите место для сохранения расшифрованного файла");
            fileChooser.setSelectedFile(new File("decrypted_file" + "." + fileExtension)); // Задаем начальное имя файла

            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return; // Если пользователь отменяет, выходим из метода
            }

            File selectedFile = fileChooser.getSelectedFile();
            String outputFilename = selectedFile.getAbsolutePath(); // Получаем выбранный путь и имя файла

            try (FileOutputStream fos = new FileOutputStream(outputFilename)) {
                Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, alg, iv);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] block = Arrays.copyOfRange(buffer, 0, bytesRead);
                    byte[] decryptedBlock = cipher.update(block);
                    if (decryptedBlock != null) {
                        fos.write(decryptedBlock);
                    }
                }

                byte[] finalBlock = cipher.doFinal();
                fos.write(finalBlock);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Выбирает алгоритм для расшифровки файлов и вызывает decryptFile
    public static void choosing_cipher_file_oll(String alg, String key, File destinationFile) throws Exception {

        switch (alg){
            case "1a": {

                decryptFile(destinationFile, "AES", key); // Загружаем для воспроизведения

                break;
            }

            case "1b": {

                decryptFile(destinationFile, "Twofish", key); // Загружаем для воспроизведения

                break;
            }

            case "1c": {

                decryptFile(destinationFile, "Serpent", key); // Загружаем для воспроизведения

                break;
            }

        }

    }

    // Выбирает алгоритм для воспроизведения аудиофайлов
    public static void choosing_cipher_vois(String alg, String key, File destinationFile, AudioPlayerState state, JMenuItem audioDownload, Frame frame) throws Exception {

        switch (alg){
            case "1a": {

                playAudioFromBinFile(destinationFile, state, "AES", key, audioDownload, frame); // Загружаем для воспроизведения

                break;
            }

            case "1b": {

                playAudioFromBinFile(destinationFile, state, "Twofish",  key, audioDownload, frame); // Загружаем для воспроизведения

                break;
            }

            case "1c": {

                playAudioFromBinFile(destinationFile, state, "Serpent", key, audioDownload, frame); // Загружаем для воспроизведения

                break;
            }

        }

    }

    // Выбирает алгоритм для расшифровки изображений
    public static byte[] choosing_cipher_image(String alg, String key, File destinationFile) throws Exception {

        byte[] resultImageByte = null;

        switch (alg){
            case "1a": {

                resultImageByte = playImageFromBinFile(destinationFile,"AES",  key); // Загружаем для воспроизведения

                break;
            }

            case "1b": {

                resultImageByte = playImageFromBinFile(destinationFile, "Twofish", key); // Загружаем для воспроизведения

                break;
            }

            case "1c": {

                resultImageByte = playImageFromBinFile(destinationFile, "Serpent", key); // Загружаем для воспроизведения

                break;
            }

        }

        return resultImageByte;
    }

    // Расшифровывает изображение из файла
    static byte[] playImageFromBinFile(File file, String alg, String key) {

        byte[] decryptedData = null;

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream()) {

            byte[] iv = new byte[16]; // Размер IV фиксирован (16 байт)
            if (fis.read(iv) != iv.length) {
                throw new IOException("Ошибка чтения IV из файла!");
            }

            Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, alg, iv);

            byte[] buffer = new byte[1024]; // Читаем блоками по 1024 байта
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBlock = cipher.update(buffer, 0, bytesRead);
                if (decryptedBlock != null) {
                    decryptedStream.write(decryptedBlock);
                }
            }

            byte[] finalBlock = cipher.doFinal();
            decryptedStream.write(finalBlock);
            decryptedData = decryptedStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptedData;
    }

    // Читает и расшифровывает файл (для текстовых данных)
    private static byte[] readAndDecryptFile(String algorithm, String provider, String keyString, String filename) throws Exception {
        try (FileInputStream fis = new FileInputStream(filename); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            List<byte[]> decryptedBlocks = new ArrayList<>();
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] block = new byte[bytesRead];
                System.arraycopy(buffer, 0, block, 0, bytesRead);
                decryptedBlocks.add(decryptBlock(algorithm, provider, keyString, block));
            }
            return mergeBlocks(decryptedBlocks);
        }
    }

    // Объединяет расшифрованные блоки
    private static byte[] mergeBlocks(List<byte[]> blocks) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] block : blocks) {
            bos.write(block);
        }
        return bos.toByteArray();
    }

    // Расшифровывает отдельный блок данных
    static byte[] decryptBlock(String algorithm, String provider, String keyString, byte[] block) throws Exception {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyString), algorithm);
        Cipher cipher = (provider == null) ? Cipher.getInstance(algorithm + "/ECB/PKCS5Padding") : Cipher.getInstance(algorithm + "/ECB/PKCS5Padding", provider);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(block);
    }

    // Сохраняет аудиоданные как WAV-файл
    private static void saveAudioAsWav(byte[] audioData, Frame frame) {
        new Thread(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить аудиофайл");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("WAV Audio", "wav"));
            fileChooser.setSelectedFile(new File("audio.wav"));

            int userSelection = fileChooser.showSaveDialog(frame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();

                if (!filePath.toLowerCase().endsWith(".wav")) {
                    filePath += ".wav";
                    fileToSave = new File(filePath);
                }

                // Создаем прогресс-бар
                JDialog progressDialog = new JDialog(frame, "Сохранение", true);
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setIndeterminate(true);
                progressDialog.setLayout(new BorderLayout());
                progressDialog.add(new JLabel("Сохранение аудиофайла..."), BorderLayout.NORTH);
                progressDialog.add(progressBar, BorderLayout.CENTER);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(frame);

                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));


                try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                     AudioInputStream audioStream = new AudioInputStream(bais,
                             new AudioFormat(44100.0f, 16, 1, true, true),
                             audioData.length / 2);
                     OutputStream os = new FileOutputStream(fileToSave)) {

                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, os);
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(frame,
                                "Аудиофайл сохранен успешно!",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(frame,
                                "Ошибка при сохранении аудиофайла: " + e.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Воспроизводит аудио из зашифрованного файла
    private static void playAudioFromBinFile(File file, AudioPlayerState state, String alg, String key, JMenuItem audioDownload, Frame frame) {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream()) {

            byte[] iv = new byte[16]; // Размер IV фиксирован (16 байт)
            if (fis.read(iv) != iv.length) {
                throw new IOException("Ошибка чтения IV из файла!");
            }

            Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, alg, iv);

            byte[] buffer = new byte[1024]; // Читаем блоками по 1024 байта
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBlock = cipher.update(buffer, 0, bytesRead);
                if (decryptedBlock != null) {
                    decryptedStream.write(decryptedBlock);
                }
            }

            byte[] finalBlock = cipher.doFinal();
            decryptedStream.write(finalBlock);
            byte[] decryptedData = decryptedStream.toByteArray();

            audioDownload.addActionListener(e -> {

                saveAudioAsWav(decryptedData, frame);

            });

            // Воспроизведение аудио
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, true);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decryptedData);
            AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, decryptedData.length / format.getFrameSize());

            state.clip = AudioSystem.getClip();
            state.clip.open(audioStream);

            // Добавляем слушатель для отслеживания окончания аудио
            state.clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) { // Когда аудио заканчивается
                    SwingUtilities.invokeLater(() -> {
                        state.clip.setFramePosition(0); // Возвращаемся в начало
                        state.progressBar.setValue(0);  // Обнуляем прогресс-бар
                        state.playPauseButton.setIcon(state.playIcon); // Меняем иконку на "Play"
                        state.isPlaying = false; // Переключаем состояние
                        state.timer.stop(); // Останавливаем таймер обновления прогресс-бара
                    });
                }
            });

            // Настройка кнопки воспроизведения и таймера
            state.playPauseButton.setEnabled(true);
            state.timer = new Timer(100, e -> updateProgressBar(state));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Обновленный getCipher с IV
    private static Cipher getCipher(int mode, String key, String alg, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), alg);
        Cipher cipher = Cipher.getInstance(alg + "/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(mode, keySpec, ivSpec);
        return cipher;
    }

    // Обновляет прогресс-бар во время воспроизведения.
    private static void updateProgressBar(AudioPlayerState state) {
        if (state.clip != null && state.clip.isRunning() && !state.isSeeking) {
            int progress = (int) ((state.clip.getFramePosition() / (double) state.clip.getFrameLength()) * 100);
            state.progressBar.setValue(progress);
        }
    }

}
