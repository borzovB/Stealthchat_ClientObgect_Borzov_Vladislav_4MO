package org.face_recognition;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// Класс NotificationDaemon реализует фоновый поток демона
// Он проверяет, нужно ли уведомить пользователя о необходимости обновления ключей шифрования и перешифровки данных
// Демон запускается автоматически при создании экземпляра класса
// Проверяет, включены ли уведомления для конкретного аккаунта
// Получает из базы данных зашифрованное время следующего уведомления и расшифровывает его
// Если наступает указанное время, отображается всплывающее окно с уведомлением пользователю
// Далее вычисляется следующее время уведомления на основе интервала, заданного в базе данных
// Новое время зашифровывается и сохраняется обратно в базу данных
// Класс синхронизирует доступ к базе данных с помощью объекта блокировки, чтобы избежать конкурентного доступа
// Используется алгоритм ChaCha20 для симметричного шифрования времени и интервалов

public class NotificationDaemon {
    // Идентификатор аккаунта и ключ шифрования
    private final String accountId;
    private final String keyAc;

    // Статическая база данных — предполагается, что существует класс Database
    private static Database database = new Database();

    // Поток демона
    private static Thread daemonThread;

    // Флаг для управления циклом работы демона
    private static volatile boolean running = true;

    // Объект для шифрования
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();

    // Объект для синхронизации доступа к базе данных
    private static final Object dbLock = new Object();

    // Конструктор демона, инициализирует и запускает его
    public NotificationDaemon(String accountId, String keyAc) {
        this.accountId = accountId;
        this.keyAc = keyAc;
        initializeDaemon();
        start();
    }

    // Метод инициализации потока демона
    void initializeDaemon() {
        daemonThread = new Thread(() -> {
            try {
                // Начальная задержка перед стартом демона
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return;
            }

            // Основной цикл работы демона
            while (running) {

                try {
                    // Синхронизированный доступ к базе данных
                    synchronized (dbLock) {
                        // Ожидаем освобождения базы данных
                        while (database.isDatabaseLocked()) {
                            dbLock.wait();
                        }

                        // Блокируем базу данных
                        database.lockDatabase();

                        try {
                            // Проверяем, включены ли уведомления
                            if (database.getStatusSafety(accountId)) {
                                // Получаем сохраненное время уведомления
                                Instant dbTime = database.getNotificationTimeFromDB(accountId, keyAc);

                                // Если пришло время показать уведомление
                                if (dbTime != null && !dbTime.isAfter(Instant.now())) {
                                    // Получаем интервалы времени (сек, мин, часы и т.д.)
                                    int[] intervals = database.getTimeInterval(accountId, keyAc);

                                    // Выводим интервалы в консоль
                                    StringBuilder sb = new StringBuilder("Содержимое массива timeIntervals: ");
                                    for (int value : intervals) {
                                        sb.append(value).append(" ");
                                    }

                                    // Проверяем, что интервалов достаточно
                                    if (intervals.length < 6) {
                                        continue; // Переход к следующей итерации (внимание: unlock в finally всё равно выполнится)
                                    }

                                    // Извлекаем значения интервалов
                                    int seconds = intervals[0];
                                    int minutes = intervals[1];
                                    int hours = intervals[2];
                                    int days = intervals[3];
                                    int months = intervals[4];
                                    int years = intervals[5];

                                    // Строковое представление интервалов
                                    String gap = String.format("%d %d %d %d %d %d", seconds, minutes, hours, days, months, years);

                                    // Получаем текущее UTC-время
                                    Instant currentUTC = database.getCurrentTimeUTC();

                                    // Переводим в локальное время и прибавляем интервалы
                                    ZonedDateTime currentZoned = currentUTC.atZone(ZoneId.systemDefault());
                                    ZonedDateTime resultZoned = currentZoned
                                            .plusYears(years)
                                            .plusMonths(months)
                                            .plusDays(days)
                                            .plusHours(hours)
                                            .plusMinutes(minutes)
                                            .plusSeconds(seconds);

                                    // Переводим обратно в UTC
                                    Instant resultUTC = resultZoned.toInstant();
                                    String data = resultUTC.toString();

                                    // Шифруем дату и интервалы
                                    String encryptedTime = encryption.chaha20Encript(keyAc, data);
                                    String encryptedGap = encryption.chaha20Encript(keyAc, gap);

                                    // Сохраняем новые значения в базу
                                    database.saveNotificationTimeToDB(encryptedTime, encryptedGap, accountId);

                                    // Повторно получаем сохраненное время (проверка)
                                    Instant utcTime = database.getNotificationTimeFromDB(accountId, keyAc);
                                    if (utcTime != null) {
                                        // Переводим время в локальное и форматируем
                                        ZonedDateTime localTime = utcTime.atZone(ZoneId.systemDefault());
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                                        String formattedResult = localTime.format(formatter);

                                        // Текст уведомления
                                        String message = String.format(
                                                "Уведомление: вам необходимо обновить ключи шифрования и перешифровать данные, " +
                                                        "следующее уведомление будет %s.",
                                                formattedResult
                                        );

                                        // Показываем диалог в UI-потоке
                                        SwingUtilities.invokeAndWait(() -> {
                                            JOptionPane.showMessageDialog(
                                                    null,
                                                    message,
                                                    "Уведомление",
                                                    JOptionPane.INFORMATION_MESSAGE
                                            );
                                        });
                                    } else {
                                        System.out.println("Не удалось получить время уведомления из БД");
                                    }
                                }
                            }
                        } finally {
                            // Разблокируем базу данных в любом случае
                            database.unlockDatabase();
                            synchronized (dbLock) {
                                dbLock.notifyAll(); // Уведомляем другие потоки
                            }
                        }
                    }

                    // Пауза между итерациями цикла (15 секунд)
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    running = false; // Завершаем работу демона
                } catch (Exception e) {
                    // Обработка любых других ошибок
                    e.printStackTrace();
                }
            }

        });

        daemonThread.setDaemon(true); // Отмечаем поток как демон (автоматически завершается при завершении программы)
    }

    // Метод запуска демона
    public static void start() {
        if (daemonThread != null && !daemonThread.isAlive()) {
            running = true;
            daemonThread.start();
        }
    }

    // Метод остановки демона
    public static void stop() {
        if (daemonThread != null && daemonThread.isAlive()) {
            running = false;
            daemonThread.interrupt(); // Прерываем выполнение потока
        }
    }
}
