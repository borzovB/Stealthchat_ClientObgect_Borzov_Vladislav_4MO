package org.face_recognition;

// Класс FileTransferData позволяет передавать данные о файлах с сообщениями
// Он реализует интерфейс Serializable для передачи объектов через потоки, обеспечивая
// транспортировку фрагментов файлов между отправителем и получателем
import java.io.Serializable;

class FileTransferData implements Serializable {
    // Имя отправителя файла
    private String senderName;
    // Имя получателя файла
    private String recipientName;
    // Фрагмент данных файла в виде массива байтов
    private byte[] fileChunk;
    // Флаг, указывающий, является ли фрагмент последним
    private boolean isLastChunk;
    // Имя файла
    private String nameFile;

    // Конструктор, инициализирующий все поля класса
    public FileTransferData(String senderName, String recipientName, byte[] fileChunk, boolean isLastChunk, String nameFile) {
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.fileChunk = fileChunk;
        this.isLastChunk = isLastChunk;
        this.nameFile = nameFile;
    }

    // Возвращает имя отправителя файла
    public String getSenderName() {
        return senderName;
    }

    // Возвращает имя получателя файла
    public String getRecipientName() {
        return recipientName;
    }

    // Возвращает фрагмент данных файла
    public byte[] getFileChunk() {
        return fileChunk;
    }

    // Возвращает имя файла
    public String getFileName() {
        return nameFile;
    }

    // Проверяет, является ли фрагмент последним
    public boolean isLastChunk() {
        return isLastChunk;
    }
}
