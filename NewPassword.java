package org.face_recognition;

import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс для создания интерфейса смены пароля пользователя в приложении StealthChat
public class NewPassword {

    // Статические поля для хранения компонентов интерфейса, сетевых параметров и данных
    private static JFrame frame; // Главное окно приложения
    private static JPanel textPanel; // Панель для первого поля ввода пароля
    private static JPanel textPanel1; // Панель для второго поля ввода пароля
    private static EntrancePanelAll entrancePanel; // Панель входа
    private static SSLSocket socket; // SSL-сокет для связи с сервером
    private static ObjectOutputStream out; // Поток вывода для отправки данных
    private static ObjectInputStream in; // Поток ввода для получения данных
    private static String IP; // IP-адрес сервера
    public static int PORT = 0; // Порт для SSL-соединения
    private static boolean choice; // Флаг выбора метода обновления пароля (сервер или локальная база)
    private static JPasswordField passwordField2; // Поле для повторного ввода пароля
    private static JPasswordField passwordField1; // Поле для ввода нового пароля
    private static Safety safety = new Safety(); // Объект для проверки надежности пароля
    private static String keyAc; // Ключ шифрования учетной записи
    private static String id_accaun; // Идентификатор учетной записи
    private static Database database = new Database(); // Объект для работы с базой данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt(); // Объект для шифрования/дешифрования
    private static ChoosingOperation choosingOperation; // Панель выбора операций
    private static String login; // Логин пользователя
    private static String mail; // Электронная почта пользователя
    private static String username; // Имя пользователя
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры
    public NewPassword(String IP, int PORT, SSLSocket socket, boolean choice, String id_accaun, String keyAc,
                       String login, String mail, String username, ObjectInputStream in, ObjectOutputStream out,
                       int width, int height) {
        this.IP = IP; // Сохранение IP-адреса
        this.PORT = PORT; // Сохранение порта
        this.socket = socket; // Сохранение сокета
        this.choice = choice; // Сохранение флага выбора
        this.id_accaun = id_accaun; // Сохранение ID учетной записи
        this.keyAc = keyAc; // Сохранение ключа шифрования
        this.login = login; // Сохранение логина
        this.mail = mail; // Сохранение электронной почты
        this.username = username; // Сохранение имени
        this.in = in; // Сохранение потока ввода
        this.out = out; // Сохранение потока вывода
        this.width = width; // Сохранение ширины окна
        this.height = height; // Сохранение высоты окна
    }

