package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Класс представляет первую панель полной регистрации, предназначенную для ввода пароля учетной записи
// и пароля соединения, с последующей передачей данных на следующую панель регистрации
public class RegistrationIncompleteFull {

    private static JFrame frame; // Главное окно приложения
    private static JPanel textPanel; // Панель для поля ввода пароля учетной записи
    private static JPanel textPanel1; // Панель для поля ввода пароля соединения
    private static String IP = null; // IP-адрес сервера
    private static int PORT; // Порт сервера
    private static Safety safety = new Safety(); // Объект для проверки безопасности пароля
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, инициализирующий параметры для регистрации
    public RegistrationIncompleteFull(String IP, int PORT, int width, int height){
        this.IP = IP;
        this.PORT = PORT;
        this.width = width;
        this.height = height;
    }

    // Метод запуска окна первой панели полной регистрации
    public static void startDFullPassword(){
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
        panel.add(Box.createRigidArea(new Dimension(0, 35))); // Пространство сверху

        // Заголовок панели
        JLabel label = new JLabel("Регистрация на устройстве");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 25))); // Пространство под заголовком

        // Инициализация объекта для управления интерфейсом
        ControlPanel controlPanel = new ControlPanel();

        // Поле для ввода пароля учетной записи
        JPasswordField passwordField1 = new JPasswordField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField1); // Настройка поля

        // Кнопка для показа/скрытия пароля учетной записи
        JButton showPasswordButton1 = new JButton();
        textTime(passwordField1, showPasswordButton1); // Настройка поведения кнопки

        // Панель для пароля учетной записи
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(showPasswordButton1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(passwordField1);
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Поле для ввода пароля соединения
        JPasswordField passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        // Кнопка для показа/скрытия пароля соединения
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2);

        // Метка для пароля учетной записи
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label1 = new JLabel("Введите пароль от учётной записи");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        // Метка для пароля соединения
        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label2 = new JLabel("Введите пароль от соединения");
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("Arial", Font.PLAIN, 17));
        label2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label2Panel.add(label2);

        // Панель для пароля соединения
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

        // Пустое пространство для выравнивания
        panel.add(Box.createRigidArea(new Dimension(0, 70)));

        // Кнопка перехода к следующей панели
        JButton entrance = new JButton("Далее");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            // Получение введенных паролей
            char[] password1 = passwordField1.getPassword();
            char[] password2 = passwordField2.getPassword();
            String connectionPassword = new String(password2);
            String accountPassword = new String(password1);
            String idAccaunt = safety.generateUniqueId(); // Генерация уникального идентификатора

            // Запуск обработки в отдельном потоке
            new Thread(() -> {
                try {
                    // Проверка заполнения полей
                    if (!connectionPassword.isEmpty() && !accountPassword.isEmpty()){
                        // Проверка сложности пароля учетной записи
                        if(safety.isValidPassword(accountPassword)){
                            // Переход к следующей панели регистрации
                            RegistrationOnFull registration = new RegistrationOnFull(IP, connectionPassword, accountPassword, idAccaunt, PORT, width, height);
                            registration.startFulPassword();
                            frame.dispose();
                        } else {
                            // Сообщение об ошибке при несоответствии пароля требованиям
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Введите другой пароль. Пароль должен содержать минимум 8 символов и включать:\n" +
                                            "- Латинские буквы (A–Z, a–z) или кириллические буквы (А–Я, а–я);\n" +
                                            "- Цифры (0–9);\n" +
                                            "- Специальные символы (!@#$%^&*()_+-=[]{};':\"|,.<>?/).",
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }else {
                        // Сообщение об ошибке при пустых полях
                        JOptionPane.showMessageDialog(
                                frame,
                                "Все поля должны быть заполнены.",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
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
            Menu menu = new Menu(IP, width, height);
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
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png"); // Иконка для "показать"
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png"); // Иконка для "скрыть"

        // Масштабируем иконки
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg); // Масштабированная иконка для "показать"

        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot); // Масштабированная иконка для "скрыть"

        // Устанавливаем начальную иконку в кнопку
        showPasswordButton.setIcon(scaledIconNot); // Начальное состояние — пароль скрыт
        showPasswordButton.setContentAreaFilled(false); // Убираем фон кнопки
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder()); // Убираем рамку

        // Сделаем кнопку квадратной (с одинаковой высотой и шириной)
        showPasswordButton.setPreferredSize(new Dimension(40, 40)); // Размеры кнопки
        showPasswordButton.setMaximumSize(new Dimension(40, 40)); // Максимальный размер кнопки
        showPasswordButton.setMinimumSize(new Dimension(40, 40)); // Минимальный размер кнопки

        // Флаг для отслеживания состояния пароля
        final boolean[] isPasswordVisible = {false}; // По умолчанию пароль скрыт

        // Таймер для автоматического скрытия пароля
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•'); // Скрываем пароль снова
            isPasswordVisible[0] = false;  // Обновляем состояние
            showPasswordButton.setIcon(scaledIconNot); // Устанавливаем иконку "скрыть"
        });

        // Настраиваем кнопку "Показать пароль"
        showPasswordButton.addActionListener(e -> {
            if (isPasswordVisible[0]) {
                // Если пароль уже виден, скрываем его
                passwordField.setEchoChar('•'); // Скрываем пароль
                isPasswordVisible[0] = false;  // Обновляем состояние
                hidePasswordTimer.stop();      // Останавливаем таймер
                showPasswordButton.setIcon(scaledIconNot); // Меняем иконку на "скрыть"
            } else {
                // Если пароль скрыт, показываем его
                passwordField.setEchoChar((char) 0); // Показываем пароль
                isPasswordVisible[0] = true;  // Обновляем состояние

                // Перезапускаем таймер для автоматического скрытия
                hidePasswordTimer.restart();
                showPasswordButton.setIcon(scaledIcon); // Меняем иконку на "показать"
            }
        });
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
        button.setPreferredSize(new Dimension(300, 80)); // Устанавливаем предпочтительный размер
        button.setMinimumSize(new Dimension(200, 60));  // Минимальный размер
        button.setMaximumSize(new Dimension(300, 60));  // Максимальный размер
    }

}
