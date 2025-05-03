package org.face_recognition;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;

// Класс для создания меню учетной записи с пунктами для просмотра данных аккаунта, уведомлений и параметров
public class MenuAccaunt {

    // Статические поля для хранения объектов и параметров
    private static ControlPanel controlPanel = new ControlPanel(); // Объект для настройки элементов интерфейса
    private static String accauntId; // Идентификатор учетной записи
    private static DynamicPanelScrollNotifications dynamicPanelScrollNotifications; // Панель уведомлений
    private static JScrollPane dynamicPanelScrollPane; // Панель прокрутки для уведомлений
    private static ContactPanel contactPanel = new ContactPanel(); // Панель для работы с контактами
    private static ObjectOutputStream out; // Поток вывода для связи с сервером
    private static String myID; // Идентификатор клиента
    private static JFrame frame; // Главное окно приложения
    private static String keyAc; // Ключ шифрования учетной записи

    // Конструктор класса, инициализирующий параметры
    public MenuAccaunt(String accauntId, ObjectOutputStream out, String myID, JFrame frame, String keyAc) {
        this.accauntId = accauntId; // Сохранение ID учетной записи
        this.out = out; // Сохранение потока вывода
        this.myID = myID; // Сохранение ID клиента
        this.frame = frame; // Сохранение главного окна
        this.keyAc = keyAc; // Сохранение ключа шифрования
    }

    // Метод для создания строки меню
    static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar(); // Создание строки меню
        JMenu textMenu = new JMenu("Информация"); // Меню "Информация"
        textMenu.setFont(textMenu.getFont().deriveFont(18f)); // Установка размера шрифта

