package org.face_recognition;

// Класс используется для хранения данных о заявке в друзья и её отображения в пользовательском интерфейсе
public class PanelDataRequests {

    // Уникальный идентификатор записи
    private String recordId;

    // Идентификатор отправителя заявки
    private String senderId;

    // Статус заявки: true — принята, false — отклонена или ожидает
    private boolean requestStatus;

    // Статус уведомления: true — нужно показать, false — скрыто
    private boolean notificationStatus;

    // Флаг блокировки пользователя
    private boolean lockFlag;

    // Публичный ключ отправителя заявки
    private String publicKey;

    // Локальный идентификатор, используемый на стороне клиента
    private String id_friend_side;

    // Идентификатор, используемый на стороне сервера
    private String id_server_side;

    // Конструктор, принимающий все параметры заявки
    public PanelDataRequests(String recordId, String senderId,
                             boolean requestStatus, boolean notificationStatus,
                             boolean lockFlag, String publicKey,
                             String id_friend_side, String id_server_side) {
        this.recordId = recordId;
        this.senderId = senderId;
        this.requestStatus = requestStatus;
        this.notificationStatus = notificationStatus;
        this.lockFlag = lockFlag;
        this.publicKey = publicKey;
        this.id_friend_side = id_friend_side;
        this.id_server_side = id_server_side;
    }

    // Получить ID записи
    public String getRecordId() {
        return recordId;
    }

    // Получить ID отправителя
    public String getSenderId() {
        return senderId;
    }

    // Получить статус заявки
    public boolean getRequestStatus() {
        return requestStatus;
    }

    // Получить статус уведомления
    public boolean getNotificationStatus() {
        return notificationStatus;
    }

    // Получить флаг блокировки
    public boolean getLockFlag() {
        return lockFlag;
    }

    // Получить ID со стороны друга
    public String getFriendSide() {
        return id_friend_side;
    }

    // Получить ID со стороны сервера
    public String getServerSide() {
        return id_server_side;
    }

    // Получить публичный ключ
    public String getPublicKey() {
        return publicKey;
    }
}
