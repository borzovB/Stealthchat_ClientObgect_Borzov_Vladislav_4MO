package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс ChoosingOperation отвечает за создание интерфейса выбора операций в приложении StealthChat
// Предоставляет пользователю возможность выбрать между изменением кодового слова и переходом к панели входа
// Управляет графическим интерфейсом с двумя кнопками и инициализирует подключение к базе данных
// Использует настройки сервера, загруженные из файла конфигурации
public class ChoosingOperation {

    private static JFrame frame;
    private static String IP;
    public static int PORT = 0;
    static Config config = new Config();
    private static String keyAc;
    private static String id_accaun;
    private static int width;
    private static int height;

    // Конструктор класса, инициализирующий параметры для работы интерфейса
    // Принимает IP-адрес, порт, идентификатор аккаунта, ключ аккаунта, ширину и высоту окна
    // Сохраняет переданные значения в поля класса
    public ChoosingOperation(String IP, int PORT, String id_accaun, String keyAc, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        this.id_accaun = id_accaun;
        this.keyAc = keyAc;
        this.width = width;
        this.height = height;
    }

    // Метод start создаёт и отображает графический интерфейс для выбора операций
    public static void start(){
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if(width>740 && height>750){
            frame.setSize(width, height);
        }else {
            frame.setSize(700, 750);
        }

        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        frame.setLocationRelativeTo(null); // Центрируем окно на экране

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();  // Получаем ширину окна
                height = frame.getHeight(); // Получаем высоту окна
            }
        });

        // Создаём главную панель с BoxLayout для вертикального центрирования
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Вертикальная компоновка
        mainPanel.setBackground(new Color(30, 30, 30)); // Фон для основной панели
        mainPanel.setOpaque(true); // Убедимся, что фон отображается

        // Добавляем пустое пространство сверху для центрирования панели
        mainPanel.add(Box.createVerticalGlue()); // Пустое пространство сверху

        // Создаём панель с кнопками
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Вертикальная компоновка для кнопок
        panel.setBackground(new Color(60, 63, 65)); // Устанавливаем тёмный фон
        PORT = config.configPort();

        Database database = new Database();
        database.createDatabase();

        // Устанавливаем фиксированные размеры для panel
        panel.setPreferredSize(new Dimension(400, 300)); // Размеры панели с кнопками
        panel.setMinimumSize(new Dimension(400, 300));  // Минимальные размеры для панели
        panel.setMaximumSize(new Dimension(400, 300));  // Максимальные размеры для панели

        // Добавляем отступы для панели
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Добавляем заголовок
        JLabel label = new JLabel("Выбрать операцию");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE); // Белый текст
        label.setFont(new Font("Arial", Font.BOLD, 19)); // Шрифт и стиль
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Добавляем пустое пространство

        // Кнопка "Вход"
        JButton entrance = new JButton("Изменить кодовое слово");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            // Создаем и запускаем новый поток
            new Thread(() -> change_code_word(entrance)).start();
        });

        // Кнопка "Изменение пароля"
        JButton passwordRecovery = new JButton("Панель входа");
        customizeButton(passwordRecovery);
        passwordRecovery.addActionListener(e -> {
            // Создаем и запускаем новый поток
            new Thread(() -> out_menu(passwordRecovery)).start();
        });

        // Добавляем кнопки
        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Разделитель между кнопками
        panel.add(passwordRecovery);

        // Устанавливаем светло-серая рамка
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10)); // Светло-серая рамка толщиной 10

        // Добавляем панель с кнопками в основную панель
        mainPanel.add(panel); // Добавляем панель по центру

        // Добавляем пустое пространство снизу для центрирования панели
        mainPanel.add(Box.createVerticalGlue()); // Пустое пространство снизу

        // Устанавливаем mainPanel как содержимое JFrame
        frame.setContentPane(mainPanel); // Используем setContentPane для установки mainPanel

        frame.setVisible(true);
    }

    // Метод для кастомизации кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175)); // Синий фон
        button.setForeground(Color.WHITE); // Белый текст
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        button.setFocusPainted(false); // Убираем обводку при фокусе
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Отступы внутри кнопки
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор "рука"

        // Устанавливаем размеры кнопки
        button.setPreferredSize(new Dimension(250, 60)); // Устанавливаем предпочтительный размер
        button.setMinimumSize(new Dimension(150, 40));  // Минимальный размер
        button.setMaximumSize(new Dimension(250, 40));  // Максимальный размер
    }

    // Обработка нажатия на кнопку "Вход"
    private static void change_code_word(JButton entranceButton) {
        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(false);  // Отключаем кнопку
            entranceButton.setText("Переход...");  // Меняем текст кнопки на "Вход..."
        });

        frame.dispose();
        ChangingCodeWord changingCodeWord = new ChangingCodeWord(IP,PORT,id_accaun,keyAc, width, height);
        changingCodeWord.startNewCodeWord();

        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(true);  // Включаем кнопку обратно
            entranceButton.setText("Изменить кодовое слово");  // Возвращаем исходный текст кнопки
        });
    }

    // Обработка нажатия на кнопку "Изменение пароля"
    private static void out_menu(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);  // Отключаем кнопку
            passwordRecoveryButton.setText("Переход...");  // Меняем текст кнопки на "Изменение пароля..."
        });

        frame.dispose();
        EntrancePanelAll entrancePanelAll = new EntrancePanelAll(IP,PORT, width, height);
        entrancePanelAll.startDevPassword();

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);  // Включаем кнопку обратно
            passwordRecoveryButton.setText("Панель входа");  // Возвращаем исходный текст кнопки
        });
    }

}
