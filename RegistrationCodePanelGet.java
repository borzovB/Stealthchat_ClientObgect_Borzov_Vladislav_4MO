package org.face_recognition;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
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

// Класс отвечает за панель ввода и подтверждения кода, отправленного на электронную почту, для завершения регистрации
public class RegistrationCodePanelGet {

    private static ObjectOutputStream out; // Поток для отправки данных на сервер
    private static ObjectInputStream in; // Поток для получения данных от сервера
    private static JFrame frame; // Главное окно приложения
    public static String email; // Электронная почта пользователя
    public static String username; // Имя пользователя
    public static String password; // Пароль пользователя
    private static Menu menu; // Экземпляр главного меню
    private static SSLSocket socket; // SSL-сокет для безопасного соединения с сервером
    private static EncryptionAccaunt encryption; // Объект для шифрования данных
    private static String myID; // Идентификатор текущего клиента
    private static JPanel textPanel; // Панель для поля ввода кода
    private static String outMail; // Зашифрованные данные для отправки на сервер
    private static String login; // Логин пользователя
    private static String myIP; // IP-адрес клиента
    private int PORT; // Порт сервера
    private static SecretKey aesKey; // Ключ AES для шифрования
    private static String confirmationCodeRestory; // Код подтверждения
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // Очередь для асинхронной обработки данных
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры для панели подтверждения
    public RegistrationCodePanelGet(String myIP, ObjectOutputStream out, ObjectInputStream in, SSLSocket socket,
                                    SecretKey aesKey, String confirmationCodeRestory, String email, String username, String password,
                                    String login, int width, int height) {
        this.myIP = myIP;
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.aesKey = aesKey;
        this.confirmationCodeRestory = confirmationCodeRestory;
        this.email = email;
        this.username = username;
        this.password = password;
        this.login = login;
        this.width = width;
        this.height = height;
    }

    // Метод запуска окна для ввода кода подтверждения
    public static void startDevCode(){
        // Создание главного окна
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Установка размеров окна с учетом минимальных требований
        if(width>740 && height>750){
            frame.setSize(width, height);
        }else {
            frame.setSize(700, 750);
        }

        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        frame.setLocationRelativeTo(null); // Центрирование окна на экране

        // Обработка закрытия окна
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Network network = new Network();
                network.exitClient(out, in, socket, myID); // Вызов метода выхода клиента
                frame.dispose(); // Закрытие окна
            }
        });

        // Создание основной панели с вертикальным расположением компонентов
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30)); // Темный фон
        mainPanel.setOpaque(true);
        mainPanel.add(Box.createVerticalGlue()); // Пространство сверху для центрирования

        // Создание панели формы
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65)); // Серый фон
        panel.setPreferredSize(new Dimension(500, 400));
        panel.setMinimumSize(new Dimension(500, 400));
        panel.setMaximumSize(new Dimension(500, 400));
        panel.add(Box.createRigidArea(new Dimension(0, 35))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Подтверждение регистрации");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 35))); // Пространство под заголовком

        // Поле для ввода кода
        JTextField passwordField1 = new JTextField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));

        // Настройка текстового поля
        ControlPanel controlPanel = new ControlPanel();
        controlPanel.configureTextField(passwordField1);

        // Панель для поля ввода кода
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        int panelHeight = 40; // Высота панели
        int panelWidth = 900; // Ширина панели
        textPanel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Метка для поля ввода кода
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label1 = new JLabel("Введите код:");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        panel.add(label1Panel);
        panel.add(textPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Кнопка подтверждения кода
        JButton entrance = new JButton("Подтвердить");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            // Запуск обработки в отдельном потоке
            new Thread(() -> {
                try {
                    String enteredCode = passwordField1.getText();
                    try {
                        // Проверка введенного кода
                        if (encryption.decrypt(enteredCode, aesKey).equals(confirmationCodeRestory)) {
                            JOptionPane.showMessageDialog(frame, "Регистрация успешна!", "Уведомление",
                                    JOptionPane.INFORMATION_MESSAGE);
                            sendMessage(); // Отправка данных на сервер
                            frame.dispose();
                            menu = new Menu(myIP, width, height);
                            menu.start(); // Запуск главного меню
                        } else {
                            JOptionPane.showMessageDialog(frame, "Неверный код подтверждения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Неверный код подтверждения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });

        // Кнопка для повторной отправки кода
        JButton showStartButton = new JButton("Получить код ещё раз");
        customizeButton(showStartButton);
        showStartButton.addActionListener(e -> {
            сode(); // Запрос нового кода
        });

        // Добавление кнопок на панель
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Разделитель между кнопками
        panel.add(showStartButton);
        panel.add(Box.createRigidArea(new Dimension(0, 80))); // Пространство снизу
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10)); // Серая рамка

        // Добавление панели в основную
        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue()); // Пространство снизу
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Метод для отправки пользовательских данных на сервер
    private static void sendMessage() {
        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            EncryptionAccaunt encryption = new EncryptionAccaunt();

            // Хэширование пользовательских данных
            String usernameNew = encryption.Argon2(username);
            String emailNew = encryption.Argon2(email);
            String passwordNew = encryption.Argon2(password);
            String loginNew = encryption.Argon2(login);

            // Создание массива данных для отправки
            String[] userData = { usernameNew, emailNew, passwordNew, loginNew };

            try {
                // Отправка данных на сервер
                out.writeObject(userData);
                out.flush();  // Очистка потока

                // Очистка полей после отправки
                username = null;
                email = null;
                password = null;
            } catch (IOException e) {
                e.printStackTrace();  // Обработка ошибок отправки
            }
        }
    }

    // Метод для запроса нового кода подтверждения
    private static void сode() {
        try {
            // Отправка запроса на сервер
            out.writeObject("MAIL");
            out.flush();  // Очистка потока

            handleRegisterResponse(); // Обработка ответа сервера
            String elementMail = queue.take(); // Получение данных из очереди

            // Отправка данных на сервер
            out.writeObject(elementMail);
            out.flush();  // Очистка потока
        } catch (IOException ioException) {
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            if (frame != null) {
                frame.dispose(); // Закрытие окна при ошибке
            }
        }
    }

    // Метод для обработки ответа сервера
    public static void handleRegisterResponse() {
        new Thread(() -> {
            try {
                Object response;
                while ((response = in.readObject()) != null) {
                    if (response instanceof PublicKey) {
                        PublicKey publicKey = (PublicKey) response;

                        // Шифрование данных
                        String encryptedEmail = encryption.encryptWithRSA(publicKey, email.getBytes());
                        aesKey = encryption.generateAESKey();
                        confirmationCodeRestory = encryption.generateCode();

                        // Шифрование кода подтверждения
                        String encryptedCode = encryption.encryptWithAES(confirmationCodeRestory, aesKey);
                        outMail = "CODE " + encryptedEmail + " " + encryptedCode;
                        queue.put(outMail); // Добавление данных в очередь
                        break;  // Прерывание цикла после получения ключа
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Ошибка обработки ответа сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // Кастомизация кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175)); // Синий фон
        button.setForeground(Color.WHITE); // Белый текст
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        button.setFocusPainted(false); // Убрать обводку при фокусе
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Отступы
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор "рука"
        button.setPreferredSize(new Dimension(300, 80));
        button.setMinimumSize(new Dimension(200, 60));
        button.setMaximumSize(new Dimension(300, 60));
    }
}