        // Пункт "Идентификатор аккаунта"
        JMenuItem menuItem = new JMenuItem("Идентификатор аккаунта");
        menuItem.setFont(menuItem.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem.addActionListener(createInfoActionListener(frame, "Идентификатор аккаунта", myID)); // Обработчик
        textMenu.add(menuItem); // Добавление пункта

        // Пункт "Ключ учетной записи"
        JMenuItem menuItem1 = new JMenuItem("Ключ учетной записи");
        menuItem1.setFont(menuItem1.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem1.addActionListener(createInfoActionListener(frame, "Ключ учетной записи", keyAc)); // Обработчик
        textMenu.add(menuItem1); // Добавление пункта

        // Пункт "Заявки в друзья"
        JMenuItem menuItem2 = new JMenuItem("Заявки в друзья");
        menuItem2.setFont(menuItem2.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem2.addActionListener(createScroll(frame, "Заявки в друзья", keyAc, "1a")); // Обработчик
        textMenu.add(menuItem2); // Добавление пункта

        // Пункт "Ответы на заявку в друзья"
        JMenuItem menuItem3 = new JMenuItem("Ответы на заявку в друзья");
        menuItem3.setFont(menuItem3.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem3.addActionListener(createScroll(frame, "Ответы на заявку в друзья", keyAc, "2a")); // Обработчик
        textMenu.add(menuItem3); // Добавление пункта

        // Пункт "Планирование бесед"
        JMenuItem menuItem4 = new JMenuItem("Планирование бесед");
        menuItem4.setFont(menuItem4.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem4.addActionListener(createScroll(frame, "Планирование бесед", keyAc, "3a")); // Обработчик
        textMenu.add(menuItem4); // Добавление пункта

        // Пункт "Заблокированные пользователи"
        JMenuItem menuItem5 = new JMenuItem("Заблокированные пользователи");
        menuItem5.setFont(menuItem5.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem5.addActionListener(createScroll(frame, "Заблокированные пользователи", keyAc, "4a")); // Обработчик
        textMenu.add(menuItem5); // Добавление пункта

        // Пункт "Параметры"
        JMenuItem menuItem6 = new JMenuItem("Параметры");
        menuItem6.setFont(menuItem6.getFont().deriveFont(16f)); // Установка размера шрифта
        menuItem6.addActionListener(createParameters(frame)); // Обработчик
        textMenu.add(menuItem6); // Добавление пункта

        menuBar.add(textMenu); // Добавление меню в строку
        return menuBar; // Возврат строки меню
    }

    // Метод для создания обработчика для показа информации (ID или ключ)
    private static ActionListener createInfoActionListener(JFrame frame, String title, String info) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfoDialog(frame, title, info); // Отображение диалога
            }
        };
    }

    // Метод для создания обработчика для показа уведомлений с прокруткой
    private static ActionListener createScroll(JFrame frame, String title, String info, String str) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Запуск в отдельном потоке для асинхронной загрузки
                Thread treadMenu = new Thread(() -> {
                    showInfoScroll(frame, title, info, str); // Отображение диалога с уведомлениями
                });
                treadMenu.start();
            }
        };
    }

    // Метод для создания обработчика для открытия диалога параметров
    private static ActionListener createParameters(JFrame frame) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Запуск в отдельном потоке для асинхронной обработки
                Thread treadMenu = new Thread(() -> {
                    ParametersDialog parametersDialog = new ParametersDialog(accauntId, keyAc, myID, out, frame);
                    parametersDialog.createDialog("Параметры"); // Открытие диалога параметров
                });
                treadMenu.start();
            }
        };
    }

    // Метод для отображения диалога с уведомлениями
    private static void showInfoScroll(JFrame parentFrame, String title, String info, String str) {
        JDialog dialog = new JDialog(parentFrame, title, true); // Создание модального диалога
        dialog.setSize(700, 400); // Размер диалога
        dialog.setLocationRelativeTo(parentFrame); // Центрирование
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие при выходе

        // Поле для текстовых сообщений (не используется в текущей реализации)
        JTextArea messageField = new JTextArea();
        messageField.setLineWrap(true); // Перенос строк
        messageField.setWrapStyleWord(true); // Перенос по словам
        messageField.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт

        // Панель прокрутки для текстового поля
        JScrollPane scrollPane = new JScrollPane(messageField);
        scrollPane.setPreferredSize(new Dimension(400, 50)); // Размер
        scrollPane.setMaximumSize(new Dimension(400, 50)); // Максимальный размер
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Горизонтальная прокрутка
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Вертикальная прокрутка

        // Главная панель диалога
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Отступы
        mainPanel.setBackground(new Color(245, 245, 245)); // Светлый фон

        // Верхняя панель с заголовком и кнопками
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245)); // Светлый фон
        topPanel.add(Box.createHorizontalStrut(5)); // Отступ слева

        // Заголовок диалога
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Шрифт
        titleLabel.setForeground(Color.BLACK); // Черный текст

        // Кнопка закрытия с иконкой
        JButton topCloseButton = contactPanel.createIconButton("pictures/exitIcon.png", "Выход");
        topCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // Закрытие диалога
                dynamicPanelScrollNotifications = null; // Сброс панели уведомлений
            }
        });
        topCloseButton.setFocusPainted(false); // Удаление обводки при фокусе
        topCloseButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Отступы

        // Кнопка очистки уведомлений
        JButton clearingChat = contactPanel.createIconButton("pictures/clearingChat.png", "Очистить уведомления");
        clearingChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accauntId != null && dynamicPanelScrollNotifications != null) {
                    dynamicPanelScrollNotifications.delDatePanel(); // Очистка уведомлений
                }
            }
        });

        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false); // Прозрачный фон
        buttonPanel.add(clearingChat); // Кнопка очистки
        buttonPanel.add(topCloseButton); // Кнопка закрытия

        topPanel.add(titleLabel, BorderLayout.CENTER); // Заголовок в центре
        topPanel.add(buttonPanel, BorderLayout.EAST); // Кнопки справа
        mainPanel.add(topPanel, BorderLayout.NORTH); // Верхняя панель

        // Текстовое поле с информацией (не используется для уведомлений)
        JTextArea infoArea = new JTextArea(info);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт
        infoArea.setLineWrap(true); // Перенос строк
        infoArea.setWrapStyleWord(true); // Перенос по словам
        infoArea.setEditable(false); // Только чтение
        infoArea.setBackground(new Color(255, 255, 255)); // Белый фон
        infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )); // Рамка и отступы

        // Создание панели уведомлений
        dynamicPanelScrollNotifications = new DynamicPanelScrollNotifications(accauntId, str, out, myID, frame, keyAc, accauntId);

        // Асинхронная загрузка панелей уведомлений
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                dynamicPanelScrollNotifications.loadInitialPanels(); // Загрузка начальных панелей
                return null;
            }

            @Override
            protected void done() {
                dynamicPanelScrollPane = dynamicPanelScrollNotifications.getPanel(); // Получение панели прокрутки
                SwingUtilities.invokeLater(() -> {
                    mainPanel.add(dynamicPanelScrollPane, BorderLayout.CENTER); // Добавление панели в центр
                    // Слушатель для прокрутки текстового поля (не используется, так как messageField не активно)
                    messageField.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) { scrollToBottom(); }
                        @Override
                        public void removeUpdate(DocumentEvent e) { scrollToBottom(); }
                        @Override
                        public void changedUpdate(DocumentEvent e) { scrollToBottom(); }

                        private void scrollToBottom() {
                            SwingUtilities.invokeLater(() ->
                                    messageField.setCaretPosition(messageField.getDocument().getLength()));
                        }
                    });
                    mainPanel.revalidate(); // Обновление компоновки
                    mainPanel.repaint(); // Перерисовка
                });
            }
        };

        worker.execute(); // Запуск загрузки

        dialog.add(mainPanel); // Добавление главной панели
        dialog.setVisible(true); // Отображение диалога
    }

    // Метод для загрузки начальных панелей уведомлений
    static void loadInitialMenu() {
        if (dynamicPanelScrollNotifications != null) {
            dynamicPanelScrollNotifications.loadInitialPanels(); // Загрузка панелей
        }
    }

    // Метод для отображения диалога с информацией об учетной записи
    private static void showInfoDialog(JFrame parentFrame, String title, String info) {
        JDialog dialog = new JDialog(parentFrame, title, true); // Создание модального диалога
        dialog.setSize(600, 300); // Размер диалога
        dialog.setLocationRelativeTo(parentFrame); // Центрирование
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Закрытие при выходе

        // Главная панель диалога
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Отступы
        mainPanel.setBackground(new Color(245, 245, 245)); // Светлый фон

        // Заголовок диалога
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Шрифт
        titleLabel.setForeground(Color.BLACK); // Черный текст
        mainPanel.add(titleLabel, BorderLayout.NORTH); // Добавление заголовка

        // Текстовое поле с информацией
        JTextArea infoArea = new JTextArea(info);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт
        infoArea.setLineWrap(true); // Перенос строк
        infoArea.setWrapStyleWord(true); // Перенос по словам
        controlPanel.configureCopyMenu(infoArea); // Настройка контекстного меню для копирования
        infoArea.setEditable(false); // Только чтение
        infoArea.setBackground(new Color(255, 255, 255)); // Белый фон
        infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )); // Рамка и отступы

        // Панель прокрутки для текстового поля
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(350, 80)); // Размер
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Добавление в центр

        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Шрифт
        closeButton.setBackground(new Color(255, 182, 193)); // Светло-розовый фон
        closeButton.setForeground(Color.WHITE); // Белый текст
        closeButton.setFocusPainted(false); // Удаление обводки
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Отступы
        closeButton.addActionListener(e -> dialog.dispose()); // Закрытие диалога

        // Панель для кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245)); // Светлый фон
        buttonPanel.add(closeButton); // Добавление кнопки
        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Добавление панели кнопки

        dialog.add(mainPanel); // Добавление главной панели
        dialog.setVisible(true); // Отображение диалога
    }
}
