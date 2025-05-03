package org.face_recognition;

// Класс GetChatPlanningPanel предназначен для хранения и передачи данных, необходимых для планирования бесед
// Он используется для создания панелей интерфейса, отображающих информацию о запланированных чатах,
// включая идентификаторы, временные рамки, сообщения и ключ шифрования
public class GetChatPlanningPanel {
    // Уникальный идентификатор записи в базе данных
    private String recordId;
    // Идентификатор учетной записи пользователя
    private String accountId;
    // Идентификатор отправителя сообщения
    private String senderId;
    // Текст сообщений, связанных с планированием беседы
    private String messages;
    // Время начала запланированной беседы
    private String startTime;
    // Время окончания запланированной беседы
    private String endTime;
    // Ключ шифрования, используемый для защиты данных беседы
    private String key;

    // Конструктор для инициализации всех полей класса
    public GetChatPlanningPanel(String recordId, String accountId, String senderId,
                                String messages, String startTime, String endTime, String key) {
        this.recordId = recordId;
        this.accountId = accountId;
        this.senderId = senderId;
        this.messages = messages;
        this.startTime = startTime;
        this.endTime = endTime;
        this.key = key;
    }

    // Возвращает уникальный идентификатор записи
    public String getRecordId() {
        return recordId;
    }

    // Возвращает идентификатор учетной записи
    public String getAccountId() {
        return accountId;
    }

    // Возвращает идентификатор отправителя
    public String getSenderId() {
        return senderId;
    }

    // Возвращает текст сообщений
    public String getMessages() {
        return messages;
    }

    // Возвращает время начала беседы
    public String getStartTime() {
        return startTime;
    }

    // Возвращает время окончания беседы
    public String getEndTime() {
        return endTime;
    }

    // Возвращает ключ шифрования
    public String getKey() {
        return key;
    }
}
