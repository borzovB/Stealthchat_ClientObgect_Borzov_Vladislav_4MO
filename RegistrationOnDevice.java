package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.border.LineBorder;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс отвечает за регистрацию только на устройстве, предоставляя интерфейс для ввода кодового слова
// и, опционально, ключа учетной записи для восстановления контактов. Данные сохраняются в локальной базе данных
public class RegistrationOnDevice {

    private static JFrame frame; // Главное окно приложения
    private static JPanel textPanel; // Панель для поля ввода кодового слова
    private static JPanel textPanel1; // Панель для поля ввода ключа учетной записи
    private static String IP = null; // IP-адрес сервера (не используется в данном контексте)
    private static String tableName = "accounts"; // Название таблицы в базе данных
    private static String accountPassword; // Пароль учетной записи
    private static String connectionPassword; // Пароль для соединения
    private static String idAccaunt; // Идентификатор учетной записи
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры для регистрации
    public RegistrationOnDevice(String IP, String connectionPassword, String accountPassword,
                                String idAccaunt, int width, int height){
        this.IP = IP;
        this.connectionPassword = connectionPassword;
        this.accountPassword = accountPassword;
        this.idAccaunt = idAccaunt;
        this.width = width;
        this.height = height;
    }

    // Метод запуска окна регистрации на устройстве
    public static void startDevPassword(){
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

        // Добавление слушателя для отслеживания изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();  // Обновление ширины окна
                height = frame.getHeight(); // Обновление высоты окна
            }
        });

        // Инициализация объектов для управления интерфейсом и базой данных
        ControlPanel controlPanel = new ControlPanel();
        Database database = new Database();

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
        panel.setPreferredSize(new Dimension(500, 490));
        panel.setMinimumSize(new Dimension(500, 490));
        panel.setMaximumSize(new Dimension(500, 490));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Отступы
        panel.add(Box.createRigidArea(new Dimension(0, 25))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Регистрация на устройстве");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 25))); // Пространство под заголовком

        // Поле для ввода кодового слова
        JPasswordField passwordField1 = new JPasswordField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField1); // Настройка поля

        // Кнопка для показа/скрытия кодового слова
        JButton showPasswordButton1 = new JButton();
        textTime(passwordField1, showPasswordButton1); // Настройка поведения кнопки

        // Панель для кодового слова
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(showPasswordButton1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Поле для ввода ключа учетной записи
        JPasswordField passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        // Кнопка для показа/скрытия ключа учетной записи
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2);

        // Метка для кодового слова
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label1 = new JLabel("Введите кодовое слово");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        // Метка для ключа учетной записи
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label2 = new JLabel("Введите ключ учетной записи");
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("Arial", Font.PLAIN, 17));
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label2Panel.add(label2);

        // Панель для ключа учетной записи
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(label1Panel);
        panel.add(textPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(label2Panel);
        panel.add(textPanel1);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Текстовое пояснение
        JPanel label3Panel = new JPanel();
        label3Panel.setLayout(new BorderLayout());
        label3Panel.setBackground(new Color(60, 63, 65));
        label3Panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JTextArea label3 = new JTextArea(
                "Если вам нужна дополнительная защита, введите кодовое слово. Это также позво-\nлит восстановить учетные данные при потере пароля от учетной записи. Если вы \nуже зарегистрированы, то введите ключ учетной записи, чтобы восстановить кон-\nтакты, если же вы входите в первый раз оставьте поле пустым."
        );
        label3.setWrapStyleWord(true); // Перенос по словам
        label3.setLineWrap(true); // Включение переноса строк
        label3.setEditable(false); // Запрет редактирования
        label3.setOpaque(false); // Прозрачный фон
        label3.setForeground(Color.WHITE);
        label3.setFont(new Font("Arial", Font.PLAIN, 11));
        label3.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 2), // Белая рамка
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Внутренние отступы
        ));
        label3Panel.add(label3);
        panel.add(label3Panel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Инициализация объектов шифрования и меню
        EncryptionAccaunt encryption = new EncryptionAccaunt();
        Menu menu = new Menu(IP, width, height);

        // Кнопка завершения регистрации
        JButton entrance = new JButton("Готово");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            // Получение введенных данных
            char[] password1 = passwordField1.getPassword();
            char[] password3 = passwordField2.getPassword();
            String account_key_pl = new String(password3);

            // Запуск обработки в отдельном потоке
            new Thread(() -> {
                try {
                    // Если ключ учетной записи не введен
                    if(account_key_pl.isEmpty()){
                        // Подтверждение отсутствия учетной записи
                        int result = JOptionPane.showConfirmDialog(frame, "Вы точно не имеете учетной " +
                                "записи на иных устройствах?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION){
                            JOptionPane.showMessageDialog(frame, "Действие выполнено.", "Результат", JOptionPane.INFORMATION_MESSAGE);
                            // Генерация нового ключа AES
                            String account_key = KeyGet.generationKeyAES();
                            // Хэширование пароля учетной записи
                            String accountPasswordNew = encryption.Argon2(accountPassword);
                            // Шифрование ключа учетной записи паролем
                            String account_keyNew = encryption.chaha20Encript(accountPassword, account_key);
                            // Шифрование пароля соединения ключом
                            String connectionNew = encryption.chaha20Encript(account_key, connectionPassword);
                            // Формирование данных для записи в базу
                            Map<String, Object> fields = new HashMap<>();
                            String passphrase = new String(password1);
                            // Если кодовое слово введено
                            if (!passphrase.isEmpty()){
                                String passphraseNew = encryption.Argon2(passphrase);
                                String backup_account_key = encryption.chaha20Encript(passphrase, account_key);
                                fields.put("passphrase", passphraseNew);
                                fields.put("backup_account_key", backup_account_key);
                            }
                            fields.put("account_id", idAccaunt);
                            fields.put("account_key", account_keyNew);
                            fields.put("account_password", accountPasswordNew);
                            fields.put("key_connection", connectionNew);
                            // Запуск главного меню и закрытие окна
                            menu.start();
                            frame.dispose();
                            // Запись данных в базу
                            database.addRecord(tableName, fields);
                        }else {
                            JOptionPane.showMessageDialog(frame, "Действие отменено.", "Результат", JOptionPane.WARNING_MESSAGE);
                        }
                    }else {
                        // Если ключ учетной записи введен
                        char[] password2 = passwordField2.getPassword();
                        String account_key = new String(password2);
                        String accountPasswordNew = encryption.Argon2(accountPassword);
                        String account_keyNew = encryption.chaha20Encript(accountPassword, account_key);
                        String connectionNew = encryption.chaha20Encript(account_key, connectionPassword);
                        Map<String, Object> fields = new HashMap<>();
                        String passphrase = new String(password1);
                        // Если кодовое слово введено
                        if (!passphrase.isEmpty()){
                            String passphraseNew = encryption.Argon2(passphrase);
                            String backup_account_key = encryption.chaha20Encript(passphrase, account_key);
                            fields.put("passphrase", passphraseNew);
                            fields.put("backup_account_key", backup_account_key);
                        }
                        fields.put("account_id", idAccaunt);
                        fields.put("account_key", account_keyNew);
                        fields.put("account_password", accountPasswordNew);
                        fields.put("key_connection", connectionNew);
                        // Запись данных в базу
                        database.addRecord(tableName, fields);
                        JOptionPane.showMessageDialog(frame, "Действие выполнено.", "Результат", JOptionPane.INFORMATION_MESSAGE);
                        // Запуск главного меню и закрытие окна
                        menu.start();
                        frame.dispose();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });

        // Кнопка возврата в главное меню
        JButton showStartButton = new JButton("Вернуться в главное меню");
        customizeButton(showStartButton);
        showStartButton.addActionListener(e -> {
            menu.start();
            frame.dispose();
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

    // Настройка кнопки показа/скрытия пароля
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton){
        // Загрузка иконок для кнопки
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png"); // Иконка "показать"
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png"); // Иконка "скрыть"
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot);
        showPasswordButton.setIcon(scaledIconNot); // Начальное состояние — скрыт
        showPasswordButton.setContentAreaFilled(false); // Убрать фон
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder()); // Убрать рамку
        showPasswordButton.setPreferredSize(new Dimension(40, 40));
        showPasswordButton.setMaximumSize(new Dimension(40, 40));
        showPasswordButton.setMinimumSize(new Dimension(40, 40));

        // Флаг состояния видимости пароля
        final boolean[] isPasswordVisible = {false};
        // Таймер для автоматического скрытия пароля через 10 секунд
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•'); // Скрыть пароль
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

        // Обработчик нажатия кнопки
        showPasswordButton.addActionListener(e -> {
            if (isPasswordVisible[0]) {
                passwordField.setEchoChar('•'); // Скрыть пароль
                isPasswordVisible[0] = false;
                hidePasswordTimer.stop();
                showPasswordButton.setIcon(scaledIconNot);
            } else {
                passwordField.setEchoChar((char) 0); // Показать пароль
                isPasswordVisible[0] = true;
                hidePasswordTimer.restart();
                showPasswordButton.setIcon(scaledIcon);
            }
        });
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
