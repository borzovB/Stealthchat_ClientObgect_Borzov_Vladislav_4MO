package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// Класс для создания диалогового окна настроек пользователя, включая синхронизацию контактов, блокировку клиентов и уведомления
public class ParametersDialog {

    // Статические поля для хранения параметров учетной записи, объектов интерфейса и данных
    private static String accauntId; // Идентификатор учетной записи
    private static String keyAc; // Ключ шифрования учетной записи
    private static String myID; // Идентификатор клиента
    private static ContactPanel contactPanel = new ContactPanel(); // Панель для работы с контактами
    private static JDialog dialog; // Диалоговое окно настроек
    private static Database database = new Database(); // Объект для работы с базой данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt(); // Объект для шифрования/дешифрования
    private static ObjectOutputStream out; // Поток вывода для связи с сервером
    private static JFrame frame; // Главное окно приложения

    // Конструктор класса, инициализирующий параметры
    public ParametersDialog(String accauntId, String keyAc, String myID, ObjectOutputStream out, JFrame frame) {
        this.accauntId = accauntId; // Сохранение ID учетной записи
        this.keyAc = keyAc; // Сохранение ключа шифрования
        this.myID = myID; // Сохранение ID клиента
        this.out = out; // Сохранение потока вывода
        this.frame = frame; // Сохранение главного окна
    }

    // Метод для создания и отображения диалогового окна настроек
    public static void createDialog(String title) {
        dialog = new JDialog(frame, title, true); // Создание модального диалогового окна
        dialog.setSize(700, 600); // Установка размеров окна
        dialog.setLocationRelativeTo(frame); // Центрирование относительно главного окна
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие окна при выходе
        dialog.setResizable(false); // Запрет изменения размера

        // Главная панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Отступы
        mainPanel.setBackground(Color.WHITE); // Белый фон

        // Верхняя панель с заголовком и кнопкой закрытия
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE); // Белый фон
        topPanel.add(Box.createHorizontalStrut(5)); // Отступ слева

        // Заголовок диалога
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Шрифт
        titleLabel.setForeground(Color.BLACK); // Черный текст

        // Кнопка закрытия с иконкой
        JButton topCloseButton = contactPanel.createIconButton("pictures/exitIcon.png", "Выход");
        topCloseButton.addActionListener(e -> dialog.dispose()); // Закрытие диалога
        topCloseButton.setFocusPainted(false); // Удаление обводки при фокусе
        topCloseButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Отступы

        topPanel.add(titleLabel, BorderLayout.CENTER); // Заголовок в центре
        topPanel.add(topCloseButton, BorderLayout.EAST); // Кнопка справа