    // Метод для запуска панели смены пароля
    public static void startNewPassword() {
        frame = new JFrame("StealthChat"); // Создание главного окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Закрытие приложения при закрытии окна
        if (width > 740 && height > 750) {
            frame.setSize(width, height); // Установка размеров окна, если они больше минимальных
        } else {
            frame.setSize(700, 750); // Установка минимальных размеров
        }

        Dimension minSize = new Dimension(740, 810); // Минимальный размер окна
        frame.setMinimumSize(minSize); // Установка минимального размера
        frame.setLocationRelativeTo(null); // Центрирование окна на экране

        // Добавление слушателя для отслеживания изменения размеров окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth(); // Обновление ширины
                height = frame.getHeight(); // Обновление высоты
            }
        });

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
        panel.setPreferredSize(new Dimension(500, 490)); // Предпочтительный размер
        panel.setMinimumSize(new Dimension(500, 490)); // Минимальный размер
        panel.setMaximumSize(new Dimension(500, 490)); // Максимальный размер
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Отступы

        panel.add(Box.createRigidArea(new Dimension(0, 35))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Введите новый пароль");
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        label.setForeground(Color.WHITE); // Белый текст
        label.setFont(new Font("Arial", Font.BOLD, 31)); // Шрифт
        panel.add(label); // Добавление заголовка

        panel.add(Box.createRigidArea(new Dimension(0, 25))); // Пространство под заголовком

        ControlPanel controlPanel = new ControlPanel(); // Объект для настройки полей ввода

        // Поле для ввода нового пароля
        passwordField1 = new JPasswordField();
        passwordField1.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configurePasswordField(passwordField1); // Настройка поля

        // Кнопка для показа/скрытия пароля
        JButton showPasswordButton1 = new JButton();
        textTime(passwordField1, showPasswordButton1); // Настройка кнопки и таймера

        // Панель для первого поля пароля
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel.add(showPasswordButton1); // Кнопка показа
        textPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ
        textPanel.add(passwordField1); // Поле ввода
        textPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ

        // Поле для повторного ввода пароля
        passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40)); // Размер поля
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16)); // Шрифт
        controlPanel.configurePasswordField(passwordField2); // Настройка поля

        // Кнопка для показа/скрытия повторного пароля
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2); // Настройка кнопки и таймера

        // Панель для метки "Введите новый пароль"
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label1Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label1 = new JLabel("Введите новый пароль");
        label1.setForeground(Color.WHITE); // Белый текст
        label1.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label1Panel.add(label1); // Добавление метки

        // Панель для метки "Повторите новый пароль"
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout()); // BorderLayout для метки
        label2Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Отступы
        JLabel label2 = new JLabel("Повторите новый пароль");
        label2.setForeground(Color.WHITE); // Белый текст
        label2.setFont(new Font("Arial", Font.PLAIN, 17)); // Шрифт
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT); // Выравнивание
        label2Panel.add(label2); // Добавление метки

        // Панель для второго поля пароля
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS)); // Горизонтальный BoxLayout
        textPanel1.add(showPasswordButton2); // Кнопка показа
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ
        textPanel1.add(passwordField2); // Поле ввода
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0))); // Отступ

        // Добавление меток и полей на центральную панель
        panel.add(label1Panel); // Метка первого пароля
        panel.add(textPanel); // Поле первого пароля
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство
        panel.add(label2Panel); // Метка второго пароля
        panel.add(textPanel1); // Поле второго пароля
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Пространство

        // Пустая панель (возможно, зарезервирована для будущих элементов)
        JPanel label3Panel = new JPanel();
        label3Panel.setLayout(new BorderLayout());
        label3Panel.setBackground(new Color(60, 63, 65)); // Серый фон
        label3Panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Отступы
        panel.add(Box.createRigidArea(new Dimension(0, 70))); // Пространство

        // Кнопка "Изменить пароль"
        JButton entrance = new JButton("Изменить пароль");
        customizeButton(entrance); // Кастомизация кнопки
        entrance.addActionListener(e -> {
            // Запуск смены пароля в отдельном потоке
            new Thread(() -> {
                changePassword(entrance); // Вызов метода смены пароля
            }).start();
        });

        // Кнопка "Вернуться в главное меню"
        JButton showStartButton = new JButton("Вернуться в главное меню");
        customizeButton(showStartButton); // Кастомизация кнопки
        showStartButton.addActionListener(e -> {
            outM(); // Переход в главное меню и закрытие ресурсов
            frame.dispose(); // Закрытие окна
        });

        // Добавление кнопок на центральную панель
        panel.add(entrance); // Кнопка изменения пароля
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

    // Метод для обработки смены пароля
    private static void changePassword(JButton button) {
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false); // Отключение кнопки
            button.setText("Подтверждение..."); // Изменение текста кнопки
        });

        // Получение паролей из полей ввода
        char[] password1 = passwordField1.getPassword();
        char[] password3 = passwordField2.getPassword();
        String password_number_one = new String(password1); // Новый пароль
        String password_number_two = new String(password3); // Повторный пароль

        // Проверка заполненности полей
        if (password_number_one.isEmpty() || password_number_two.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                button.setEnabled(true); // Включение кнопки
                button.setText("Изменить пароль"); // Восстановление текста
            });
            return;
        }

        // Проверка надежности пароля
        if (!safety.isValidPassword(password_number_one)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Введите другой пароль. Пароль должен содержать минимум 8 символов и включать:\n" +
                            "- Латинские буквы (A–Z, a–z) или кириллические буквы (А–Я, а–я);\n" +
                            "- Цифры (0–9);\n" +
                            "- Специальные символы (!@#$%^&*()_+-=[]{};':\"|,.<>?/).",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            SwingUtilities.invokeLater(() -> {
                button.setEnabled(true); // Включение кнопки
                button.setText("Изменить пароль"); // Восстановление текста
            });
            return;
        }

        // Проверка совпадения паролей
        if (password_number_one.equals(password_number_two)) {
            if (choice) {
                // Обновление пароля через сервер
                String argonNewPassword = encryption.Argon2(password_number_one); // Хеширование пароля
                try {
                    handleServerResponse(); // Запуск обработки ответа сервера
                    // Отправка запроса на сервер
                    out.writeObject("UPDATE_PASSWORD " + username + " " + mail + " " + login + " " + argonNewPassword);
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(frame, "Ошибка при подключении к серверу - 1", "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    if (frame != null) {
                        frame.dispose(); // Закрытие окна при ошибке
                    }
                }
            } else {
                // Обновление пароля в локальной базе данных
                try {
                    String dbPath = "user_accounts.db"; // Путь к базе данных
                    String tableName = "accounts"; // Имя таблицы
                    Object[] searchValues = {id_accaun}; // Условие поиска
                    String[] searchColumns = {"account_id"}; // Колонка для поиска
                    String[] updateColumns = {"account_password", "account_key"}; // Колонки для обновления
                    String usernameNew = encryption.Argon2(password_number_one); // Хешированный пароль
                    String account_keyNew = encryption.chaha20Encript(password_number_one, keyAc); // Зашифрованный ключ
                    Object[] newValues = {usernameNew, account_keyNew}; // Новые значения
                    // Обновление записи в базе данных
                    database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);
                    frame.dispose(); // Закрытие окна
                    // Переход к панели выбора операций
                    choosingOperation = new ChoosingOperation(IP, PORT, id_accaun, keyAc, width, height);
                    choosingOperation.start();
                } catch (Exception e) {
                    throw new RuntimeException(e); // Обработка ошибок
                }
            }
        } else {
            // Ошибка при несовпадении паролей
            JOptionPane.showMessageDialog(frame, "Второй пароль не совпадает с первым паролем", "Ошибка", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                button.setEnabled(true); // Включение кнопки
                button.setText("Изменить пароль"); // Восстановление текста
            });
        }
    }

    // Метод для обработки ответа сервера
    public static void handleServerResponse() {
        new Thread(() -> {
            try {
                Object response;
                while ((response = in.readObject()) != null) {
                    if (response instanceof String) {
                        String lineString = (String) response;
                        if (lineString.equals("1")) {
                            // Успешное изменение пароля
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(frame, "Пароль успешно изменен!");
                                frame.dispose(); // Закрытие окна
                                // Переход к панели выбора операций
                                choosingOperation = new ChoosingOperation(IP, PORT, id_accaun, keyAc, width, height);
                                choosingOperation.start();
                            });
                        } else {
                            // Ошибка при изменении пароля
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        frame,
                                        "Не удалось изменить пароль, проверьте логин, имя и электронную почту!",
                                        "Ошибка",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            });
                        }
                    }
                    break; // Прерывание цикла после обработки ответа
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Критическая ошибка: данные не удалось обработать (ClassNotFoundException).",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Ошибка подключения к серверу. Проверьте соединение.",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Произошла непредвиденная ошибка: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    // Метод для возврата в главное меню и закрытия ресурсов
    private static void outM() {
        // Запуск панели входа
        entrancePanel = new EntrancePanelAll(IP, PORT, width, height);
        entrancePanel.startDevPassword();
        try {
            // Закрытие сетевых ресурсов
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e); // Обработка ошибок
        }
        frame.dispose(); // Закрытие окна
    }

    // Метод для настройки кнопки показа/скрытия пароля с таймером
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton) {
        // Загрузка и масштабирование иконок
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
        button.setPreferredSize(new Dimension(300, 80)); // Предпочтительный размер
        button.setMinimumSize(new Dimension(200, 60)); // Минимальный размер
        button.setMaximumSize(new Dimension(300, 60)); // Максимальный размер
    }
}
