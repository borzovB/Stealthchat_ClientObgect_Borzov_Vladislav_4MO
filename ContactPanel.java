package org.face_recognition;

// Класс ContactPanel отвечает за управление панелью контактов в приложении
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ContactPanel {

    // Количество контактов, загружаемых за один раз
    private static final int limit = 7;
    // Имя файла базы данных SQLite
    private static final String DATABASE_NAME = "user_accounts.db";
    // Строка подключения к базе данных
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Имя таблицы для хранения информации о чатах
    private static Safety safety = new Safety();
    // Объект для работы с файлами
    private static Fileslock fileslock = new Fileslock();

    // Возвращает количество страниц контактов, доступных для загрузки
    int canLoadMoreFrom(String account_id) {
        // SQL-запрос для подсчёта записей в таблице chats
        String sqlCountQuery = "SELECT COUNT(*) FROM chats WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlCountQuery)) {

            pstmt.setString(1, account_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Общее количество записей в таблице
                int totalCount = rs.getInt(1);
                // Округляем вверх: ceil(totalCount / limit)
                return (int) Math.ceil((double) totalCount / limit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Значение по умолчанию в случае ошибки
        return 0;
    }

    // Возвращает остаток от деления общего количества контактов на limit
    int getRemainingRecords(String account_id) {
        // SQL-запрос для подсчёта записей в таблице chats
        String sqlCountQuery = "SELECT COUNT(*) FROM chats WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlCountQuery)) {

            // Фильтруем записи по account_id
            pstmt.setString(1, account_id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Общее количество контактов для аккаунта
                int totalCount = rs.getInt(1);
                // Вычисляем остаток от деления
                int remainder = totalCount % limit;

                return remainder;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Значение по умолчанию в случае ошибки
        return 0;
    }

    // Возвращает целую часть от деления общего количества контактов на limit
    int getRemainingMin(String account_id) {
        // SQL-запрос для подсчёта записей в таблице chats
        String sqlCountQuery = "SELECT COUNT(*) FROM chats WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlCountQuery)) {
            // Фильтруем по account_id
            pstmt.setString(1, account_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Общее количество записей в таблице
                int totalCount = rs.getInt(1);
                // Вычисляем целую часть от деления
                int remainder = totalCount / limit;

                return remainder;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Значение по умолчанию в случае ошибки
        return 0;
    }

    // Возвращает тип файла по его file_id
    public static String findSenderFileTipy(String filename) {
        // SQL-запрос для получения file_type из таблицы messages
        String sqlQuery = "SELECT file_type FROM messages WHERE file_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            // Устанавливаем параметр имени файла
            pstmt.setString(1, filename);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Возвращаем тип файла
                    return rs.getString("file_type");
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка работы с базой данных: " + e.getMessage());
        }
        // Возвращаем null, если тип файла не найден
        return null;
    }

    // Возвращает идентификатор алгоритма шифрования для указанного file_id
    public static String findSenderAlg(String filename) {
        // SQL-запрос с объединением таблиц messages и sessions
        String query = "SELECT s.encryption_algorithm_id FROM messages m " +
                "JOIN sessions s ON m.session_id = s.session_id " +
                "WHERE m.file_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    // Устанавливаем параметр file_id
                    pstmt.setString(1, filename);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Возвращаем идентификатор алгоритма
                            return rs.getString("encryption_algorithm_id");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка при выборке данных: " + e.getMessage());
        }
        // Возвращаем null, если алгоритм не найден
        return null;
    }

    // Возвращает ключ сессии для указанного file_id
    public static String findSenderKey(String filename) {
        // SQL-запрос с объединением таблиц messages и sessions
        String query = "SELECT s.session_key FROM messages m " +
                "JOIN sessions s ON s.session_id = m.session_id " +
                "WHERE m.file_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    // Устанавливаем параметр file_id
                    pstmt.setString(1, filename);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Возвращаем ключ сессии
                            return rs.getString("session_key");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка при выборке данных: " + e.getMessage());
        }
        // Возвращаем null, если ключ не найден
        return null;
    }

    // Обновляет резервный ключ сессии в таблице chats
    public void updateSessionKeyReserve(String contactId, String accountId, String newSessionKeyReserve) {
        // SQL-запрос для обновления session_key_reserve
        String updateQuery = "UPDATE chats SET session_key_reserve = ? WHERE contact_id = ? AND account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            // Устанавливаем параметры запроса
            if (newSessionKeyReserve != null) {
                pstmt.setString(1, newSessionKeyReserve);
            } else {
                pstmt.setNull(1, java.sql.Types.VARCHAR);
            }
            pstmt.setString(2, contactId);
            pstmt.setString(3, accountId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка работы с базой данных: " + e.getMessage());
        }
    }

    // Проверяет, достиг ли пользователь начала прокрутки JScrollPane
    boolean isStartOfScroll(JScrollPane scrollPane) {
        // Получаем вертикальную полосу прокрутки
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        // Проверяем, находится ли полоса в минимальном положении
        return verticalBar.getValue() == verticalBar.getMinimum();
    }

    // Удаляет сообщения и сессии, связанные с указанным contactId и account_id
    public static void deleteMessagesAndSessionsByContactId(String contactId, String ac_id) {
        // SQL-запрос для получения conversation_id
        String getConversationQuery = "SELECT conversation_id FROM chats WHERE contact_id = ? AND account_id = ?";
        // SQL-запрос для получения session_id
        String getSessionsQuery = "SELECT session_id FROM messages WHERE conversation_id = ?";
        // SQL-запрос для получения file_id
        String getNameFile = "SELECT file_id FROM messages WHERE conversation_id = ?";
        // SQL-запрос для удаления сообщений
        String deleteMessagesQuery = "DELETE FROM messages WHERE conversation_id = ?";
        // SQL-запрос для удаления сессий
        String deleteSessionQuery = "DELETE FROM sessions WHERE session_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                // Получаем conversation_id
                String conversationId = null;
                try (PreparedStatement pstmt = conn.prepareStatement(getConversationQuery)) {
                    pstmt.setString(1, contactId);
                    pstmt.setString(2, ac_id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            conversationId = rs.getString("conversation_id");
                        }
                    }
                }

                if (conversationId == null) {
                    return;
                }

                // Получаем все session_id
                List<String> sessionIds = new ArrayList<>();
                try (PreparedStatement pstmt = conn.prepareStatement(getSessionsQuery)) {
                    pstmt.setString(1, conversationId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            sessionIds.add(rs.getString("session_id"));
                        }
                    }
                }

                // Получаем все file_id
                List<String> fileIds = new ArrayList<>();
                try (PreparedStatement pstmt = conn.prepareStatement(getNameFile)) {
                    pstmt.setString(1, conversationId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            fileIds.add(rs.getString("file_id"));
                        }
                    }
                }

                // Удаляем файлы
                for (String nameFile : fileIds) {
                    fileslock.deleteFile(nameFile);
                }

                // Удаляем сообщения
                try (PreparedStatement pstmt = conn.prepareStatement(deleteMessagesQuery)) {
                    pstmt.setString(1, conversationId);
                    int deletedMessages = pstmt.executeUpdate();
                    System.out.println("Удалено сообщений: " + deletedMessages);
                }

                // Удаляем сессии
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSessionQuery)) {
                    for (String sessionId : sessionIds) {
                        pstmt.setString(1, sessionId);
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка при удалении сообщений и сессий: " + e.getMessage());
        }
    }

    // Возвращает contact_image_id для указанного contactId и accountId
    public static String getChatData(String contactId, String accountId) throws SQLException {
        // SQL-запрос для получения conversation_id и contact_image_id
        String sqlSelectChatData = "SELECT conversation_id, contact_image_id FROM chats WHERE contact_id = ? AND account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlSelectChatData)) {

            pstmt.setString(1, contactId);
            pstmt.setString(2, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Возвращаем contact_image_id
                return rs.getString("contact_image_id");
            }

        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка работы с базой данных: " + e.getMessage());
        }
        // Возвращаем null, если данные не найдены
        return null;
    }

    // Создаёт кнопку с иконкой и всплывающей подсказкой
    JButton createIconButton(String iconPath, String tooltip) {
        JButton button = new JButton();
        // Устанавливаем размер кнопки
        button.setPreferredSize(new Dimension(40, 40));
        ImageIcon icon = new ImageIcon(iconPath);
        // Масштабируем изображение до 30x30
        Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImage));
        // Устанавливаем всплывающую подсказку
        button.setToolTipText(tooltip);
        return button;
    }

    // Вычисляет высоту добавленных элементов
    int getAddedElementsHeight(int addedElements) {
        // Фиксированная высота одного элемента
        int elementHeight = 30;
        // Умножаем количество элементов на высоту
        return elementHeight * addedElements;
    }

    // Проверяет, достиг ли пользователь конца прокрутки JScrollPane
    boolean isEndOfScroll(JScrollPane scrollPane) {
        // Получаем вертикальную полосу прокрутки
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        // Проверяем, достигла ли полоса максимума
        return verticalBar.getValue() + verticalBar.getVisibleAmount() >= verticalBar.getMaximum();
    }

    // Проверяет, можно ли загрузить дополнительные контакты сверху
    boolean canLoadMoreFromTop(int offset, String account_id) {
        // SQL-запрос для подсчёта записей в таблице chats
        String sqlCountQuery = "SELECT COUNT(*) FROM chats WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlCountQuery)) {
            pstmt.setString(1, account_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Проверяем, есть ли данные для загрузки сверху
                return offset > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Значение по умолчанию в случае ошибки
        return false;
    }

    // Возвращает conversation_id для указанных accountId и contactId
    public static String getConversationId(String accountId, String contactId) {
        // SQL-запрос для получения conversation_id
        String query = "SELECT conversation_id FROM chats WHERE account_id = ? AND contact_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, accountId);
            pstmt.setString(2, contactId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Возвращаем conversation_id
                    return rs.getString("conversation_id");
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка при получении conversation_id: EH" + e.getMessage());
        }
        // Возвращаем null, если запись не найдена
        return null;
    }

    // Вставляет новое сообщение в таблицу messages и создаёт сессию
    public static void insertMessage(String nameId, String message, boolean status, String key, String alg, String tipy) {
        // SQL-запрос для вставки в таблицу messages
        String sqlInsert = "INSERT INTO messages (message_id, conversation_id, file_id, file_type, participant_status" +
                ", session_id) VALUES (?, ?, ?, ?, ?, ?)";
        // SQL-запрос для вставки в таблицу sessions
        String sqlInsertSession = "INSERT INTO sessions (session_id, session_key, encryption_algorithm_id" +
                ", session_id) VALUES (?, ?, ?, ?)";

        // Генерируем уникальный session_id
        String session_key = safety.generateUniqueId();

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertSession)) {
                    // Устанавливаем параметры для сессии
                    pstmt.setString(1, session_key);
                    pstmt.setString(2, key);
                    pstmt.setString(3, alg);
                    pstmt.setString(4, session_key);

                    // Выполняем запрос для сессии
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    // Устанавливаем параметры для сообщения
                    pstmt.setString(1, safety.generateUniqueId());
                    pstmt.setString(2, nameId);
                    pstmt.setString(3, message);
                    pstmt.setString(4, tipy);
                    pstmt.setBoolean(5, status);
                    pstmt.setString(6, session_key);

                    // Выполняем запрос для сообщения
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // Выводим сообщение об ошибке
            System.out.println("Ошибка при вставке записи в messages: " + e.getMessage());
        }
    }

    // Проверяет, можно ли загрузить дополнительные контакты снизу
    boolean canLoadMoreFromBottom(int currentOffset, String account_id) {
        // SQL-запрос для подсчёта записей в таблице chats
        String sqlCountQuery = "SELECT COUNT(*) FROM chats WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlCountQuery)) {
            pstmt.setString(1, account_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Общее количество записей в таблице
                int totalCount = rs.getInt(1);
                // Проверяем, есть ли данные для загрузки снизу
                return currentOffset + limit < totalCount;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Значение по умолчанию в случае ошибки
        return false;
    }
}
