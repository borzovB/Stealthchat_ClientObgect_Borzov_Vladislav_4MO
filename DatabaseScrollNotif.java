package org.face_recognition;

// Класс DatabaseScrollNotif предоставляет методы для работы с базой данных SQLite
// для управления уведомлениями и запросами, включая получение данных о запросах,
// заявках, блокировках и планировании чатов
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseScrollNotif {

    // Константа с именем базы данных
    private static final String DATABASE_NAME = "user_accounts.db";
    // URL для подключения к базе данных SQLite
    private static final String DB_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Входной параметр, обычно идентификатор аккаунта
    private static String input;
    // Экземпляр для шифрования и дешифрования данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Ключ шифрования аккаунта
    private static String keyAc;

    // Конструктор, инициализирующий параметры
    public DatabaseScrollNotif(String input, String keyAc) {
        this.input = input;
        this.keyAc = keyAc;
    }

    // Устанавливает соединение с базой данных SQLite
    public static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            return null;
        }
    }

    // Возвращает общее количество записей в таблице request_responses для указанного аккаунта
    public static int getTotalPanelsFriend() {
        String query = "SELECT COUNT(*) FROM request_responses WHERE account_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении общего количества панелей: " + e.getMessage());
        }
        return 0;
    }

    // Выполняет удаление записей из базы данных по заданному SQL-запросу
    public static void delScroll(String query, String accountId) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, accountId);
            int rowsDeleted = ps.executeUpdate();
            System.out.println("Удалено строк: " + rowsDeleted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получает данные запроса по индексу из таблицы request_responses
    public static PanelDataRequests getPanelFriend(int index) {
        String query = "SELECT record_id, sender_id, request_status, notification_status, lock_flag, " +
                "public_key, record_ac_id_friend, record_ac_id_data FROM request_responses " +
                "WHERE account_id = ? LIMIT 1 OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String record_id = rs.getString("record_id");
                    String sender_id = rs.getString("sender_id");
                    boolean request_status = rs.getBoolean("request_status");
                    boolean notification_status = rs.getBoolean("notification_status");
                    boolean lock_flag = rs.getBoolean("lock_flag");
                    String public_key = rs.getString("public_key");
                    String record_ac_id_friend = rs.getString("record_ac_id_friend");
                    String record_ac_id_data = rs.getString("record_ac_id_data");
                    return new PanelDataRequests(record_id, sender_id, request_status, notification_status, lock_flag, public_key,
                            record_ac_id_friend, record_ac_id_data);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панели: " + e.getMessage());
        }
        return null;
    }

    // Получает список запросов из таблицы request_responses в заданном диапазоне
    public static List<PanelDataRequests> getPanelsFriend(int start, int end) {
        List<PanelDataRequests> panels = new ArrayList<>();
        String query = "SELECT record_id, sender_id, request_status, notification_status, lock_flag, " +
                "public_key, record_ac_id_friend, record_ac_id_data FROM request_responses " +
                "WHERE account_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, end - start + 1);
            pstmt.setInt(3, start);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String record_id = rs.getString("record_id");
                    String sender_id = rs.getString("sender_id");
                    boolean request_status = rs.getBoolean("request_status");
                    boolean notification_status = rs.getBoolean("notification_status");
                    boolean lock_flag = rs.getBoolean("lock_flag");
                    String public_key = rs.getString("public_key");
                    String record_ac_id_friend = rs.getString("record_ac_id_friend");
                    String record_ac_id_data = rs.getString("record_ac_id_data");
                    panels.add(new PanelDataRequests(record_id, sender_id, request_status, notification_status, lock_flag, public_key,
                            record_ac_id_friend, record_ac_id_data));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панелей: " + e.getMessage());
        }
        return panels;
    }

    // Возвращает общее количество записей в таблице application_responses для указанного аккаунта
    public static int getTotalPanelsFriend_1() {
        String query = "SELECT COUNT(*) FROM application_responses WHERE account_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении общего количества панелей: " + e.getMessage());
        }
        return 0;
    }

    // Получает данные заявки по индексу из таблицы application_responses
    public static RequestsResponses getPanelFriend_1(int index) {
        String query = "SELECT name_contact, request_status, lock_flag, record_id FROM application_responses " +
                "WHERE account_id = ? LIMIT 1 OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String sender_name = encryption.chaha20Decrypt(keyAc, rs.getString("name_contact"));
                    boolean request_status = rs.getBoolean("request_status");
                    boolean lock_flag = rs.getBoolean("lock_flag");
                    String record_id = rs.getString("record_id");
                    return new RequestsResponses(sender_name, request_status, lock_flag, record_id);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панели: " + e.getMessage());
        }
        return null;
    }

    // Получает список заявок из таблицы application_responses в заданном диапазоне
    public static List<RequestsResponses> getPanelsFriend_1(int start, int end) {
        List<RequestsResponses> panels = new ArrayList<>();
        String query = "SELECT name_contact, request_status, lock_flag, record_id FROM application_responses " +
                "WHERE account_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, end - start + 1);
            pstmt.setInt(3, start);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String sender_name = encryption.chaha20Decrypt(keyAc, rs.getString("name_contact"));
                    boolean request_status = rs.getBoolean("request_status");
                    boolean lock_flag = rs.getBoolean("lock_flag");
                    String record_id = rs.getString("record_id");
                    panels.add(new RequestsResponses(sender_name, request_status, lock_flag, record_id));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панелей: " + e.getMessage());
        }
        return panels;
    }

    // Возвращает общее количество записей в таблице clientBlock для указанного аккаунта
    public static int getTotalPanelsFriend_2() {
        String query = "SELECT COUNT(*) FROM clientBlock WHERE account_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении общего количества панелей: " + e.getMessage());
        }
        return 0;
    }

    // Получает данные блокировки по индексу из таблицы clientBlock
    public static FriendBlockPanel getPanelFriend_2(int index) {
        String query = "SELECT record_id, sender_id FROM clientBlock " +
                "WHERE account_id = ? LIMIT 1 OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String sender_id = rs.getString("sender_id");
                    String record_id = rs.getString("record_id");
                    return new FriendBlockPanel(record_id, sender_id);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панели: " + e.getMessage());
        }
        return null;
    }

    // Получает список блокировок из таблицы clientBlock в заданном диапазоне
    public static List<FriendBlockPanel> getPanelsFriend_2(int start, int end) {
        List<FriendBlockPanel> panels = new ArrayList<>();
        String query = "SELECT record_id, sender_id FROM clientBlock " +
                "WHERE account_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, end - start + 1);
            pstmt.setInt(3, start);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String sender_id = rs.getString("sender_id");
                    String record_id = rs.getString("record_id");
                    panels.add(new FriendBlockPanel(record_id, sender_id));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панелей: " + e.getMessage());
        }
        return panels;
    }

    // Получает имя контакта из таблицы chats по идентификатору аккаунта и контакта
    public String getContactName(String accountId, String contactId) {
        String query = "SELECT contact_name FROM chats WHERE account_id = ? AND contact_id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountId);
            stmt.setString(2, contactId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("contact_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Возвращает общее количество записей в таблице chat_planning для указанного аккаунта
    public static int getTotalPanelsFriend_4() {
        String query = "SELECT COUNT(*) FROM chat_planning WHERE account_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении общего количества панелей: " + e.getMessage());
        }
        return 0;
    }

    // Получает данные планирования чата по индексу из таблицы chat_planning
    public static GetChatPlanningPanel getPanelFriend_4(int index) {
        String query = "SELECT record_id, account_id, sender_id, messages, start_time, end_time, key_planning " +
                "FROM chat_planning WHERE account_id = ? LIMIT 1 OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String recordId = rs.getString("record_id");
                    String accountId = rs.getString("account_id");
                    String senderId = rs.getString("sender_id");
                    String messages = rs.getString("messages");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    String key = rs.getString("key_planning");
                    return new GetChatPlanningPanel(recordId, accountId, senderId, messages, startTime, endTime, key);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панели chat_planning: " + e.getMessage());
        }
        return null;
    }

    // Получает список данных планирования чатов из таблицы chat_planning в заданном диапазоне
    public static List<GetChatPlanningPanel> getPanelsFriend_4(int start, int end) {
        List<GetChatPlanningPanel> panels = new ArrayList<>();
        String query = "SELECT record_id, account_id, sender_id, messages, start_time, end_time, key_planning " +
                "FROM chat_planning WHERE account_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            pstmt.setInt(2, end - start + 1);
            pstmt.setInt(3, start);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String recordId = rs.getString("record_id");
                    String accountId = rs.getString("account_id");
                    String senderId = rs.getString("sender_id");
                    String messages = rs.getString("messages");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    String key = rs.getString("key_planning");
                    panels.add(new GetChatPlanningPanel(recordId, accountId, senderId, messages, startTime, endTime, key));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панелей chat_planning: " + e.getMessage());
        }
        return panels;
    }
}
