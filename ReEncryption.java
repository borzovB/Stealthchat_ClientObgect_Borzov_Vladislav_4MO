package org.face_recognition;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

// Класс отвечает за перешифровку данных в базе данных и файлов, связанных с учетной записью, с использованием нового ключа
public class ReEncryption {

    // Статические поля для хранения ключей, идентификаторов и объектов шифрования
    private static String accauntKeyNew; // Новый ключ шифрования
    private static String keyAc; // Старый ключ шифрования
    private static String accauntId; // Идентификатор учетной записи
    private static final String DATABASE_NAME = "user_accounts.db"; // Имя базы данных SQLite
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME; // URL подключения к базе данных
    private static EncryptionAccaunt encryptionAccaunt = new EncryptionAccaunt(); // Объект для шифрования/дешифрования с использованием ChaCha20
    private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Символы для генерации случайных имен файлов
    private static String myID; // Идентификатор клиента
    private static DecryptFile decryptFile = new DecryptFile(); // Объект для расшифровки файлов
    private static KeyGet keyGet = new KeyGet(); // Объект для генерации ключей и шифрования файлов

    // Конструктор класса, инициализирующий параметры для перешифровки
    public ReEncryption(String accauntKeyNew, String keyAc, String accauntId, String myID) {
        this.accauntKeyNew = accauntKeyNew; // Сохраняем новый ключ
        this.keyAc = keyAc; // Сохраняем старый ключ
        this.accauntId = accauntId; // Сохраняем идентификатор учетной записи
        this.myID = myID; // Сохраняем идентификатор клиента
    }

