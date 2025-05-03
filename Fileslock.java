package org.face_recognition;

// Класс Fileslock предоставляет методы для работы с файлами, включая удаление, выбор,
// отправку и получение файлов, а также управление прокруткой JScrollPane
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;

public class Fileslock extends Component {

    // Удаляет файл по указанному пути
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Файл удалён: " + filePath);
            } else {
                System.out.println("Не удалось удалить файл: " + filePath);
            }
        } else {
            System.out.println("Файл не существует или это не файл: " + filePath);
        }
    }

    // Прокручивает JScrollPane в самый низ
    static void scrollToBottom(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int maximum = verticalBar.getMaximum();
            int visibleAmount = verticalBar.getVisibleAmount();
            int maxScrollable = maximum - visibleAmount;
            verticalBar.setValue(maxScrollable - 3);
        });
    }

    // Возвращает абсолютный путь к файлу
    public static String getFilePath(File file) {
        return file.getAbsolutePath();
    }

    // Отправляет файл получателю через ObjectOutputStream
    void sendText(String recipientName, ObjectOutputStream out, String myID, JFrame frame, JTextArea textsClient, String nameFile, String fileName) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Имя получателя не может быть пустым", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = new File(nameFile);
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024 * 1024]; // Буфер 1 МБ
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    boolean isLastChunk = (fis.available() == 0);
                    byte[] fileChunk = Arrays.copyOf(buffer, bytesRead);
                    out.writeObject(new FileTransferData(myID, recipientName, fileChunk, isLastChunk, fileName));
                }
            } catch (IOException e) {
                textsClient.append("Ошибка при отправке файла: " + e.getMessage() + "\n");
            }
        } else {
            textsClient.append("Файл не найден: " + nameFile + "\n");
        }
    }

    // Открывает диалог выбора файла и возвращает выбранный файл
    public static File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    // Проверяет, существует ли файл
    public static boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        File file = new File(fileName);
        return file.exists() && file.isFile();
    }

    // Обрабатывает получение файла
    boolean handleFileChunk(FileTransferData data, JTextArea textsClient, String fileName) {
        File file = new File(fileName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        return receiveFileChunk(data);
    }

    // Сохраняет файла
    boolean receiveFileChunk(FileTransferData data) {
        File file = new File(data.getFileName());
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(data.getFileChunk());
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
            return false;
        }
        if (data.isLastChunk()) {
            System.out.println("Файл полностью получен: " + data.getFileName());
        }
        return data.isLastChunk();
    }
}