        // Панель для переключателей настроек
        JPanel switchPanel = new JPanel();
        switchPanel.setLayout(new BoxLayout(switchPanel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        switchPanel.setBackground(Color.WHITE); // Белый фон
        switchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Отступ сверху

        // Панель для переключателей синхронизации и блокировки
        JPanel toggleBlock1 = new JPanel();
        toggleBlock1.setLayout(new BoxLayout(toggleBlock1, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        toggleBlock1.setBackground(Color.WHITE); // Белый фон
        toggleBlock1.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        toggleBlock1.setPreferredSize(new Dimension(700, 300)); // Размер

        // Панель для настройки синхронизации контактов
        JPanel syncPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        syncPanel.setBackground(Color.WHITE); // Белый фон
        syncPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Черная рамка
        syncPanel.setMaximumSize(new Dimension(700, 80)); // Максимальный размер
        syncPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Метка для синхронизации
        JLabel syncLabel = new JLabel("Синхронизация контактов:");
        syncLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        syncLabel.setForeground(Color.BLACK); // Черный текст

        // Контейнер для переключателя синхронизации
        JPanel switchContainer1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchContainer1.setBackground(Color.WHITE); // Белый фон
        switchContainer1.setPreferredSize(new Dimension(200, 40)); // Размер

        try {
            // Создание переключателя для синхронизации контактов
            Toggle toggleSwitch1 = new Toggle(accauntId, "1a", out, myID);
            toggleSwitch1.setPreferredSize(new Dimension(200, 40)); // Размер переключателя
            switchContainer1.add(toggleSwitch1); // Добавление переключателя
        } catch (SQLException e) {
            e.printStackTrace(); // Логирование ошибок SQL
        }

        syncPanel.add(syncLabel); // Добавление метки
        syncPanel.add(switchContainer1); // Добавление переключателя

        // Панель для настройки блокировки клиентов
        JPanel blockPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        blockPanel.setBackground(Color.WHITE); // Белый фон
        blockPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Черная рамка
        blockPanel.setMaximumSize(new Dimension(700, 80)); // Максимальный размер
        blockPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Проверка и установка времени уведомления, если оно отсутствует
        if (!database.getNotificationGap(accauntId)) {
            // Установка периода по умолчанию (7 дней)
            int seconds = 0, minutes = 0, hours = 0, days = 7, months = 0, years = 0;
            String gap = seconds + " " + minutes + " " + hours + " " + days + " " + months + " " + years;

            // Получение текущего времени в UTC
            Instant currentUTC = database.getCurrentTimeUTC();
            ZonedDateTime currentZoned = currentUTC.atZone(ZoneId.systemDefault());
            // Вычисление времени следующего уведомления
            ZonedDateTime resultZoned = currentZoned
                    .plusYears(years)
                    .plusMonths(months)
                    .plusDays(days)
                    .plusHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);
            Instant resultUTC = resultZoned.toInstant();
            String data = resultUTC.toString();

            try {
                // Сохранение зашифрованного времени уведомления и интервала в базу данных
                database.saveNotificationTimeToDB(
                        encryption.chaha20Encript(keyAc, data),
                        encryption.chaha20Encript(keyAc, gap),
                        accauntId
                );
            } catch (Exception e) {
                throw new RuntimeException(e); // Обработка ошибок
            }
        }

        // Метка для блокировки клиентов
        JLabel syncLabelBlock = new JLabel("Блокированные клиенты:");
        syncLabelBlock.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        syncLabelBlock.setForeground(Color.BLACK); // Черный текст

        // Контейнер для переключателя блокировки
        JPanel switchContainerBlock = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchContainerBlock.setBackground(Color.WHITE); // Белый фон
        switchContainerBlock.setPreferredSize(new Dimension(200, 40)); // Размер

        try {
            // Создание переключателя для блокировки клиентов
            Toggle toggleSwitchBlock = new Toggle(accauntId, "2a", out, myID);
            toggleSwitchBlock.setPreferredSize(new Dimension(200, 40)); // Размер переключателя
            switchContainerBlock.add(toggleSwitchBlock); // Добавление переключателя
        } catch (SQLException e) {
            e.printStackTrace(); // Логирование ошибок SQL
        }

        blockPanel.add(syncLabelBlock); // Добавление метки
        blockPanel.add(switchContainerBlock); // Добавление переключателя

        // Добавление элементов на панель toggleBlock1
        toggleBlock1.add(Box.createVerticalStrut(20)); // Пространство
        toggleBlock1.add(syncPanel); // Панель синхронизации
        toggleBlock1.add(Box.createVerticalStrut(30)); // Пространство
        toggleBlock1.add(blockPanel); // Панель блокировки
        toggleBlock1.add(Box.createVerticalStrut(20)); // Пространство

        // Панель для настройки уведомлений
        JPanel toggleBlock2 = new JPanel();
        toggleBlock2.setLayout(new BoxLayout(toggleBlock2, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        toggleBlock2.setBackground(Color.WHITE); // Белый фон
        toggleBlock2.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        toggleBlock2.setPreferredSize(new Dimension(700, 300)); // Размер

        // Метка для уведомлений
        JLabel notifyLabel = new JLabel("Уведомления:", SwingConstants.CENTER);
        notifyLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        notifyLabel.setForeground(Color.BLACK); // Черный текст
        notifyLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Контейнер для переключателя уведомлений
        JPanel switchContainer2 = new JPanel();
        switchContainer2.setLayout(new BoxLayout(switchContainer2, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        switchContainer2.setBackground(Color.WHITE); // Белый фон
        switchContainer2.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Черная рамка
        switchContainer2.setPreferredSize(new Dimension(700, 230)); // Размер
        switchContainer2.setMinimumSize(new Dimension(700, 230)); // Минимальный размер

        // Панель для переключателя уведомлений
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        togglePanel.setBackground(Color.WHITE); // Белый фон

        // Динамическая панель для отображения состояния уведомлений
        JPanel dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        dynamicPanel.setBackground(Color.WHITE); // Белый фон
        dynamicPanel.setPreferredSize(new Dimension(400, 180)); // Размер
        dynamicPanel.setMaximumSize(new Dimension(400, 180)); // Максимальный размер
        dynamicPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Панель для состояния "уведомления выключены"
        JPanel offPanel = new JPanel();
        offPanel.setBackground(Color.WHITE); // Белый фон
        offPanel.setPreferredSize(new Dimension(400, 180)); // Размер
        offPanel.setMaximumSize(new Dimension(400, 180)); // Максимальный размер
        offPanel.setLayout(new BorderLayout());
        offPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )); // Рамка и отступы

        // Метка для состояния "уведомления выключены"
        JLabel offLabel = new JLabel("Включите уведомления", SwingConstants.CENTER);
        offLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        offLabel.setForeground(new Color(80, 80, 80)); // Серый текст

        try {
            // Добавление иконки для состояния "уведомления выключены"
            ImageIcon icon = new ImageIcon("pictures/notification-off.png");
            Image scaledIcon = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            offPanel.add(iconLabel, BorderLayout.NORTH); // Иконка сверху
        } catch (Exception e) {
            System.out.println("Иконка не найдена, пропускаем..."); // Логирование ошибки
        }

        // Панель для метки "уведомления выключены"
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(Color.WHITE); // Белый фон
        labelPanel.add(offLabel, BorderLayout.CENTER); // Метка в центре
        labelPanel.add(Box.createVerticalStrut(20), BorderLayout.SOUTH); // Пространство снизу
        offPanel.add(labelPanel, BorderLayout.CENTER); // Добавление метки

        // Панель для состояния "уведомления включены"
        JPanel onPanel = new JPanel();
        onPanel.setLayout(new BoxLayout(onPanel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        onPanel.setBackground(Color.WHITE); // Белый фон
        onPanel.setPreferredSize(new Dimension(400, 180)); // Размер
        onPanel.setMaximumSize(new Dimension(400, 180)); // Максимальный размер
        onPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Отступы

        // Кнопка для установки периода уведомлений
        JButton periodButton = new JButton("Установить период");
        periodButton.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        periodButton.setBackground(new Color(70, 130, 180)); // Синий фон
        periodButton.setForeground(Color.WHITE); // Белый текст
        periodButton.setFocusPainted(false); // Удаление обводки при фокусе
        periodButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        periodButton.setMaximumSize(new Dimension(200, 40)); // Максимальный размер
        periodButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g.setColor(new Color(50, 110, 160)); // Темный синий при нажатии
                } else {
                    g.setColor(new Color(70, 130, 180)); // Обычный синий
                }
                g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10); // Закругленные углы
                super.paint(g, c);
            }
        });

        // Кнопка для перешифровки данных
        JButton encryptButton = new JButton("Перешифровать");
        encryptButton.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        encryptButton.setBackground(new Color(100, 149, 237)); // Светло-синий фон
        encryptButton.setForeground(Color.WHITE); // Белый текст
        encryptButton.setFocusPainted(false); // Удаление обводки при фокусе
        encryptButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        encryptButton.setMaximumSize(new Dimension(200, 40)); // Максимальный размер
        encryptButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g.setColor(new Color(80, 129, 217)); // Темный синий при нажатии
                } else {
                    g.setColor(new Color(100, 149, 237)); // Обычный синий
                }
                g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10); // Закругленные углы
                super.paint(g, c);
            }
        });

        // Панель для отображения времени следующего уведомления
        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setLayout(new BoxLayout(dateTimePanel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        dateTimePanel.setBackground(Color.WHITE); // Белый фон
        dateTimePanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Метка для времени следующего уведомления
        JLabel nextNotificationLabel = new JLabel("Следующее уведомление будет:", SwingConstants.CENTER);
        nextNotificationLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт
        nextNotificationLabel.setForeground(new Color(50, 50, 50)); // Темно-серый текст
        nextNotificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование

        // Поле для отображения времени уведомления
        JTextField dateTimeField = new JTextField(getNotificationTimeLocalFromDB());
        dateTimeField.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт
        dateTimeField.setEditable(false); // Поле только для чтения
        dateTimeField.setBorder(BorderFactory.createEmptyBorder()); // Без рамки
        dateTimeField.setHorizontalAlignment(JTextField.CENTER); // Центрирование текста
        dateTimeField.setBackground(Color.WHITE); // Белый фон
        dateTimeField.setMaximumSize(new Dimension(250, 30)); // Максимальный размер

        // Добавление элементов на панель времени
        dateTimePanel.add(nextNotificationLabel); // Метка
        dateTimePanel.add(Box.createVerticalStrut(5)); // Пространство
        dateTimePanel.add(dateTimeField); // Поле времени

        // Добавление элементов на панель состояния "уведомления включены"
        onPanel.add(periodButton); // Кнопка периода
        onPanel.add(Box.createVerticalStrut(10)); // Пространство
        onPanel.add(encryptButton); // Кнопка перешифровки
        onPanel.add(Box.createVerticalStrut(10)); // Пространство
        onPanel.add(dateTimePanel); // Панель времени

        try {
            // Создание переключателя для уведомлений
            Toggle toggleSwitch2 = new Toggle(accauntId, "3a", out, myID);
            toggleSwitch2.setPreferredSize(new Dimension(200, 40)); // Размер переключателя
            togglePanel.add(notifyLabel); // Метка уведомлений
            togglePanel.add(toggleSwitch2); // Переключатель

            // Установка начального состояния панели (вкл/выкл уведомления)
            dynamicPanel.add(toggleSwitch2.isOn() ? onPanel : offPanel);

            // Обработчик переключения состояния уведомлений
            toggleSwitch2.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dynamicPanel.removeAll(); // Удаление текущего содержимого
                    dynamicPanel.add(toggleSwitch2.isOn() ? onPanel : offPanel); // Добавление новой панели
                    dynamicPanel.revalidate(); // Обновление компоновки
                    dynamicPanel.repaint(); // Перерисовка
                }
            });

            // Обработчик кнопки "Установить период"
            periodButton.addActionListener(e -> {
                // Создание диалогового окна для установки периода
                JDialog dateTimeDialog = new JDialog(frame, "Установить период", true);
                dateTimeDialog.setSize(600, 300); // Размер окна
                dateTimeDialog.setLocationRelativeTo(frame); // Центрирование
                dateTimeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие при выходе
                dateTimeDialog.setResizable(false); // Запрет изменения размера

                // Создание редактора времени
                DateTimeEditor dateTimeEditor = new DateTimeEditor(dateTimeDialog, dateTimeField, keyAc, accauntId);
                dateTimeDialog.add(dateTimeEditor); // Добавление редактора

                dialog.dispose(); // Закрытие текущего диалога
                dateTimeDialog.setVisible(true); // Отображение нового диалога

                // Обработчик закрытия диалога
                dateTimeDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        createDialog(title); // Повторное открытие диалога настроек
                    }
                });
            });

            // Обработчик кнопки "Перешифровать"
            encryptButton.addActionListener(e -> {
                // Проверка наличия кодового слова
                if (database.isPassphrasePresent(accauntId)) {
                    // Если кодовое слово есть, открывается панель полной расшифровки
                    JDialog dialogIncomplete = new JDialog(frame, "Безопасность", true);
                    dialogIncomplete.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие при выходе
                    dialogIncomplete.setSize(520, 526); // Размер окна
                    dialogIncomplete.setResizable(false); // Запрет изменения размера
                    dialogIncomplete.setLocationRelativeTo(null); // Центрирование

                    dialog.dispose(); // Закрытие текущего диалога

                    // Запуск панели полной расшифровки
                    DecryptionPanelComplete decryptionPanelComplete =
                            new DecryptionPanelComplete(frame, accauntId, myID, keyAc, out);
                    decryptionPanelComplete.startDevPassword(dialogIncomplete);
                } else {
                    // Если кодового слова нет, открывается панель неполной расшифровки
                    JDialog dialogIncomplete = new JDialog(frame, "Безопасность", true);
                    dialogIncomplete.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие при выходе
                    dialogIncomplete.setSize(520, 350); // Размер окна
                    dialogIncomplete.setResizable(false); // Запрет изменения размера
                    dialogIncomplete.setLocationRelativeTo(null); // Центрирование

                    dialog.dispose(); // Закрытие текущего диалога

                    // Запуск панели неполной расшифровки
                    DecryptionPanelIncomplete decryptionPanelIncomplete =
                            new DecryptionPanelIncomplete(frame, accauntId, myID, keyAc, out);
                    decryptionPanelIncomplete.startDevPassword(dialogIncomplete);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace(); // Логирование ошибок SQL
        }

        // Добавление элементов на панель уведомлений
        switchContainer2.add(togglePanel); // Панель переключателя
        switchContainer2.add(Box.createVerticalStrut(10)); // Пространство
        switchContainer2.add(dynamicPanel); // Динамическая панель

        toggleBlock2.add(Box.createVerticalStrut(10)); // Пространство
        toggleBlock2.add(switchContainer2); // Панель уведомлений

        // Добавление всех панелей на главную панель переключателей
        switchPanel.add(toggleBlock1); // Панель синхронизации и блокировки
        switchPanel.add(Box.createVerticalStrut(10)); // Пространство
        switchPanel.add(toggleBlock2); // Панель уведомлений

        // Добавление панелей на главную панель
        mainPanel.add(topPanel, BorderLayout.NORTH); // Верхняя панель
        mainPanel.add(switchPanel, BorderLayout.CENTER); // Панель переключателей

        dialog.add(mainPanel); // Добавление главной панели в диалог
        dialog.setVisible(true); // Отображение диалога
    }

    // Метод для получения локального времени следующего уведомления из базы данных
    public static String getNotificationTimeLocalFromDB() {
        // Получение времени в формате UTC
        Instant utcTime = database.getNotificationTimeFromDB(accauntId, keyAc);
        // Конвертация в локальное время
        ZonedDateTime localTime = utcTime.atZone(ZoneId.systemDefault());
        // Форматирование времени
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return localTime.format(formatter); // Возврат отформатированного времени
    }
}