    // Статический блок для регистрации криптографического провайдера BouncyCastle
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Метод для перешифровки данных в таблице chats
    static void tableChats() {
        // SQL-запрос для выборки данных чатов по account_id
        String selectSql = "SELECT conversation_id, contact_id, contact_name, contact_image_id, session_key_reserve " +
                "FROM chats WHERE account_id = ?";
        // SQL-запрос для обновления данных чатов
        String updateSql = "UPDATE chats SET contact_name = ?, contact_image_id = ?, session_key_reserve = ? " +
                "WHERE conversation_id = ? AND account_id = ?";

        Random random = new Random(); // Генератор случайных чисел для имен файлов
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // Формат даты для имен файлов

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement selectStmt = conn.prepareStatement(selectSql); // Подготовка запроса выборки
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) { // Подготовка запроса обновления

            selectStmt.setString(1, accauntId); // Установка account_id в запрос
            ResultSet rs = selectStmt.executeQuery(); // Выполнение выборки

            while (rs.next()) { // Обработка каждой записи
                String conversationId = rs.getString("conversation_id"); // ID беседы
                String contactId = rs.getString("contact_id"); // ID контакта
                String contactNameOld = rs.getString("contact_name"); // Зашифрованное имя контакта
                String contactImageIdOld = rs.getString("contact_image_id"); // Путь к зашифрованному изображению
                String sessionKeyReserveOld = rs.getString("session_key_reserve"); // Зашифрованный резервный ключ сессии

                // Расшифровка изображения контакта, если оно существует
                byte[] resultImageByte = null;
                if (contactImageIdOld != null) {
                    File file = new File(contactImageIdOld);
                    if (file.exists()) {
                        resultImageByte = decryptFile.playImageFromBinFile(file, "AES", keyAc); // Расшифровка изображения
                    }
                }

                // Удаление старого файла изображения, если он существует
                if (contactImageIdOld != null) {
                    File oldFile = new File(contactImageIdOld);
                    if (oldFile.exists()) {
                        if (oldFile.delete()) {
                            System.out.println("Старый файл успешно удален: " + contactImageIdOld);
                        } else {
                            System.out.println("Не удалось удалить старый файл: " + contactImageIdOld);
                        }
                    } else {
                        System.out.println("Старый файл не найден: " + contactImageIdOld);
                    }
                }

                // Расшифровка данных старым ключом
                String decryptedContactName = encryptionAccaunt.chaha20Decrypt(keyAc, contactNameOld); // Расшифровка имени
                String decryptedSessionKeyReserve = sessionKeyReserveOld != null ?
                        encryptionAccaunt.chaha20Decrypt(keyAc, sessionKeyReserveOld) : null; // Расшифровка резервного ключа

                // Шифрование данных новым ключом
                String contactNameNew = encryptionAccaunt.chaha20Encript(accauntKeyNew, decryptedContactName); // Шифрование имени
                String sessionKeyReserveNew = decryptedSessionKeyReserve != null ?
                        encryptionAccaunt.chaha20Encript(accauntKeyNew, decryptedSessionKeyReserve) : null; // Шифрование резервного ключа

                // Генерация нового имени файла для изображения
                String formattedDate = dateFormat.format(new java.util.Date()) +
                        CHARACTERS.charAt(random.nextInt(CHARACTERS.length())); // Формирование уникального имени
                String contactImageIdNew = "out/contact_images/" + myID + "/" + contactId + "/" + formattedDate + ".bin";

                // Сохранение нового зашифрованного изображения, если оно было
                if (resultImageByte != null) {
                    File fileNew = new File(contactImageIdNew);
                    fileNew.getParentFile().mkdirs(); // Создание директорий для файла
                    keyGet.encryptImageIcon(resultImageByte, contactImageIdNew, accauntKeyNew, "AES"); // Шифрование и сохранение
                }

                // Обновление записи в базе данных
                updateStmt.setString(1, contactNameNew); // Новое имя контакта
                updateStmt.setString(2, contactImageIdNew); // Новый путь к изображению
                updateStmt.setString(3, sessionKeyReserveNew); // Новый резервный ключ
                updateStmt.setString(4, conversationId); // ID беседы
                updateStmt.setString(5, accauntId); // ID учетной записи
                int affectedRows = updateStmt.executeUpdate(); // Выполнение обновления

                // Логирование результата обновления
                if (affectedRows > 0) {
                    System.out.println("Успешно обновлена запись для conversation_id: " + conversationId);
                } else {
                    System.out.println("Не удалось обновить запись conversation_id: " + conversationId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage()); // Обработка ошибок SQL
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка шифрования/дешифрования: " + e.getMessage()); // Обработка ошибок шифрования
        }
    }

    // Метод для перешифровки текстовых файлов
    public static void reencryptFileText(String fileId, String decryptedEncryptionAlgorithmId, String decryptedSessionKey, String secretKeyStr, String contactImageIdNew, String alg) {
        try {
            // Создание директорий для нового файла, если они не существуют
            File newFile = new File(contactImageIdNew);
            File parentDir = newFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs(); // Создание всех необходимых директорий
            }

            String algorithmNew; // Новый алгоритм шифрования
            String providerNew = null; // Провайдер для алгоритма (для Twofish/Serpent)

            // Расшифровка старого файла с использованием старого ключа и алгоритма
            String decryptedData = decryptFile.choosing_cipher(decryptedEncryptionAlgorithmId, fileId, decryptedSessionKey);

            if (decryptedData == null) {
                throw new RuntimeException("Не удалось расшифровать файл: " + fileId); // Проверка успешности расшифровки
            }

            // Определение нового алгоритма шифрования на основе кода alg
            switch (alg) {
                case "1a":
                    algorithmNew = "AES"; // Алгоритм AES
                    break;
                case "1b":
                    algorithmNew = "Twofish"; // Алгоритм Twofish
                    providerNew = "BC"; // Провайдер BouncyCastle
                    break;
                case "1c":
                    algorithmNew = "Serpent"; // Алгоритм Serpent
                    providerNew = "BC"; // Провайдер BouncyCastle
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный алгоритм шифрования: " + alg); // Обработка неизвестного алгоритма
            }

            // Шифрование данных новым ключом и алгоритмом и запись в новый файл
            keyGet.encryptAndWriteToFile(algorithmNew, providerNew, secretKeyStr, decryptedData, contactImageIdNew);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при перешифровке файла: " + e.getMessage()); // Обработка ошибок
        }
    }

