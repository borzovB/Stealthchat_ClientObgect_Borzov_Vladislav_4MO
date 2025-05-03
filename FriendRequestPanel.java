package org.face_recognition;

// Класс FriendRequestPanel отвечает за создание и управление панелью интерфейса для обработки заявок в друзья
// Он предоставляет визуальное представление заявки, включая информацию об отправителе, статус запроса,
// возможность блокировки отправителя и отправку ответа на сервер с использованием шифрования
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.ObjectOutputStream;
import java.security.PublicKey;

public class FriendRequestPanel {
    // Статус принятия заявки в друзья
    private boolean requestStatus;
    // Статус уведомления (отправлено или нет)
    private boolean notificationStatus;
    // Флаг блокировки отправителя
    private boolean lockFlag;
    // Идентификатор отправителя заявки
    private String senderId;
    // Чекбокс для статуса принятия заявки
    private JCheckBox requestStatusCheckBox;
    // Чекбокс для блокировки отправителя
    private JCheckBox lockFlagCheckBox;
    // Кнопка для отправки ответа на сервер
    private JButton sendButton;
    // Поток для отправки данных на сервер
    private ObjectOutputStream out;
    // Уникальный идентификатор записи заявки
    private String recordId;
    // Публичный ключ отправителя для шифрования
    private String publicKey;
    // Идентификатор записи друга
    private String record_ac_id_friend;
    // Идентификатор данных записи
    private String record_ac_id_data;
    // Экземпляр базы данных для выполнения запросов
    private static Database database = new Database();
    // Экземпляр класса для работы с ключами и шифрованием
    private static KeyGet keyGet = new KeyGet();
    // Идентификатор текущего пользователя
    private static String myID;
    // Главное окно приложения
    private static JFrame frame;
    // Ключ шифрования учетной записи
    private static String keyAc;
    // Идентификатор учетной записи
    private static String accauntId;
    // Экземпляр класса для генерации уникальных идентификаторов
    private static Safety safety = new Safety();
    // Слушатель событий мыши для контекстного меню
    private static MouseAdapter popupListener;
    // Экземпляр класса для шифрования данных учетной записи
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();

    // Конструктор, инициализирующий все поля класса
    public FriendRequestPanel(String senderId, boolean requestStatus, boolean notificationStatus,
                              boolean lockFlag, ObjectOutputStream out, String recordId,
                              String publicKey, String record_ac_id_friend, String record_ac_id_date,
                              String myID, JFrame frame, String keyAc, String accauntId, MouseAdapter popupListener) {
        this.senderId = senderId;
        this.requestStatus = requestStatus;
        this.notificationStatus = notificationStatus;
        this.lockFlag = lockFlag;
        this.out = out;
        this.recordId = recordId;
        this.publicKey = publicKey;
        this.record_ac_id_friend = record_ac_id_friend;
        this.record_ac_id_data = record_ac_id_date;
        this.myID = myID;
        this.frame = frame;
        this.keyAc = keyAc;
        this.accauntId = accauntId;
        this.popupListener = popupListener;
    }

