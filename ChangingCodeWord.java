package org.face_recognition;

// Класс ChangingCodeWord предоставляет интерфейс для изменения кодового слова пользователя
// Поддерживает ввод и проверку нового кодового слова, шифрование и обновление данных в базе
// Включает функционал временного отображения пароля с автоматическим скрытием через таймер
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ChangingCodeWord {

    // Поля класса для хранения состояния и компонентов
    private static JFrame frame; // Основное окно приложения
    private static JPanel textPanel; // Панель для первого поля пароля и кнопки
    private static JPanel textPanel1; // Панель для второго поля пароля и кнопки
    private static EntrancePanelAll entrancePanel; // Панель входа для возврата
    private static String IP; // IP-адрес сервера
    public static int PORT = 0; // Порт сервера
    private static JPasswordField passwordField1; // Поле для ввода первого кодового слова
    private static JPasswordField passwordField2; // Поле для повторного ввода кодового слова
    private static String keyAc; // Ключ аккаунта для шифрования
    private static Database database = new Database(); // Объект для работы с базой данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt(); // Объект для шифрования
    private static String idAccaun; // Идентификатор аккаунта
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры
    public ChangingCodeWord(String IP, int PORT, String idAccaun, String keyAc, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        this.idAccaun = idAccaun;
        this.keyAc = keyAc;
        this.width = width;
        this.height = height;
    }

    // Метод startNewCodeWord создаёт и отображает интерфейс для изменения кодового слова
    // Настраивает главное окно с минимальными размерами
    // Создаёт панель с полями ввода, метками и кнопками
    // Добавляет обработчики для изменения размера окна
    public static void startNewCodeWord() {
        // Создаём главное окно
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Устанавливаем размер окна
        if (width > 740 && height > 750) {
            frame.setSize(width, height);
        } else {
            frame.setSize(700, 750);
        }

        // Устанавливаем минимальный размер окна
        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        frame.setLocationRelativeTo(null);

        // Обработчик изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Создаём основную панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);

        mainPanel.add(Box.createVerticalGlue());

        // Создаём панель для контента
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        panel.setPreferredSize(new Dimension(500, 490));
        panel.setMinimumSize(new Dimension(500, 490));
        panel.setMaximumSize(new Dimension(500, 490));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Добавляем заголовок
        JLabel label = new JLabel("Введите новое кодовое слово");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        ControlPanel controlPanel = new ControlPanel();

        // Настраиваем первое поле пароля
        passwordField1 = new JPasswordField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField1);

        // Кнопка "Показать пароль" для первого поля
        JButton showPasswordButton1 = new JButton();
        textTime(passwordField1, showPasswordButton1);

        // Панель для первого поля и кнопки
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(showPasswordButton1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Настраиваем второе поле пароля
        passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        // Кнопка "Показать пароль" для второго поля
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2);

        // Панель для метки первого поля
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel label1 = new JLabel("Введите новое кодовое слово");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        // Панель для метки второго поля
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel label2 = new JLabel("Повторите новое кодовое слово");
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("Arial", Font.PLAIN, 17));
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label2Panel.add(label2);

        // Панель для второго поля и кнопки
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

        // Добавляем элементы на панель
        panel.add(label1Panel);
        panel.add(textPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label2Panel);
        panel.add(textPanel1);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel label3Panel = new JPanel();
        label3Panel.setLayout(new BorderLayout());
        label3Panel.setBackground(new Color(60, 63, 65));
        label3Panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        panel.add(Box.createRigidArea(new Dimension(0, 70)));

        // Кнопка "Изменить кодовое слово"
        JButton entrance = new JButton("Изменить кодовое слово");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            new Thread(() -> {
                changeCodeWord(entrance);
            }).start();
        });

        // Кнопка "Вернуться на панель входа"
        JButton showStartButton = new JButton("Вернуться на панель входа");
        customizeButton(showStartButton);
        showStartButton.addActionListener(e -> {
            outM();
            frame.dispose();
        });

        // Добавляем кнопки на панель
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(showStartButton);
        panel.add(Box.createRigidArea(new Dimension(0, 80)));

        // Устанавливаем светло-серую рамку
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        // Устанавливаем mainPanel как содержимое окна
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Метод changeCodeWord обрабатывает изменение кодового слова
    // Проверяет заполненность полей и совпадение паролей
    // Шифрует новое кодовое слово и обновляет данные в базе
    // Возвращает пользователя на панель входа
    private static void changeCodeWord(JButton button) {
        // Отключаем кнопку и меняем текст
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false);
            button.setText("Подтверждение...");
        });

        // Получаем введённые пароли
        char[] password1 = passwordField1.getPassword();
        char[] password3 = passwordField2.getPassword();
        String password_number_one = new String(password1);
        String password_number_two = new String(password3);

        // Проверяем, заполнены ли поля
        if (password_number_one.isEmpty() || password_number_two.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                button.setEnabled(true);
                button.setText("Изменить кодовое слово");
            });
            return;
        }

        // Проверяем совпадение паролей
        if (password_number_one.equals(password_number_two)) {
            try {
                // Обновляем данные в базе
                String dbPath = "user_accounts.db";
                String tableName = "accounts";
                Object[] searchValues = {idAccaun};
                String[] searchColumns = {"account_id"};
                String[] updateColumns = {"passphrase", "backup_account_key"};
                String usernameNew = encryption.Argon2(password_number_one);
                String account_keyNew = encryption.chaha20Encript(password_number_one, keyAc);
                Object[] newValues = {usernameNew, account_keyNew};
                database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);

                // Возвращаем на панель входа
                entrancePanel = new EntrancePanelAll(IP, PORT, width, height);
                entrancePanel.startDevPassword();
                frame.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Второе кодовое слово не совпадает с первым кодовым словом", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Восстанавливаем кнопку
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("Изменить кодовое слово");
        });
    }

    // Метод outM возвращает пользователя на панель входа
    private static void outM() {
        entrancePanel = new EntrancePanelAll(IP, PORT, width, height);
        entrancePanel.startDevPassword();
        frame.dispose();
    }

    // Метод textTime настраивает кнопку для временного отображения пароля
    // Добавляет иконки для показа/скрытия пароля
    // Устанавливает таймер для автоматического скрытия через 10 секунд
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton) {
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png"); // Иконка "показать"
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png"); // Иконка "скрыть"

        // Масштабируем иконки
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot);

        // Устанавливаем начальную иконку
        showPasswordButton.setIcon(scaledIconNot);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder());
        showPasswordButton.setPreferredSize(new Dimension(40, 40));
        showPasswordButton.setMaximumSize(new Dimension(40, 40));
        showPasswordButton.setMinimumSize(new Dimension(40, 40));

        // Флаг состояния видимости пароля
        final boolean[] isPasswordVisible = {false};

        // Таймер для скрытия пароля через 10 секунд
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•');
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

        // Обработчик кнопки "Показать/Скрыть пароль"
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

    // Метод customizeButton настраивает внешний вид кнопок
    // Устанавливает цвет, шрифт, размеры и отступы
    // Добавляет курсор "рука" для интерактивности
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