    // Метод для обработки сообщений и сессий, перешифровка связанных файлов
    static void processMessagesAndSessions() {
        // SQL-запросы для выборки данных
        String SELECT_CHATS = "SELECT conversation_id, contact_id FROM chats WHERE account_id = ?";
        String SELECT_MESSAGES = "SELECT message_id, file_id, file_type, session_id FROM messages WHERE conversation_id = ?";
        String SELECT_SESSIONS = "SELECT session_key, encryption_algorithm_id FROM sessions WHERE session_id = ?";
        String UPDATE_SESSIONS = "UPDATE sessions SET session_key = ?, encryption_algorithm_id = ? WHERE session_id = ?";
        String UPDATE_MESSAGES = "UPDATE messages SET file_id = ? WHERE message_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement chatsStmt = conn.prepareStatement(SELECT_CHATS); // Запрос для чатов
             PreparedStatement messagesStmt = conn.prepareStatement(SELECT_MESSAGES); // Запрос для сообщений
             PreparedStatement sessionsStmt = conn.prepareStatement(SELECT_SESSIONS); // Запрос для сессий
             PreparedStatement updateSessionsStmt = conn.prepareStatement(UPDATE_SESSIONS); // Обновление сессий
             PreparedStatement updateMessagesStmt = conn.prepareStatement(UPDATE_MESSAGES)) { // Обновление сообщений

            chatsStmt.setString(1, accauntId); // Установка account_id
            try (ResultSet chatsRs = chatsStmt.executeQuery()) { // Выполнение запроса
                boolean hasRecords = false;
                while (chatsRs.next()) { // Обработка каждой беседы
                    hasRecords = true;
                    // Обработка сообщений и сессий для каждой беседы
                    processConversation(chatsRs.getString("conversation_id"), chatsRs.getString("contact_id"),
                            messagesStmt, sessionsStmt, updateSessionsStmt, updateMessagesStmt);
                }
                if (!hasRecords) {
                    System.out.println("Записи для account_id: " + accauntId + " не найдены"); // Логирование отсутствия записей
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage(), e); // Обработка ошибок SQL
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования/дешифрования: " + e.getMessage(), e); // Обработка ошибок шифрования
        }
    }

    // Вспомогательный метод для обработки сообщений и сессий в конкретной беседе
    private static void processConversation(String conversationId, String contactId,
                                            PreparedStatement messagesStmt, PreparedStatement sessionsStmt,
                                            PreparedStatement updateSessionsStmt, PreparedStatement updateMessagesStmt)
            throws SQLException {
        messagesStmt.setString(1, conversationId); // Установка ID беседы
        try (ResultSet messagesRs = messagesStmt.executeQuery()) { // Выборка сообщений
            while (messagesRs.next()) { // Обработка каждого сообщения
                String messageId = messagesRs.getString("message_id"); // ID сообщения
                String fileId = messagesRs.getString("file_id"); // Путь к файлу
                String fileType = messagesRs.getString("file_type"); // Тип файла (texts, jpg, wav и т.д.)
                String sessionId = messagesRs.getString("session_id"); // ID сессии

                sessionsStmt.setString(1, sessionId); // Установка ID сессии
                String newFileId = generateNewFileId(contactId); // Генерация нового пути к файлу

                String alg = EncryptionAccaunt.getRandomString(); // Случайный выбор алгоритма шифрования
                String secretKeyStr = generateSecretKey(alg); // Генерация нового ключа шифрования

                // Шифрование нового ключа и алгоритма новым ключом учетной записи
                String newKey = encryptionAccaunt.chaha20Encript(accauntKeyNew, secretKeyStr);
                String newAlg = encryptionAccaunt.chaha20Encript(accauntKeyNew, alg);
                try (ResultSet sessionsRs = sessionsStmt.executeQuery()) { // Выборка данных сессии
                    if (sessionsRs.next()) {
                        // Обработка сообщения и сессии
                        processMessageAndSession(messageId, fileId, fileType, sessionId,
                                sessionsRs.getString("session_key"), sessionsRs.getString("encryption_algorithm_id"),
                                updateSessionsStmt, updateMessagesStmt, newFileId, alg, secretKeyStr, newKey, newAlg);
                        Thread.sleep(1000); // Задержка для избежания перегрузки
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e); // Обработка ошибок
                }

                deleteOldFile(fileId); // Удаление старого файла
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // Обработка ошибок
        }
    }

    // Метод для обработки одного сообщения и его сессии
    private static void processMessageAndSession(String messageId, String fileId, String fileType, String sessionId,
                                                 String sessionKeyOld, String encryptionAlgorithmIdOld,
                                                 PreparedStatement updateSessionsStmt, PreparedStatement updateMessagesStmt, String newFileId, String alg, String secretKeyStr, String newKey, String newAlg)
            throws Exception {
        // Расшифровка старого ключа и алгоритма сессии
        String decryptedSessionKey = encryptionAccaunt.chaha20Decrypt(keyAc, sessionKeyOld);
        String decryptedEncryptionAlgorithmId = encryptionAccaunt.chaha20Decrypt(keyAc, encryptionAlgorithmIdOld);

        // Повторное шифрование ключа и алгоритма новым ключом
        String newKeyNewDate = encryptionAccaunt.chaha20Encript(accauntKeyNew, secretKeyStr);
        String newAlgNewDate = encryptionAccaunt.chaha20Encript(accauntKeyNew, alg);

        // Обновление базы данных
        updateDatabase(updateSessionsStmt, updateMessagesStmt, sessionId, messageId,
                newKeyNewDate, newAlgNewDate, newFileId);

        // Запуск перешифровки файла в отдельном потоке
        Thread thread = new Thread(() -> {
            reencryptFile(fileId, fileType, decryptedSessionKey, decryptedEncryptionAlgorithmId, newFileId, alg, secretKeyStr);
        });

        thread.start();
        thread.join(); // Ожидание завершения потока
    }

    // Метод для генерации нового пути к файлу
    private static String generateNewFileId(String contactId) {
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // Формат даты
        String formattedDate = dateFormat.format(new Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length())); // Уникальное имя
        return "out/" + contactId + "/" + formattedDate + ".bin"; // Формирование пути
    }

