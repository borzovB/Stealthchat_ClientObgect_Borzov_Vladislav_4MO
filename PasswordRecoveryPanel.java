package org.face_recognition;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс для создания панели восстановления пароля в приложении StealthChat
public class PasswordRecoveryPanel {

    // Статические поля для хранения компонентов интерфейса, сетевых параметров и данных
    private static JFrame frame; // Главное окно приложения
    private static JPanel textPanel; // Панель для поля ввода имени
    private static JPanel textPanel1; // Панель для поля ввода кодового слова
    private static JPanel textPanel3; // Панель для поля ввода логина
    private static JPanel textPanel4; // Панель для поля ввода электронной почты
    private static JTextField passwordField1; // Поле для ввода имени
    private static JPasswordField passwordField2; // Поле для ввода кодового слова
    private static JTextField passwordField4; // Поле для ввода электронной почты
    private static JTextField passwordField; // Поле для ввода логина
    private static int PORT; // Порт для SSL-соединения
    private static String IP; // IP-адрес сервера
    private static EntrancePanelAll entrancePanel; // Панель входа
    private static String[] conf; // Конфигурация сервера из Config
    private static String connect; // Ключ подключения
    public static String mail; // Электронная почта пользователя
    public static String username; // Имя пользователя
    public static String code_sey; // Кодовое слово
    public static String login; // Логин пользователя
    private static SSLSocket socket; // SSL-сокет для связи с сервером
    private static ObjectOutputStream out; // Поток вывода для отправки данных
    private static ObjectInputStream in; // Поток ввода для получения данных
    private static String stringStart; // Ответ сервера
    private static String[] data; // Данные, полученные из базы данных
    private static java.util.List<String> fields; // Список полей для запроса в базу данных
    private static String tableName = "accounts"; // Имя таблицы в базе данных
    private static Database database = new Database(); // Объект для работы с базой данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt(); // Объект для шифрования/дешифрования
    private static Config config = new Config(); // Объект для получения конфигурации
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий сетевые параметры и размеры окна
    public PasswordRecoveryPanel(String IP, int PORT, int width, int height) {
        this.PORT = PORT; // Сохранение порта
        this.IP = IP; // Сохранение IP-адреса
        conf = config.configServer(); // Получение конфигурации сервера
        this.width = width; // Сохранение ширины окна
        this.height = height; // Сохранение высоты окна
    }

