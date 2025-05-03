package org.face_recognition;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс отвечает за выбор пользователем операции восстановления доступа к учетной записи —
// либо на устройстве, ли в сети
public class SelectingRecoveryOperation {

    // Главное окно
    private static JFrame frame;

    // IP-адрес сервера
    private static String IP;

    // Порт подключения
    public static int PORT = 0;

    // Панель входа
    private static EntrancePanelAll entrancePanel;

    // Конфигурация клиента
    static Config config = new Config();

    // Защищённый сокет для подключения
    private static SSLSocket socket;

    // Потоки ввода/вывода для обмена данными с сервером
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    // Данные аккаунта
    private static String id_accaun;
    private static String mail;
    private static String username;
    private static String login;

    // Класс для шифрования
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();

    // Симметричный ключ AES
    private static SecretKey aesKey = null;

    // Сгенерированный код подтверждения
    private static String confirmationCodeRestory;

    // Подготовленная строка для отправки на сервер
    private static String outMail;

    // Ключ восстановления
    private static String keyAc;

    // Размеры окна
    private static int width;
    private static int height;

    // Конструктор инициализирует переменные
    public SelectingRecoveryOperation(String IP, ObjectOutputStream out, ObjectInputStream in,
                                      String id_accaun, String mail, SSLSocket socket,
                                      String username, String login, String keyAc, int width, int height){
        this.IP = IP;
        this.out = out;
        this.in = in;
        this.id_accaun = id_accaun;
        this.mail = mail;
        this.socket = socket;
        this.username = username;
        this.login = login;
        this.keyAc = keyAc;
        this.width = width;
        this.height = height;
    }

    // Метод запускает интерфейс выбора операции восстановления
    public static void startChose(){
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Устанавливаем размеры окна
        if(width > 740 && height > 750){
            frame.setSize(width, height);
        } else {
            frame.setSize(700, 750);
        }

        // Минимальные размеры окна
        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        frame.setLocationRelativeTo(null); // Центрируем окно

        // Обновляем размеры окна при изменении
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Главная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);

        mainPanel.add(Box.createVerticalGlue()); // Центрирование по вертикали

        // Панель с кнопками
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        PORT = config.configPort(); // Получение порта из конфигурации

        // Инициализация базы данных
        Database database = new Database();
        database.createDatabase();

        // Размеры панели с кнопками
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setMinimumSize(new Dimension(400, 300));
        panel.setMaximumSize(new Dimension(400, 300));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Заголовок
        JLabel label = new JLabel("Выберите операцию восстановления");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 19));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Кнопка восстановления через устройство
        JButton entrance = new JButton("На устройстве");
        customizeButton(entrance);
        entrance.addActionListener(e -> new Thread(() -> pasdev(entrance)).start());

        // Кнопка восстановления пароля через код на почту
        JButton passwordRecovery = new JButton("Пароль в сети");
        customizeButton(passwordRecovery);
        passwordRecovery.addActionListener(e -> new Thread(() -> pascon(passwordRecovery)).start());

        // Кнопка выхода в меню
        JButton outMenu = new JButton("Вернуться в меню");
        customizeButton(outMenu);
        outMenu.addActionListener(e -> new Thread(() -> outM()).start());

        // Добавляем кнопки на панель
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordRecovery);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(outMenu);

        // Светлая рамка панели
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        // Добавляем панель в основную
        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        // Устанавливаем панель в окно
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Метод стилизует кнопки
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(250, 60));
        button.setMinimumSize(new Dimension(150, 40));
        button.setMaximumSize(new Dimension(250, 40));
    }

    // Обработка нажатия кнопки "На устройстве"
    private static void pasdev(JButton entrance) {
        SwingUtilities.invokeLater(() -> {
            entrance.setEnabled(false);
            entrance.setText("Ожидание...");
        });

        getCode();

        try {
            JOptionPane.showMessageDialog(frame, "Код отправлен на почту", "Уведомление", JOptionPane.INFORMATION_MESSAGE);
            CodePanelRecovery codePanelRecovery = new CodePanelRecovery(confirmationCodeRestory, in, out, mail, login, username,
                    socket, IP, PORT, aesKey, false, id_accaun, keyAc, width, height);
            codePanelRecovery.startCode();
            frame.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            entrance.setEnabled(true);
            entrance.setText("На устройстве");
        });
    }

    // Обработка нажатия кнопки "Пароль в сети"
    private static void pascon(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);
            passwordRecoveryButton.setText("Регистрация...");
        });

        getCode();

        try {
            JOptionPane.showMessageDialog(frame, "Код отправлен на почту", "Уведомление", JOptionPane.INFORMATION_MESSAGE);
            CodePanelRecovery codePanelRecovery = new CodePanelRecovery(confirmationCodeRestory, in, out, mail, login, username,
                    socket, IP, PORT, aesKey, true, id_accaun, keyAc, width, height);
            codePanelRecovery.startCode();
            frame.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);
            passwordRecoveryButton.setText("Регистрация");
        });
    }

    // Обработка выхода в главное меню
    private static void outM() {
        entrancePanel = new EntrancePanelAll(IP, PORT, width, height);
        entrancePanel.startDevPassword();
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        frame.dispose();
    }

    // Отправка запроса на сервер для получения публичного ключа и кода подтверждения
    private static void getCode() {
        try {
            out.writeObject("MAIL");
            out.flush();

            Object response;
            while ((response = in.readObject()) != null) {
                if (response instanceof PublicKey) {
                    PublicKey publicKey = (PublicKey) response;

                    String encryptedEmail = encryption.encryptWithRSA(publicKey, mail.getBytes());
                    aesKey = encryption.generateAESKey();
                    confirmationCodeRestory = encryption.generateCode();

                    String encryptedCode = encryption.encryptWithAES(confirmationCodeRestory, aesKey);
                    outMail = "CODE " + encryptedEmail + " " + encryptedCode;

                    break;
                }
            }

            out.writeObject(outMail);
            out.flush();
        } catch (IOException | ClassNotFoundException ioException) {
            JOptionPane.showMessageDialog(frame, "Ошибка при отправке письма", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            if (frame != null) {
                frame.dispose();
            }
        }
    }
}
