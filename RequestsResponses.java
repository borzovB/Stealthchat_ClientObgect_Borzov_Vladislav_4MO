package org.face_recognition;

// Класс, представляющий информацию об ответах на заявки в друзья
public class RequestsResponses {

    // Идентификатор отправителя
    private String senderId;

    // Статус заявки (true — заявка принята, false — заявка отклонена)
    private boolean requestStatus;

    // Флаг блокировки (true — пользователь заблокирован, false — доступ разрешён)
    private boolean lockFlag;

    // Идентификатор записи (например, ID записи запроса или ответа)
    private String record_id;

    // Конструктор, который инициализирует все поля класса
    public RequestsResponses(String senderId, boolean requestStatus, boolean lockFlag, String record_id) {
        this.senderId = senderId; // Инициализация ID отправителя
        this.requestStatus = requestStatus; // Инициализация статуса заявки
        this.lockFlag = lockFlag; // Инициализация флага блокировки
        this.record_id = record_id; // Инициализация ID записи
    }

    // Метод для получения ID отправителя
    public String getSenderId() {
        return senderId; // Возвращаем ID отправителя
    }

    // Метод для получения статуса заявки
    public boolean getRequestStatus() {
        return requestStatus; // Возвращаем статус заявки (принята или отклонена)
    }

    // Метод для получения флага блокировки
    public boolean getLockFlag() {
        return lockFlag; // Возвращаем флаг блокировки (заблокирован или доступ разрешён)
    }

    // Метод для получения ID записи
    public String getRecord() {
        return record_id; // Возвращаем ID записи, связанную с запросом или ответом
    }

}