    // Метод для перешифровки файла в зависимости от его типа
    private static void reencryptFile(String fileId, String fileType,
                                      String decryptedSessionKey, String decryptedEncryptionAlgorithmId,
                                      String newFileId, String alg, String secretKeyStr) {
        switch (fileType.toLowerCase()) { // Выбор метода перешифровки по типу файла
            case "texts" -> reencryptFileText(fileId, decryptedEncryptionAlgorithmId, decryptedSessionKey,
                    secretKeyStr, newFileId, alg); // Текстовые файлы
            case "jpg", "wav" -> reencryptMediaFile(fileId, decryptedEncryptionAlgorithmId, decryptedSessionKey,
                    secretKeyStr, newFileId, alg); // Медиафайлы (jpg, wav)
            default -> keyGet.reencryptFileBlocks(fileId, decryptedEncryptionAlgorithmId,
                    decryptedSessionKey, secretKeyStr, alg, newFileId); // Блочные файлы
        }
    }

    // Метод для генерации секретного ключа шифрования
    private static String generateSecretKey(String alg) throws NoSuchAlgorithmException, NoSuchProviderException {
        return switch (alg) { // Выбор метода генерации ключа по алгоритму
            case "1a" -> KeyGet.generationKeyAES(); // Ключ для AES
            case "1b" -> KeyGet.generationKeyTwoFishAndSerpent("Twofish", "BC"); // Ключ для Twofish
            case "1c" -> KeyGet.generationKeyTwoFishAndSerpent("Serpent", "BC"); // Ключ для Serpent
            default -> throw new IllegalArgumentException("Неизвестный алгоритм: " + alg); // Обработка ошибки
        };
    }

    // Метод для перешифровки медиафайлов (jpg, wav)
    private static void reencryptMediaFile(String fileId, String decryptedEncryptionAlgorithmId,
                                           String decryptedSessionKey, String secretKeyStr,
                                           String newFileId, String alg) {
        File file = new File(fileId); // Старый файл
        if (!file.exists()) return; // Пропуск, если файл не существует

        String algorithmOld = mapAlgorithmCode(decryptedEncryptionAlgorithmId); // Старый алгоритм
        byte[] decryptedData = decryptFile.playImageFromBinFile(file, algorithmOld, decryptedSessionKey); // Расшифровка

        if (decryptedData != null) { // Если расшифровка успешна
            File newFile = new File(newFileId); // Новый файл
            newFile.getParentFile().mkdirs(); // Создание директорий
            String algorithmNew = mapAlgorithmCode(alg); // Новый алгоритм
            keyGet.encryptImageIcon(decryptedData, newFileId, secretKeyStr, algorithmNew); // Шифрование и сохранение
        }
    }

    // Метод для маппинга кодов алгоритмов на их названия
    private static String mapAlgorithmCode(String algCode) {
        return switch (algCode.toLowerCase()) { // Преобразование кода в название алгоритма
            case "1a" -> "AES";
            case "1b" -> "Twofish";
            case "1c" -> "Serpent";
            default -> throw new IllegalArgumentException("Неизвестный код алгоритма: " + algCode); // Обработка ошибки
        };
    }

