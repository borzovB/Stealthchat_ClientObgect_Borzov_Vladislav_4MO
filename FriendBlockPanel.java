package org.face_recognition;

// Класс FriendBlockPanel отвечает за перенос данных для сообщений о заблокированных пользователях
// Он используется для хранения и передачи информации о записи блокировки и идентификаторе заблокированного пользователя
public class FriendBlockPanel {
    // Уникальный идентификатор записи о блокировке
    private String recordID;
    // Идентификатор учетной записи заблокированного пользователя
    private String account_id;

    // Конструктор, инициализирующий поля класса
    public FriendBlockPanel(String recordID, String account_id) {
        this.recordID = recordID;
        this.account_id = account_id;
    }

    // Возвращает уникальный идентификатор записи о блокировке
    public String getRecordIDFriend() {
        return recordID;
    }

    // Возвращает идентификатор учетной записи заблокированного пользователя
    public String getFriendID() {
        return account_id;
    }
}
