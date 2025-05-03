package org.face_recognition;

//Страница для выбора входа или регистрации

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс для создания начальной страницы приложения StealthChat с выбором входа или регистрации
public class Menu {

    // Статические поля для хранения компонентов интерфейса и параметров
    private static JFrame frame; // Главное окно приложения
    private static String IP; // IP-адрес сервера
    public static int PORT = 0; // Порт для соединения
    private static EntrancePanelAll entrancePanel; // Панель входа
    private static RegistrationSelection registrationPanel; // Панель регистрации
    static Config config = new Config(); // Объект для получения конфигурации
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры
    public Menu(String IP, int width, int height) {
        this.IP = IP; // Сохранение IP-адреса
        this.width = width; // Сохранение ширины окна
        this.height = height; // Сохранение высоты окна
    }

    // Метод для запуска начальной страницы
    public static void start() {
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

        // Создание центральной панели для кнопок и заголовка
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Вертикальный BoxLayout
        panel.setBackground(new Color(60, 63, 65)); // Серый фон
        PORT = config.configPort(); // Получение порта из конфигурации

        // Инициализация базы данных
        Database database = new Database();
        database.createDatabase(); // Создание базы данных, если она не существует

        // Установка фиксированных размеров центральной панели
        panel.setPreferredSize(new Dimension(400, 300)); // Предпочтительный размер
        panel.setMinimumSize(new Dimension(400, 300)); // Минимальный размер
        panel.setMaximumSize(new Dimension(400, 300)); // Максимальный размер
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Отступы

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Добро пожаловать");
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        label.setForeground(Color.WHITE); // Белый текст
        label.setFont(new Font("Arial", Font.BOLD, 19)); // Шрифт
        panel.add(label); // Добавление заголовка

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Пространство под заголовком

        // Кнопка "Вход"
        JButton entrance = new JButton("Вход");
        customizeButton(entrance); // Кастомизация кнопки
        entrance.addActionListener(e -> {
            // Запуск обработки входа в отдельном потоке
            new Thread(() -> handleEntrance(entrance)).start();
        });

        // Кнопка "Регистрация" (названа как "Изменение пароля" в коде, но текст кнопки — "Регистрация")
        JButton passwordRecovery = new JButton("Регистрация");
        customizeButton(passwordRecovery); // Кастомизация кнопки
        passwordRecovery.addActionListener(e -> {
            // Запуск обработки регистрации в отдельном потоке
            new Thread(() -> handlePasswordRecovery(passwordRecovery)).start();
        });

        // Добавление кнопок на центральную панель
        panel.add(entrance); // Кнопка входа
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Пространство между кнопками
        panel.add(passwordRecovery); // Кнопка регистрации

        // Установка светло-серой рамки для центральной панели
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10)); // Рамка толщиной 10

        // Добавление центральной панели на главную панель
        mainPanel.add(panel); // Центрирование панели
        mainPanel.add(Box.createVerticalGlue()); // Пространство снизу

        // Установка главной панели как содержимого окна
        frame.setContentPane(mainPanel);
        frame.setVisible(true); // Отображение окна
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
        button.setPreferredSize(new Dimension(250, 60)); // Предпочтительный размер
        button.setMinimumSize(new Dimension(150, 40)); // Минимальный размер
        button.setMaximumSize(new Dimension(250, 40)); // Максимальный размер
    }

    // Обработка нажатия на кнопку "Вход"
    private static void handleEntrance(JButton entranceButton) {
        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(false); // Отключение кнопки
            entranceButton.setText("Вход..."); // Изменение текста кнопки
        });

        entrancePanel = new EntrancePanelAll(IP, PORT, width, height); // Создание панели входа
        entrancePanel.startDevPassword(); // Запуск панели входа
        frame.dispose(); // Закрытие текущего окна

        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(true); // Включение кнопки
            entranceButton.setText("Вход"); // Восстановление текста
        });
    }

    // Обработка нажатия на кнопку "Регистрация"
    private static void handlePasswordRecovery(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false); // Отключение кнопки
            passwordRecoveryButton.setText("Регистрация..."); // Изменение текста кнопки
        });

        registrationPanel = new RegistrationSelection(IP, PORT, width, height); // Создание панели регистрации
        registrationPanel.startRegistration(); // Запуск панели регистрации
        frame.dispose(); // Закрытие текущего окна

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true); // Включение кнопки
            passwordRecoveryButton.setText("Регистрация"); // Восстановление текста
        });
    }
}
