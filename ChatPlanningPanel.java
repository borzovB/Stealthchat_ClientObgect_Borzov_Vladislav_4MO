package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectOutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.toedter.calendar.JCalendar;

// Класс ChatPlanningPanel отвечает за создание интерфейса планирования чата в приложении
// Предоставляет пользователю возможность указать дату и время начала и окончания чата, а также текст сообщения
// Поддерживает выбор дат через календарь, валидацию ввода и шифрование данных перед отправкой на сервер
// Использует базу данных для получения ключей шифрования и SSL-соединение для передачи данных
public class ChatPlanningPanel {

    // Уникальный идентификатор текущего пользователя
    private static String myID;
    // IP-адрес или идентификатор контакта, с которым планируется чат
    private static String ipContact;
    // Поток вывода для отправки данных на сервер
    private static ObjectOutputStream out;
    // Основная панель интерфейса планирования чата
    private JPanel panel;
    // Текстовое поле для ввода даты и времени начала чата
    private JTextField startDateField;
    // Текстовое поле для ввода даты и времени окончания чата
    private JTextField endDateField;
    // Текстовое поле для ввода текстового сообщения
    private JTextField textInputField;
    // Кнопка для вызова календаря для выбора даты начала
    private JButton startCalendarButton;
    // Кнопка для вызова календаря для выбора даты окончания
    private JButton endCalendarButton;
    // Кнопка для подтверждения и отправки данных
    private JButton submitButton;
    // Кнопка для очистки всех полей ввода
    private JButton clearButton;
    // Кнопка для закрытия диалогового окна
    private JButton exitButton;
    // Форматтер для отображения даты и времени в формате dd.MM.yyyy HH:mm
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    // Форматтер для преобразования даты в ISO-формат
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    // Плейсхолдер для поля даты начала
    private final String START_PLACEHOLDER = "Пример: 18.03.2025 14:00";
    // Плейсхолдер для поля даты окончания
    private final String END_PLACEHOLDER = "Пример: 19.03.2025 16:00";
    // Плейсхолдер для поля сообщения
    private final String TEXT_PLACEHOLDER = "Введите сообщение (макс. 65 символов)";
    // Ключ аккаунта для шифрования
    private static String keyAc;
    // Объект для выполнения операций шифрования
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Идентификатор аккаунта пользователя
    private static String accaunt_id;
    // Объект для работы с базой данных
    private static Database database = new Database();

    // Конструктор класса ChatPlanningPanel, создающий интерфейс для планирования чата
    // Инициализирует графический интерфейс с полями для ввода даты начала, окончания и сообщения
    // Настраивает кнопки для очистки, подтверждения, закрытия и выбора дат через календарь
    // Принимает диалоговое окно, идентификаторы пользователя и контакта, поток вывода, ключ и ID аккаунта
    public ChatPlanningPanel(JDialog dialog, String myID, String ipContact, ObjectOutputStream out, String keyAc, String accaunt_id) {
        // Инициализация полей класса
        this.myID = myID;
        this.accaunt_id = accaunt_id;
        this.ipContact = ipContact;
        this.out = out;
        this.keyAc = keyAc;

        // Создаём основную панель с BorderLayout и тёмным фоном
        panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new Dimension(600, 600));