    // Метод для удаления старого файла
    private static void deleteOldFile(String fileId) {
        File oldFile = new File(fileId); // Старый файл
        if (oldFile.exists()) { // Проверка существования
            if (oldFile.delete()) {
                System.out.println("Удалён старый файл: " + fileId); // Успешное удаление
            } else {
                System.out.println("Не удалось удалить старый файл: " + fileId); // Ошибка удаления
            }
        } else {
            System.out.println("Файл не существует: " + fileId); // Файл не найден
        }
    }

    // Метод для обновления базы данных (сессии и сообщения)
    private static void updateDatabase(PreparedStatement updateSessionsStmt, PreparedStatement updateMessagesStmt,
                                       String sessionId, String messageId, String sessionKeyNew,
                                       String encryptionAlgorithmIdNew, String newFileId) throws SQLException {
        // Обновление сессии
        updateSessionsStmt.setString(1, sessionKeyNew); // Новый ключ сессии
        updateSessionsStmt.setString(2, encryptionAlgorithmIdNew); // Новый алгоритм
        updateSessionsStmt.setString(3, sessionId); // ID сессии
        System.out.println(updateSessionsStmt.executeUpdate() > 0
                ? "Успешно обновлены session_key и encryption_algorithm_id для session_id: " + sessionId
                : "Не удалось обновить session_id: " + sessionId); // Логирование результата

        // Обновление сообщения
        updateMessagesStmt.setString(1, newFileId); // Новый путь к файлу
        updateMessagesStmt.setString(2, messageId); // ID сообщения
        System.out.println(updateMessagesStmt.executeUpdate() > 0
                ? "Успешно обновлён file_id в messages для message_id: " + messageId
                : "Не удалось обновить file_id в messages для message_id: " + messageId); // Логирование результата
    }

    // Метод для перешифровки данных в таблице potential_contacts
    static void tableContactsPlan() {
        // SQL-запрос для выборки потенциальных контактов
        String selectSql = "SELECT record_id, contact_id, contact_name, contact_private_key, contact_image_id " +
                "FROM potential_contacts WHERE account_id = ?";
        // SQL-запрос для обновления данных
        String updateSql = "UPDATE potential_contacts SET contact_name = ?, contact_private_key = ?, contact_image_id = ? " +
                "WHERE record_id = ? AND account_id = ?";

        Random random = new Random(); // Генератор случайных чисел
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // Формат даты

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement selectStmt = conn.prepareStatement(selectSql); // Запрос выборки
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) { // Запрос обновления

            selectStmt.setString(1, accauntId); // Установка account_id
            ResultSet rs = selectStmt.executeQuery(); // Выполнение выборки

            while (rs.next()) { // Обработка каждой записи
                String recordId = rs.getString("record_id"); // ID записи
                String contactId = rs.getString("contact_id"); // ID контакта
                String contactNameOld = rs.getString("contact_name"); // Зашифрованное имя
                String contactPrivateKeyOld = rs.getString("contact_private_key"); // Зашифрованный ключ
                String contactImageIdOld = rs.getString("contact_image_id"); // Путь к изображению

                // Расшифровка изображения, если оно существует
                byte[] resultImageByte = null;
                if (contactImageIdOld != null) {
                    File file = new File(contactImageIdOld);
                    if (file.exists()) {
                        resultImageByte = decryptFile.playImageFromBinFile(file, "AES", keyAc); // Расшифровка
                    }
                }

                // Удаление старого файла изображения
                if (contactImageIdOld != null) {
                    File oldFile = new File(contactImageIdOld);
                    if (oldFile.exists()) {
                        if (oldFile.delete()) {
                            System.out.println("Старый файл успешно удален: " + contactImageIdOld);
                        } else {
                            System.out.println("Не удалось удалить старый файл: " + contactImageIdOld);
                        }
                    } else {
                        System.out.println("Старый файл не найден: " + contactImageIdOld);
                    }
                }

                // Расшифровка данных старым ключом
                String decryptedContactName = encryptionAccaunt.chaha20Decrypt(keyAc, contactNameOld); // Имя контакта
                String decryptedContactPrivateKey = encryptionAccaunt.chaha20Decrypt(keyAc, contactPrivateKeyOld); // Приватный ключ

                // Шифрование данных новым ключом
                String contactNameNew = encryptionAccaunt.chaha20Encript(accauntKeyNew, decryptedContactName); // Новое имя
                String contactPrivateKeyNew = encryptionAccaunt.chaha20Encript(accauntKeyNew, decryptedContactPrivateKey); // Новый ключ

                // Генерация нового имени файла для изображения
                String formattedDate = dateFormat.format(new java.util.Date()) +
                        CHARACTERS.charAt(random.nextInt(CHARACTERS.length())); // Уникальное имя
                String contactImageIdNew = "out/contact_images/" + myID + "/" + contactId + "/" + formattedDate + ".bin";

                // Сохранение нового зашифрованного изображения
                if (resultImageByte != null) {
                    File fileNew = new File(contactImageIdNew);
                    fileNew.getParentFile().mkdirs(); // Создание директорий
                    keyGet.encryptImageIcon(resultImageByte, contactImageIdNew, accauntKeyNew, "AES"); // Шифрование и сохранение
                }

                // Обновление записи в базе данных
                updateStmt.setString(1, contactNameNew); // Новое имя
                updateStmt.setString(2, contactPrivateKeyNew); // Новый ключ
                updateStmt.setString(3, contactImageIdNew); // Новый путь к изображению
                updateStmt.setString(4, recordId); // ID записи
                updateStmt.setString(5, accauntId); // ID учетной записи
                int affectedRows = updateStmt.executeUpdate(); // Выполнение обновления

                // Логирование результата
                if (affectedRows > 0) {
                    System.out.println("Успешно обновлена запись для record_id: " + recordId);
                } else {
                    System.out.println("Не удалось обновить запись record_id: " + recordId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage()); // Обработка ошибок SQL
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка шифрования/дешифрования: " + e.getMessage()); // Обработка ошибок шифрования
        }
    }

    // Метод для перешифровки данных учетной записи с использованием одного пароля
    static void tableAccountsReEncryption(String password1) {
        String[] keys = getAccountKeysById(accauntId); // Получение текущих ключей учетной записи

        try {
            // Расшифровка старых значений с использованием текущего ключа
            String notification_time_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[0]); // Время уведомления
            String gap_time_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[1]); // Временной интервал
            String connect_contacts_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[2]); // Ключ подключения контактов

