package org.face_recognition;

// Класс отвечает за регистрацию пользователя в сети, предоставляя интерфейс для ввода данных
// и взаимодействие с сервером через защищенное соединение

import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class RegistrationOnServer {

    private static JFrame frame; // Главное окно приложения
    private static JPanel textPanel; // Панель для поля ввода имени
    private static JPanel textPanel1; // Панель для поля ввода пароля
    private static JPanel textPanel3; // Панель для поля ввода логина
    private static JPanel textPanel4; // Панель для поля ввода email
    private static String IP; // IP-адрес сервера
    private static int PORT; // Порт сервера
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // Очередь для обмена данными
    private static SSLSocket socket; // SSL-сокет для защищенного соединения
    private static ObjectOutputStream out; // Поток вывода объектов
    private static ObjectInputStream in; // Поток ввода объектов
    public static String myID; // Идентификатор пользователя
    public static String email; // Электронная почта
    public static String username; // Имя пользователя
    public static String password; // Пароль
    public static String login; // Логин
    private static Safety safety = new Safety(); // Объект для проверки безопасности
    private static RegistrationCodePanelGet registrationCodePanel; // Панель для ввода кода подтверждения
    private static SecretKey aesKey = null; // Ключ AES для шифрования
    private static String confirmationCodeRestory; // Код подтверждения
    private static String outMail; // Зашифрованные данные для отправки
    private static EncryptionAccaunt encryption; // Объект для шифрования
    private static Config config = new Config(); // Конфигурация сервера
    private static String[] conf; // Параметры конфигурации
    private static String connectionPassword; // Пароль для подключения
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса
    public RegistrationOnServer(String IP, int PORT, String connectionPassword, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        conf = config.configServer();
        this.connectionPassword = connectionPassword;
        this.width = width;
        this.height = height;
    }

    // Запуск окна регистрации
    public static void startDevPassword(){
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Установка размера окна
        if(width>740 && height>750){
            frame.setSize(width, height);
        }else {
            frame.setSize(700, 750);
        }

        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        // Центрирование окна по горизонтали и установка у верхнего края
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = 0;
        frame.setLocation(x, y);

        // Обработчик закрытия окна
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Network network = new Network();
                network.exitClient(out, in, socket, myID);
                frame.dispose();
            }
        });

        // Обработчик изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        ControlPanel controlPanel = new ControlPanel();

        // Основная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);
        mainPanel.add(Box.createVerticalGlue());

        // Панель формы
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        panel.setPreferredSize(new Dimension(520, 610));
        panel.setMinimumSize(new Dimension(520, 610));
        panel.setMaximumSize(new Dimension(520, 610));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Заголовок
        JLabel label = new JLabel("Регистрация в сети");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Поле для имени
        JTextField passwordField1 = new JTextField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField1);

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

        // Поле для логина
        JTextField passwordField = new JTextField();
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField);

        textPanel3 = new JPanel();
        textPanel3.setLayout(new BoxLayout(textPanel3, BoxLayout.X_AXIS));
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel3.add(passwordField);
        textPanel3.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel3.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel3.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel3.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Поле для email
        JTextField passwordField4 = new JTextField();
        passwordField4.setPreferredSize(new Dimension(250, 40));
        passwordField4.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configureTextField(passwordField4);

        textPanel4 = new JPanel();
        textPanel4.setLayout(new BoxLayout(textPanel4, BoxLayout.X_AXIS));
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel4.add(passwordField4);
        textPanel4.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel4.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel4.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel4.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Поле для пароля
        JPasswordField passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2);

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

        // Метка для email
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label2 = new JLabel("Введите электронную почту");
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

        // Метка для пароля
        JPanel label4Panel = new JPanel();
        label4Panel.setLayout(new BorderLayout());
        label4Panel.setBackground(new Color(60, 63, 65));
        label4Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label4 = new JLabel("Введите пароль");
        label4.setForeground(Color.WHITE);
        label4.setFont(new Font("Arial", Font.PLAIN, 17));
        label4.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label4Panel.add(label4);

        // Панель для поля пароля
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
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

        // Кнопка регистрации
        JButton entrance = new JButton("Готово");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            char[] password2 = passwordField2.getPassword();
            username = passwordField1.getText();
            email = passwordField4.getText();
            password = new String(password2);
            login = passwordField.getText();
            new Thread(() -> {
                try {
                    reg();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });

        // Кнопка возврата в меню
        JButton showStartButton = new JButton("Вернуться в главное меню");
        customizeButton(showStartButton);
        showStartButton.addActionListener(e -> {
            Menu menu = new Menu(IP, width, height);
            menu.start();
            frame.dispose();
        });

        // Добавление кнопок
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(showStartButton);
        panel.add(Box.createRigidArea(new Dimension(0, 80)));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Метод регистрации
    private static void reg() {
        // Проверка заполнения полей
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Проверка сложности пароля
        if (!safety.isValidPassword(password)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Введите другой пароль. Пароль должен содержать минимум 8 символов и включать:\n" +
                            "- Латинские буквы (A–Z, a–z) или кириллические буквы (А–Я, а–я);\n" +
                            "- Цифры (0–9);\n" +
                            "- Специальные символы (!@#$%^&*()_+-=[]{};':\"|,.<>?/).",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean isUnique = true;

        try {
            // Настройка SSL-соединения
            System.setProperty(conf[0], conf[1]);
            System.setProperty(conf[2], connectionPassword);
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) ssf.createSocket(IP, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Отправка запроса на проверку уникальности
            out.writeObject("UNIQUENESS "+login + " "+email + " " + username);

            // Чтение ответа сервера
            Object response;
            while ((response = in.readObject()) != null) {
                if (response instanceof String) {
                    String line = (String) response;
                    String[] bol = line.split(" ");
                    isUnique = true;

                    // Проверка уникальности логина
                    if (bol[0].equals("false")) {
                        JOptionPane.showMessageDialog(frame, "Такой логин уже существует.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        isUnique = false;
                    }

                    // Проверка уникальности email
                    if (bol[1].equals("false")) {
                        JOptionPane.showMessageDialog(frame, "Такая электронная почта уже зарегистрирована.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        isUnique = false;
                    }

                    // Проверка уникальности имени
                    if (bol[2].equals("false")) {
                        JOptionPane.showMessageDialog(frame, "Такое имя уже существует.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        isUnique = false;
                    }
                    break;
                }
            }
        } catch (IOException ioEx) {
            JOptionPane.showMessageDialog(frame, "Ошибка ввода-вывода при подключении к серверу: " + ioEx.getMessage(), "Ошибка: Подключение к серверу", JOptionPane.ERROR_MESSAGE);
            ioEx.printStackTrace();
        } catch (ClassNotFoundException cnfEx) {
            JOptionPane.showMessageDialog(frame, "Ошибка: Получен неизвестный объект от сервера: " + cnfEx.getMessage(), "Ошибка: Неверный ответ сервера", JOptionPane.ERROR_MESSAGE);
            cnfEx.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Общая ошибка при подключении к серверу: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // Закрытие потоков
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Если данные уникальны, получение кода подтверждения
        if(isUnique){
            Thread tart = new Thread(() -> {
                try {
                    getCode();
                    frame.dispose();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            tart.start();
            try {
                tart.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ожидание потока было прервано", e);
            }

            // Запуск панели ввода кода
            try {
                registrationCodePanel = new RegistrationCodePanelGet(IP, out, in, socket, aesKey, confirmationCodeRestory,
                        email, username, password, login, width, height);
                registrationCodePanel.startDevCode();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Получение кода подтверждения
    private static void getCode() {
        try {
            // Настройка SSL-соединения
            System.setProperty(conf[0], conf[1]);
            System.setProperty(conf[2], connectionPassword);
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) ssf.createSocket(IP, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Запрос отправки кода
            out.writeObject("MAIL");
            out.flush();

            handleRegisterResponse();
            String elementMail = queue.take();

            // Отправка зашифрованных данных
            out.writeObject(elementMail);
            out.flush();
        } catch (IOException ioException) {
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу - 2", "Ошибка", JOptionPane.ERROR_MESSAGE);
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    // Обработка ответа сервера
    public static void handleRegisterResponse() {
        new Thread(() -> {
            try {
                Object response;
                while ((response = in.readObject()) != null) {
                    if (response instanceof PublicKey) {
                        PublicKey publicKey = (PublicKey) response;
                        String encryptedEmail = encryption.encryptWithRSA(publicKey, email.getBytes());
                        aesKey = encryption.generateAESKey();
                        confirmationCodeRestory = encryption.generateCode();
                        String encryptedCode = encryption.encryptWithAES(confirmationCodeRestory, aesKey);
                        outMail = "CODE " + encryptedEmail + " " + encryptedCode;
                        queue.put(outMail);
                        break;
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Ошибка обработки ответа сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // Настройка кнопки показа пароля
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton){
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png");
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png");
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot);
        showPasswordButton.setIcon(scaledIconNot);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder());
        showPasswordButton.setPreferredSize(new Dimension(40, 40));
        showPasswordButton.setMaximumSize(new Dimension(40, 40));
        showPasswordButton.setMinimumSize(new Dimension(40, 40));

        final boolean[] isPasswordVisible = {false};
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•');
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

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

    // Кастомизация кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 80));
        button.setMinimumSize(new Dimension(200, 60));
        button.setMaximumSize(new Dimension(300, 60));
    }
}
