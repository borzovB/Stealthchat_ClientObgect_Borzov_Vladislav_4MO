package org.face_recognition;

// Класс используется для хранения данных о сообщении, включая путь к файлу и статус владельца
public class PanelData {

    // Имя файла или путь к файлу, связанному с сообщением
    private String nameFile;

    // Статус сообщения: true — сообщение от владельца учётной записи, false — от контакта
    private boolean status;

    // Конструктор инициализирует имя файла и статус сообщения
    public PanelData(String nameFile, boolean status) {
        this.nameFile = nameFile;
        this.status = status;
    }

    // Получить имя или путь к файлу
    public String getName() {
        return nameFile;
    }

    // Получить статус сообщения
    public boolean getStat() {
        return status;
    }
}
