package org.face_recognition;

// Класс для взаимодействия с базой данных — основной класс для взаимодействия с базой данных

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Database {

    // Название файла базы данных SQLite
    private static final String DATABASE_NAME = "user_accounts.db";

    // Полный путь подключения к базе данных
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;

    // Объект соединения с базой данных
    private static Connection connection;

    // Название основной таблицы, используемой в БД
    private static String tableName = "accounts";

    // Объект для получения ключей (возможно, шифрования или безопасности)
    private static KeyGet keyGet = new KeyGet();

    // Класс для шифрования аккаунтов (например, ChaCha20)
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();

    // Структура данных, используемая в проекте (возможно, для хранения пользовательских данных)
    static DataStructure dataStructure = new DataStructure();

    // Класс для расшифровки файлов
    private static DecryptFile decryptFil = new DecryptFile();

    // Ещё один объект шифрования (возможно, резервный или с другим алгоритмом)
    private static EncryptionAccaunt encryptionChacha = new EncryptionAccaunt();

    // Генератор случайных чисел
    private static Random random = new Random();

    // Строка, содержащая допустимые символы для генерации случайных значений (например, паролей, токенов)
    private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // Класс, связанный с безопасностью или проверками доступа
    private static Safety safety = new Safety();

    // Флаг, указывающий, заблокирована ли база данных (например, при одновременном доступе)
    private static boolean isDatabaseLocked = false;

    // Метод для создания базы данных, если она не существует
    public static void createbd(String bd_client){
        try (Connection conn = DriverManager.getConnection(bd_client)) {
            if (conn != null) {
                System.out.println("База данных успешно создана или уже существует.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    // Получение флага colour_account (true/false) по account_id
    public static boolean getLite(String accaunt_id) throws SQLException {
        SQLiteHelper(DATABASE_URL); // Инициализация подключения (предположительно)
        boolean lite = getColourAccountById(accaunt_id); // Получение значения из базы
        return lite;
    }

    // Метод возвращает значение colour_account из таблицы по account_id
    public static boolean getColourAccountById(String accountId) throws SQLException {
        // Запрос для получения значения color_account по account_id
        String query = "SELECT colour_account FROM " + tableName + " WHERE account_id = ?";

        // Подготовка запроса
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Устанавливаем параметр в запрос
            statement.setString(1, accountId);

            // Выполнение запроса
            ResultSet resultSet = statement.executeQuery();

            // Проверка, если результат найден
            if (resultSet.next()) {
                // Возвращаем значение поля colour_account
                return resultSet.getBoolean("colour_account");
            } else {
                // Если не найдено, возвращаем false (или можно выбросить исключение, если нужно)
                return false;
            }
        }
    }

    // Метод возвращает массив строк, содержащий идентификаторы заблокированных отправителей для указанного аккаунта
    // Первый элемент массива — "BLOCK_OLL", второй — собственный ID пользователя (myID),
    // далее следуют sender_id всех заблокированных пользователей из таблицы clientBlock

    public static String[] getClientBlockArray(String accountId, String myID) {
        String selectQuery = "SELECT sender_id FROM clientBlock WHERE account_id = ?";
        List<String> resultList = new ArrayList<>();

        // Добавляем специальные элементы в начало списка
        resultList.add("BLOCK_OLL");
        resultList.add(myID);

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
                    pstmt.setString(1, accountId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String senderId = rs.getString("sender_id");
                            // Добавляем найденный sender_id в список
                            resultList.add(senderId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении данных из clientBlock: " + e.getMessage());
        }

        // Преобразуем список в массив и возвращаем
        return resultList.toArray(new String[0]);
    }

    // Метод проверяет, включена ли синхронизация (synchronization_locks) для указанного аккаунта
    public static boolean getSynchronizationLocks(String accountId) {
        String query = "SELECT synchronization_locks FROM accounts WHERE account_id = ?";
        boolean synchronizationLocks = false; // Значение по умолчанию

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, accountId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Получаем значение поля synchronization_locks
                            synchronizationLocks = rs.getBoolean("synchronization_locks");
                        } else {
                            System.out.println("Запись с account_id = " + accountId + " не найдена.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении synchronization_locks: " + e.getMessage());
        }

        return synchronizationLocks;
    }

    // Сохраняет зашифрованное время уведомления и интервал между уведомлениями по account_id
    public static void saveNotificationTimeToDB(String notificationTime, String gap, String accountId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE accounts SET gap_time = ?, notification_time = ? WHERE account_id = ?")) {
            pstmt.setString(1, gap);
            pstmt.setString(2, notificationTime);
            pstmt.setString(3, accountId); // accountId используется в условии WHERE
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получает расшифрованное время уведомления по account_id
    public static Instant getNotificationTimeFromDB(String accauntId, String keyAc) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT notification_time FROM accounts WHERE account_id = ?")) {
            pstmt.setString(1, accauntId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String timeStr = encryption.chaha20Decrypt(keyAc, rs.getString("notification_time"));
                return Instant.parse(timeStr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getCurrentTimeUTC().plus(Duration.ofDays(7));
    }

    // Получает и расшифровывает массив интервалов между уведомлениями по account_id
    public static int[] getTimeInterval(String accountId, String keyAc) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT gap_time FROM accounts WHERE account_id = ?")) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Получаем зашифрованную строку gap_time
                String encryptedTime = rs.getString("gap_time");
                if (encryptedTime != null) {
                    // Расшифровываем строку
                    String decryptedTime = encryption.chaha20Decrypt(keyAc, encryptedTime);

                    // Разбиваем строку на массив строк по пробелам
                    String[] timeParts = decryptedTime.trim().split(" ");

                    // Преобразуем массив строк в массив целых чисел
                    int[] timeIntervals = new int[timeParts.length];
                    for (int i = 0; i < timeParts.length; i++) {
                        timeIntervals[i] = Integer.parseInt(timeParts[i]);
                    }

                    return timeIntervals;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Ошибка преобразования строки в число: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Возвращаем массив по умолчанию, если что-то пошло не так
        int[] defaultArray = new int[]{7}; // Эквивалент 7 дней по умолчанию
        return defaultArray;
    }

    // Проверяет, заполнены ли поля уведомлений (gap_time и notification_time)
    public static boolean getNotificationGap(String accountId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT notification_time, gap_time FROM accounts WHERE account_id = ?")) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String notificationTime = rs.getString("notification_time");
                String gapTime = rs.getString("gap_time");

                // Проверяем, что оба поля не null и не пустые
                if (notificationTime == null || notificationTime.isEmpty() ||
                        gapTime == null || gapTime.isEmpty()) {
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Если запись не найдена или произошла ошибка, возвращаем false
        return false;
    }

    // Возвращает текущее время в формате UTC
    public static Instant getCurrentTimeUTC() {
        return Instant.now();
    }

    // Обновляет значение colour_account по account_id
    public static void getLiteNew(String accaunt_id, boolean newColourAccount) throws SQLException {

        SQLiteHelper(DATABASE_URL);
        updateColourAccountById(accaunt_id, newColourAccount);

    }

    // Метод обновления colour_account по account_id
    public static void updateColourAccountById(String accountId, boolean newColourAccount) throws SQLException {
        // Запрос для обновления значения color_account по account_id
        String query = "UPDATE " + tableName + " SET colour_account = ? WHERE account_id = ?";

        // Подготовка запроса
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Устанавливаем параметры в запрос
            statement.setBoolean(1, newColourAccount); // Новый цвет аккаунта
            statement.setString(2, accountId);         // account_id

            // Выполнение запроса
            int rowsAffected = statement.executeUpdate();

            // Вы можете обработать количество затронутых строк (например, если оно 0, значит ничего не обновлено)
            if (rowsAffected == 0) {
                System.out.println("Не удалось обновить запись с account_id: " + accountId);
            } else {
                System.out.println("Цвет аккаунта успешно обновлен.");
            }
        }
    }

    // Проверка наличия соединения контактов по account_id
    public static boolean checkAccount(String accountId) {
        String query = "SELECT connect_contacts FROM accounts WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("connect_contacts");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удаляет сообщения с message_status = false и связанные сессии
    public static void delMessangNot(){

        String query = "SELECT m.file_id, m.participant_status, m.session_id, m.message_status " +
                "FROM messages m " +
                "JOIN chats c ON m.conversation_id = c.conversation_id " +
                "WHERE m.message_status = false";  // Добавлено WHERE

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String nameFile = rs.getString("file_id");
                            String sessionId = rs.getString("session_id");

                            deleteMessageAndSession(conn, nameFile, sessionId);

                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выборке данных из messages: " + e.getMessage());
        }

    }

    // Обновляет значение connect_contacts по account_id
    public void updateAccountStatus(String accountId, boolean status) throws SQLException {
        String query = "UPDATE accounts SET connect_contacts = ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к БД
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, status); // Устанавливаем новое значение
            pstmt.setString(2, accountId); // Фильтр по account_id

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("Не найден аккаунт с ID: " + accountId);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка обновления connect_contacts: " + e.getMessage());
            throw e;
        }
    }


    // Вставка записи в таблицу clientBlock, если такой ещё нет
    public static void fillClientBlock(String recordId, String accountId, String senderId) {
        String insertQuery = "INSERT INTO clientBlock (record_id, account_id, sender_id) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(record_id) DO NOTHING"; // Предотвращает дубликаты по primary key

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    // Установка параметров запроса
                    pstmt.setString(1, recordId);
                    pstmt.setString(2, accountId);
                    pstmt.setString(3, senderId);

                    // Выполнение вставки
                    int rowsAffected46 = pstmt.executeUpdate();

                    if (rowsAffected46 > 0) {
                        System.out.println("Успешно добавлена запись в clientBlock: " + recordId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении данных в clientBlock: " + e.getMessage());
        }
    }

    // Обновляет изображение контакта и удаляет старый файл
    public static void updateContactImage(String newImagePath, String contactId, String accountId) {
        // SQL-запрос для получения старого пути к файлу и обновления нового
        String selectOldImageQuery = "SELECT contact_image_id FROM chats WHERE contact_id = ? AND account_id = ?";
        String updateImageQuery = "UPDATE chats SET contact_image_id = ? WHERE contact_id = ? AND account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                // 1. Получаем старый путь к файлу изображения
                String oldImagePath = null;
                try (PreparedStatement selectStmt = conn.prepareStatement(selectOldImageQuery)) {
                    selectStmt.setString(1, contactId);
                    selectStmt.setString(2, accountId);

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            oldImagePath = rs.getString("contact_image_id");
                        }
                    }
                }

                // 2. Обновляем contact_image_id на новый путь
                try (PreparedStatement updateStmt = conn.prepareStatement(updateImageQuery)) {
                    updateStmt.setString(1, newImagePath); // Новый путь к файлу
                    updateStmt.setString(2, contactId);    // contact_id
                    updateStmt.setString(3, accountId);    // account_id

                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {

                        // 3. Удаляем старый файл, если он существует и не равен null
                        if (oldImagePath != null) {
                            File oldFile = new File(oldImagePath);
                            if (oldFile.exists()) {
                                if (oldFile.delete()) {
                                    System.out.println("Старый файл успешно удален: " + oldImagePath);
                                } else {
                                    System.out.println("Не удалось удалить старый файл: " + oldImagePath);
                                }
                            } else {
                                System.out.println("Старый файл не найден: " + oldImagePath);
                            }
                        }
                    } else {
                        System.out.println("Запись для обновления не найдена: contact_id = " + contactId +
                                ", account_id = " + accountId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении contact_image_id: " + e.getMessage());
        }
    }

    // Удаляет сообщение по file_id и связанную сессию
    static void deleteMessageAndSession(Connection conn, String fileId, String sessionId) {
        String deleteMessageQuery = "DELETE FROM messages WHERE file_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteMessageQuery)) {
            pstmt.setString(1, fileId);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                deleteSession(conn, sessionId); // Немедленно удаляем сессию
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении сообщения: " + e.getMessage());
        }
    }

    // Удаляет сессию по session_id
    private static void deleteSession(Connection conn, String sessionId) {

        String deleteSessionQuery = "DELETE FROM sessions WHERE session_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteSessionQuery)) {
            pstmt.setString(1, sessionId);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                System.out.println("Удалена сессия с session_id: " + sessionId);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении session_id: " + e.getMessage());
        }
    }

    // Метод для создания таблицы в базе данных
    public static void createtable(String url, String sql) {
        // Подключаемся к базе данных и выполняем SQL-запрос на создание таблицы
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql); // Выполнение SQL-запроса

        } catch (SQLException e) {
            // В случае ошибки выводим сообщение
            System.out.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }

    // Метод для проверки существования базы данных по имени файла
    public static boolean databaseExists(String dbName) {
        File dbFile = new File(dbName);
        // Проверка существования файла базы данных
        return dbFile.exists();
    }

    // Основной метод для создания базы данных и всех необходимых таблиц
    public static void createDatabase() {
        String databaseName = "user_accounts.db";  // Имя файла базы данных
        String url = "jdbc:sqlite:" + databaseName;  // JDBC URL для SQLite

        // SQL-запрос для создания таблицы аккаунтов
        String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_id TEXT PRIMARY KEY NOT NULL, " +
                "passphrase TEXT DEFAULT NULL, " +
                "account_key TEXT NOT NULL, " +
                "account_password TEXT NOT NULL, " +
                "key_connection TEXT NOT NULL, " +
                "backup_account_key TEXT DEFAULT NULL, " +
                "colour_account BOOLEAN NOT NULL DEFAULT false, " +
                "connect_contacts BOOLEAN NOT NULL DEFAULT false, " +
                "synchronization_locks BOOLEAN NOT NULL DEFAULT false, " +
                "notification_time TEXT DEFAULT NULL, " +
                "encryption_data BOOLEAN NOT NULL DEFAULT false, " +
                "gap_time TEXT DEFAULT NULL" +
                ");";

        // SQL-запрос для создания таблицы запланированных чатов
        String createChatPlanningTable = "CREATE TABLE IF NOT EXISTS chat_planning (" +
                "record_id TEXT PRIMARY KEY NOT NULL, " +
                "account_id TEXT NOT NULL, " +
                "sender_id TEXT NOT NULL, " +
                "messages TEXT DEFAULT NULL, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT DEFAULT NULL, " +
                "key_planning TEXT DEFAULT NULL, " +
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы откликов на запросы
        String createRequestResponsesTable = "CREATE TABLE IF NOT EXISTS request_responses (" +
                "record_id TEXT PRIMARY KEY NOT NULL, " +
                "account_id TEXT NOT NULL, " +
                "sender_id TEXT NOT NULL, " +
                "request_status BOOLEAN NOT NULL DEFAULT false, " + // Запрос принят
                "notification_status BOOLEAN NOT NULL DEFAULT false, " + // Запрос отвечен
                "lock_flag BOOLEAN DEFAULT false NOT NULL, " + // Контакт заблокирован
                "public_key TEXT NOT NULL, " +
                "record_ac_id_friend TEXT NOT NULL, " +
                "record_ac_id_data TEXT NOT NULL, " +
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы потенциальных контактов
        String createPotentialContactsTable = "CREATE TABLE IF NOT EXISTS potential_contacts (" +
                "record_id TEXT PRIMARY KEY NOT NULL, " +
                "account_id TEXT NOT NULL, " +
                "contact_id TEXT NOT NULL, " +
                "contact_name TEXT NOT NULL, " +
                "contact_private_key TEXT NOT NULL, " +
                "contact_image_id TEXT DEFAULT NULL, " +
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы откликов на заявки
        String createTableApplicationResponses = "CREATE TABLE IF NOT EXISTS application_responses (" +
                "record_id TEXT PRIMARY KEY, " +
                "account_id TEXT NOT NULL, " +
                "name_contact TEXT NOT NULL, " +
                "lock_flag BOOLEAN DEFAULT false NOT NULL, " +
                "request_status BOOLEAN DEFAULT TRUE NOT NULL, " +
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы чатов
        String createChatsTable = "CREATE TABLE IF NOT EXISTS chats (" +
                "conversation_id TEXT PRIMARY KEY NOT NULL, " +
                "contact_id TEXT NOT NULL, " +
                "contact_name TEXT NOT NULL, " +
                "contact_image_id TEXT DEFAULT NULL, " +
                "account_id TEXT NOT NULL, " +
                "session_key_reserve TEXT DEFAULT NULL, " + // Зарезервированный ключ сессии
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы сессий
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS sessions (" +
                "session_id TEXT PRIMARY KEY NOT NULL, " +
                "session_key TEXT NOT NULL, " +
                "encryption_algorithm_id TEXT NOT NULL" +
                ");";

        // SQL-запрос для создания таблицы сообщений
        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                "message_id TEXT PRIMARY KEY NOT NULL, " +
                "conversation_id TEXT NOT NULL, " +
                "file_id TEXT NOT NULL, " +
                "file_type TEXT NOT NULL, " +
                "participant_status BOOLEAN NOT NULL DEFAULT true, " + // Статус участника (например, активен ли)
                "session_id TEXT NOT NULL, " +
                "message_status BOOLEAN NOT NULL DEFAULT false, " + // Статус доставки/прочтения
                "FOREIGN KEY(conversation_id) REFERENCES chats(conversation_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(session_id) REFERENCES sessions(session_id) ON DELETE CASCADE" +
                ");";

        // SQL-запрос для создания таблицы блокировок пользователей
        String createTebleBlockKlient = "CREATE TABLE IF NOT EXISTS clientBlock (" +
                "record_id TEXT PRIMARY KEY NOT NULL, " +
                "account_id TEXT NOT NULL, " +
                "sender_id TEXT NOT NULL, " +
                "FOREIGN KEY(account_id) REFERENCES accounts(account_id) ON DELETE CASCADE" +
                ");";

        // Проверка существования базы данных. Если есть — вывод сообщения. Если нет — создаём всё с нуля.
        if (Database.databaseExists(databaseName)) {
            System.out.println("База данных существует.");
        } else {
            System.out.println("База данных не найдена.");

            // Создание базы данных (предполагается, что createbd делает это)
            createbd(url);

            // Создание всех необходимых таблиц
            createtable(url, createAccountsTable);
            createtable(url, createChatPlanningTable);
            createtable(url, createRequestResponsesTable);
            createtable(url, createPotentialContactsTable);
            createtable(url, createChatsTable);
            createtable(url, createSessionsTable);
            createtable(url, createMessagesTable);
            createtable(url, createTableApplicationResponses);
            createtable(url, createTebleBlockKlient);
        }
    }

    // Изменённое имя метода с использованием аналогичного подхода для подключения к базе данных
    // Метод получает зарезервированный ключ сессии (session_key_reserve) для указанного контакта и аккаунта
    // из таблицы chats в базе данных. Возвращает его в виде массива строк
    public static String[] getSessionAndEncryptionData(String contactId, String accountId) {

        // SQL-запрос для получения session_key_reserve из таблицы chats по contact_id и account_id
        String selectQuery = "SELECT session_key_reserve FROM chats WHERE contact_id = ? AND account_id = ?";

        // Список для хранения результатов запроса
        List<String> resultList = new ArrayList<>();

        // Открытие соединения с базой данных в блоке try-with-resources (гарантирует закрытие соединения)
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {  // Подключение к базе данных
            if (conn != null) {
                // Подготовка SQL-запроса с параметрами
                try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

                    // Устанавливаем параметры запроса: сначала contactId, потом accountId
                    pstmt.setString(1, contactId);
                    pstmt.setString(2, accountId);

                    // Выполнение запроса и получение результата
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Если результат есть, извлекаем значение поля session_key_reserve
                            String sessionKeyReserve = rs.getString("session_key_reserve");

                            // Если значение не null, добавляем его в список, иначе добавляем "NULL" как строку
                            resultList.add(sessionKeyReserve != null ? sessionKeyReserve : "NULL");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Обработка исключений SQL: вывод сообщения об ошибке в консоль
            System.out.println("Ошибка при получении данных из таблицы chats: " + e.getMessage());
        }

        // Преобразуем список в массив строк и возвращаем
        return resultList.toArray(new String[0]);
    }

    // Удаляет записи из таблицы chat_planning для указанного аккаунта и контакта
    public void deleteResponsesByAccountAndContact(String accountId, String contact_id) throws SQLException {
        // SQL-запрос для удаления записей из таблицы chat_planning
        String sql = "DELETE FROM chat_planning WHERE account_id = ? AND sender_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Устанавливаем параметры запроса
            pstmt.setString(1, accountId);
            pstmt.setString(2, contact_id);

            // Выполняем запрос и получаем количество удаленных строк
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Удалено записей: " + rowsAffected);
        }
    }

    // Обновляет резервный ключ и кодовую фразу для указанного аккаунта в таблице accounts
    public static void updateAccountCredentials(String accountId, String backupAccountKey, String passphrase) {
        // SQL-запрос для обновления backup_account_key и passphrase
        String updateQuery = "UPDATE accounts SET backup_account_key = ?, passphrase = ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Проверяем успешное подключение
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    // Устанавливаем параметры запроса
                    pstmt.setString(1, backupAccountKey);
                    pstmt.setString(2, passphrase);
                    pstmt.setString(3, accountId);

                    // Выполняем обновление и получаем количество измененных строк
                    int rowsAffected = pstmt.executeUpdate();

                    // Логируем результат
                    if (rowsAffected > 0) {
                        System.out.println("Успешно обновлены данные для account_id: " + accountId);
                    } else {
                        System.out.println("Запись с account_id: " + accountId + " не найдена.");
                    }
                }
            }
        } catch (SQLException e) {
            // Логируем ошибку SQL
            System.out.println("Ошибка при обновлении данных в accounts: " + e.getMessage());
        }
    }

    // Проверяет соответствие введенных кодовой фразы и пароля аккаунта хэшированным значениям в таблице accounts
    public static boolean[] checkHashedCredentials(String passphrase, String accountPassword) {
        // Флаги для отслеживания совпадений кодовой фразы и пароля
        boolean foundPassphrase = false;
        boolean foundAccountPassword = false;

        // Создаем экземпляр Argon2 для проверки хэшей
        Argon2 argon2 = Argon2Factory.create();

        // SQL-запрос для получения хэшированных кодовой фразы и пароля из таблицы accounts
        String query = "SELECT passphrase, account_password FROM accounts";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Перебираем все записи в результате запроса
            while (rs.next()) {
                String hashedPassphrase = rs.getString("passphrase");
                String hashedAccountPassword = rs.getString("account_password");

                // Проверяем кодовую фразу, если она еще не найдена
                if (!foundPassphrase && hashedPassphrase != null &&
                        argon2.verify(hashedPassphrase, passphrase.toCharArray())) {
                    foundPassphrase = true;
                }

                // Проверяем пароль аккаунта, если он еще не найден
                if (!foundAccountPassword && hashedAccountPassword != null &&
                        argon2.verify(hashedAccountPassword, accountPassword.toCharArray())) {
                    foundAccountPassword = true;
                }

                // Прерываем цикл, если оба значения найдены
                if (foundPassphrase && foundAccountPassword) {
                    break;
                }
            }

        } catch (SQLException e) {
            // Логируем ошибку SQL
            e.printStackTrace();
        }

        // Возвращаем массив, указывающий, были ли найдены совпадения для кодовой фразы и пароля
        return new boolean[]{foundPassphrase, foundAccountPassword};
    }

    // Проверяет, соответствует ли введенный пароль какому-либо хэшированному паролю в таблице accounts
    public static boolean checkAccountPassword(String accountPassword) {
        // Создаем экземпляр Argon2 для проверки хэша пароля
        Argon2 argon2 = Argon2Factory.create();
        // SQL-запрос для получения всех хэшированных паролей из таблицы accounts
        String query = "SELECT account_password FROM accounts";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Перебираем все записи в результате запроса
            while (rs.next()) {
                String hashedAccountPassword = rs.getString("account_password");

                // Проверяем, что хэш пароля не null и соответствует введенному паролю
                if (hashedAccountPassword != null &&
                        argon2.verify(hashedAccountPassword, accountPassword.toCharArray())) {
                    return true; // Пароль совпадает
                }
            }

        } catch (SQLException e) {
            // Логируем ошибку SQL
            e.printStackTrace();
        }

        // Возвращаем false, если совпадений не найдено
        return false;
    }

    //Этот метод необходим для выбора панели, где вводится пароль и кодовое слово или только пароль
    public static boolean isPassphrasePresent(String accountId) {
        String query = "SELECT passphrase FROM accounts WHERE account_id = ?";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passphrase = rs.getString("passphrase");
                    return passphrase != null && !passphrase.isEmpty(); // true, если не пустое
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // если не найдено или пустое
    }

    // Получает команды синхронизации чатов для указанного аккаунта
    public String[] getSyncCommands(String accountId, String myId) {
        // Подготавливаем список для результатов
        List<String> commands = new ArrayList<>();

        // Добавляем первую команду
        commands.add("SYNCHRONY_OLL_CHAT");
        commands.add(myId);

        try {
            // Подключаемся к базе данных (предполагается, что Connection уже настроен)
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            String query = "SELECT contact_id FROM chats WHERE account_id = ?";

            // Подготавливаем запрос
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, accountId);

            // Выполняем запрос и получаем результаты
            ResultSet rs = pstmt.executeQuery();

            // Обрабатываем каждую запись
            while (rs.next()) {
                String contactId = rs.getString("contact_id");
                // Формируем строку вида "contact_id myID"
                String command = contactId + " " + myId;
                commands.add(command);
            }

            // Закрываем ресурсы
            rs.close();
            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // В случае ошибки можно вернуть пустой массив или обработать иначе
        }

        // Преобразуем список в массив и возвращаем
        return commands.toArray(new String[0]);
    }

    // Получает резервный ключ сессии для указанного аккаунта и контакта из таблицы chats
    public String getSessionKeyReserve(String accountId, String contactId) {
        // SQL-запрос для получения session_key_reserve
        String query = "SELECT session_key_reserve FROM chats WHERE account_id = ? AND contact_id = ?";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметры запроса
            stmt.setString(1, accountId);
            stmt.setString(2, contactId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Возвращаем найденный ключ
                    return rs.getString("session_key_reserve");
                }
            }
        } catch (SQLException e) {
            // Логируем ошибку SQL
            e.printStackTrace();
        }
        // Возвращаем null, если запись не найдена
        return null;
    }

    // Получает статус шифрования данных (encryption_data) для указанного аккаунта
    public boolean getStatusSafety(String accountId) {
        // SQL-запрос для получения encryption_data
        String query = "SELECT encryption_data FROM accounts WHERE account_id = ?";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметр account_id
            stmt.setString(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Возвращаем значение encryption_data
                    boolean encryptionData = rs.getBoolean("encryption_data");
                    return encryptionData;
                }
            }
        } catch (SQLException e) {
            // Логируем ошибку SQL
            e.printStackTrace();
        }
        // Возвращаем false по умолчанию, если запись не найдена
        return false;
    }

    // Получает зашифрованную информацию о чатах для указанного аккаунта
    public String[] getChatsInfo(String accountId, String myId, String keyAc) {
        String query = "SELECT contact_id, session_key_reserve FROM chats WHERE account_id = ?";

        // Список для хранения строк результатов
        List<String> resultList = new ArrayList<>();

        // Добавляем команду KEY_EXIT и myId в список
        resultList.add("KEY_EXIT");
        resultList.add(myId);

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметры для запроса
            stmt.setString(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Пробегаем по всем результатам запроса и добавляем их в список
                while (rs.next()) {
                    String contactId = rs.getString("contact_id");
                    String sessionKeyReserve = rs.getString("session_key_reserve");

                    byte[] resultEnKey = keyGet.encryptBlock("AES", null, keyAc, encryption.chaha20Decrypt(keyAc, sessionKeyReserve).getBytes());
                    String nameEncKey = Base64.getEncoder().encodeToString(resultEnKey);

                    // Добавляем строку вида contact_id + " " + session_key_reserve в результат
                    resultList.add(contactId + " " + nameEncKey);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Преобразуем список в массив строк и возвращаем его
        return resultList.toArray(new String[0]);
    }


    // Получает список чатов для указанного аккаунта, шифруя имена контактов и ключи сессий
    public String[] getChatList(String accountId, String myID, String keyAc) {
        // SQL-запрос для получения contact_id, contact_name и session_key_reserve из таблицы chats
        String query = "SELECT contact_id, contact_name, session_key_reserve FROM chats WHERE account_id = ?";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметр account_id в запрос
            statement.setString(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                // Инициализируем список для хранения результатов
                ArrayList<String> chatList = new ArrayList<>();
                // Добавляем начальные элементы
                chatList.add("SYNCHRONY_OLL_CHAT_PLUSS");
                chatList.add(myID);

                // Обрабатываем каждую запись результата
                while (resultSet.next()) {
                    String contactId = resultSet.getString("contact_id");
                    String contactName = resultSet.getString("contact_name");
                    String secretKey = resultSet.getString("session_key_reserve");

                    // Расшифровываем имя контакта и ключ сессии с использованием ChaCha20
                    // Затем шифруем их с использованием AES и кодируем в Base64
                    byte[] resultEn = keyGet.encryptBlock("AES", null, keyAc, encryption.chaha20Decrypt(keyAc, contactName).getBytes());
                    String nameEnc = Base64.getEncoder().encodeToString(resultEn);

                    byte[] resultEnKey = keyGet.encryptBlock("AES", null, keyAc, encryption.chaha20Decrypt(keyAc, secretKey).getBytes());
                    String nameEncKey = Base64.getEncoder().encodeToString(resultEnKey);

                    // Формируем строку с данными чата и добавляем в список
                    chatList.add(myID + " " + contactId + " " + nameEnc + " " + nameEncKey);
                }

                // Возвращаем массив строк с данными чатов
                return chatList.toArray(new String[0]);
            } catch (Exception e) {
                // В случае ошибки при обработке данных (например, дешифровании) выбрасываем исключение
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            // Логируем ошибку SQL
            e.printStackTrace();
        }

        // Возвращаем пустой массив в случае ошибки
        return new String[0];
    }

    // Вставляет данные контакта в таблицу chats, расшифровывая имя и ключ, создавая зашифрованное изображение
    // и добавляя запись с уникальным идентификатором чата
    public static String[] insertDateContact(String[] contact, String keyAc, String accaunt_id) {
        // Список для хранения идентификаторов с ошибочными ключами
        List<String> invalidKeys = new ArrayList<>();
        invalidKeys.add("SYNCHRONY_OLL_CHAT_PLUSS_DEL");

        // Перебираем каждую строку из массива контактов
        for (String part : contact) {
            // Разделяем строку на части с помощью метода splitString
            String[] parts = dataStructure.splitString(part);

            try {
                // Расшифровываем имя контакта и ключ с использованием AES
                byte[] resultDecName = decryptFil.decryptBlock("AES", null, keyAc, Base64.getDecoder().decode(parts[3]));
                byte[] resultDecKey = decryptFil.decryptBlock("AES", null, keyAc, Base64.getDecoder().decode(parts[4]));
                String nameDec = new String(resultDecName); // Имя контакта
                String nameDecKey = new String(resultDecKey); // Ключ сессии

                // Шифруем имя и ключ с использованием ChaCha20
                String nameEn = encryptionChacha.chaha20Encript(keyAc, nameDec);
                String keyEn = encryptionChacha.chaha20Encript(keyAc, nameDecKey);

                // Читаем содержимое файла изображения по умолчанию
                File file = new File("pictures/defolt.bin");
                byte[] fileBytes = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileBytes);
                }

                // Форматируем текущую дату и время для создания уникального имени файла
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

                // Генерируем путь к файлу изображения
                String fileName = "out/contact_images/" + parts[1] + "/" + parts[2] + "/" + formattedDate + ".bin";
                File filePath = new File(fileName);

                // Создаем директории для файла, если они не существуют
                File parentDir = filePath.getParentFile();
                if (!parentDir.exists()) {
                    boolean dirsCreated = parentDir.mkdirs();
                    if (dirsCreated) {
                        System.out.println("Директории успешно созданы: " + parentDir.getAbsolutePath());
                    } else {
                        System.err.println("Не удалось создать директории: " + parentDir.getAbsolutePath());
                    }
                }

                // Шифруем изображение и сохраняем его
                keyGet.encryptImageIcon(fileBytes, fileName, keyAc, "AES");

                // Формируем поля для вставки в таблицу chats
                Map<String, Object> fields = new HashMap<>();
                fields.put("conversation_id", safety.generateUniqueId()); // Уникальный ID чата
                fields.put("contact_id", parts[2]); // ID контакта
                fields.put("contact_name", nameEn); // Зашифрованное имя контакта
                fields.put("contact_image_id", fileName); // Путь к изображению
                fields.put("account_id", accaunt_id); // ID аккаунта
                fields.put("session_key_reserve", keyEn); // Зашифрованный ключ сессии

                // Добавляем запись в таблицу chats
                addRecord("chats", fields);

            } catch (Exception e) {
                // В случае ошибки добавляем идентификатор в список ошибок
                invalidKeys.add(parts[0]);
            }
        }

        // Возвращаем массив ошибочных ключей или null, если ошибок нет
        return invalidKeys.isEmpty() ? null : invalidKeys.toArray(new String[0]);
    }

    // Устанавливает значение synchronization_locks для указанного аккаунта в таблице accounts
    public static void setSynchronizationLocks(String accountId, boolean synchronizationLocks) {
        // SQL-запрос для обновления значения synchronization_locks по account_id
        String query = "UPDATE accounts SET synchronization_locks = ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Проверка успешного подключения к базе данных
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    // Установка параметров запроса
                    pstmt.setBoolean(1, synchronizationLocks); // Новое значение synchronization_locks
                    pstmt.setString(2, accountId);            // Идентификатор аккаунта

                    // Выполнение запроса и получение количества измененных строк
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Synchronization_locks для account_id = " + accountId + " обновлено на " + synchronizationLocks);
                    } else {
                        System.out.println("Запись с account_id = " + accountId + " не найдена");
                    }
                }
            }
        } catch (SQLException e) {
            // Обработка ошибок SQL
            System.out.println("Ошибка при обновлении synchronization_locks: " + e.getMessage());
        }
    }

    // Устанавливает значение encryption_data для аккаунта
    public static void setEncryptionData(String accountId, boolean encryption_data) {
        String query = "UPDATE accounts SET encryption_data = ? WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setBoolean(1, encryption_data); // Устанавливаем новое значение
                    pstmt.setString(2, accountId);            // Указываем account_id

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Поиск account_id = " + accountId + " обновлено на " + encryption_data);
                    } else {
                        System.out.println("Запись с account_id = " + accountId + " не найдена.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении synchronization_locks: " + e.getMessage());
        }
    }

    // Получает значение encryption_data для аккаунта
    public static boolean getEncryptionData(String accountId) {
        String query = "SELECT encryption_data FROM accounts WHERE account_id = ?";
        boolean synchronizationLocks = false; // Значение по умолчанию

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, accountId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            synchronizationLocks = rs.getBoolean("encryption_data");
                        } else {
                            System.out.println("Запись с account_id = " + accountId + " не найдена.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении synchronization_locks: " + e.getMessage());
        }

        return synchronizationLocks;
    }

    // Обновляет имя контакта в таблице chats
    public static void upContactNew(String accountId, String senderId, String nameNew) {
        String query = "UPDATE chats SET contact_name = ? WHERE account_id = ? AND contact_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, nameNew);
                    pstmt.setString(2, accountId);
                    pstmt.setString(3, senderId);

                    // Выполняем обновление и получаем количество измененных строк
                    int rowsAffected = pstmt.executeUpdate();

                    // Проверка, были ли изменены данные
                    if (rowsAffected > 0) {
                        System.out.println("Имя контакта успешно обновлено. Изменено строк: " + rowsAffected);
                    } else {
                        System.out.println("Данные не были обновлены: запись не найдена или имя уже соответствует заданному.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении contact_name: " + e.getMessage());
        }
    }

    // Обновляет резервный ключ сессии для чата
    public void updateChatSession(String contactId, String accountId, String newSessionKey) {

        try {
            SQLiteHelper(DATABASE_URL);

            // SQL-запрос для обновления session_key_reserve и encryption_algorithm_reserve_id
            String query = "UPDATE chats SET session_key_reserve = ? " +
                    "WHERE contact_id = ? AND account_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Устанавливаем параметры запроса
                statement.setString(1, newSessionKey);
                statement.setString(2, contactId);
                statement.setString(3, accountId);

                // Выполняем запрос обновления
                statement.executeUpdate(); // Не возвращаем результат, просто выполняем запрос

            } catch (SQLException e) {
                e.printStackTrace(); // Логируем ошибку
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // Получает данные контакта по record_id из таблицы potential_contacts
    public String[] getContactDetailsByRecordId(String recordId) throws SQLException {

        SQLiteHelper(DATABASE_URL);

        String[] contactDetails = new String[4]; // Массив для хранения 4 полей
        String query = "SELECT contact_name, contact_private_key, contact_image_id " +
                "FROM potential_contacts " +
                "WHERE record_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, recordId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    contactDetails[0] = rs.getString("contact_name");
                    contactDetails[1] = rs.getString("contact_private_key");
                    contactDetails[2] = rs.getString("contact_image_id"); // Может быть null
                } else {
                    return null; // Или можно выбросить исключение, если запись не найдена
                }
            }
        }

        return contactDetails;
    }

    // Проверяет существование контакта в таблице chats
    public boolean isContactExists(String contactId, String accountId) {
        String query = "SELECT 1 FROM chats WHERE contact_id = ? AND account_id = ? LIMIT 1";

        try {
            SQLiteHelper(DATABASE_URL); // Подключение к базе данных

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, contactId);
                statement.setString(2, accountId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next(); // Если найдена строка, контакт существует
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Логируем ошибку
            return false; // В случае ошибки считаем, что контакт отсутствует
        }
    }

    // Удаляет контакт из таблицы potential_contacts по record_id
    public void deleteContactByRecordId(String recordId) throws SQLException {
        SQLiteHelper(DATABASE_URL); // Предполагаю, что это устанавливает соединение

        String query = "DELETE FROM potential_contacts WHERE record_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, recordId);

            int rowsAffected = pstmt.executeUpdate();

            // Опционально: можно проверить, была ли удалена запись
            if (rowsAffected == 0) {
                System.out.println("Контакт успешно удален");
            }
        }
    }

    // Удаляет файл по указанному пути
    public void deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Файл успешно удален: " + filePath);
            } else {
                System.out.println("Не удалось удалить файл: " + filePath);
            }
        } else {
            System.out.println("Файл не найден: " + filePath);
        }
    }

    // Добавляет запись в указанную таблицу
    public static void addRecord(String tableName, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Список полей не может быть пустым.");
        }

        // Формирование SQL-запроса
        StringBuilder fieldNames = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (String field : fields.keySet()) {
            if (fieldNames.length() > 0) {
                fieldNames.append(", ");
                placeholders.append(", ");
            }
            fieldNames.append(field);
            placeholders.append("?");
        }

        String sqlInsert = "INSERT INTO " + tableName + " (" + fieldNames + ") VALUES (" + placeholders + ")";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {

            // Устанавливаем параметры
            int index = 1;
            for (Object value : fields.values()) {
                pstmt.setObject(index++, value);
            }

            // Выполняем запрос
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Ошибка при добавлении записи в таблицу " + tableName + ": " + e.getMessage());
        }
    }

    // Метод для обновления записи в таблице request_responses
    public void updateRequestResponse(String recordId, boolean requestStatus, boolean notificationStatus, boolean lockFlag) {

        String sql = "UPDATE request_responses SET request_status = ?, notification_status = ?, lock_flag = ? WHERE record_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Устанавливаем параметры запроса
            pstmt.setBoolean(1, requestStatus);
            pstmt.setBoolean(2, notificationStatus);
            pstmt.setBoolean(3, lockFlag);
            pstmt.setString(4, recordId);

            // Выполняем обновление
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("База данных успешно обновлена для record_id: " + recordId);
            } else {
                System.out.println("Запись с record_id: " + recordId + " не найдена");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении базы данных: " + e.getMessage());
        }
    }

    // Получает данные из таблицы с фильтрацией по столбцу и значению
    static String [] getDate(List<String> fields, String tableName, String filterColumn, String filterValue) {

        try {
            SQLiteHelper(DATABASE_URL);

            String[] data = getTableData(tableName, fields, filterColumn, filterValue);

            // Закрываем соединение
            close();

            return data;

        } catch (SQLException e) {
            return null;
        }

    }

    // Устанавливает соединение с базой данных
    private static void SQLiteHelper(String databaseUrl) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl);
    }

    // Получает данные из таблицы с фильтрацией по хэшированному значению
    public static String[] getTableData(String tableName, List<String> fields, String filterColumn, String filterValue) throws SQLException {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Список полей не может быть нулевым или пустым");
        }

        if (filterColumn == null || filterColumn.isEmpty()) {
            throw new IllegalArgumentException("Столбец фильтра не может быть нулевым или пустым");
        }

        // Формируем SQL-запрос для получения данных по указанному столбцу
        String fieldList = String.join(", ", fields); // Преобразуем список в строку через запятую
        String query = "SELECT " + fieldList + ", " + filterColumn + " FROM " + tableName;

        // Подготавливаем и выполняем запрос
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        // Объект Argon2 для проверки хэша
        Argon2 argon2 = Argon2Factory.create();

        // Проверяем результаты
        while (resultSet.next()) {
            // Получаем хэш из столбца фильтра
            String storedHash = resultSet.getString(filterColumn);

            if (storedHash != null){

                // Сравниваем хэш с переданным значением
                if (argon2.verify(storedHash, filterValue.toCharArray())) {
                    // Если хэш совпал, создаём массив для хранения значений полей одной строки
                    String[] values = new String[fields.size()];
                    for (int i = 0; i < fields.size(); i++) {
                        values[i] = resultSet.getString(fields.get(i));
                    }
                    return values;
                }

            }

        }

        // Если ничего не найдено, возвращаем null
        return null;
    }

    // Закрытие соединения
    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Метод для проверки, заблокирована ли база данных
    public synchronized boolean isDatabaseLocked() {
        return isDatabaseLocked;
    }

    // Метод для блокировки базы данных
    public synchronized void lockDatabase() {
        while (isDatabaseLocked) {
            try {
                wait(); // Ожидаем, пока база данных не будет разблокирована
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        isDatabaseLocked = true; // Блокируем базу данных
    }

    // Метод для разблокировки базы данных
    public synchronized void unlockDatabase() {
        isDatabaseLocked = false; // Разблокируем базу данных
        notifyAll(); // Уведомляем все потоки, что база данных теперь доступна
    }

    // Обновляет записи в таблице с использованием условий поиска и новых значений
    public static void updateRecords(String dbPath, String tableName,
                                     Object[] searchValues, String[] searchColumns,
                                     String[] updateColumns, Object[] newValues) throws IllegalArgumentException {
        if (searchColumns.length != searchValues.length) {
            throw new IllegalArgumentException("Количество столбцов поиска должно совпадать с количеством значений поиска.");
        }

        if (updateColumns.length != newValues.length) {
            throw new IllegalArgumentException("Количество столбцов обновления должно совпадать с количеством новых значений.");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Подключение к базе данных
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            // Формирование SQL-запроса
            StringBuilder whereClause = new StringBuilder();
            for (int i = 0; i < searchColumns.length; i++) {
                if (i > 0) {
                    whereClause.append(" AND ");
                }
                whereClause.append(searchColumns[i]).append(" = ?");
            }

            StringBuilder setClause = new StringBuilder();
            for (int i = 0; i < updateColumns.length; i++) {
                if (i > 0) {
                    setClause.append(", ");
                }
                setClause.append(updateColumns[i]).append(" = ?");
            }

            String sqlQuery = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;

            // Подготовка запроса
            preparedStatement = connection.prepareStatement(sqlQuery);

            int parameterIndex = 1;
            for (Object value : newValues) {
                preparedStatement.setObject(parameterIndex++, value);
            }

            // Установка значений для условий поиска
            for (Object value : searchValues) {
                preparedStatement.setObject(parameterIndex++, value);
            }

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка работы с базой данных: " + e.getMessage());
        } finally {
            // Закрытие ресурсов
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

}