        // Создаём панель для кнопок с горизонтальной компоновкой
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        // Устанавливаем составную границу для визуального разделения
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)),
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220))
        ));

        // Устанавливаем размер кнопок
        int buttonSize = 40;

        // Создаём кнопку очистки с иконкой
        clearButton = new JButton();
        ImageIcon clearIcon = new ImageIcon("pictures/clearingChat.png");
        clearButton.setIcon(new ImageIcon(clearIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH)));
        clearButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        clearButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        clearButton.setMinimumSize(new Dimension(buttonSize, buttonSize));
        clearButton.setToolTipText("Очистить");

        // Создаём кнопку подтверждения с иконкой
        submitButton = new JButton();
        ImageIcon saveIcon = new ImageIcon("pictures/save.png");
        submitButton.setIcon(new ImageIcon(saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH)));
        submitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        submitButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        submitButton.setMinimumSize(new Dimension(buttonSize, buttonSize));
        submitButton.setToolTipText("Подтвердить");

        // Создаём кнопку закрытия с иконкой
        exitButton = new JButton();
        ImageIcon exitIcon = new ImageIcon("pictures/exitIcon.png");
        exitButton.setIcon(new ImageIcon(exitIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH)));
        exitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        exitButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        exitButton.setMinimumSize(new Dimension(buttonSize, buttonSize));
        exitButton.setToolTipText("Закрыть");

        // Добавляем кнопки на панель с отступами
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(submitButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createHorizontalStrut(5));

        // Создаём панель контента с GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.LIGHT_GRAY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Создаём обёрточную панель для контента
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(Color.DARK_GRAY);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Настраиваем размещение контента в обёрточной панели
        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0;
        wrapperGbc.gridy = 0;
        wrapperGbc.weightx = 1.0;
        wrapperGbc.weighty = 1.0;
        wrapperGbc.fill = GridBagConstraints.BOTH;
        wrapperPanel.add(contentPanel, wrapperGbc);

        // Настраиваем параметры компоновки для контента
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Настраиваем шрифты для меток и полей
        Font labelFont = new Font("Arial", Font.PLAIN, 18);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Добавляем вертикальный отступ
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        contentPanel.add(Box.createVerticalStrut(10), gbc);

        // Добавляем метку для даты начала
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel startLabel = new JLabel("Дата начала (обязательно):");
        startLabel.setFont(labelFont);
        contentPanel.add(startLabel, gbc);

        // Создаём поле для даты начала с плейсхолдером
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        startDateField = new JTextField(START_PLACEHOLDER);
        startDateField.setFont(fieldFont);
        startDateField.setForeground(Color.GRAY);
        startDateField.setPreferredSize(new Dimension(0, 40));
        startDateField.setMinimumSize(new Dimension(0, 40));
        startDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Создаём кнопку календаря для даты начала
        startCalendarButton = new JButton(new ImageIcon(new ImageIcon("pictures/calendar_icon.png")
                .getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH)));
        startCalendarButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        startCalendarButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        startCalendarButton.setMinimumSize(new Dimension(buttonSize, buttonSize));

        // Создаём панель для поля и кнопки даты начала
        JPanel startInputPanel = new JPanel(new GridBagLayout());
        startInputPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints inputGbc = new GridBagConstraints();
        inputGbc.gridx = 0;
        inputGbc.weightx = 1.0;
        inputGbc.fill = GridBagConstraints.HORIZONTAL;
        inputGbc.insets = new Insets(0, 0, 0, 5);
        startInputPanel.add(startDateField, inputGbc);

        inputGbc.gridx = 1;
        inputGbc.weightx = 0.0;
        inputGbc.fill = GridBagConstraints.NONE;
        inputGbc.insets = new Insets(0, 0, 0, 0);
        startInputPanel.add(startCalendarButton, inputGbc);

        contentPanel.add(startInputPanel, gbc);

        // Добавляем метку для даты окончания
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel endLabel = new JLabel("Дата окончания:");
        endLabel.setFont(labelFont);
        contentPanel.add(endLabel, gbc);

        // Создаём поле для даты окончания с плейсхолдером
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        endDateField = new JTextField(END_PLACEHOLDER);
        endDateField.setFont(fieldFont);
        endDateField.setForeground(Color.GRAY);
        endDateField.setPreferredSize(new Dimension(0, 40));
        endDateField.setMinimumSize(new Dimension(0, 40));
        endDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Создаём кнопку календаря для даты окончания
        endCalendarButton = new JButton(new ImageIcon(new ImageIcon("pictures/calendar_icon.png")
                .getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH)));
        endCalendarButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        endCalendarButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        endCalendarButton.setMinimumSize(new Dimension(buttonSize, buttonSize));

        // Создаём панель для поля и кнопки даты окончания
        JPanel endInputPanel = new JPanel(new GridBagLayout());
        endInputPanel.setBackground(Color.LIGHT_GRAY);
        inputGbc.gridx = 0;
        inputGbc.gridy = 0;
        inputGbc.weightx = 1.0;
        inputGbc.fill = GridBagConstraints.HORIZONTAL;
        inputGbc.insets = new Insets(0, 0, 0, 5);
        endInputPanel.add(endDateField, inputGbc);

        inputGbc.gridx = 1;
        inputGbc.weightx = 0.0;
        inputGbc.fill = GridBagConstraints.NONE;
        inputGbc.insets = new Insets(0, 0, 0, 0);
        endInputPanel.add(endCalendarButton, inputGbc);

        contentPanel.add(endInputPanel, gbc);

        // Добавляем метку для сообщения
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel textLabel = new JLabel("Сообщение:");
        textLabel.setFont(labelFont);
        contentPanel.add(textLabel, gbc);

        // Создаём поле для сообщения с плейсхолдером
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        textInputField = new JTextField(TEXT_PLACEHOLDER);
        textInputField.setFont(fieldFont);
        textInputField.setForeground(Color.GRAY);
        textInputField.setPreferredSize(new Dimension(0, 40));
        textInputField.setMinimumSize(new Dimension(0, 40));
        textInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        // Применяем ограничение на длину сообщения
        applyTextLimit(textInputField, 65, TEXT_PLACEHOLDER);
        contentPanel.add(textInputField, gbc);

        // Добавляем пустое пространство снизу
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(Box.createVerticalGlue(), gbc);

        // Добавляем панели в основную панель
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(wrapperPanel, BorderLayout.CENTER);

        // Добавляем основную панель в диалоговое окно
        dialog.add(panel);

        // Отключаем фокус для полей по умолчанию
        startDateField.setFocusable(false);
        endDateField.setFocusable(false);
        textInputField.setFocusable(false);

        // Включаем фокус при клике мышью для поля даты начала
        startDateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startDateField.setFocusable(true);
                startDateField.requestFocusInWindow();
            }
        });
        // Включаем фокус при клике мышью для поля даты окончания
        endDateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                endDateField.setFocusable(true);
                endDateField.requestFocusInWindow();
            }
        });
        // Включаем фокус при клике мышью для поля сообщения
        textInputField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textInputField.setFocusable(true);
                textInputField.requestFocusInWindow();
            }
        });

        // Настраиваем плейсхолдеры для полей
        setupPlaceholder(startDateField, START_PLACEHOLDER);
        setupPlaceholder(endDateField, END_PLACEHOLDER);
        setupPlaceholder(textInputField, TEXT_PLACEHOLDER);

        // Обработчик кнопки календаря для даты начала
        startCalendarButton.addActionListener(e -> showCalendar(startDateField, dialog));
        // Обработчик кнопки календаря для даты окончания
        endCalendarButton.addActionListener(e -> showCalendar(endDateField, dialog));
        // Обработчик кнопки подтверждения
        submitButton.addActionListener(e -> {
            try {
                processInput(dialog);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        // Обработчик кнопки очистки
        clearButton.addActionListener(e -> {
            startDateField.setText(START_PLACEHOLDER);
            startDateField.setForeground(Color.GRAY);
            endDateField.setText(END_PLACEHOLDER);
            endDateField.setForeground(Color.GRAY);
            textInputField.setText(TEXT_PLACEHOLDER);
            textInputField.setForeground(Color.GRAY);
        });
        // Обработчик кнопки закрытия
        exitButton.addActionListener(e -> dialog.dispose());

        // Устанавливаем фокус на кнопку подтверждения
        SwingUtilities.invokeLater(() -> submitButton.requestFocusInWindow());
    }

    // Метод getPanel возвращает основную панель интерфейса
    // Используется для доступа к панели из внешнего кода
    public JPanel getPanel() {
        return panel;
    }

    // Метод applyTextLimit ограничивает максимальную длину текста в текстовом поле
    private void applyTextLimit(JTextField textField, int limit, String placeholder) {
        // Устанавливаем документ с ограничением длины текста
        textField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                // Проверяем, не превышает ли новая длина текста установленный лимит
                if ((getLength() + str.length()) <= limit) {
                    super.insertString(offset, str, attr);
                }
            }
        });

        // Устанавливаем плейсхолдер и серый цвет текста
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        // Настраиваем обработку событий фокуса
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // При получении фокуса очищаем поле, если там плейсхолдер
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // При потере фокуса восстанавливаем плейсхолдер, если поле пустое
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
    }

    // Метод setupPlaceholder настраивает плейсхолдер для текстового поля
    // Очищает поле при получении фокуса, если там плейсхолдер
    // Восстанавливает плейсхолдер при потере фокуса, если поле пустое
    private void setupPlaceholder(JTextField field, String placeholder) {
        // Настраиваем обработку событий фокуса
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // При получении фокуса очищаем поле, если там плейсхолдер
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // При потере фокуса восстанавливаем плейсхолдер, если поле пустое
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    // Метод showCalendar отображает диалоговое окно с календарем для выбора даты и времени
    private void showCalendar(JTextField targetField, JDialog parentDialog) {
        // Создаём модальное диалоговое окно
        JDialog dialog = new JDialog(parentDialog, "Выберите дату и время", true);
        dialog.setResizable(false);
        dialog.setSize(300, 350);

        // Создаём календарь
        JCalendar calendar = new JCalendar();
        calendar.setTodayButtonVisible(false);
        calendar.setPreferredSize(new Dimension(280, 200));

        // Создаём спиннер для выбора времени
        SpinnerDateModel timeModel = new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.HOUR_OF_DAY);
        JSpinner timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new java.util.Date());

        // Создаём кнопки
        JButton todayButton = new JButton("Текущая дата");
        JButton okButton = new JButton("ОК");

        // Обработчик для кнопки текущей даты
        todayButton.addActionListener(e -> {
            java.util.Date now = new java.util.Date();
            // Устанавливаем текущую дату и время в календарь и спиннер
            calendar.setDate(now);
            timeSpinner.setValue(now);
        });

        // Обработчик для кнопки подтверждения
        okButton.addActionListener(e -> {
            // Получаем выбранные дату и время
            java.util.Date selectedDate = calendar.getDate();
            java.util.Date selectedTime = (java.util.Date) timeSpinner.getValue();

            // Форматируем дату и время в нужный формат
            ZonedDateTime zonedDateTime = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .withHour(selectedTime.getHours())
                    .withMinute(selectedTime.getMinutes())
                    .withSecond(0)
                    .withNano(0);

            String formattedDate = zonedDateTime.format(DISPLAY_FORMATTER);
            // Устанавливаем отформатированную дату в текстовое поле
            targetField.setText(formattedDate);
            targetField.setForeground(Color.BLACK);
            // Закрываем диалоговое окно
            dialog.dispose();
        });

        // Создаём панель для кнопок и спиннера
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(todayButton);
        southPanel.add(new JLabel("Время:"));
        southPanel.add(timeSpinner);
        southPanel.add(okButton);

        // Настраиваем компоновку диалогового окна
        dialog.setLayout(new BorderLayout());
        dialog.add(calendar, BorderLayout.CENTER);
        dialog.add(southPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(parentDialog);
        // Отображаем диалоговое окно
        dialog.setVisible(true);
    }

    // Метод processInput обрабатывает введённые пользователем данные и отправляет их на сервер
    private void processInput(JDialog dialog) throws Exception {
        // Получаем введённые данные
        String startInput = startDateField.getText().trim();
        String endInput = endDateField.getText().trim();
        String textInput = textInputField.getText().trim();

        // Проверяем, не является ли начальная дата плейсхолдером
        if (startInput.equals(START_PLACEHOLDER)) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(dialog,
                            "Начальная дата не должна быть пустой!",
                            "Информация",
                            JOptionPane.INFORMATION_MESSAGE));
            return;
        }

        // Парсим начальную дату
        Instant startInstant = parseInput(startInput);
        if (startInstant == null) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(dialog,
                            "Неверный формат начальной даты или времени!",
                            "Информация",
                            JOptionPane.INFORMATION_MESSAGE));
            return;
        }

        // Парсим конечную дату, если она указана
        Instant endInstant = null;
        if (!endInput.equals(END_PLACEHOLDER) && !endInput.isEmpty()) {
            endInstant = parseInput(endInput);
            if (endInstant == null) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog,
                                "Неверный формат конечной даты или времени!",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE));
                return;
            }

            // Проверяем, что начальная дата раньше конечной
            if (!startInstant.isBefore(endInstant)) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog,
                                "Начальная дата должна быть раньше конечной!",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE));
                return;
            }
        }

        // Получаем данные шифрования из базы данных
        String[] dateKey = database.getSessionAndEncryptionData(ipContact, accaunt_id);
        // Расшифровываем ключ
        String key = encryption.chaha20Decrypt(keyAc, dateKey[0]);

        // Форматируем и шифруем начальную дату
        String startIso = ISO_FORMATTER.format(startInstant);
        String encryptedStart = encryption.chaha20Encript(key, startIso);

        // Проверяем, заполнены ли конечная дата и сообщение
        boolean isEndDateFilled = (endInstant != null);
        boolean isTextFilled = !textInput.equals(TEXT_PLACEHOLDER) && !textInput.isEmpty();

        // Отправляем данные на сервер в зависимости от заполненных полей
        if (!isEndDateFilled && !isTextFilled) {
            // Отправляем только начальную дату
            out.writeObject("CHAT_GET_1 " + ipContact + " " + myID + " " + encryptedStart);
        } else if (isEndDateFilled && !isTextFilled) {
            // Отправляем начальную и конечную даты
            String endIso = ISO_FORMATTER.format(endInstant);
            String encryptedEnd = encryption.chaha20Encript(key, endIso);
            out.writeObject("CHAT_GET_1_2 " + ipContact + " " + myID + " " + encryptedStart + " " + encryptedEnd);
        } else if (!isEndDateFilled && isTextFilled) {
            // Отправляем начальную дату и сообщение
            String encryptedText = encryption.chaha20Encript(key, textInput);
            out.writeObject("CHAT_GET_1_3 " + ipContact + " " + myID + " " + encryptedStart + " " + encryptedText);
        } else {
            // Отправляем начальную дату, конечную дату и сообщение
            String endIso = ISO_FORMATTER.format(endInstant);
            String encryptedEnd = encryption.chaha20Encript(key, endIso);
            String encryptedText = encryption.chaha20Encript(key, textInput);
            out.writeObject("CHAT_GET_1_2_3 " + ipContact + " " + myID + " " + encryptedStart + " " + encryptedEnd + " " + encryptedText);
        }

        // Закрываем диалоговое окно
        dialog.dispose();
    }

    // Метод parseInput преобразует введённую строку даты в объект Instant
    // Пытается разобрать строку в формате dd.MM.yyyy HH:mm
    // Возвращает null при неверном формате даты
    private Instant parseInput(String input) {
        try {
            // Парсим строку даты и преобразуем в Instant
            ZonedDateTime zonedDateTime = LocalDateTime.parse(input, DISPLAY_FORMATTER)
                    .atZone(ZoneId.systemDefault());
            return zonedDateTime.toInstant();
        } catch (DateTimeParseException e) {
            // Возвращаем null при ошибке парсинга
            return null;
        }
    }
}
