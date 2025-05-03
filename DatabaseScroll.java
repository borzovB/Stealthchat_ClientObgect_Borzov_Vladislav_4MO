package org.face_recognition;

// Класс DatabaseScroll предоставляет методы для работы с базой данных SQLite,
// связанных с получением и удалением сообщений в чатах
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseScroll {

    // Константа с именем базы данных
    private static final String DATABASE_NAME = "user_accounts.db";
    // URL для подключения к базе данных SQLite
    private static final String DB_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Идентификатор контакта (входной параметр)
    private static String input;
    // Экземпляр класса для работы с файлами
    private static Fileslock fileslock = new Fileslock();
    // Экземпляр класса для работы с базой данных
    private static Database database = new Database();
    // Идентификатор аккаунта
    private static String accaunt_id;

    // Конструктор, инициализирующий параметры
    public DatabaseScroll(String input, String accaunt_id) {
        this.input = input;
        this.accaunt_id = accaunt_id;
    }

    // Устанавливает соединение с базой данных SQLite
    public static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к базе данных SQLite: " + e.getMessage());
            return null;
        }
    }

    // Удаляет сообщения и связанные с ними сессии, если соответствующий файл не существует
    public static void delMessang() {
        String query = "SELECT m.file_id, m.participant_status, m.session_id, m.message_status " +
                "FROM messages m " +
                "LEFT JOIN chats c ON m.conversation_id = c.conversation_id " +
                "WHERE c.account_id = ? AND c.contact_id = ? AND m.message_status <> false";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, accaunt_id);
                    pstmt.setString(2, input);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String nameFile = rs.getString("file_id");
                            String sessionId = rs.getString("session_id");
                            if (!fileslock.fileExists(nameFile)) {
                                database.deleteMessageAndSession(conn, nameFile, sessionId);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выборке данных из messages: " + e.getMessage());
        }
    }

    // Возвращает общее количество сообщений для указанного аккаунта и контакта
    public static int getTotalPanels() {
        // Запускает удаление отсутствующих файлов в отдельном потоке
        Thread del = new Thread(() -> delMessang());
        del.start();
        try {
            del.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String query = "SELECT COUNT(*) FROM messages m " +
                "LEFT JOIN chats c ON m.conversation_id = c.conversation_id " +
                "WHERE c.account_id = ? AND c.contact_id = ? AND m.message_status <> false";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accaunt_id);
            pstmt.setString(2, input);
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

    // Получает данные сообщения по индексу
    public static PanelData getPanel(int index) {
        String query = "SELECT m.file_id, m.participant_status FROM messages m " +
                "LEFT JOIN chats c ON m.conversation_id = c.conversation_id " +
                "WHERE c.account_id = ? AND c.contact_id = ? AND m.message_status <> false " +
                "LIMIT 1 OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accaunt_id);
            pstmt.setString(2, input);
            pstmt.setInt(3, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nameFile = rs.getString("file_id");
                    boolean status = rs.getBoolean("participant_status");
                    return new PanelData(nameFile, status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панели: " + e.getMessage());
        }
        return null;
    }

    // Получает список сообщений в заданном диапазоне
    public static List<PanelData> getPanels(int start, int end) {
        List<PanelData> panels = new ArrayList<>();
        String query = "SELECT m.file_id, m.participant_status FROM messages m " +
                "LEFT JOIN chats c ON m.conversation_id = c.conversation_id " +
                "WHERE c.account_id = ? AND c.contact_id = ? AND m.message_status <> false " +
                "LIMIT ? OFFSET ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accaunt_id);
            pstmt.setString(2, input);
            pstmt.setInt(3, end - start + 1);
            pstmt.setInt(4, start);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nameFile = rs.getString("file_id");
                    boolean status = rs.getBoolean("participant_status");
                    panels.add(new PanelData(nameFile, status));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении панелей: " + e.getMessage());
        }
        return panels;
    }
}
