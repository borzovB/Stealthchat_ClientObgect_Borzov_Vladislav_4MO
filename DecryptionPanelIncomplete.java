package org.face_recognition;

// Класс DecryptionPanelIncomplete предоставляет интерфейс для ввода пароля учетной записи
// и запуска процесса перешифровки данных с отображением прогресса
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;

public class DecryptionPanelIncomplete {

    // Панель для текстового поля и кнопки отображения пароля
    private static JPanel textPanel1;
    // Главное окно приложения
    private static JFrame frame;
    // Идентификатор учетной записи
    private static String accauntId;
    // Идентификатор пользователя
    private static String myID;
    // Ключ шифрования учетной записи
    private static String keyAc;
    // Поток для отправки данных
    private static ObjectOutputStream out;
    // Экземпляр базы данных
    private static Database database = new Database();
    // Флаг для управления закрытием диалога
    private static boolean closes = true;

    // Конструктор, инициализирующий параметры
    public DecryptionPanelIncomplete(JFrame frame, String accauntId, String myID, String keyAc, ObjectOutputStream out) {
        this.frame = frame;
        this.accauntId = accauntId;
        this.myID = myID;
        this.keyAc = keyAc;
        this.out = out;
    }

    // Запускает диалоговое окно для ввода пароля и перешифровки
    public static void startDevPassword(JDialog dialogIncomplete) {
        // Создает главную панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);
        mainPanel.add(Box.createVerticalGlue());

        // Создает панель для содержимого
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        panel.setPreferredSize(new Dimension(520, 350));
        panel.setMinimumSize(new Dimension(520, 350));
        panel.setMaximumSize(new Dimension(520, 350));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Добавляет заголовок
        JLabel label = new JLabel("Ввод пароля");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        ControlPanel controlPanel = new ControlPanel();

        // Создает поле для ввода пароля
        JPasswordField passwordField1 = new JPasswordField();
        passwordField1.setPreferredSize(new Dimension(250, 40));
        passwordField1.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField1);

        // Создает кнопку для отображения пароля
        JButton showPasswordButton1 = new JButton();
        textTime(passwordField1, showPasswordButton1);

        // Панель для метки
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel label1 = new JLabel("Введите пароль от учетной записи");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        // Панель для поля ввода и кнопки
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton1);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField1);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

        panel.add(label1Panel);
        panel.add(new JPanel()); // Пустая панель для выравнивания
        panel.add(textPanel1);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Создает кнопку "Готово"
        JButton entrance = new JButton("Готово");
        customizeButton(entrance);
        entrance.addActionListener(e -> {
            String password1 = new String(passwordField1.getPassword());
            String message;

            // Проверяет, заполнено ли поле пароля
            if (password1.isEmpty()) {
                message = "Поле для ввода пароля от учетной записи пустое";
                JOptionPane.showMessageDialog(dialogIncomplete, message, "Проверка полей", JOptionPane.INFORMATION_MESSAGE);
            } else if (database.checkAccountPassword(password1)) {
                try {
                    closes = false;
                    String accauntKeyNew = KeyGet.generationKeyAES();
                    dialogIncomplete.dispose();

                    // Запускает перешифровку в отдельном потоке
                    new Thread(() -> {
                        JDialog dialog = new JDialog(frame, "Перешифровка", true);
                        JProgressBar progressBar = new JProgressBar();
                        progressBar.setIndeterminate(true);
                        progressBar.setStringPainted(true);
                        progressBar.setString("Перешифровка данных...");
                        dialog.add(progressBar, BorderLayout.CENTER);
                        dialog.setSize(300, 100);
                        dialog.setLocationRelativeTo(frame);
                        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

                        try {
                            out.writeObject("EXIT " + myID);
                            out.flush();
                            NotificationDaemon.stop();
                            Thread.sleep(5000);

                            // Выполняет перешифровку
                            ReEncryption encryptionNew = new ReEncryption(accauntKeyNew, keyAc, accauntId, myID);
                            encryptionNew.tableAccountsReEncryption(password1);
                            encryptionNew.reEncryptApplicationResponses();
                            encryptionNew.reEncryptChatPlanning();
                            encryptionNew.tableChats();
                            encryptionNew.tableContactsPlan();
                            encryptionNew.processMessagesAndSessions();

                            // Показывает завершение
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(100);
                                progressBar.setString("Перешифровка завершена");
                            });
                            Thread.sleep(1000);
                            closes = false;
                            MainPage.updateKeyAc(accauntKeyNew);

                        } catch (IOException | InterruptedException ex) {
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setIndeterminate(false);
                                progressBar.setString("Ошибка: " + ex.getMessage());
                                progressBar.setForeground(Color.RED);
                            });
                            throw new RuntimeException(ex);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        } finally {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }
                            SwingUtilities.invokeLater(dialog::dispose);
                        }
                    }).start();
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                message = "Вы ввели неверный пароль от учетной записи";
                JOptionPane.showMessageDialog(dialogIncomplete, message, "Проверка полей", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(Box.createRigidArea(new Dimension(0, 80)));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));

        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        dialogIncomplete.setContentPane(mainPanel);
        dialogIncomplete.pack();

        // Обрабатывает закрытие диалога
        dialogIncomplete.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (closes) {
                    ParametersDialog parametersDialog = new ParametersDialog(accauntId, keyAc, myID, out, frame);
                    parametersDialog.createDialog("Параметры");
                }
                closes = true;
            }
        });

        dialogIncomplete.setVisible(true);
    }

    // Настраивает кнопку отображения пароля
    private static void textTime(JPasswordField passwordField, JButton showPasswordButton) {
        ImageIcon originalIconNot = new ImageIcon("pictures/eye_icon.png");
        ImageIcon originalIcon = new ImageIcon("pictures/eye_icon_not.png");

        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
        Image scaledImgNot = originalIconNot.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIconNot = new ImageIcon(scaledImgNot);

        showPasswordButton.setIcon(scaledIconNot);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder());
        showPasswordButton.setPreferredSize(new Dimension(40, 40));
        showPasswordButton.setMaximumSize(new Dimension(40, 40));
        showPasswordButton.setMinimumSize(new Dimension(40, 40));

        final boolean[] isPasswordVisible = {false};
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•');
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

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

    // Настраивает стиль кнопки
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
