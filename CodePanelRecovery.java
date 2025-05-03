package org.face_recognition;

// Класс CodePanelRecovery отвечает за интерфейс восстановления пароля в приложении StealthChat
// Реализует графический интерфейс для ввода кода подтверждения и взаимодействия с сервером
// Управляет процессом проверки кода, повторной отправки кода и перехода к установке нового пароля
// Использует SSL-соединение для безопасного обмена данными с сервером
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class CodePanelRecovery {

    // Поля класса для хранения состояния и компонентов интерфейса
    private static JFrame frame; // Основное окно приложения
    private static JPanel textPanel; // Панель для текстового поля
    private static String mail; // Адрес электронной почты пользователя
    private static String username; // Имя пользователя
    private static boolean choice; // Флаг выбора (например, тип восстановления)
    private static String id_accaun; // Идентификатор аккаунта
    private static String keyAc; // Ключ аккаунта
    private static SSLSocket socket; // SSL-сокет для связи с сервером
    private static ObjectOutputStream out; // Поток вывода для отправки данных
    private static ObjectInputStream in; // Поток ввода для получения данных
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>(); // Очередь для асинхронной обработки данных
    private static String IP; // IP-адрес сервера
    private static int PORT; // Порт сервера
    private static EncryptionAccaunt encryption; // Объект для шифрования данных
    private static String outMail; // Зашифрованные данные для отправки
    private static String login; // Логин пользователя
    private static SecretKey aesKey; // Ключ AES для шифрования
    private static String confirmationCodeRestory; // Код подтверждения для восстановления
    private static NewPassword newPassword; // Объект для перехода к установке нового пароля
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий все необходимые параметры
    public CodePanelRecovery(String confirmationCodeRestory, ObjectInputStream in,
                             ObjectOutputStream out, String mail, String login, String username,
                             SSLSocket socket, String IP, int PORT, SecretKey aesKey, boolean choice,
                             String id_accaun, String keyAc, int width, int height) {
        // Инициализация полей класса
        this.confirmationCodeRestory = confirmationCodeRestory;
        this.in = in;
        this.out = out;
        this.mail = mail;
        this.login = login;
        this.username = username;
        this.socket = socket;
        this.IP = IP;
        this.PORT = PORT;
        this.aesKey = aesKey;
        this.choice = choice;
        this.id_accaun = id_accaun;
        this.keyAc = keyAc;
        this.width = width;
        this.height = height;
    }

    // Метод startCode создаёт и отображает графический интерфейс для ввода кода подтверждения
    public static void startCode(){
        // Создаём основное окно
        frame = new JFrame("StealthChat");
        // Устанавливаем действие при закрытии окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Устанавливаем размер окна с учётом переданных параметров
        if(width>740 && height>750){
            frame.setSize(width, height);
        }else {
            frame.setSize(700, 750);
        }

        // Устанавливаем минимальный размер окна
        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        // Центрируем окно на экране
        frame.setLocationRelativeTo(null);

        // Добавляем слушатель для отслеживания изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Обновляем значения ширины и высоты
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Создаём главную панель с вертикальной компоновкой
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        // Устанавливаем тёмный фон
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);

        // Добавляем пустое пространство сверху
        mainPanel.add(Box.createVerticalGlue());

        // Создаём панель для кнопок и текстового поля
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // Устанавливаем тёмный фон
        panel.setBackground(new Color(60, 63, 65));

        // Устанавливаем фиксированные размеры панели
        panel.setPreferredSize(new Dimension(580, 400));
        panel.setMinimumSize(new Dimension(580, 400));
        panel.setMaximumSize(new Dimension(580, 400));

        // Добавляем отступ сверху
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Создаём заголовок
        JLabel label = new JLabel("Подтверждение изменения пароля");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        // Добавляем отступ
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Создаём текстовое поле для ввода кода
        JTextField passwordField1 = new JTextField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));

        // Настраиваем текстовое поле через ControlPanel
        ControlPanel controlPanel = new ControlPanel();
        controlPanel.configureTextField(passwordField1);

        // Создаём панель для текстового поля
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        // Устанавливаем размеры панели
        int panelHeight = 40;
        int panelWidth = 900;
        textPanel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setMinimumSize(new Dimension(panelWidth, panelHeight));
        textPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Создаём панель для метки
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        // Устанавливаем отступы
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Создаём метку для текстового поля
        JLabel label1 = new JLabel("Введите код:");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);

        label1Panel.add(label1);

        // Добавляем метку и текстовое поле на панель
        panel.add(label1Panel);
        panel.add(textPanel);
        // Добавляем отступ
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Создаём кнопку "Подтвердить"
        JButton entrance = new JButton("Подтвердить");
        customizeButton(entrance);
        // Добавляем обработчик нажатия
        entrance.addActionListener(e -> {
            // Запускаем проверку кода в отдельном потоке
            new Thread(() -> {
                confirmation(entrance, passwordField1);
            }).start();
        });

        // Создаём кнопку "Получить код ещё раз"
        JButton showStartButton = new JButton("Получить код ещё раз");
        customizeButton(showStartButton);
        // Добавляем обработчик нажатия
        showStartButton.addActionListener(e -> {
            // Запускаем повторную отправку кода в отдельном потоке
            new Thread(() -> {
                replay(showStartButton);
            }).start();
        });

        // Добавляем кнопки на панель
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(showStartButton);
        panel.add(Box.createRigidArea(new Dimension(0, 80)));
        // Устанавливаем светло-серую рамку
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        // Добавляем панель в основную панель
        mainPanel.add(panel);
        // Добавляем пустое пространство снизу
        mainPanel.add(Box.createVerticalGlue());

        // Устанавливаем основную панель как содержимое окна
        frame.setContentPane(mainPanel);
        // Делаем окно видимым
        frame.setVisible(true);
    }

    // Метод replay управляет повторной отправкой кода подтверждения
    private static void replay(JButton button){
        // Обновляем интерфейс в потоке EDT
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false);
            button.setText("Ожидание...");
        });

        // Отправляем запрос на повторный код
        сode();

        // Восстанавливаем кнопку
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("Получить код ещё раз");
        });
    }

    // Метод confirmation проверяет введённый код подтверждения
    private static void confirmation(JButton button, JTextField passwordField1){
        // Обновляем интерфейс в потоке EDT
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false);
            button.setText("Подтверждение...");
        });

        // Получаем введённый код
        String enteredCode = passwordField1.getText();
        try {
            // Проверяем правильность кода
            if (encryption.decrypt(enteredCode, aesKey).equals(confirmationCodeRestory)) {
                // Показываем сообщение об успехе
                JOptionPane.showMessageDialog(frame, "Подтверждение кода успешно", "Уведомление",
                        JOptionPane.INFORMATION_MESSAGE);

                // Создаём окно для установки нового пароля
                if(choice){
                    newPassword = new NewPassword(IP,PORT, socket, true, id_accaun, keyAc,
                            login, mail, username, in, out, width, height);
                    newPassword.startNewPassword();
                }else {
                    newPassword = new NewPassword(IP,PORT, socket, false, id_accaun, keyAc,
                            login, mail, username, in, out, width, height);
                    newPassword.startNewPassword();
                }

                // Закрываем текущее окно
                frame.dispose();
            } else {
                // Показываем сообщение об ошибке
                JOptionPane.showMessageDialog(frame, "Неверный код подтверждения", "Ошибка1", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            // Показываем сообщение об ошибке при исключении
            JOptionPane.showMessageDialog(frame, "Неверный код подтверждения", "Ошибка2", JOptionPane.ERROR_MESSAGE);
        }

        // Восстанавливаем кнопку
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("Подтвердить");
        });
    }

    // Метод customizeButton настраивает стиль кнопок
    private static void customizeButton(JButton button) {
        // Устанавливаем выравнивание
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Устанавливаем цвет фона
        button.setBackground(new Color(75, 110, 175));
        // Устанавливаем цвет текста
        button.setForeground(Color.WHITE);
        // Устанавливаем шрифт
        button.setFont(new Font("Arial", Font.BOLD, 16));
        // Убираем обводку при фокусе
        button.setFocusPainted(false);
        // Устанавливаем отступы
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        // Устанавливаем курсор
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Устанавливаем размеры кнопки
        button.setPreferredSize(new Dimension(300, 80));
        button.setMinimumSize(new Dimension(200, 60));
        button.setMaximumSize(new Dimension(300, 60));
    }

    // Метод сode отправляет запрос на сервер для получения нового кода подтверждения
    private static void сode() {
        try {
            // Отправляем команду серверу
            out.writeObject("MAIL");
            out.flush();

            // Обрабатываем ответ сервера
            handleRegisterResponse();
            String elementMail = queue.take();

            // Отправляем данные серверу
            out.writeObject(elementMail);
            out.flush();

        } catch (IOException ioException) {
            // Показываем сообщение об ошибке ввода-вывода
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Показываем сообщение об ошибке подключения
            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            // Закрываем окно при ошибке
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    // Метод handleRegisterResponse обрабатывает ответ сервера
    public static void handleRegisterResponse() {
        // Запускаем обработку в отдельном потоке
        new Thread(() -> {
            try {
                Object response;
                // Читаем объекты из потока ввода
                while ((response = in.readObject()) != null) {
                    if (response instanceof PublicKey) {
                        // Получаем публичный ключ
                        PublicKey publicKey = (PublicKey) response;

                        // Шифруем данные
                        String encryptedEmail = encryption.encryptWithRSA(publicKey, mail.getBytes());
                        aesKey = encryption.generateAESKey();
                        confirmationCodeRestory = encryption.generateCode();

                        // Шифруем код подтверждения
                        String encryptedCode = encryption.encryptWithAES(confirmationCodeRestory, aesKey);
                        outMail = "CODE " + encryptedEmail + " " + encryptedCode;
                        // Добавляем данные в очередь
                        queue.put(outMail);
                        // Прерываем цикл после обработки
                        break;
                    }
                }
            } catch (Exception e) {
                // Показываем сообщение об ошибке в потоке EDT
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Ошибка обработки ответа сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