            // Шифрование значений новым ключом
            String notification_time_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, notification_time_old); // Новое время уведомления
            String gap_time_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, gap_time_old); // Новый интервал
            String connect_contacts_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, connect_contacts_old); // Новый ключ подключения

            // Шифрование нового ключа учетной записи с использованием password1
            String accaunt_key_new = encryptionAccaunt.chaha20Encript(password1, accauntKeyNew);

            // Обновление данных в базе данных
            updateAccountKeys(accauntId, notification_time_new, gap_time_new, accaunt_key_new, connect_contacts_new);

        } catch (Exception e) {
            throw new RuntimeException(e); // Обработка ошибок
        }
    }

    // Метод для перешифровки ответов на запросы приложений
    public static void reEncryptApplicationResponses() {
        // SQL-запрос для выборки записей из таблицы application_responses
        String selectSql = "SELECT record_id, name_contact FROM application_responses WHERE account_id = ?";
        // SQL-запрос для обновления поля name_contact
        String updateSql = "UPDATE application_responses SET name_contact = ? WHERE record_id = ? AND account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement selectStmt = conn.prepareStatement(selectSql); // Запрос выборки
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) { // Запрос обновления

            selectStmt.setString(1, accauntId); // Установка account_id
            ResultSet rs = selectStmt.executeQuery(); // Выполнение выборки

            while (rs.next()) { // Обработка каждой записи
                String recordId = rs.getString("record_id"); // ID записи
                String encryptedNameContactOld = rs.getString("name_contact"); // Зашифрованное имя контакта

                // Расшифровка старого значения с использованием текущего ключа
                String decryptedNameContact = encryptionAccaunt.chaha20Decrypt(keyAc, encryptedNameContactOld);

                // Шифрование новым ключом
                String encryptedNameContactNew = encryptionAccaunt.chaha20Encript(accauntKeyNew, decryptedNameContact);

                // Обновление записи в базе данных
                updateStmt.setString(1, encryptedNameContactNew); // Новое зашифрованное имя
                updateStmt.setString(2, recordId); // ID записи
                updateStmt.setString(3, accauntId); // ID учетной записи
                updateStmt.executeUpdate(); // Выполнение обновления
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage()); // Обработка ошибок SQL
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage()); // Обработка ошибок шифрования
        }
    }

    // Метод для перешифровки данных планирования чатов
    public static void reEncryptChatPlanning() {
        // SQL-запрос для выборки записей из таблицы chat_planning
        String selectSql = "SELECT record_id, messages, start_time, end_time, key_planning FROM chat_planning WHERE account_id = ?";
        // SQL-запрос для обновления данных
        String updateSql = "UPDATE chat_planning SET messages = ?, start_time = ?, end_time = ?, key_planning = ? WHERE record_id = ? AND account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement selectStmt = conn.prepareStatement(selectSql); // Запрос выборки
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) { // Запрос обновления

            selectStmt.setString(1, accauntId); // Установка account_id
            ResultSet rs = selectStmt.executeQuery(); // Выполнение выборки

            while (rs.next()) { // Обработка каждой записи
                String recordId = rs.getString("record_id"); // ID записи
                String messagesOld = rs.getString("messages"); // Зашифрованные сообщения
                String start_timeOld = rs.getString("start_time"); // Зашифрованное время начала
                String end_timeOld = rs.getString("end_time"); // Зашифрованное время окончания
                String key_planningOld = rs.getString("key_planning"); // Зашифрованный ключ планирования

                // Расшифровка ключа планирования
                String key_date = encryptionAccaunt.chaha20Decrypt(keyAc, key_planningOld);

                // Расшифровка данных с использованием ключа планирования
                String messages = messagesOld != null ? encryptionAccaunt.chaha20Decrypt(key_date, messagesOld) : null;
                String start_time = start_timeOld != null ? encryptionAccaunt.chaha20Decrypt(key_date, start_timeOld) : null;
                String end_time = end_timeOld != null ? encryptionAccaunt.chaha20Decrypt(key_date, end_timeOld) : null;

                // Генерация нового ключа для шифрования
                String alg = EncryptionAccaunt.getRandomString(); // Случайный выбор алгоритма
                String secretKeyStr = null;
                switch (alg) { // Генерация ключа в зависимости от алгоритма
                    case "1a":
                        secretKeyStr = KeyGet.generationKeyAES(); // Ключ AES
                        break;
                    case "1b":
                        secretKeyStr = KeyGet.generationKeyTwoFishAndSerpent("Twofish", "BC"); // Ключ Twofish
                        break;
                    case "1c":
                        secretKeyStr = KeyGet.generationKeyTwoFishAndSerpent("Serpent", "BC"); // Ключ Serpent
                        break;
                }

                // Шифрование данных новым ключом
                String messagesNew = messages != null ? encryptionAccaunt.chaha20Encript(secretKeyStr, messages) : null;
                String start_timeNew = start_time != null ? encryptionAccaunt.chaha20Encript(secretKeyStr, start_time) : null;
                String end_timeNew = end_time != null ? encryptionAccaunt.chaha20Encript(secretKeyStr, end_time) : null;
                String keyNew = encryptionAccaunt.chaha20Encript(accauntKeyNew, secretKeyStr); // Шифрование нового ключа

                // Обновление записи в базе данных
                updateStmt.setString(1, messagesNew); // Новые сообщения
                updateStmt.setString(2, start_timeNew); // Новое время начала
                updateStmt.setString(3, end_timeNew); // Новое время окончания
                updateStmt.setString(4, keyNew); // Новый ключ планирования
                updateStmt.setString(5, recordId); // ID записи
                updateStmt.setString(6, accauntId); // ID учетной записи
                updateStmt.executeUpdate(); // Выполнение обновления
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage()); // Обработка ошибок SQL
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка шифрования/дешифрования: " + e.getMessage()); // Обработка ошибок шифрования
        }
    }

    // Метод для обновления ключей учетной записи (с одним паролем)
    public static void updateAccountKeys(String accauntId, String notification_time_new, String gap_time_new,
                                         String accaunt_key_new, String connect_contacts_new) {
        // SQL-запрос для обновления данных учетной записи
        String sql = "UPDATE accounts SET account_key = ?, notification_time = ?, gap_time = ?, key_connection = ? " +
                "WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Подготовка запроса

            // Установка параметров запроса
            pstmt.setString(1, accaunt_key_new); // Новый ключ учетной записи
            pstmt.setString(2, notification_time_new); // Новое время уведомления
            pstmt.setString(3, gap_time_new); // Новый временной интервал
            pstmt.setString(4, connect_contacts_new); // Новый ключ подключения
            pstmt.setString(5, accauntId); // ID учетной записи

            pstmt.executeUpdate(); // Выполнение обновления

        } catch (Exception e) {
            e.printStackTrace(); // Логирование ошибок
        }
    }

    // Метод для перешифровки учетной записи с использованием двух паролей
    static void tableAccountsReEncryption(String password1, String password2) {
        String[] keys = getAccountKeysById(accauntId); // Получение текущих ключей учетной записи

        try {
            // Расшифровка старых значений
            String notification_time_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[0]); // Время уведомления
            String gap_time_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[1]); // Временной интервал
            String connect_contacts_old = encryptionAccaunt.chaha20Decrypt(keyAc, keys[2]); // Ключ подключения

            // Шифрование значений новым ключом
            String notification_time_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, notification_time_old);
            String gap_time_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, gap_time_old);
            String connect_contacts_new = encryptionAccaunt.chaha20Encript(accauntKeyNew, connect_contacts_old);

            // Шифрование нового ключа учетной записи двумя паролями
            String accaunt_key_new = encryptionAccaunt.chaha20Encript(password1, accauntKeyNew); // Основной пароль
            String accaunt_passphrase_new = encryptionAccaunt.chaha20Encript(password2, accauntKeyNew); // Кодовое слово

            // Обновление данных в базе данных
            updateAccountKeys(accauntId, notification_time_new, gap_time_new, accaunt_key_new, accaunt_passphrase_new, connect_contacts_new);

        } catch (Exception e) {
            throw new RuntimeException(e); // Обработка ошибок
        }
    }

    // Метод для обновления ключей учетной записи (с двумя паролями)
    public static void updateAccountKeys(String accauntId, String notification_time_new, String gap_time_new,
                                         String accaunt_key_new, String accaunt_passphrase_new, String connect_contacts_new) {
        // SQL-запрос для обновления данных учетной записи
        String sql = "UPDATE accounts SET account_key = ?, backup_account_key = ?, notification_time = ?, gap_time = ?, key_connection = ? " +
                "WHERE account_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Подготовка запроса

            // Установка параметров запроса
            pstmt.setString(1, accaunt_key_new); // Новый ключ учетной записи
            pstmt.setString(2, accaunt_passphrase_new); // Новый резервный ключ (кодовое слово)
            pstmt.setString(3, notification_time_new); // Новое время уведомления
            pstmt.setString(4, gap_time_new); // Новый временной интервал
            pstmt.setString(5, connect_contacts_new); // Новый ключ подключения
            pstmt.setString(6, accauntId); // ID учетной записи

            int affectedRows = pstmt.executeUpdate(); // Выполнение обновления

            // Логирование результата
            if (affectedRows > 0) {
                System.out.println("Данные успешно обновлены для account_id: " + accauntId);
            } else {
                System.out.println("Данные НЕ были обновлены (возможно, account_id не найден): " + accauntId);
            }

        } catch (Exception e) {
            e.printStackTrace(); // Логирование ошибок
        }
    }

    // Метод для получения текущих ключей учетной записи
    public static String[] getAccountKeysById(String accountId) {
        // SQL-запрос для выборки данных учетной записи
        String sql = "SELECT notification_time, gap_time, key_connection FROM accounts WHERE account_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL); // Подключение к базе данных
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Подготовка запроса

            pstmt.setString(1, accountId); // Установка account_id
            ResultSet rs = pstmt.executeQuery(); // Выполнение выборки

            if (rs.next()) { // Если запись найдена
                String notification_time = rs.getString("notification_time"); // Время уведомления
                String gap_time = rs.getString("gap_time"); // Временной интервал
                String connect_contacts = rs.getString("key_connection"); // Ключ подключения
                return new String[]{notification_time, gap_time, connect_contacts}; // Возврат массива ключей
            }

        } catch (Exception e) {
            e.printStackTrace(); // Логирование ошибок
        }
        return null; // Возврат null в случае ошибки или отсутствия записи
    }
}
