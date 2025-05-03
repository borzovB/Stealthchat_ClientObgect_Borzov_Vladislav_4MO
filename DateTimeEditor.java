package org.face_recognition;

// Класс DateTimeEditor предоставляет интерфейс для выбора временного интервала
// с использованием спиннеров для секунд, минут, часов, дней, месяцев и лет,
// а также сохраняет результат в базе данных в зашифрованном виде
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeEditor extends JPanel {
    // Спиннеры для выбора времени
    private JSpinner secondsSpinner, minutesSpinner, hoursSpinner, daysSpinner, monthsSpinner, yearsSpinner;
    // Текстовое поле для отображения результата
    private JTextField dateTimeField;
    // Ключ шифрования учетной записи
    private String keyAc;
    // Экземпляр для шифрования данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Экземпляр базы данных
    private static Database database = new Database();
    // Идентификатор учетной записи
    private static String accauntId;

    // Конструктор, инициализирующий компоненты и параметры
    public DateTimeEditor(JDialog parentDialog, JTextField dateTimeField, String keyAc, String accauntId) {
        this.dateTimeField = dateTimeField;
        this.keyAc = keyAc;
        this.accauntId = accauntId;
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(600, 300));
        setBackground(new Color(240, 240, 245));

        // Панель для спиннеров
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 240, 245));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.PLAIN, 18);
        Font spinnerFont = new Font("Arial", Font.PLAIN, 16);

        // Добавляет спиннеры
        addSpinner(0, 0, "Секунды:", 0, 59, inputPanel, gbc, labelFont, spinnerFont, secondsSpinner = new JSpinner());
        addSpinner(0, 1, "Минуты:", 0, 59, inputPanel, gbc, labelFont, spinnerFont, minutesSpinner = new JSpinner());
        addSpinner(0, 2, "Часы:", 0, 23, inputPanel, gbc, labelFont, spinnerFont, hoursSpinner = new JSpinner());
        addSpinner(1, 0, "Дни:", 0, 30, inputPanel, gbc, labelFont, spinnerFont, daysSpinner = new JSpinner());
        addSpinner(1, 1, "Месяцы:", 0, 11, inputPanel, gbc, labelFont, spinnerFont, monthsSpinner = new JSpinner());
        addSpinner(1, 2, "Годы:", 0, Integer.MAX_VALUE, inputPanel, gbc, labelFont, spinnerFont, yearsSpinner = new JSpinner());

        // Добавляет обработчики изменений для автоматического переноса значений
        addChangeListener(secondsSpinner, minutesSpinner, 60);
        addChangeListener(minutesSpinner, hoursSpinner, 60);
        addChangeListener(hoursSpinner, daysSpinner, 24);
        addChangeListener(daysSpinner, monthsSpinner, 31);
        addChangeListener(monthsSpinner, yearsSpinner, 12);

        // Панель для кнопки "Готово"
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JButton doneButton = new JButton("Готово");
        doneButton.setFont(new Font("Arial", Font.BOLD, 18));
        doneButton.setBackground(new Color(70, 130, 180));
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        doneButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                if (b.getModel().isPressed()) {
                    g.setColor(new Color(50, 110, 160));
                } else {
                    g.setColor(new Color(70, 130, 180));
                }
                g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                super.paint(g, c);
            }
        });
        buttonPanel.add(doneButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обрабатывает нажатие кнопки "Готово"
        doneButton.addActionListener(e -> {
            calculateAndSaveResult();
            if (parentDialog != null) {
                parentDialog.dispose();
            }
        });
    }

    // Добавляет спиннер с меткой на панель
    private void addSpinner(int row, int col, String labelText, int min, int max, JPanel panel, GridBagConstraints gbc, Font labelFont, Font spinnerFont, JSpinner spinner) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(new Color(50, 50, 50));
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        SpinnerNumberModel model = new SpinnerNumberModel(0, min, max, 1);
        spinner.setModel(model);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setEditable(false);
            textField.setFont(spinnerFont);
            textField.setPreferredSize(new Dimension(70, 35));
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBackground(Color.WHITE);
            textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }
        spinner.setFont(spinnerFont);
        spinner.setPreferredSize(new Dimension(70, 35));
        panel.add(spinner, gbc);
    }

    // Добавляет обработчик изменений для спиннера
    private void addChangeListener(JSpinner currentSpinner, JSpinner nextSpinner, int max) {
        currentSpinner.addChangeListener(e -> {
            int value = (int) currentSpinner.getValue();
            if (value >= max) {
                currentSpinner.setValue(0);
                if (nextSpinner != null) {
                    int nextValue = (int) nextSpinner.getValue();
                    nextSpinner.setValue(nextValue + 1);
                }
            } else if (value < 0) {
                currentSpinner.setValue(0);
            }
        });
    }

    // Вычисляет и сохраняет результат в базе данных
    private void calculateAndSaveResult() {
        try {
            int seconds = (int) secondsSpinner.getValue();
            int minutes = (int) minutesSpinner.getValue();
            int hours = (int) hoursSpinner.getValue();
            int days = (int) daysSpinner.getValue();
            int months = (int) monthsSpinner.getValue();
            int years = (int) yearsSpinner.getValue();

            // Устанавливает значение по умолчанию (7 дней), если все поля равны 0
            if (seconds == 0 && minutes == 0 && hours == 0 && days == 0 && months == 0 && years == 0) {
                days = 7;
            }

            String gap = seconds + " " + minutes + " " + hours + " " + days + " " + months + " " + years;

            // Вычисляет результирующее время
            Instant currentUTC = database.getCurrentTimeUTC();
            ZonedDateTime currentZoned = currentUTC.atZone(ZoneId.systemDefault());
            ZonedDateTime resultZoned = currentZoned
                    .plusYears(years)
                    .plusMonths(months)
                    .plusDays(days)
                    .plusHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);

            Instant resultUTC = resultZoned.toInstant();
            String data = resultUTC.toString();
            // Сохраняет зашифрованные данные в базе
            database.saveNotificationTimeToDB(encryption.chaha20Encript(keyAc, data), encryption.chaha20Encript(keyAc, gap), accauntId);

            // Форматирует и отображает результат
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedResult = resultZoned.format(formatter);
            dateTimeField.setText(formattedResult);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
