package org.face_recognition;

// Класс EntrancePanelAll отвечает за создание и управление панелью входа в приложение StealthChat
// Он предоставляет интерфейс для ввода имени пользователя, логина, пароля учетной записи и пароля устройства,
// а также обработку авторизации через SSL-соединение с сервером, восстановление пароля и возврат в главное меню
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class EntrancePanelAll {
    // Главное окно приложения
    private static JFrame frame;
    // Панели для размещения текстовых полей и кнопок
    private static JPanel textPanel;
    private static JPanel textPanel1;
    private static JPanel textPanel3;
    private static JPanel textPanel4;
    // IP-адрес сервера
    private static String IP;
    // Порт сервера
    private static int PORT;
    // SSL-сокет для защищенного соединения
    private static SSLSocket socket;
    // Поток для отправки данных на сервер
    private static ObjectOutputStream out;
    // Поток для получения данных от сервера
    private static ObjectInputStream in;
    // Идентификатор текущего пользователя
    public static String myID;
    // Пароль устройства
    public static String passwordDev;
    // Имя пользователя
    public static String username;
    // Пароль учетной записи
    public static String password;
    // Логин пользователя
    public static String login;
    // Экземпляр класса для загрузки конфигурации сервера
    private static Config config = new Config();
    // Массив с настройками конфигурации
    private static String[] conf;
    // Панель для восстановления пароля
    private static PasswordRecoveryPanel showRestore;
    // Главное меню приложения
    private static Menu menu;
    // Главная страница приложения
    private static MainPage mainPage;
    // Название таблицы в базе данных
    private static String tableName = "accounts";
    // Экземпляр класса для работы с базой данных
    private static Database database = new Database();
    // Массив данных, полученных из базы
    private static String[] data;
    // Список полей для запроса из базы данных
    private static List<String> fields;
    // Экземпляр класса для шифрования данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Текстовое поле для имени пользователя
    private static JTextField passwordField3;
    // Текстовое поле для имени
    private static JTextField passwordField1;
    // Поле для ввода пароля учетной записи
    private static JPasswordField passwordField2;
    // Поле для ввода пароля устройства
    private static JPasswordField passwordField4;
    // Текстовое поле для логина
    private static JTextField passwordField;
    // Ключ соединения с сервером
    private static String connect;
    // Ширина окна
    private static int width;
    // Высота окна
    private static int height;

    // Конструктор, инициализирующий параметры соединения и размеры окна
    public EntrancePanelAll(String IP, int PORT, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        conf = config.configServer(); // Загружает конфигурацию сервера
        this.width = width;
        this.height = height;
    }

    // Запускает панель входа для ввода пароля устройства
    public static void startDevPassword() {
        frame = new JFrame("StealthChat"); // Создает главное окно
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Закрытие приложения при выходе
        if (width > 740 && height > 750) {
            frame.setSize(width, height); // Устанавливает заданные размеры
        } else {
            frame.setSize(700, 750); // Минимальные размеры по умолчанию
        }

        Dimension minSize = new Dimension(740, 810); // Минимальный размер окна
        frame.setMinimumSize(minSize);

        // Центрирует окно по горизонтали и размещает его вверху экрана
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = 0;
        frame.setLocation(x, y);

        // Слушатель изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Слушатель закрытия окна для корректного завершения соединения
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Network network = new Network();
                network.exitClient(out, in, socket, myID); // Закрывает соединение
                frame.dispose();
            }
        });

        ControlPanel controlPanel = new ControlPanel(); // Экземпляр для настройки полей ввода

        // Основная панель с вертикальным расположением элементов
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);
        mainPanel.add(Box.createVerticalGlue()); // Пространство сверху

        // Панель для элементов ввода
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        panel.setPreferredSize(new Dimension(520, 610));
        panel.setMinimumSize(new Dimension(520, 610));
        panel.setMaximumSize(new Dimension(520, 610));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Заголовок панели
        JLabel label = new JLabel("Панель входа");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Поле для ввода имени
        passwordField1 = new JTextField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField1);

        // Панель для поля ввода имени
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        int panelHeight = 40;
        int panelWidth = 900;
        textPanel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Поле для ввода логина
        passwordField3 = new JTextField();
        passwordField3.setPreferredSize(new Dimension(250, 40));
        passwordField3.setFont(new Font("Arial", Font.PLAIN, 16));

        // Поле для ввода логина
        passwordField = new JTextField();
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField);

        // Панель для поля ввода логина
        textPanel3 = new JPanel();
        textPanel3.setLayout(new BoxLayout(textPanel3, BoxLayout.X_AXIS));
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel3.add(passwordField);
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel3.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel3.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel3.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Поле для ввода пароля устройства
        passwordField4 = new JPasswordField();
        passwordField4.setPreferredSize(new Dimension(250, 40));
        passwordField4.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField4);

        JButton showPasswordButton4 = new JButton();
        textTime(passwordField4, showPasswordButton4); // Настройка кнопки показа пароля

        // Панель для поля ввода пароля устройства
        textPanel4 = new JPanel();
        textPanel4.setLayout(new BoxLayout(textPanel4, BoxLayout.X_AXIS));
        textPanel4.add(showPasswordButton4);
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel4.add(passwordField4);
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel4.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel4.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel4.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Поле для ввода пароля учетной записи
        passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2); // Настройка кнопки показа пароля

        // Метка для имени
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label1 = new JLabel("Введите имя");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        // Метка для пароля устройства
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label2 = new JLabel("Введите пароль от учетной записи устройства");
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("Arial", Font.PLAIN, 17));
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label2Panel.add(label2);

        // Метка для логина
        JPanel label3Panel = new JPanel();
        label3Panel.setLayout(new BorderLayout());
        label3Panel.setBackground(new Color(60, 63, 65));
        label3Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label3 = new JLabel("Введите логин");
        label3.setForeground(Color.WHITE);
        label3.setFont(new Font("Arial", Font.PLAIN, 17));
        label3.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label3Panel.add(label3);

        // Метка для пароля учетной записи
        JPanel label4Panel = new JPanel();
        label4Panel.setLayout(new BorderLayout());
        label4Panel.setBackground(new Color(60, 63, 65));
        label4Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label4 = new JLabel("Введите пароль");
        label4.setForeground(Color.WHITE);
        label4.setFont(new Font("Arial", Font.PLAIN, 17));
        label4.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label4Panel.add(label4);

        // Панель для поля ввода пароля учетной записи
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

        // Добавляет элементы на панель
        panel.add(label1Panel);
        panel.add(textPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label3Panel);
        panel.add(textPanel3);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label2Panel);
        panel.add(textPanel4);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label4Panel);
        panel.add(textPanel1);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Кнопка "Войти"
        JButton entrance = new JButton("Войти");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            new Thread(() -> {
                handleInput(entrance); // Обрабатывает вход в отдельном потоке
            }).start();
        });

        // Кнопка "Вернуться в главное меню"
        JButton showStartButton = new JButton("Вернуться в главное меню");
        customizeButton(showStartButton);
        showStartButton.addActionListener(e -> {
            new Thread(() -> {
                mainMenuBar(showStartButton); // Переходит в главное меню
            }).start();
        });

        // Кнопка "Восстановить пароль"
        JButton restore_the_password = new JButton("Восстановить пароль");
        customizeButton(restore_the_password);
        restore_the_password.addActionListener(e -> {
            new Thread(() -> {
                try {
                    passwordRecovery(restore_the_password); // Запускает восстановление пароля
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });

        // Добавляет кнопки на панель
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(showStartButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(restore_the_password);
        panel.add(Box.createRigidArea(new Dimension(0, 80)));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10)); // Устанавливает рамку

        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue()); // Пространство снизу

        frame.setContentPane(mainPanel); // Устанавливает основную панель в окно
        frame.setVisible(true); // Делает окно видимым
    }

    // Обрабатывает данные, введенные пользователем, и выполняет авторизацию
    private static void handleInput(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);
            passwordRecoveryButton.setText("Вход...");
        });

        // Получает введенные данные
        char[] password2 = passwordField2.getPassword();
        char[] password3 = passwordField4.getPassword();
        username = passwordField1.getText();
        passwordDev = new String(password3);
        password = new String(password2);
        login = passwordField.getText();

        try {
            // Проверяет, что все поля заполнены
            if (username.isEmpty() || passwordDev.isEmpty() || login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } else {
                fields = List.of("account_key", "account_password", "key_connection", "account_id");
                data = database.getDate(fields, tableName, "account_password", passwordDev); // Запрашивает данные из базы

                if (data == null) {
                    JOptionPane.showMessageDialog(frame, "Вы ввели неверный пароль от учётной записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        // Расшифровывает ключи
                        String keyAc = encryption.chaha20Decrypt(passwordDev, data[0]);
                        connect = encryption.chaha20Decrypt(keyAc, data[2]);

                        // Настраивает SSL-свойства
                        System.setProperty(conf[0], conf[1]);
                        System.setProperty(conf[2], connect);

                        // Устанавливает SSL-соединение
                        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        socket = (SSLSocket) ssf.createSocket(IP, PORT);
                        out = new ObjectOutputStream(socket.getOutputStream());
                        in = new ObjectInputStream(socket.getInputStream());

                        // Отправляет запрос на сервер
                        out.writeObject("GET_EMPLOYEES " + username + " " + password + " " + login);

                        Object response;
                        while ((response = in.readObject()) != null) {
                            if (response instanceof String) {
                                myID = (String) response;
                                SwingUtilities.invokeLater(() -> {
                                    if (myID.equals("NULL")) {
                                        JOptionPane.showMessageDialog(frame, "Вы ввели неверные данные.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    } else if (myID.equals("Erro")) {
                                        JOptionPane.showMessageDialog(frame, "Вы уже подключены к этому аккаунту.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "Вход успешен! Ваш идентификатор в сети: " + myID + " " + data[3] + ".");
                                        frame.dispose();
                                        mainPage = new MainPage(IP, myID, PORT, keyAc, connect, data[3], height, width);
                                        try {
                                            out.close();
                                            in.close();
                                            socket.close();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        mainPage.showMainPage(); // Открывает главную страницу
                                    }
                                });
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            if (frame != null) frame.dispose();
                        });
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Вы ввели неверный пароль от учётной записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);
            passwordRecoveryButton.setText("Войти");
        });
    }

    // Переходит в главное меню приложения
    private static void mainMenuBar(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);
            passwordRecoveryButton.setText("Переход на главное меню...");
        });

        frame.dispose();
        Network network = new Network();
        network.exitClient(out, in, socket, myID); // Закрывает соединение
        menu = new Menu(IP, width, height);
        menu.start(); // Запускает главное меню

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);
            passwordRecoveryButton.setText("Вернуться в главное меню");
        });
    }

    // Запускает панель восстановления пароля
    private static void passwordRecovery(JButton passwordRecoveryButton) throws IOException {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);
            passwordRecoveryButton.setText("Изменение пароля...");
        });

        showRestore = new PasswordRecoveryPanel(IP, PORT, width, height);
        showRestore.startRecordPassword(); // Открывает панель восстановления
        frame.dispose();

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);
            passwordRecoveryButton.setText("Восстановить пароль");
        });
    }

    // Настраивает кнопку для показа/скрытия пароля
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton) {
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png"); // Иконка для "показать"
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png"); // Иконка для "скрыть"

        // Масштабирует иконки
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot);

        // Устанавливает начальную иконку
        showPasswordButton.setIcon(scaledIconNot);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder());
        showPasswordButton.setPreferredSize(new Dimension(40, 40));
        showPasswordButton.setMaximumSize(new Dimension(40, 40));
        showPasswordButton.setMinimumSize(new Dimension(40, 40));

        // Флаг состояния видимости пароля
        final boolean[] isPasswordVisible = {false};

        // Таймер для автоматического скрытия пароля через 10 секунд
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•');
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

        // Обработчик кнопки показа/скрытия пароля
        showPasswordButton.addActionListener(e -> {
            if (isPasswordVisible[0]) {
                passwordField.setEchoChar('•');
                isPasswordVisible[0] = false;
                hidePasswordTimer.stop();
                showPasswordButton.setIcon(scaledIconNot);
            } else {
                passwordField.setEchoChar((char) 0);
                isPasswordVisible[0] = true;
                hidePasswordTimer.restart();
                showPasswordButton.setIcon(scaledIcon);
            }
        });
    }

    // Настраивает стиль кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 70));
        button.setMinimumSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(300, 50));
    }
}
