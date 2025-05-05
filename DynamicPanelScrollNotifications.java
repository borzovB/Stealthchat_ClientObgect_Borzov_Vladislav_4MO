package org.face_recognition;

// Класс DynamicPanelScrollNotifications управляет динамической прокруткой панелей с уведомлениями,
// поддерживая различные типы данных (заявки в друзья, ответы, блокировки, планирование чатов)
// Он обеспечивает загрузке панелей при прокрутке, удаление записей и адаптивное изменение размеров
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.List;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeParseException;

public class DynamicPanelScrollNotifications {
    // Константы для минимального количества панелей и зоны буфера прокрутки
    private static final int MAX_PANELS = 10; // Максимальное количество отображаемых панелей
    private static final int BUFFER_ZONE = 20; // Зона в пикселях для активации подгрузки панелей
    // Минимальная и максимальная высота панелей
    private static int panelHeightMin;
    private static int intpanelHeightMax;
    // Главная панель для размещения элементов
    private JPanel mainPanel;
    // Прокручиваемая область для отображения панелей
    private JScrollPane scrollPane;
    // Индексы первой и последней видимых панелей
    private int firstVisibleIndex;
    private int lastVisibleIndex;
    // Флаг, указывающий на процесс загрузки данных
    private boolean isLoading = false;
    // Флаг для начальной загрузки
    private boolean start = true;
    // Текущий цвет фона панелей
    private Color currentBackgroundColor = Color.WHITE;
    // Главное окно приложения
    private static JFrame frame;
    // Экземпляр базы данных для работы с уведомлениями
    private static DatabaseScrollNotif database;
    // Тип уведомлений (1a, 2a, 3a, 4a)
    private static String type;
    // Поток для отправки данных на сервер
    private static ObjectOutputStream out;
    // Идентификатор текущего пользователя
    private static String myID;
    // Ключ шифрования учетной записи
    private static String keyAc;
    // Идентификатор учетной записи
    private static String accauntId;
    // Экземпляр класса для шифрования данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Имя базы данных SQLite
    private static final String DATABASE_NAME = "user_accounts.db";
    // URL для подключения к базе данных
    private static final String DB_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Форматтер для отображения времени в локальном формате
    private static DateTimeFormatter localFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    // Фиксированная ширина панелей
    private static final int FIXED_WIDTH = 600;

    // Конструктор, инициализирующий параметры и настройки панели
    public DynamicPanelScrollNotifications(String ipContact, String type, ObjectOutputStream out, String myID,
                                           JFrame frame, String keyAc, String accauntId) {
        this.type = type;
        this.out = out;
        this.myID = myID;
        this.frame = frame;
        this.keyAc = keyAc;
        this.accauntId = accauntId;

        // Устанавливает высоты панелей в зависимости от типа уведомления
        if (type.equals("4a")) {
            panelHeightMin = 100; // Минимальная высота для типа 4a (блокировки)
            intpanelHeightMax = 100; // Максимальная высота для типа 4a
        } else if (type.equals("3a")) {
            panelHeightMin = 100; // Минимальная высота для типа 3a (планирование чатов)
            intpanelHeightMax = 200; // Максимальная высота для типа 3a
        } else {
            panelHeightMin = 180; // Стандартная высота для остальных типов
            intpanelHeightMax = 180;
        }

        // Инициализирует базу данных
        database = new DatabaseScrollNotif(ipContact, keyAc);
        // Создает главную панель с вертикальным расположением
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(currentBackgroundColor);

        // Создает прокручиваемую область
        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Всегда отображает вертикальную полосу прокрутки
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Отключает горизонтальную прокрутку
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Устанавливает шаг прокрутки
        scrollPane.setPreferredSize(new Dimension(400, 300)); // Устанавливает предпочтительный размер

        // Слушатель изменения размера области просмотра
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = scrollPane.getViewport().getWidth(); // Получает новую ширину
                mainPanel.setPreferredSize(new Dimension(newWidth, mainPanel.getPreferredSize().height));
                // Обновляет размеры существующих панелей
                for (Component comp : mainPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        comp.setPreferredSize(new Dimension(newWidth, comp.getPreferredSize().height));
                        comp.setMaximumSize(new Dimension(newWidth, comp.getPreferredSize().height));
                    }
                }
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        // Слушатель прокрутки для загрузки панелей
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int value = e.getValue(); // Текущая позиция прокрутки
                int extent = e.getAdjustable().getVisibleAmount(); // Размер видимой области
                int max = e.getAdjustable().getMaximum(); // Максимальная позиция прокрутки
                int adjustedBufferZone = BUFFER_ZONE; // Зона активации подгрузки

                // Прокрутка вверх: загрузки панелей при достижении верхней зоны
                if (value <= BUFFER_ZONE && firstVisibleIndex > 0) {
                    isLoading = true;
                    loadPanelUp();
                    scrollPane.getViewport().setViewPosition(new Point(0, BUFFER_ZONE + 10)); // Корректирует позицию
                }

