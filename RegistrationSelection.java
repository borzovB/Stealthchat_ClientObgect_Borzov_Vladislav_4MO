package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

//Этот класс предназначен для создания и отображения окна,
// которое позволяет пользователю выбрать способ регистрации в приложении
public class RegistrationSelection {

    private static String IP = null; // IP-адрес
    private static JFrame frame; // Основное окно
    private static RegistrationIncomplete registrationIncomplete; // Объект для незавершенной регистрации
    private static int PORT; // Порт
    private static int width; // Ширина окна
    private static int height; // Высота окна

    // Конструктор класса, который принимает IP, порт, ширину и высоту окна
    public RegistrationSelection(String IP, int PORT, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        this.width = width;
        this.height = height;
    }

    // Метод для запуска окна регистрации
    public static void startRegistration(){
        // Создание основного окна с названием "StealthChat"
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Устанавливаем размеры окна. Если размеры меньше минимальных, устанавливаем размер по умолчанию.
        if(width > 740 && height > 750){
            frame.setSize(width, height);
        } else {
            frame.setSize(700, 750);
        }

        // Устанавливаем минимальные размеры для окна
        Dimension minSize = new Dimension(740, 810);
        frame.setMinimumSize(minSize);
        frame.setLocationRelativeTo(null); // Центрируем окно на экране

        // Добавляем слушатель для изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();  // Получаем ширину окна
                height = frame.getHeight(); // Получаем высоту окна
            }
        });

        // Создаем объект для незавершенной регистрации
        registrationIncomplete = new RegistrationIncomplete(IP, width, height);

        // Создаем главную панель с вертикальной компоновкой для центрирования
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Вертикальная компоновка
        mainPanel.setBackground(new Color(30, 30, 30)); // Темный фон
        mainPanel.setOpaque(true); // Обеспечиваем отображение фона

        // Добавляем пустое пространство сверху для центрирования панели
        mainPanel.add(Box.createVerticalGlue());

        // Создаем панель с кнопками
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Вертикальная компоновка для кнопок
        panel.setBackground(new Color(60, 63, 65)); // Темный фон для панели с кнопками

        // Устанавливаем фиксированные размеры для панели с кнопками
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setMinimumSize(new Dimension(400, 300));  // Минимальные размеры для панели
        panel.setMaximumSize(new Dimension(400, 300));  // Максимальные размеры для панели

        // Добавляем отступы для панели
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Отступ перед заголовком

        // Создаем заголовок "Регистрация"
        JLabel label = new JLabel("Регистрация");
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Выравнивание по центру
        label.setForeground(Color.WHITE); // Белый цвет текста
        label.setFont(new Font("Arial", Font.BOLD, 19)); // Шрифт и стиль
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Отступ перед кнопками

        // Кнопка "Полная регистрация"
        JButton fullRegistration = new JButton("Полная регистрация");
        customizeButton(fullRegistration); // Настроить стиль кнопки
        fullRegistration.addActionListener(e -> {
            // Создаем новый поток для запуска полной регистрации
            new Thread(() -> full(fullRegistration)).start();
        });

        // Кнопка "Регистрация на устройстве"
        JButton deviceRegistration = new JButton("Регистрация на устройстве");
        customizeButton(deviceRegistration); // Настроить стиль кнопки
        deviceRegistration.addActionListener(e -> {
            // Создаем новый поток для регистрации на устройстве
            new Thread(() -> device(deviceRegistration)).start();
        });

        // Кнопка "Панель входа"
        JButton mainMenu = new JButton("Панель входа");
        customizeButton(mainMenu); // Настроить стиль кнопки
        mainMenu.addActionListener(e -> {
            // Создаем новый поток для перехода в главное меню
            new Thread(() -> menu(mainMenu)).start();
        });

        // Добавляем кнопки в панель
        panel.add(fullRegistration);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Отступ между кнопками
        panel.add(deviceRegistration);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Отступ между кнопками
        panel.add(mainMenu);

        // Добавляем светло-серую рамку вокруг панели
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        // Добавляем панель с кнопками в основную панель
        mainPanel.add(panel);

        // Добавляем пустое пространство снизу для центрирования
        mainPanel.add(Box.createVerticalGlue());

        // Устанавливаем mainPanel как содержимое для окна
        frame.setContentPane(mainPanel);

        // Отображаем окно
        frame.setVisible(true);
    }

    // Метод для перехода в главное меню
    private static void menu(JButton mainMenu) {
        SwingUtilities.invokeLater(() -> {
            mainMenu.setEnabled(false);  // Отключаем кнопку
            mainMenu.setText("Переход в главное меню...");  // Меняем текст кнопки
        });

        // Создаем объект меню и запускаем его
        Menu menu = new Menu(IP, width, height);
        menu.start();
        frame.dispose(); // Закрываем текущее окно

        SwingUtilities.invokeLater(() -> {
            mainMenu.setEnabled(true);  // Включаем кнопку обратно
            mainMenu.setText("Вернуться в главное меню");  // Возвращаем исходный текст кнопки
        });
    }

    // Метод для кастомизации кнопок (цвет, шрифт, размеры и т.д.)
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Выравнивание по центру
        button.setBackground(new Color(75, 110, 175)); // Синий фон
        button.setForeground(Color.WHITE); // Белый текст
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Шрифт
        button.setFocusPainted(false); // Убираем обводку при фокусе
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Отступы внутри кнопки
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Курсор "рука"

        // Устанавливаем размеры кнопки
        button.setPreferredSize(new Dimension(250, 60));
        button.setMinimumSize(new Dimension(150, 40));  // Минимальный размер
        button.setMaximumSize(new Dimension(250, 40));  // Максимальный размер
    }

    // Метод для обработки нажатия на кнопку "Полная регистрация"
    private static void full(JButton entranceButton) {
        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(false);  // Отключаем кнопку
            entranceButton.setText("Полная регистрация...");  // Меняем текст кнопки
        });

        // Создаем объект для полной регистрации и начинаем процесс
        RegistrationIncompleteFull registration = new RegistrationIncompleteFull(IP, PORT, width, height);
        registration.startDFullPassword();
        frame.dispose(); // Закрываем текущее окно

        SwingUtilities.invokeLater(() -> {
            entranceButton.setEnabled(true);  // Включаем кнопку обратно
            entranceButton.setText("Полная регистрация");  // Возвращаем исходный текст кнопки
        });
    }

    // Метод для обработки нажатия на кнопку "Регистрация на устройстве"
    private static void device(JButton passwordRecoveryButton) {
        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(false);  // Отключаем кнопку
            passwordRecoveryButton.setText("Регистрация на устройстве...");  // Меняем текст кнопки
        });

        // Начинаем процесс регистрации на устройстве
        registrationIncomplete.startDevPassword();
        frame.dispose(); // Закрываем текущее окно

        SwingUtilities.invokeLater(() -> {
            passwordRecoveryButton.setEnabled(true);  // Включаем кнопку обратно
            passwordRecoveryButton.setText("Регистрация на устройстве");  // Возвращаем исходный текст кнопки
        });
    }
}