    // Метод для запуска панели восстановления пароля
    public static void startRecordPassword() throws IOException {
        frame = new JFrame("StealthChat"); // Создание главного окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Закрытие приложения при закрытии окна
        if (width > 740 && height > 750) {
            frame.setSize(width, height); // Установка размеров окна, если они больше минимальных
        } else {
            frame.setSize(700, 750); // Установка минимальных размеров
        }

        Dimension minSize = new Dimension(740, 810); // Минимальный размер окна
        frame.setMinimumSize(minSize); // Установка минимального размера

        // Центрирование окна по горизонтали и размещение у верхнего края экрана
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2; // Центр по горизонтали
        int y = 0; // Верхний край
        frame.setLocation(x, y); // Установка позиции окна

        // Добавление слушателя для отслеживания изменения размеров окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth(); // Обновление ширины
                height = frame.getHeight(); // Обновление высоты
            }
        });

        ControlPanel controlPanel = new ControlPanel(); // Объект для настройки текстовых полей

        // Создание главной панели
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        mainPanel.setBackground(new Color(30, 30, 30)); // Темный фон
        mainPanel.setOpaque(true); // Непрозрачность панели

        mainPanel.add(Box.createVerticalGlue()); // Пространство сверху для центрирования

        // Создание центральной панели для полей ввода и кнопок
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        panel.setBackground(new Color(60, 63, 65)); // Серый фон
        panel.setPreferredSize(new Dimension(520, 610)); // Предпочтительный размер
        panel.setMinimumSize(new Dimension(520, 610)); // Минимальный размер
        panel.setMaximumSize(new Dimension(520, 610)); // Максимальный размер
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Отступы

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Панель для");
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        label.setForeground(Color.WHITE); // Белый текст
        label.setFont(new Font("Arial", Font.BOLD, 31)); // Шрифт

        JLabel labelTwo = new JLabel("Восстановления пароля");
        labelTwo.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        labelTwo.setForeground(Color.WHITE); // Белый текст
        labelTwo.setFont(new Font("Arial", Font.BOLD, 31)); // Шрифт
        panel.add(label); // Добавление первой части заголовка
        panel.add(labelTwo); // Добавление второй части заголовка

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство под заголовком

        // Поле для ввода имени
        passwordField1 = new JTextField();
        passwordField1.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configureTextField(passwordField1); // Настройка поля

        // Панель для поля имени
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ слева
        textPanel.add(passwordField1); // Добавление поля
        textPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ справа
        int panelHeight = 40; // Высота панели
        int panelWidth = 900; // Ширина панели
        textPanel.setMaximumSize(new Dimension(panelWidth, panelHeight)); // Максимальный размер
        textPanel.setMinimumSize(new Dimension(panelWidth, panelHeight)); // Минимальный размер
        textPanel.setPreferredSize(new Dimension(panelWidth, panelHeight)); // Предпочтительный размер

        // Поле для ввода логина
        passwordField = new JTextField();
        passwordField.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configureTextField(passwordField); // Настройка поля

        // Панель для поля логина
        textPanel3 = new JPanel();
        textPanel3.setLayout(new BoxLayout(textPanel3, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ слева
        textPanel3.add(passwordField); // Добавление поля
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ справа
        textPanel3.setMaximumSize(new Dimension(panelWidth, panelHeight)); // Максимальный размер
        textPanel3.setMinimumSize(new Dimension(panelWidth, panelHeight)); // Минимальный размер
        textPanel3.setPreferredSize(new Dimension(panelWidth, panelHeight)); // Предпочтительный размер

        // Поле для ввода электронной почты
        passwordField4 = new JTextField();
        passwordField4.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField4.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configureTextField(passwordField4); // Настройка поля

        // Панель для поля электронной почты
        textPanel4 = new JPanel();
        textPanel4.setLayout(new BoxLayout(textPanel4, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ слева
        textPanel4.add(passwordField4); // Добавление поля
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ справа
        textPanel4.setMaximumSize(new Dimension(panelWidth, panelHeight)); // Максимальный размер
        textPanel4.setMinimumSize(new Dimension(panelWidth, panelHeight)); // Минимальный размер
        textPanel4.setPreferredSize(new Dimension(panelWidth, panelHeight)); // Предпочтительный размер

        // Поле для ввода кодового слова
        passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configurePasswordField(passwordField2); // Настройка поля

        // Кнопка для показа/скрытия кодового слова
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2); // Настройка кнопки и таймера

        // Панель для метки "Введите имя"
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label1Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label1 = new JLabel("Введите имя");
        label1.setForeground(Color.WHITE); // Белый текст
        label1.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label1Panel.add(label1); // Добавление метки

        // Панель для метки "Введите электронную почту"
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label2Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label2 = new JLabel("Введите электронную почту");
        label2.setForeground(Color.WHITE); // Белый текст
        label2.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label2Panel.add(label2); // Добавление метки

        // Панель для метки "Введите логин"
        JPanel label3Panel = new JPanel();
        label3Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label3Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label3Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label3 = new JLabel("Введите логин");
        label3.setForeground(Color.WHITE); // Белый текст
        label3.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label3.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label3Panel.add(label3); // Добавление метки

        // Панель для метки "Введите кодовое слово"
        JPanel label4Panel = new JPanel();
        label4Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label4Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label4Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label4 = new JLabel("Введите кодовое слово");
        label4.setForeground(Color.WHITE); // Белый текст
        label4.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label4.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label4Panel.add(label4); // Добавление метки

        // Панель для поля кодового слова и кнопки показа
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel1.add(showPasswordButton2); // Добавление кнопки показа
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ
        textPanel1.add(passwordField2); // Добавление поля
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ

        // Добавление меток и полей на центральную панель
        panel.add(label1Panel); // Метка имени
        panel.add(textPanel); // Поле имени
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство
        panel.add(label3Panel); // Метка логина
        panel.add(textPanel3); // Поле логина
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство
        panel.add(label2Panel); // Метка электронной почты
        panel.add(textPanel4); // Поле электронной почты
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство
        panel.add(label4Panel); // Метка кодового слова
        panel.add(textPanel1); // Поле кодового слова
        panel.add(Box.createRigidArea(new Dimension(0, 35))); // Пространство

        // Кнопка "Восстановить пароль"
        JButton entrance = new JButton("Восстановить пароль");
        customizeButton(entrance); // Кастомизация кнопки
        entrance.addActionListener(e -> {
            // Запуск восстановления пароля в отдельном потоке
            new Thread(() -> {
                try {
                    startRecord(entrance); // Вызов метода восстановления
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex); // Обработка ошибок
                }
            }).start();
        });

        // Кнопка "Вернуться на панель входа"
        JButton showStartButton = new JButton("Вернуться на панель входа");
        customizeButton(showStartButton); // Кастомизация кнопки
        showStartButton.addActionListener(e -> {
            // Переход на панель входа в отдельном потоке
            new Thread(() -> {
                mainEntrance(showStartButton); // Вызов метода перехода
            }).start();
        });

        // Добавление кнопок на центральную панель
        panel.add(entrance); // Кнопка восстановления
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Пространство между кнопками
        panel.add(showStartButton); // Кнопка возврата
        panel.add(Box.createRigidArea(new Dimension(0, 80))); // Пространство снизу

        // Установка светло-серой рамки для центральной панели
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10)); // Рамка толщиной 10

        // Добавление центральной панели на главную панель
        mainPanel.add(panel); // Центрирование панели
        mainPanel.add(Box.createVerticalGlue()); // Пространство снизу

        // Установка главной панели как содержимого окна
        frame.setContentPane(mainPanel);
        frame.setVisible(true); // Отображение окна
    }

    // Метод для обработки восстановления пароля
    private static void startRecord(JButton passwordRecoveryButton) throws IOException, ClassNotFoundException {
        // Отключение кнопки и изменение текста в потоке EDT
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false); // Отключение кнопки
            passwordRecoveryButton.setText("Восстановление пароля..."); // Изменение текста
        });

        // Получение данных из полей ввода
        char[] password2 = passwordField2.getPassword(); // Кодовое слово
        username = passwordField1.getText(); // Имя
        mail = passwordField4.getText(); // Электронная почта
        code_sey = new String(password2); // Преобразование кодового слова в строку
        login = passwordField.getText(); // Логин

        // Проверка заполненности полей
        if (username.isEmpty() || mail.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } else {
            if (code_sey.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Вы не ввели кодовое слово, если у вас его нет, то вы не сможете восстановить учетную запись.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } else {
                // Список полей для запроса к базе данных
                fields = java.util.List.of("backup_account_key", "passphrase", "key_connection", "account_id");

                // Получение данных из базы данных по кодовому слову
                data = database.getDate(fields, tableName, "passphrase", code_sey);

                if (data == null) {
                    // Если кодовое слово неверное
                    JOptionPane.showMessageDialog(frame, "Вы ввели неверное кодовое слово от учётной записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        // Расшифровка ключа учетной записи и ключа подключения
                        String keyAc = encryption.chaha20Decrypt(code_sey, data[0]); // Ключ учетной записи
                        connect = encryption.chaha20Decrypt(keyAc, data[2]); // Ключ подключения

                        // Настройка SSL-свойств
                        System.setProperty(conf[0], conf[1]);
                        System.setProperty(conf[2], connect);

                        // Установление SSL-соединения с сервером
                        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        socket = (SSLSocket) ssf.createSocket(IP, PORT); // Создание сокета
                        out = new ObjectOutputStream(socket.getOutputStream()); // Поток вывода
                        in = new ObjectInputStream(socket.getInputStream()); // Поток ввода

                        // Отправка запроса на проверку данных пользователя
                        out.writeObject("CHECK_UP " + username + " " + login + " " + mail);
                        out.flush(); // Сброс буфера

                        // Получение ответа от сервера
                        Object response;
                        while ((response = in.readObject()) != null) {
                            if (response instanceof String) {
                                stringStart = (String) response;
                                if (stringStart.equals("PLUSS") || stringStart.equals("NULL_PLUSS")) {
                                    break; // Прерывание цикла при получении ответа
                                }
                            }
                        }

                        if (stringStart.equals("PLUSS")) {
                            // Если данные верны, переход к выбору операции восстановления
                            SelectingRecoveryOperation selectingRecoveryOperation = new SelectingRecoveryOperation(
                                    IP, out, in, data[3], mail, socket, username, login, keyAc, width, height);
                            selectingRecoveryOperation.startChose(); // Запуск панели выбора операции
                            frame.dispose(); // Закрытие текущего окна
                        } else if (stringStart.equals("NULL_PLUSS")) {
                            // Если данные неверны
                            JOptionPane.showMessageDialog(frame, "Вы ввели неверную электронную почту имя или логин.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        // Обработка ошибок подключения
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу - 2", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }
            }
        }

        // Включение кнопки и восстановление текста в потоке EDT
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true); // Включение кнопки
            passwordRecoveryButton.setText("Восстановить пароль"); // Восстановление текста
        });
    }

    // Метод для перехода на панель входа
    private static void mainEntrance(JButton passwordRecoveryButton) {
        // Отключение кнопки и изменение текста в потоке EDT
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false); // Отключение кнопки
            passwordRecoveryButton.setText("Переход на панель входа..."); // Изменение текста
        });

        // Запуск панели входа
        entrancePanel = new EntrancePanelAll(IP, PORT, width, height);
        entrancePanel.startDevPassword(); // Отображение панели входа
        frame.dispose(); // Закрытие текущего окна

        // Включение кнопки и восстановление текста в потоке EDT
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true); // Включение кнопки
            passwordRecoveryButton.setText("Вернуться на панель входа"); // Восстановление текста
        });
    }

    // Метод для настройки кнопки показа/скрытия пароля с таймером
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton) {
        // Загрузка и масштабирование иконок для кнопки
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png"); // Иконка "показать"
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png"); // Иконка "скрыть"
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg); // Масштабированная иконка "показать"
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot); // Масштабированная иконка "скрыть"

        // Настройка кнопки
        showPasswordButton.setIcon(scaledIconNot); // Установка начальной иконки
        showPasswordButton.setContentAreaFilled(false); // Удаление фона кнопки
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder()); // Удаление рамки
        showPasswordButton.setPreferredSize(new Dimension(40, 40)); // Размер кнопки
        showPasswordButton.setMaximumSize(new Dimension(40, 40)); // Максимальный размер
        showPasswordButton.setMinimumSize(new Dimension(40, 40)); // Минимальный размер

        // Флаг для отслеживания состояния видимости пароля
        final boolean[] isPasswordVisible = {false}; // По умолчанию пароль скрыт

        // Таймер для автоматического скрытия пароля через 10 секунд
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•'); // Скрытие пароля
            isPasswordVisible[0] = false; // Обновление состояния
            showPasswordButton.setIcon(scaledIconNot); // Установка иконки "скрыть"
        });

        // Обработчик нажатия на кнопку показа/скрытия пароля
        showPasswordButton.addActionListener(e -> {
            if (isPasswordVisible[0]) {
                // Если пароль видим, скрываем его
                passwordField.setEchoChar('•'); // Скрытие пароля
                isPasswordVisible[0] = false; // Обновление состояния
                hidePasswordTimer.stop(); // Остановка таймера
                showPasswordButton.setIcon(scaledIconNot); // Установка иконки "скрыть"
            } else {
                // Если пароль скрыт, показываем его
                passwordField.setEchoChar((char) 0); // Показ пароля
                isPasswordVisible[0] = true; // Обновление состояния
                hidePasswordTimer.restart(); // Перезапуск таймера
                showPasswordButton.setIcon(scaledIcon); // Установка иконки "показать"
            }
        });
    }

    // Метод для кастомизации кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование кнопки
        button.setBackground(new Color(75, 110, 175)); // Синий фон
        button.setForeground(Color.WHITE); // Белый текст
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        button.setFocusPainted(false); // Удаление обводки при фокусе
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Отступы
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор "рука"
        button.setPreferredSize(new Dimension(300, 70)); // Предпочтительный размер
        button.setMinimumSize(new Dimension(200, 50)); // Минимальный размер
        button.setMaximumSize(new Dimension(300, 50)); // Максимальный размер
    }
}
