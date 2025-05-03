package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectOutputStream;

// Класс отвечает за отображение панели смены кодового слова (пароля) учетной записи
public class PanelChangingCodeWord {

    // Панель для ввода пароля
    private static JPanel textPanel1;

    // Основное окно приложения
    private static JFrame frame;

    // Поток для отправки данных
    private static ObjectOutputStream out;

    // ID аккаунта, для которого меняется кодовое слово
    private static String accauntId;

    // ID пользователя
    private static String myID;

    // Ключ шифрования
    private static String keyAc;

    // Флаг для отслеживания, было ли окно закрыто пользователем
    private static boolean closes = true;

    // Объект для шифрования
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();

    // Объект для работы с базой данных
    private static Database database = new Database();

    // Конструктор получает нужные параметры для выполнения смены кодового слова
    public PanelChangingCodeWord(JFrame frame, ObjectOutputStream out,
                                 String keyAc, String accauntId, String myID) {
        this.frame = frame;
        this.out = out;
        this.accauntId = accauntId;
        this.myID = myID;
        this.keyAc = keyAc;
    }

    // Метод запуска панели смены кодового слова
    public static void startDevPassword(JDialog dialog) {

        // Основная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setOpaque(true);
        mainPanel.add(Box.createVerticalGlue());

        // Внутренняя панель с элементами интерфейса
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));
        panel.setPreferredSize(new Dimension(520, 350));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Заголовок
        JLabel label = new JLabel("Изменить кодовое слово");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 31));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Панель управления полем пароля
        ControlPanel controlPanel = new ControlPanel();

        // Поле для ввода нового кодового слова
        JPasswordField passwordField2 = new JPasswordField();
        passwordField2.setPreferredSize(new Dimension(250, 40));
        passwordField2.setFont(new Font("Arial", Font.PLAIN, 16));
        controlPanel.configurePasswordField(passwordField2);

        // Кнопка "показать/скрыть пароль"
        JButton showPasswordButton2 = new JButton();
        textTime(passwordField2, showPasswordButton2);

        // Панель с поясняющим текстом
        JPanel label1Panel = new JPanel();
        label1Panel.setLayout(new BorderLayout());
        label1Panel.setBackground(new Color(60, 63, 65));
        label1Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel label1 = new JLabel("Введите новое кодовое слово");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("Arial", Font.PLAIN, 17));
        label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label1Panel.add(label1);

        JPanel label2Panel = new JPanel();
        label2Panel.setLayout(new BorderLayout());
        label2Panel.setBackground(new Color(60, 63, 65));
        label2Panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Панель с кнопкой и полем ввода
        textPanel1 = new JPanel();
        textPanel1.setLayout(new BoxLayout(textPanel1, BoxLayout.X_AXIS));
        textPanel1.add(showPasswordButton2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel1.add(passwordField2);
        textPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

        // Добавляем панели в интерфейс
        panel.add(label1Panel);
        panel.add(label2Panel);
        panel.add(textPanel1);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Кнопка "Готово"
        JButton entrance = new JButton("Готово");
        customizeButton(entrance);

        // Обработка нажатия кнопки "Готово"
        entrance.addActionListener(e -> {
            String password1 = new String(passwordField2.getPassword());
            System.out.println(password1);

            // Проверка: пустое ли поле
            if (password1.isEmpty()) {
                String message = "Поле для ввода нового кодового слова пустое";
                JOptionPane.showMessageDialog(dialog, message, "Проверка полей", JOptionPane.INFORMATION_MESSAGE);
            } else {
                try {
                    // Хэшируем и шифруем новое кодовое слово
                    String passphraseNew = encryption.Argon2(password1);
                    String account_keyNew = encryption.chaha20Encript(password1, keyAc);

                    // Обновляем данные в базе
                    database.updateAccountCredentials(accauntId, account_keyNew, passphraseNew);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                // Переход к следующей панели после успешного изменения
                JDialog dialogIncomplete = new JDialog(frame, "Безопасность", true);
                dialogIncomplete.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialogIncomplete.setSize(520, 526);
                dialogIncomplete.setResizable(false);
                dialogIncomplete.setLocationRelativeTo(null);

                closes = false;
                dialog.dispose();

                DecryptionPanelComplete decryptionPanelComplete =
                        new DecryptionPanelComplete(frame, accauntId, myID, keyAc, out);
                decryptionPanelComplete.startDevPassword(dialogIncomplete);
            }
        });

        panel.add(entrance);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(Box.createRigidArea(new Dimension(0, 80)));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));
        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        // Слушатель закрытия окна — если пользователь просто закрыл, возвращаем на предыдущую панель
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (closes) {
                    JDialog dialogIncomplete = new JDialog(frame, "Безопасность", true);
                    dialogIncomplete.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialogIncomplete.setSize(520, 526);
                    dialogIncomplete.setResizable(false);
                    dialogIncomplete.setLocationRelativeTo(null);

                    DecryptionPanelComplete decryptionPanelComplete =
                            new DecryptionPanelComplete(frame, accauntId, myID, keyAc, out);
                    decryptionPanelComplete.startDevPassword(dialogIncomplete);
                }
                closes = true;
            }
        });

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setVisible(true);
    }

    // Метод добавляет возможность временного показа пароля по кнопке
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

        final boolean[] isPasswordVisible = {false};

        // Таймер автоматически скрывает пароль через 10 секунд
        Timer hidePasswordTimer = new Timer(10000, event -> {
            passwordField.setEchoChar('•');
            isPasswordVisible[0] = false;
            showPasswordButton.setIcon(scaledIconNot);
        });

        // Обработчик нажатия на кнопку показа пароля
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

    // Кастомизация внешнего вида кнопки
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 80));
    }
}