                // Обработка загрузки вниз в зависимости от типа уведомления
                if (type.equals("1a")) {
                    if (value + extent >= max - adjustedBufferZone && lastVisibleIndex < database.getTotalPanelsFriend() - 1) {
                        isLoading = true;
                        loadPanelDown();
                    }
                } else if (type.equals("2a")) {
                    if (value + extent >= max - adjustedBufferZone && lastVisibleIndex < database.getTotalPanelsFriend_1() - 1) {
                        isLoading = true;
                        loadPanelDown();
                    }
                } else if (type.equals("4a")) {
                    if (value + extent >= max - adjustedBufferZone && lastVisibleIndex < database.getTotalPanelsFriend_2() - 1) {
                        isLoading = true;
                        loadPanelDown();
                    }
                } else {
                    if (value + extent >= max - adjustedBufferZone && lastVisibleIndex < database.getTotalPanelsFriend_4() - 1) {
                        isLoading = true;
                        loadPanelDown();
                    }
                }
            }
        });
    }

    // Обновляет размер главной панели на основе высот дочерних панелей
    private void updateMainPanelSize() {
        int newHeight = 0;
        for (Component comp : mainPanel.getComponents()) {
            newHeight += comp.getPreferredSize().height; // Суммирует высоты всех панелей
        }
        mainPanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), newHeight));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Загружает начальный набор панелей
    void loadInitialPanels() {
        mainPanel.removeAll(); // Очищает главную панель

        // Вычисляет количество видимых панелей
        int viewportHeight = Math.max(scrollPane.getViewport().getHeight(), 1);
        int visiblePanels = Math.max(viewportHeight / panelHeightMin, MAX_PANELS);

        // Загружает панели в зависимости от типа уведомления
        if (type.equals("1a")) {
            int totalPanels = database.getTotalPanelsFriend();
            firstVisibleIndex = Math.max(0, totalPanels - visiblePanels);
            lastVisibleIndex = totalPanels - 1;
            List<PanelDataRequests> panels = database.getPanelsFriend(firstVisibleIndex, lastVisibleIndex);
            for (PanelDataRequests data : panels) {
                addPanel(data, false); // Добавляет панели в конец
            }
        } else if (type.equals("2a")) {
            int totalPanels = database.getTotalPanelsFriend_1();
            firstVisibleIndex = Math.max(0, totalPanels - visiblePanels);
            lastVisibleIndex = totalPanels - 1;
            List<RequestsResponses> panels = database.getPanelsFriend_1(firstVisibleIndex, lastVisibleIndex);
            for (RequestsResponses data : panels) {
                addPanel_1(data, false);
            }
        } else if (type.equals("4a")) {
            int totalPanels = database.getTotalPanelsFriend_2();
            firstVisibleIndex = Math.max(0, totalPanels - visiblePanels);
            lastVisibleIndex = totalPanels - 1;
            List<FriendBlockPanel> panels = database.getPanelsFriend_2(firstVisibleIndex, lastVisibleIndex);
            for (FriendBlockPanel data : panels) {
                addPanel_2(data, false);
            }
        } else {
            int totalPanels = database.getTotalPanelsFriend_4();
            firstVisibleIndex = Math.max(0, totalPanels - visiblePanels);
            lastVisibleIndex = totalPanels - 1;
            List<GetChatPlanningPanel> panels = database.getPanelsFriend_4(firstVisibleIndex, lastVisibleIndex);
            for (GetChatPlanningPanel data : panels) {
                addPanel_4(data, false);
            }
        }

        updateMainPanelSize(); // Обновляет размер панели

        // Прокручивает к последней панели
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
            SwingUtilities.invokeLater(() -> verticalBar.setValue(verticalBar.getMaximum()));
        });
    }

    // Подгружает панели вниз при прокрутке
    private void loadPanelDown() {
        // Проверяет, можно ли подгрузить больше панелей
        if (type.equals("1a")) {
            if (lastVisibleIndex >= database.getTotalPanelsFriend() - 1) {
                isLoading = false;
                return;
            }
        } else if (type.equals("2a")) {
            if (lastVisibleIndex >= database.getTotalPanelsFriend_1() - 1) {
                isLoading = false;
                return;
            }
        } else if (type.equals("4a")) {
            if (lastVisibleIndex >= database.getTotalPanelsFriend_2() - 1) {
                isLoading = false;
                return;
            }
        } else {
            if (lastVisibleIndex >= database.getTotalPanelsFriend_4() - 1) {
                isLoading = false;
                return;
            }
        }

        // Подгружает новую панель
        if (type.equals("1a")) {
            int newLastIndex = lastVisibleIndex + 1;
            PanelDataRequests data = database.getPanelFriend(newLastIndex);
            if (data != null) {
                addPanel(data, false);
                removePanel(true); // Удаляет верхнюю панель
                lastVisibleIndex = newLastIndex;
                firstVisibleIndex++;
                // Корректирует позицию прокрутки
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    int newScrollValue = verticalBar.getValue();
                    if (!start) {
                        verticalBar.setValue(newScrollValue - panelHeightMin);
                    }
                    start = false;
                });
            }
        } else if (type.equals("2a")) {
            int newLastIndex = lastVisibleIndex + 1;
            RequestsResponses data = database.getPanelFriend_1(newLastIndex);
            if (data != null) {
                addPanel_1(data, false);
                removePanel(true);
                lastVisibleIndex = newLastIndex;
                firstVisibleIndex++;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    int newScrollValue = verticalBar.getValue();
                    if (!start) {
                        verticalBar.setValue(newScrollValue - panelHeightMin);
                    }
                    start = false;
                });
            }
        } else if (type.equals("4a")) {
            int newLastIndex = lastVisibleIndex + 1;
            FriendBlockPanel data = database.getPanelFriend_2(newLastIndex);
            if (data != null) {
                addPanel_2(data, false);
                removePanel(true);
                lastVisibleIndex = newLastIndex;
                firstVisibleIndex++;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    int newScrollValue = verticalBar.getValue();
                    if (!start) {
                        verticalBar.setValue(newScrollValue - panelHeightMin);
                    }
                    start = false;
                });
            }
        } else {
            int newLastIndex = lastVisibleIndex + 1;
            GetChatPlanningPanel data = database.getPanelFriend_4(newLastIndex);
            if (data != null) {
                addPanel_4(data, false);
                removePanel(true);
                lastVisibleIndex = newLastIndex;
                firstVisibleIndex++;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    int newScrollValue = verticalBar.getValue();
                    if (!start) {
                        verticalBar.setValue(newScrollValue - panelHeightMin);
                    }
                    start = false;
                });
            }
        }

        isLoading = false;
    }

    // Подгружает панели вверх при прокрутке
    private void loadPanelUp() {
        if (firstVisibleIndex <= 0) {
            isLoading = false;
            return;
        }

        // Подгружает новую панель
        if (type.equals("1a")) {
            int newFirstIndex = firstVisibleIndex - 1;
            PanelDataRequests data = database.getPanelFriend(newFirstIndex);
            if (data != null) {
                addPanel(data, true);
                removePanel(false);
                firstVisibleIndex = newFirstIndex;
                lastVisibleIndex--;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getValue() + panelHeightMin);
                });
            }
        } else if (type.equals("2a")) {
            int newFirstIndex = firstVisibleIndex - 1;
            RequestsResponses data = database.getPanelFriend_1(newFirstIndex);
            if (data != null) {
                addPanel_1(data, true);
                removePanel(false);
                firstVisibleIndex = newFirstIndex;
                lastVisibleIndex--;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getValue() + panelHeightMin);
                });
            }
        } else if (type.equals("4a")) {
            int newFirstIndex = firstVisibleIndex - 1;
            FriendBlockPanel data = database.getPanelFriend_2(newFirstIndex);
            if (data != null) {
                addPanel_2(data, true);
                removePanel(false);
                firstVisibleIndex = newFirstIndex;
                lastVisibleIndex--;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getValue() + panelHeightMin);
                });
            }
        } else {
            int newFirstIndex = firstVisibleIndex - 1;
            GetChatPlanningPanel data = database.getPanelFriend_4(newFirstIndex);
            if (data != null) {
                addPanel_4(data, true);
                removePanel(false);
                firstVisibleIndex = newFirstIndex;
                lastVisibleIndex--;
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getValue() + panelHeightMin);
                });
            }
        }

        isLoading = false;
    }

    // Добавляет панель для заявок в друзья (тип 1a)
    private void addPanel(PanelDataRequests data, boolean atTop) {
        int currentHeight = intpanelHeightMax;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setPreferredSize(new Dimension(mainPanel.getWidth(), currentHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(currentBackgroundColor);

        String recordId = data.getRecordId();

        // Создает контекстное меню для удаления
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteItem.setBackground(Color.WHITE);
        deleteItem.setForeground(Color.BLACK);
        deleteItem.addActionListener(e -> deletePanel_3(recordId, panel));
        popup.add(deleteItem);

        // Слушатель мыши для контекстного меню
        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        // Создает панель для заявки в друзья
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        String senderId = data.getSenderId();
        boolean requestStatus = data.getRequestStatus();
        boolean notificationStatus = data.getNotificationStatus();
        boolean lockFlag = data.getLockFlag();
        String publicKey = data.getPublicKey();
        String record_ac_id_friend = data.getFriendSide();
        String record_ac_id_data = data.getServerSide();

        FriendRequestPanel friendRequestPanel = new FriendRequestPanel(senderId, requestStatus, notificationStatus,
                lockFlag, out, recordId, publicKey, record_ac_id_friend, record_ac_id_data, myID, frame, keyAc, accauntId, popupListener);
        leftPanel.add(friendRequestPanel.createPanel());

        panel.add(leftPanel, BorderLayout.WEST);
        if (atTop) {
            mainPanel.add(panel, 0);
        } else {
            mainPanel.add(panel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Добавляет панель для заблокированных пользователей (тип 4a)
    private void addPanel_2(FriendBlockPanel data, boolean atTop) {
        int currentHeight = intpanelHeightMax;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setPreferredSize(new Dimension(mainPanel.getWidth(), currentHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(currentBackgroundColor);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        String record_id = data.getRecordIDFriend();
        String sender_id = data.getFriendID();

        // Создает панель для заблокированного пользователя
        PanelFrandsBlock panelFrandsBlock = PanelFrandsBlock.createPanel(sender_id, record_id, out, myID, this, panel);
        leftPanel.add(panelFrandsBlock);

        panel.add(leftPanel, BorderLayout.WEST);
        if (atTop) {
            mainPanel.add(panel, 0);
        } else {
            mainPanel.add(panel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Добавляет панель для планирования чатов (тип 3a)
    private void addPanel_4(GetChatPlanningPanel data, boolean atTop) {
        String recordId = data.getRecordId();
        String accountId = String.valueOf(data.getAccountId());
        String key_plan = data.getKey();
        String senderId = data.getSenderId();

        // Вычисляет высоту панели в зависимости от количества ненулевых полей
        int nonNullCount = countNonNullValues(data);
        int currentHeight = (nonNullCount == 1) ? 150 : (nonNullCount == 2) ? 200 : 230;

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(255, 255, 255));
        Dimension panelSize = new Dimension(FIXED_WIDTH, currentHeight);
        panel.setPreferredSize(panelSize);
        panel.setSize(FIXED_WIDTH, currentHeight);
        panel.setOpaque(true);

        // Создает контекстное меню для удаления
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteItem.setBackground(Color.WHITE);
        deleteItem.setForeground(Color.BLACK);
        deleteItem.addActionListener(e -> deletePanel(recordId, panel));
        popup.add(deleteItem);

        // Слушатель мыши для контекстного меню
        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        try {
            // Расшифровывает ключ и имя контакта
            String key_or = encryption.chaha20Decrypt(keyAc, key_plan);
            String nameContact = encryption.chaha20Decrypt(keyAc, database.getContactName(accountId, senderId));

            // Создает левую панель для содержимого
            JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
            leftPanel.setBackground(new Color(197, 193, 193));
            Dimension leftPanelSize = new Dimension(FIXED_WIDTH, currentHeight - 20);
            leftPanel.setPreferredSize(leftPanelSize);
            leftPanel.setMinimumSize(leftPanelSize);
            leftPanel.setMaximumSize(leftPanelSize);
            leftPanel.addMouseListener(popupListener);

            // Создает заголовок с информацией об отправителе
            JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            headerPanel.setBackground(new Color(197, 193, 193));
            Dimension headerSize = new Dimension(FIXED_WIDTH, 50);
            headerPanel.setPreferredSize(headerSize);
            headerPanel.setMinimumSize(headerSize);
            headerPanel.setMaximumSize(headerSize);

            JLabel senderLabel = new JLabel("Сообщение от:");
            senderLabel.setFont(new Font("Arial", Font.BOLD, 16));
            senderLabel.setForeground(new Color(33, 150, 243));
            senderLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            headerPanel.add(senderLabel);

            JLabel senderNameLabel = new JLabel(nameContact);
            senderNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            senderNameLabel.setForeground(new Color(66, 66, 66));
            senderNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            headerPanel.add(senderNameLabel);

            leftPanel.add(headerPanel, BorderLayout.NORTH);

            // Создает панель для содержимого (время начала, окончания, сообщение)
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(new Color(197, 193, 193));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            Dimension contentSize = new Dimension(FIXED_WIDTH, currentHeight - 70);
            contentPanel.setPreferredSize(contentSize);
            contentPanel.setMinimumSize(contentSize);
            contentPanel.setMaximumSize(contentSize);

            // Добавляет время начала, если оно присутствует
            if (data.getStartTime() != null) {
                JLabel startTimeHeader = new JLabel("Время начала:");
                startTimeHeader.setFont(new Font("Arial", Font.BOLD, 16));
                startTimeHeader.setForeground(new Color(33, 150, 243));
                contentPanel.add(startTimeHeader);
                contentPanel.add(Box.createVerticalStrut(5));

                String decryptedStartTime = encryption.chaha20Decrypt(key_or, data.getStartTime());
                String formattedStartTime;
                try {
                    Instant startInstant = Instant.parse(decryptedStartTime);
                    ZonedDateTime localStartTime = startInstant.atZone(ZoneId.systemDefault());
                    formattedStartTime = localStartTime.format(localFormatter);
                } catch (DateTimeParseException e) {
                    formattedStartTime = "Неверный формат времени";
                    System.err.println("Ошибка парсинга startTime: " + decryptedStartTime);
                }

                JLabel startTimeLabel = new JLabel(formattedStartTime);
                startTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                startTimeLabel.setForeground(new Color(66, 66, 66));
                startTimeLabel.setMaximumSize(new Dimension(FIXED_WIDTH - 20, startTimeLabel.getPreferredSize().height));
                contentPanel.add(startTimeLabel);
                contentPanel.add(Box.createVerticalStrut(10));
            }

            // Добавляет время окончания, если оно присутствует
            if (data.getEndTime() != null) {
                JLabel endTimeHeader = new JLabel("Время окончания:");
                endTimeHeader.setFont(new Font("Arial", Font.BOLD, 16));
                endTimeHeader.setForeground(new Color(33, 150, 243));
                contentPanel.add(endTimeHeader);
                contentPanel.add(Box.createVerticalStrut(5));

                String decryptedEndTime = encryption.chaha20Decrypt(key_or, data.getEndTime());
                String formattedEndTime;
                try {
                    Instant endInstant = Instant.parse(decryptedEndTime);
                    ZonedDateTime localEndTime = endInstant.atZone(ZoneId.systemDefault());
                    formattedEndTime = localEndTime.format(localFormatter);
                } catch (DateTimeParseException e) {
                    formattedEndTime = "Неверный формат времени";
                }

                JLabel endTimeLabel = new JLabel(formattedEndTime);
                endTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                endTimeLabel.setForeground(new Color(66, 66, 66));
                endTimeLabel.setMaximumSize(new Dimension(FIXED_WIDTH - 20, endTimeLabel.getPreferredSize().height));
                contentPanel.add(endTimeLabel);
                contentPanel.add(Box.createVerticalStrut(10));
            }

            // Добавляет сообщение, если оно присутствует
            if (data.getMessages() != null) {
                JLabel messageHeader = new JLabel("Сообщение:");
                messageHeader.setFont(new Font("Arial", Font.BOLD, 16));
                messageHeader.setForeground(new Color(33, 150, 243));
                contentPanel.add(messageHeader);
                contentPanel.add(Box.createVerticalStrut(5));

                String decryptedMessage = encryption.chaha20Decrypt(key_or, data.getMessages());
                JLabel messageLabel = new JLabel(decryptedMessage);
                messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                messageLabel.setForeground(new Color(66, 66, 66));
                messageLabel.setMaximumSize(new Dimension(FIXED_WIDTH - 20, messageLabel.getPreferredSize().height));
                contentPanel.add(messageLabel);
            }

            leftPanel.add(contentPanel, BorderLayout.CENTER);
            panel.add(leftPanel, BorderLayout.WEST);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (atTop) {
            mainPanel.add(panel, 0);
        } else {
            mainPanel.add(panel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Удаляет запись о заявке в друзья (тип 1a)
    private void deletePanel_3(String nameFile, JPanel panel) {
        String deleteSQL = "DELETE FROM request_responses WHERE record_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, nameFile);
            pstmt.executeUpdate();

            isLoading = true;
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int currentScroll = verticalBar.getValue();

            // Вычисляет верхний видимый индекс и смещение
            final int topVisibleIndex;
            final int topVisibleOffset;
            Component[] components = mainPanel.getComponents();
            int cumulativeHeight = 0;
            int tempTopVisibleIndex = -1;
            int tempTopVisibleOffset = 0;
            for (int i = 0; i < components.length; i++) {
                int panelHeight = components[i].getPreferredSize().height;
                if (cumulativeHeight <= currentScroll && cumulativeHeight + panelHeight > currentScroll) {
                    tempTopVisibleIndex = firstVisibleIndex + i;
                    tempTopVisibleOffset = currentScroll - cumulativeHeight;
                    break;
                }
                cumulativeHeight += panelHeight;
            }
            topVisibleIndex = tempTopVisibleIndex;
            topVisibleOffset = tempTopVisibleOffset;

            // Находит индекс удаляемой панели
            int panelIndex = -1;
            for (int i = 0; i < components.length; i++) {
                if (components[i] == panel) {
                    panelIndex = i;
                    break;
                }
            }

            mainPanel.remove(panel); // Удаляет панель из интерфейса

            int totalPanels = database.getTotalPanelsFriend();
            if (totalPanels > 0) {
                // Корректирует индексы
                if (panelIndex >= 0) {
                    int globalIndex = firstVisibleIndex + panelIndex;
                    if (globalIndex <= lastVisibleIndex) {
                        lastVisibleIndex--;
                    }
                    if (panelIndex == 0 && firstVisibleIndex > 0) {
                        firstVisibleIndex--;
                    }
                }

                // Подгружает панели, если их меньше минимального количества
                int currentCount = mainPanel.getComponentCount();
                if (currentCount < MAX_PANELS && totalPanels > currentCount) {
                    if (firstVisibleIndex > 0) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, firstVisibleIndex);
                        List<PanelDataRequests> newPanels = database.getPanelsFriend(firstVisibleIndex - panelsToLoad, firstVisibleIndex - 1);
                        for (int i = newPanels.size() - 1; i >= 0; i--) {
                            addPanel(newPanels.get(i), true);
                        }
                        firstVisibleIndex -= panelsToLoad;
                    } else if (lastVisibleIndex < totalPanels - 1) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, totalPanels - lastVisibleIndex - 1);
                        List<PanelDataRequests> newPanels = database.getPanelsFriend(lastVisibleIndex + 1, lastVisibleIndex + panelsToLoad);
                        for (PanelDataRequests newPanel : newPanels) {
                            addPanel(newPanel, false);
                        }
                        lastVisibleIndex += panelsToLoad;
                    }
                }

                updateMainPanelSize();
                SwingUtilities.invokeLater(() -> {
                    if (topVisibleIndex >= 0) {
                        int newTopPosition = 0;
                        Component[] newComponents = mainPanel.getComponents();
                        int newTopIndex = topVisibleIndex - firstVisibleIndex;
                        if (newTopIndex >= 0 && newTopIndex < newComponents.length) {
                            for (int i = 0; i < newTopIndex; i++) {
                                newTopPosition += newComponents[i].getPreferredSize().height;
                            }
                            int newScroll = newTopPosition + topVisibleOffset;
                            verticalBar.setValue(newScroll);
                        }
                    }
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    isLoading = false;
                });
            } else {
                mainPanel.removeAll();
                updateMainPanelSize();
                isLoading = false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            isLoading = false;
        }
    }

    // Удаляет запись о планировании чата (тип 3a)
    private void deletePanel(String nameFile, JPanel panel) {
        String deleteSQL = "DELETE FROM chat_planning WHERE record_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, nameFile);
            pstmt.executeUpdate();

            isLoading = true;
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int currentScroll = verticalBar.getValue();

            final int topVisibleIndex;
            final int topVisibleOffset;
            Component[] components = mainPanel.getComponents();
            int cumulativeHeight = 0;
            int tempTopVisibleIndex = -1;
            int tempTopVisibleOffset = 0;
            for (int i = 0; i < components.length; i++) {
                int panelHeight = components[i].getPreferredSize().height;
                if (cumulativeHeight <= currentScroll && cumulativeHeight + panelHeight > currentScroll) {
                    tempTopVisibleIndex = firstVisibleIndex + i;
                    tempTopVisibleOffset = currentScroll - cumulativeHeight;
                    break;
                }
                cumulativeHeight += panelHeight;
            }
            topVisibleIndex = tempTopVisibleIndex;
            topVisibleOffset = tempTopVisibleOffset;

            int panelIndex = -1;
            for (int i = 0; i < components.length; i++) {
                if (components[i] == panel) {
                    panelIndex = i;
                    break;
                }
            }

            mainPanel.remove(panel);

            int totalPanels = database.getTotalPanelsFriend_4();
            if (totalPanels > 0) {
                if (panelIndex >= 0) {
                    int globalIndex = firstVisibleIndex + panelIndex;
                    if (globalIndex <= lastVisibleIndex) {
                        lastVisibleIndex--;
                    }
                    if (panelIndex == 0 && firstVisibleIndex > 0) {
                        firstVisibleIndex--;
                    }
                }

                int currentCount = mainPanel.getComponentCount();
                if (currentCount < MAX_PANELS && totalPanels > currentCount) {
                    if (firstVisibleIndex > 0) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, firstVisibleIndex);
                        List<GetChatPlanningPanel> newPanels = database.getPanelsFriend_4(firstVisibleIndex - panelsToLoad, firstVisibleIndex - 1);
                        for (int i = newPanels.size() - 1; i >= 0; i--) {
                            addPanel_4(newPanels.get(i), true);
                        }
                        firstVisibleIndex -= panelsToLoad;
                    } else if (lastVisibleIndex < totalPanels - 1) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, totalPanels - lastVisibleIndex - 1);
                        List<GetChatPlanningPanel> newPanels = database.getPanelsFriend_4(lastVisibleIndex + 1, lastVisibleIndex + panelsToLoad);
                        for (GetChatPlanningPanel newPanel : newPanels) {
                            addPanel_4(newPanel, false);
                        }
                        lastVisibleIndex += panelsToLoad;
                    }
                }

                updateMainPanelSize();
                SwingUtilities.invokeLater(() -> {
                    if (topVisibleIndex >= 0) {
                        int newTopPosition = 0;
                        Component[] newComponents = mainPanel.getComponents();
                        int newTopIndex = topVisibleIndex - firstVisibleIndex;
                        if (newTopIndex >= 0 && newTopIndex < newComponents.length) {
                            for (int i = 0; i < newTopIndex; i++) {
                                newTopPosition += newComponents[i].getPreferredSize().height;
                            }
                            int newScroll = newTopPosition + topVisibleOffset;
                            verticalBar.setValue(newScroll);
                        }
                    }
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    isLoading = false;
                });
            } else {
                mainPanel.removeAll();
                updateMainPanelSize();
                isLoading = false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            isLoading = false;
        }
    }

    // Удаляет запись об ответе на заявку (тип 2a)
    private void deletePanel_2(String nameFile, JPanel panel) {
        String deleteSQL = "DELETE FROM application_responses WHERE record_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, nameFile);
            pstmt.executeUpdate();

            isLoading = true;
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int currentScroll = verticalBar.getValue();

            final int topVisibleIndex;
            final int topVisibleOffset;
            Component[] components = mainPanel.getComponents();
            int cumulativeHeight = 0;
            int tempTopVisibleIndex = -1;
            int tempTopVisibleOffset = 0;
            for (int i = 0; i < components.length; i++) {
                int panelHeight = components[i].getPreferredSize().height;
                if (cumulativeHeight <= currentScroll && cumulativeHeight + panelHeight > currentScroll) {
                    tempTopVisibleIndex = firstVisibleIndex + i;
                    tempTopVisibleOffset = currentScroll - cumulativeHeight;
                    break;
                }
                cumulativeHeight += panelHeight;
            }
            topVisibleIndex = tempTopVisibleIndex;
            topVisibleOffset = tempTopVisibleOffset;

            int panelIndex = -1;
            for (int i = 0; i < components.length; i++) {
                if (components[i] == panel) {
                    panelIndex = i;
                    break;
                }
            }

            mainPanel.remove(panel);

            int totalPanels = database.getTotalPanelsFriend_1();
            if (totalPanels > 0) {
                if (panelIndex >= 0) {
                    int globalIndex = firstVisibleIndex + panelIndex;
                    if (globalIndex <= lastVisibleIndex) {
                        lastVisibleIndex--;
                    }
                    if (panelIndex == 0 && firstVisibleIndex > 0) {
                        firstVisibleIndex--;
                    }
                }

                int currentCount = mainPanel.getComponentCount();
                if (currentCount < MAX_PANELS && totalPanels > currentCount) {
                    if (firstVisibleIndex > 0) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, firstVisibleIndex);
                        List<RequestsResponses> newPanels = database.getPanelsFriend_1(firstVisibleIndex - panelsToLoad, firstVisibleIndex - 1);
                        for (int i = newPanels.size() - 1; i >= 0; i--) {
                            addPanel_1(newPanels.get(i), true);
                        }
                        firstVisibleIndex -= panelsToLoad;
                    } else if (lastVisibleIndex < totalPanels - 1) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, totalPanels - lastVisibleIndex - 1);
                        List<RequestsResponses> newPanels = database.getPanelsFriend_1(lastVisibleIndex + 1, lastVisibleIndex + panelsToLoad);
                        for (RequestsResponses newPanel : newPanels) {
                            addPanel_1(newPanel, false);
                        }
                        lastVisibleIndex += panelsToLoad;
                    }
                }

                updateMainPanelSize();
                SwingUtilities.invokeLater(() -> {
                    if (topVisibleIndex >= 0) {
                        int newTopPosition = 0;
                        Component[] newComponents = mainPanel.getComponents();
                        int newTopIndex = topVisibleIndex - firstVisibleIndex;
                        if (newTopIndex >= 0 && newTopIndex < newComponents.length) {
                            for (int i = 0; i < newTopIndex; i++) {
                                newTopPosition += newComponents[i].getPreferredSize().height;
                            }
                            int newScroll = newTopPosition + topVisibleOffset;
                            verticalBar.setValue(newScroll);
                        }
                    }
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    isLoading = false;
                });
            } else {
                mainPanel.removeAll();
                updateMainPanelSize();
                isLoading = false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            isLoading = false;
        }
    }

    // Удаляет запись о заблокированном пользователе (тип 4a)
    private void deletePanel_1(String record_id, JPanel panel) {
        String deleteSQL = "DELETE FROM clientBlock WHERE record_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, record_id);
            pstmt.executeUpdate();

            isLoading = true;
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int currentScroll = verticalBar.getValue();

            final int topVisibleIndex;
            final int topVisibleOffset;
            Component[] components = mainPanel.getComponents();
            int cumulativeHeight = 0;
            int tempTopVisibleIndex = -1;
            int tempTopVisibleOffset = 0;
            for (int i = 0; i < components.length; i++) {
                int panelHeight = components[i].getPreferredSize().height;
                if (cumulativeHeight <= currentScroll && cumulativeHeight + panelHeight > currentScroll) {
                    tempTopVisibleIndex = firstVisibleIndex + i;
                    tempTopVisibleOffset = currentScroll - cumulativeHeight;
                    break;
                }
                cumulativeHeight += panelHeight;
            }
            topVisibleIndex = tempTopVisibleIndex;
            topVisibleOffset = tempTopVisibleOffset;

            int panelIndex = -1;
            for (int i = 0; i < components.length; i++) {
                if (components[i] == panel) {
                    panelIndex = i;
                    break;
                }
            }

            mainPanel.remove(panel);

            int totalPanels = database.getTotalPanelsFriend_2();
            if (totalPanels > 0) {
                if (panelIndex >= 0) {
                    int globalIndex = firstVisibleIndex + panelIndex;
                    if (globalIndex <= lastVisibleIndex) {
                        lastVisibleIndex--;
                    }
                    if (panelIndex == 0 && firstVisibleIndex > 0) {
                        firstVisibleIndex--;
                    }
                }

                int currentCount = mainPanel.getComponentCount();
                if (currentCount < MAX_PANELS && totalPanels > currentCount) {
                    if (firstVisibleIndex > 0) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, firstVisibleIndex);
                        List<FriendBlockPanel> newPanels = database.getPanelsFriend_2(firstVisibleIndex - panelsToLoad, firstVisibleIndex - 1);
                        for (int i = newPanels.size() - 1; i >= 0; i--) {
                            addPanel_2(newPanels.get(i), true);
                        }
                        firstVisibleIndex -= panelsToLoad;
                    } else if (lastVisibleIndex < totalPanels - 1) {
                        int panelsToLoad = Math.min(MAX_PANELS - currentCount, totalPanels - lastVisibleIndex - 1);
                        List<FriendBlockPanel> newPanels = database.getPanelsFriend_2(lastVisibleIndex + 1, lastVisibleIndex + panelsToLoad);
                        for (FriendBlockPanel newPanel : newPanels) {
                            addPanel_2(newPanel, false);
                        }
                        lastVisibleIndex += panelsToLoad;
                    }
                }

                updateMainPanelSize();
                SwingUtilities.invokeLater(() -> {
                    if (topVisibleIndex >= 0) {
                        int newTopPosition = 0;
                        Component[] newComponents = mainPanel.getComponents();
                        int newTopIndex = topVisibleIndex - firstVisibleIndex;
                        if (newTopIndex >= 0 && newTopIndex < newComponents.length) {
                            for (int i = 0; i < newTopIndex; i++) {
                                newTopPosition += newComponents[i].getPreferredSize().height;
                            }
                            int newScroll = newTopPosition + topVisibleOffset;
                            verticalBar.setValue(newScroll);
                        }
                    }
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    isLoading = false;
                });
            } else {
                mainPanel.removeAll();
                updateMainPanelSize();
                isLoading = false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            isLoading = false;
        }
    }

    // Подсчитывает количество ненулевых полей в объекте GetChatPlanningPanel
    public static int countNonNullValues(GetChatPlanningPanel data) {
        int count = 0;
        if (data.getMessages() != null) count++;
        if (data.getStartTime() != null) count++;
        if (data.getEndTime() != null) count++;
        return count;
    }

    // Добавляет панель для ответа на заявку (тип 2a)
    private void addPanel_1(RequestsResponses data, boolean atTop) {
        int currentHeight = intpanelHeightMax;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setPreferredSize(new Dimension(mainPanel.getWidth(), currentHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(currentBackgroundColor);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        String senderName = data.getSenderId();
        boolean requestStatus = data.getRequestStatus();
        boolean lockFlag = data.getLockFlag();
        String recordId = data.getRecord();

        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteItem.setBackground(Color.WHITE);
        deleteItem.setForeground(Color.BLACK);
        deleteItem.addActionListener(e -> deletePanel_2(recordId, panel));
        popup.add(deleteItem);

        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        ResponsePanel panelAll = ResponsePanel.createPanel(senderName, requestStatus, lockFlag, popupListener);
        leftPanel.add(panelAll);

        panel.add(leftPanel, BorderLayout.WEST);
        if (atTop) {
            mainPanel.add(panel, 0);
        } else {
            mainPanel.add(panel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Удаляет панель, если их больше минимального количества
    private void removePanel(boolean removeTop) {
        if (mainPanel.getComponentCount() > MAX_PANELS) {
            if (removeTop) {
                mainPanel.remove(0);
            } else {
                mainPanel.remove(mainPanel.getComponentCount() - 1);
            }
        }
        updateMainPanelSize();
    }

    // Удаляет все панели определенного типа для указанного пользователя
    void delDatePanel() {
        String query = null;
        if ("1a".equals(type)) {
            query = "DELETE FROM request_responses WHERE account_id = ?";
        } else if ("2a".equals(type)) {
            query = "DELETE FROM application_responses WHERE account_id = ?";
        } else if ("4a".equals(type)) {
            query = "DELETE FROM clientBlock WHERE account_id = ?";
        } else if ("3a".equals(type)) {
            query = "DELETE FROM chat_planning WHERE account_id = ?";
        }
        database.delScroll(query, accauntId);
        loadInitialPanels();
    }

    // Удаляет блокировку клиента
    void deleteBlockClient(JPanel panel, String recordId) {
        deletePanel_1(recordId, panel);
    }

    // Возвращает прокручиваемую панель
    public JScrollPane getPanel() {
        return scrollPane;
    }
}