    // Создает и возвращает панель для отображения заявки в друзья
    public JPanel createPanel() {
        // Главная панель с фиксированным размером и отступами
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));
        Dimension panelSize = new Dimension(500, 230);
        panel.setPreferredSize(panelSize);
        panel.setMinimumSize(panelSize);
        panel.setMaximumSize(panelSize);
        panel.addMouseListener(popupListener);

        // Левая панель для заголовка и чекбоксов
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(new Color(245, 245, 245));
        Dimension leftPanelSize = new Dimension(340, 210);
        leftPanel.setPreferredSize(leftPanelSize);
        leftPanel.setMinimumSize(leftPanelSize);
        leftPanel.setMaximumSize(leftPanelSize);
        leftPanel.addMouseListener(popupListener);

        // Панель заголовка с информацией об отправителе
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.addMouseListener(popupListener);
        headerPanel.setBackground(new Color(245, 245, 245));
        Dimension headerSize = new Dimension(320, 50);
        headerPanel.setPreferredSize(headerSize);
        headerPanel.setMinimumSize(headerSize);
        headerPanel.setMaximumSize(headerSize);

        // Метка с текстом "Получена заявка в друзья от"
        JLabel headerLabel = new JLabel("Получена заявка в друзья от");
        headerLabel.addMouseListener(popupListener);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(new Color(33, 150, 243));
        headerPanel.add(headerLabel);

        // Метка с идентификатором отправителя
        JLabel senderIdLabel = new JLabel(senderId);
        senderIdLabel.addMouseListener(popupListener);
        senderIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        senderIdLabel.setForeground(new Color(66, 66, 66));
        headerPanel.add(senderIdLabel);

        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // Панель для чекбоксов (статус заявки и блокировка)
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setBackground(new Color(245, 245, 245));
        Dimension checkBoxSize = new Dimension(320, 140);
        checkBoxPanel.setPreferredSize(checkBoxSize);
        checkBoxPanel.setMinimumSize(checkBoxSize);
        checkBoxPanel.setMaximumSize(checkBoxSize);
        checkBoxPanel.addMouseListener(popupListener);

        // Чекбокс для принятия заявки
        requestStatusCheckBox = new JCheckBox("Заявка принята");
        requestStatusCheckBox.setSelected(requestStatus);
        requestStatusCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
        requestStatusCheckBox.setForeground(new Color(33, 150, 243));
        requestStatusCheckBox.addActionListener(e -> {
            this.requestStatus = requestStatusCheckBox.isSelected(); // Обновляет статус заявки
        });
        checkBoxPanel.add(requestStatusCheckBox);
        checkBoxPanel.add(Box.createVerticalStrut(10));

        // Чекбокс для блокировки отправителя
        lockFlagCheckBox = new JCheckBox("Отправитель заблокирован");
        lockFlagCheckBox.setSelected(lockFlag);
        lockFlagCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
        lockFlagCheckBox.setForeground(new Color(33, 150, 243));
        lockFlagCheckBox.addActionListener(e -> {
            this.lockFlag = lockFlagCheckBox.isSelected(); // Обновляет флаг блокировки
        });
        checkBoxPanel.add(lockFlagCheckBox);

        leftPanel.add(checkBoxPanel, BorderLayout.CENTER);

        // Правая панель для кнопки отправки
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 70));
        rightPanel.setBackground(new Color(245, 245, 245));
        Dimension rightPanelSize = new Dimension(120, 210);
        rightPanel.setPreferredSize(rightPanelSize);
        rightPanel.setMinimumSize(rightPanelSize);
        rightPanel.setMaximumSize(rightPanelSize);

        // Кнопка для отправки ответа на сервер
        sendButton = new JButton("Отправить");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(33, 150, 243));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.setMinimumSize(new Dimension(100, 30));
        sendButton.setMaximumSize(new Dimension(100, 30));

        // Обработчик действия кнопки отправки
        sendButton.addActionListener(e -> {
            this.notificationStatus = true; // Устанавливает статус уведомления
            updateSendButtonVisibility(); // Обновляет видимость кнопки
            updatePanelState(); // Обновляет состояние панели

            // Обновляет запись в базе данных
            database.updateRequestResponse(recordId, this.requestStatus, this.notificationStatus, this.lockFlag);

            try {
                // Декодирует публичный ключ отправителя
                PublicKey restoredPublicKey = keyGet.decodePublicKeyFromString(publicKey);

                // Генерирует случайный алгоритм и ключ
                String alg = EncryptionAccaunt.getRandomString();
                String secretKeyStr = switch (alg) {
                    case "1a" -> KeyGet.generationKeyAES();
                    case "1b" -> KeyGet.generationKeyTwoFishAndSerpent("Twofish", "BC");
                    case "1c" -> KeyGet.generationKeyTwoFishAndSerpent("Serpent", "BC");
                    default -> throw new IllegalStateException("Unexpected algorithm: " + alg);
                };

                // Шифрует ключ с использованием публичного ключа
                String encryptedText = keyGet.encrypt(secretKeyStr, restoredPublicKey);

                if (this.requestStatus) {
                    // Если заявка принята
                    if (database.isContactExists(senderId, accauntId)) {
                        // Если контакт уже существует, обновляет сессию
                        out.writeObject("ANSWER_FRIENDS_SERVER " + myID + " " + senderId + " " + encryptedText +
                                " " + record_ac_id_friend + " " + record_ac_id_data + " " + requestStatus + " " + lockFlag);

                        database.updateChatSession(senderId, accauntId, encryption.chaha20Encript(keyAc, secretKeyStr));

                        if (lockFlag) {
                            // Добавляет отправителя в список заблокированных
                            database.fillClientBlock(safety.generateUniqueId(), accauntId, senderId);
                        }
                    } else {
                        // Если контакт новый, открывает диалог для ввода данных
                        SwingWorker<String[], Void> worker = new SwingWorker<>() {
                            @Override
                            protected String[] doInBackground() throws Exception {
                                RegistretionEdit registretionEdit = new RegistretionEdit(frame, keyAc);
                                return registretionEdit.dialogPanelReceived(myID, senderId);
                            }

                            @Override
                            protected void done() {
                                try {
                                    String[] dateFriend = get(); // Получает данные из диалога
                                    if (dateFriend[0] != null) {
                                        // Отправляет ответ на сервер
                                        out.writeObject("ANSWER_FRIENDS_SERVER " + myID + " " + senderId + " " +
                                                encryptedText + " " + record_ac_id_friend + " " +
                                                record_ac_id_data + " " + requestStatus +
                                                " " + lockFlag);

                                        // Добавляет новый контакт
                                        MainPage.reloadContacts(senderId, dateFriend[0], dateFriend[1], secretKeyStr, true);

                                        if (lockFlag) {
                                            // Добавляет отправителя в список заблокированных
                                            database.fillClientBlock(safety.generateUniqueId(), accauntId, senderId);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        worker.execute(); // Запускает фоновую задачу
                    }
                } else {
                    // Если заявка отклонена, отправляет ответ на сервер
                    out.writeObject("ANSWER_FRIENDS_SERVER " + myID + " " + senderId + " " + encryptedText + " " +
                            record_ac_id_friend + " " + record_ac_id_data + " " + requestStatus +
                            " " + lockFlag);

                    if (lockFlag) {
                        // Добавляет отправителя в список заблокированных
                        database.fillClientBlock(safety.generateUniqueId(), accauntId, senderId);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        rightPanel.addMouseListener(popupListener);
        leftPanel.addMouseListener(popupListener);

        rightPanel.add(sendButton);

        // Добавляет левую и правую панели в главную панель
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        updateSendButtonVisibility(); // Обновляет видимость кнопки
        updatePanelState(); // Обновляет состояние панели

        return panel;
    }

    // Управляет видимостью кнопки отправки в зависимости от статуса уведомления
    private void updateSendButtonVisibility() {
        sendButton.setVisible(!notificationStatus);
    }

    // Обновляет состояние элементов панели (активность чекбоксов и кнопки)
    private void updatePanelState() {
        boolean isEditable = !notificationStatus;
        requestStatusCheckBox.setEnabled(isEditable);
        lockFlagCheckBox.setEnabled(isEditable);
        sendButton.setEnabled(isEditable);
    }
}
