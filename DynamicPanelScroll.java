package org.face_recognition;

// Класс DynamicPanelScroll управляет динамической прокруткой панелей для отображения сообщений и файлов
// в чате, поддерживая текст, изображения, аудио и другие типы файлов с функциями воспроизведения и удаления
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import java.awt.Component;
import javax.swing.JFileChooser;
import java.io.FileOutputStream;
import java.io.IOException;

public class DynamicPanelScroll {
    // Константы для минимального количества панелей и зоны буфера прокрутки
    private static final int MIN_PANELS = 10; // Минимальное количество отображаемых панелей
    private static final int BUFFER_ZONE = 20; // Зона в пикселях для активации подгрузки панелей
    // Высоты панелей для различных типов контента
    private static int panelHeightMin = 60; // Минимальная высота панели (для коротких текстов)
    private static int intpanelHeightMax = 300; // Максимальная высота панели (для изображений)
    private static int fileHeight = 110; // Высота панели для файлов
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
    // Входная строка (не используется в коде, возможно, зарезервирована)
    private static String input;
    // Экземпляр базы данных для работы с сообщениями
    private static DatabaseScroll database;
    // Экземпляр панели контактов для получения информации о файлах
    private static ContactPanel contactPanel = new ContactPanel();
    // Экземпляр для дешифровки файлов
    private static DecryptFile decryptFile = new DecryptFile();
    // Текущий цвет фона панелей
    private Color currentBackgroundColor = Color.WHITE;
    // Вспомогательная панель для текстовых сообщений
    private JPanel panelSmol;
    // Главное окно приложения
    private static Frame frame;
    // Экземпляр для отображения окон (например, полного текста или изображений)
    private static DemonstrationWindows demonstrationWindows = new DemonstrationWindows();
    // Экземпляр для изменения переменных и управления цветами
    private static ChangingVariabl changingVariabl = new ChangingVariabl();
    // Экземпляр панели управления для настройки контекстного меню
    private static ControlPanel controlPanel = new ControlPanel();
    // Ключ шифрования учетной записи
    private static String keyAc;
    // Экземпляр класса для шифрования данных
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Имя базы данных SQLite
    private static final String DATABASE_NAME = "user_accounts.db";
    // URL для подключения к базе данных
    private static final String DB_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Экземпляр для управления блокировкой файлов
    private static Fileslock fileslock = new Fileslock();

    // Конструктор, инициализирующий параметры и настройки панели
    public DynamicPanelScroll(String ipContact, Frame frame, String keyAc, String accaunt_id) {
        this.frame = frame;
        this.keyAc = keyAc;
        // Инициализирует базу данных с указанным IP и ID учетной записи
        database = new DatabaseScroll(ipContact, accaunt_id);
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

                // Прокрутка вверх: загрузка панелей при достижении верхней зоны
                if (value <= BUFFER_ZONE && firstVisibleIndex > 0) {
                    isLoading = true;
                    loadPanelUp();
                    scrollPane.getViewport().setViewPosition(new Point(0, BUFFER_ZONE + 10)); // Корректирует позицию
                }

                // Прокрутка вниз: загрузка панелей при достижении нижней зоны
                if (value + extent >= max - adjustedBufferZone && lastVisibleIndex < database.getTotalPanels() - 1) {
                    isLoading = true;
                    loadPanelDown();
                }
            }
        });
    }

    // Обновляет цвет фона панели и всех дочерних элементов
    public void updatePanelColor(Color backgroundColor) {
        currentBackgroundColor = backgroundColor; // Сохраняет новый цвет фона
        mainPanel.setBackground(currentBackgroundColor);
        if (panelSmol != null) {
            panelSmol.setBackground(currentBackgroundColor); // Обновляет цвет вспомогательной панели
        }
        // Обновляет цвет всех дочерних панелей
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel panals) {
                comp.setBackground(backgroundColor);
                changingVariabl.updatePanelColorsRecursively(panals, backgroundColor); // Рекурсивно обновляет цвета
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
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
        int visiblePanels = Math.max(viewportHeight / panelHeightMin, MIN_PANELS);
        int totalPanels = database.getTotalPanels();
        firstVisibleIndex = Math.max(0, totalPanels - visiblePanels);
        lastVisibleIndex = totalPanels - 1;
        // Загружает панели из базы данных
        List<PanelData> panels = database.getPanels(firstVisibleIndex, lastVisibleIndex);
        for (PanelData data : panels) {
            addPanel(data, false); // Добавляет панели в конец
        }
        updateMainPanelSize();
        // Прокручивает к последней панели
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
            SwingUtilities.invokeLater(() -> verticalBar.setValue(verticalBar.getMaximum()));
        });
    }

    // Подгружает панели вниз при прокрутке
    private void loadPanelDown() {
        if (lastVisibleIndex >= database.getTotalPanels() - 1) {
            isLoading = false;
            return;
        }
        int newLastIndex = lastVisibleIndex + 1;
        PanelData data = database.getPanel(newLastIndex);
        if (data != null) {
            addPanel(data, false); // Добавляет панель в конец
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
        isLoading = false;
    }

    // Подгружает панели вверх при прокрутке
    private void loadPanelUp() {
        if (firstVisibleIndex <= 0) {
            isLoading = false;
            return;
        }
        int newFirstIndex = firstVisibleIndex - 1;
        PanelData data = database.getPanel(newFirstIndex);
        if (data != null) {
            addPanel(data, true); // Добавляет панель в начало
            removePanel(false); // Удаляет нижнюю панель
            firstVisibleIndex = newFirstIndex;
            lastVisibleIndex--;
            // Корректирует позицию прокрутки
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getValue() + panelHeightMin);
            });
        }
        isLoading = false;
    }

    // Вычисляет высоту панели для текстового сообщения на основе длины текста
    private int calculatePanelHeight(String text) {
        int length = text.length();
        if (length <= 25) {
            return 60;
        } else if (length <= 64) {
            return 80;
        } else if (length <= 93) {
            return 100;
        } else {
            return 110;
        }
    }

    // Добавляет панель для отображения сообщения или файла
    private void addPanel(PanelData data, boolean atTop) {
        // Получает информацию о файле (тип, алгоритм шифрования, ключ)
        String type = contactPanel.findSenderFileTipy(data.getName());
        String alg = contactPanel.findSenderAlg(data.getName());
        String key = contactPanel.findSenderKey(data.getName());
        try {
            // Расшифровывает ключ и алгоритм
            String encryptKey = encryption.chaha20Decrypt(keyAc, key);
            String encryptAlg = encryption.chaha20Decrypt(keyAc, alg);
            int currentHeight = 0;
            String nameFile = data.getName();
            String result = null;

            // Обрабатывает текстовые сообщения
            if (type.equals("texts")) {
                result = decryptFile.choosing_cipher(encryptAlg, nameFile, encryptKey);
                currentHeight = calculatePanelHeight(result);
            } else {
                currentHeight = getPanelHeight(type); // Устанавливает высоту для других типов
            }

            // Создает панель для контента
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setPreferredSize(new Dimension(mainPanel.getWidth(), currentHeight));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setBackground(currentBackgroundColor);

            // Контейнер для элементов
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            leftPanel.setOpaque(false);
            boolean status = data.getStat(); // Статус сообщения (например, отправлено/получено)

            // Создает контекстное меню для удаления
            JPopupMenu popup = new JPopupMenu();
            JMenuItem deleteItem = new JMenuItem("Удалить");
            deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
            deleteItem.setBackground(Color.WHITE);
            deleteItem.setForeground(Color.BLACK);
            deleteItem.addActionListener(e -> deletePanel(nameFile, panel));
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

            // Обрабатывает текстовые сообщения
            if (type.equals("texts")) {
                JPanel textPanel = new JPanel(new GridBagLayout());
                int textHeight = 0;

                int length = result.length();

                if (length <= 25) {
                    textHeight =  50;
                } else if (length <= 64) {
                    textHeight =  70;
                } else {
                    if (length <= 93){
                        textHeight =  90;
                    }else {
                        textHeight =  80;
                    }

                }

                textPanel.setPreferredSize(new Dimension(280, textHeight));
                textPanel.setBackground(new Color(173, 216, 230)); // Светло-голубой цвет
                textPanel.addMouseListener(popupListener);

                // Создаем текстовую область
                JTextArea textArea = new JTextArea();
                // После настройки textArea
                textArea.setText(result);
                textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
                textArea.setEditable(false);
                textArea.setOpaque(true);
                textArea.setBackground(new Color(173, 216, 230));
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setBorder(null);
                textArea.setCaretPosition(0);

                textArea.addMouseListener(popupListener);

                // Обрезаем длинный текст
                if (result.length() > 93) {
                    textArea.setText(result.substring(0, 93) + "...");
                }

                controlPanel.configureCopyMenuEndDell(textArea, deleteItem);
                if(status){
                    textPanel.setBackground(new Color(173, 216, 230)); // Светло-голубой цвет
                    textArea.setBackground(new Color(173, 216, 230)); // Светло-голубой цвет
                }else {
                    textPanel.setBackground(new Color(255, 182, 193)); // Светло-розовый цвет
                    textArea.setBackground(new Color(255, 182, 193)); // Светло-розовый цвет
                }

                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setBorder(null);
                textArea.setCaretPosition(0); // Гарантируем, что начало текста видно

                // Создаем JScrollPane для textArea
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(260, textHeight - 20));
                scrollPane.setBorder(null); // Убираем рамку
                scrollPane.setBackground(new Color(173, 216, 230));
                scrollPane.getViewport().setBackground(new Color(173, 216, 230));
                scrollPane.addMouseListener(popupListener);

                // Отключаем все функции прокрутки
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                scrollPane.setWheelScrollingEnabled(false); // Отключаем прокрутку колесом мыши
                scrollPane.setEnabled(false); // Делаем scrollPane неактивным
                scrollPane.getViewport().setEnabled(false); // Отключаем взаимодействие с viewport
                scrollPane.setFocusable(false); // Убираем возможность фокусировки
                scrollPane.getViewport().setFocusable(false);

                // Опционально: убираем видимость полос прокрутки, если они появляются
                scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
                scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

                // Отключаем горизонтальный скролл и скрываем вертикальный, если текст помещается
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(result.length() > 93 ?
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED : JScrollPane.VERTICAL_SCROLLBAR_NEVER);

                // Делаем scrollPane "неактивным"
                scrollPane.setFocusable(false);
                scrollPane.getViewport().setFocusable(false); // Отключаем фокус для viewport
                scrollPane.setWheelScrollingEnabled(false);

                // Передаем событие скролла родительскому контейнеру
                scrollPane.addMouseWheelListener(new MouseWheelListener() {
                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        leftPanel.dispatchEvent(e); // Передаем событие прокрутки leftPanel
                    }
                });

                scrollPane.addMouseListener(popupListener);

                // Настраиваем GridBagConstraints
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 0, 0, 0);

                // Добавляем scrollPane в textPanel
                textPanel.add(scrollPane, gbc);

                // Создаем панель для градиентного фона

                JPanel fadePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;

                        Color startColor = status ? new Color(173, 216, 230, 255) : new Color(255, 182, 193, 255); // Светло-голубой или светло-розовый
                        Color endColor = status ? new Color(173, 216, 230, 0) : new Color(255, 182, 193, 0); // Прозрачный градиент

                        // Создаем градиент
                        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                        g2d.setPaint(gp);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                };

                fadePanel.setPreferredSize(new Dimension(280, 30));
                fadePanel.setOpaque(false);
                fadePanel.setLayout(new BorderLayout());

                JLabel arrowLabel = new JLabel("▼", SwingConstants.CENTER);
                arrowLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
                arrowLabel.setForeground(new Color(100, 100, 100));

                fadePanel.add(arrowLabel, BorderLayout.CENTER);
                fadePanel.addMouseListener(popupListener);

                String finalResult = result;
                fadePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        demonstrationWindows.showFullTextDialog(finalResult, status, frame);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        arrowLabel.setForeground(Color.BLACK);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        arrowLabel.setForeground(new Color(100, 100, 100));
                    }
                });

                // Показ fadePanel, если текст слишком длинный
                fadePanel.setVisible(result.length() > 93);

                // Создаем панель для размещения с BoxLayout (вертикальное расположение)
                panelSmol = new JPanel();
                panelSmol.setLayout(new BoxLayout(panelSmol, BoxLayout.Y_AXIS));
                panelSmol.setBackground(currentBackgroundColor);

                // Добавляем textPanel и fadePanel
                panelSmol.add(textPanel);
                panelSmol.add(fadePanel);

                panelSmol.addMouseListener(popupListener);

                // Добавляем panelSmol в основной контейнер
                leftPanel.add(panelSmol);

            } else if (type.equals("wav")) {
                // Обрабатывает аудиофайлы
                ImageIcon playIcon = new ImageIcon(new ImageIcon("pictures/start.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                ImageIcon pauseIcon = new ImageIcon(new ImageIcon("pictures/stop.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                AudioPlayerState state = new AudioPlayerState(playIcon, pauseIcon, input);
                state.playPauseButton = new JButton(state.playIcon);
                state.playPauseButton.setPreferredSize(new Dimension(40, 40));
                state.playPauseButton.setBorder(BorderFactory.createLineBorder(new Color(0x0534EF), 2));
                state.playPauseButton.setBackground(new Color(0x289BD7));
                state.playPauseButton.setFocusPainted(false);
                state.playPauseButton.setOpaque(true);
                state.playPauseButton.setEnabled(false);
                state.playPauseButton.addActionListener(e -> togglePlayPause(state));

                // Создает полосу прогресса для аудио
                state.progressBar = new JSlider(0, 100, 0);
                state.progressBar.setPreferredSize(new Dimension(300, 20));
                state.progressBar.setBackground(Color.WHITE);
                state.progressBar.setForeground(new Color(0x4C60AF));
                state.progressBar.setOpaque(true);
                state.progressBar.addChangeListener(e -> seekAudio(state));
                state.progressBar.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        seekAudioOnClick(state, e);
                    }
                });

                // Создает панель для аудио
                JPanel panelAudio = new JPanel();
                panelAudio.addMouseListener(popupListener);
                panelAudio.setBackground(status ? new Color(173, 216, 230) : new Color(255, 182, 193));
                panelAudio.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
                panelAudio.setPreferredSize(new Dimension(400, 100));
                panelAudio.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

                // Добавляет текстовое поле с именем файла
                JTextField textField = new JTextField(changingVariabl.removeParts(data.getName(), "out/", ".bin"), 40);
                textField.setFont(new Font("Arial", Font.PLAIN, 11));
                textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                textField.setOpaque(false);
                textField.setForeground(Color.BLACK);
                textField.addMouseListener(popupListener);
                textField.setPreferredSize(new Dimension(100, 30));
                textField.setMaximumSize(new Dimension(1100, 30));

                panelAudio.add(state.playPauseButton);
                panelAudio.add(state.progressBar);
                panelAudio.add(textField);

                // Добавляет пункт меню для скачивания аудио
                JMenuItem audioDownload = new JMenuItem("Скачать");
                audioDownload.setFont(new Font("Arial", Font.PLAIN, 14));
                audioDownload.setBackground(Color.WHITE);
                audioDownload.setForeground(Color.BLACK);
                popup.add(audioDownload);

                // Загружает аудиофайл в фоновом потоке
                new Thread(() -> {
                    File destinationFile = new File(nameFile);
                    try {
                        decryptFile.choosing_cipher_vois(encryptAlg, encryptKey, destinationFile, state, audioDownload, frame);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                leftPanel.add(panelAudio);
                leftPanel.addMouseListener(popupListener);

            } else if (type.equals("jpg")) {
                // Обрабатывает изображения
                JMenuItem mageDownload = new JMenuItem("Скачать");
                mageDownload.setFont(new Font("Arial", Font.PLAIN, 14));
                mageDownload.setBackground(Color.WHITE);
                mageDownload.setForeground(Color.BLACK);
                popup.add(mageDownload);
                loadImage(encryptAlg, encryptKey, nameFile, leftPanel, status, popupListener, mageDownload);

            } else {
                // Обрабатывает другие типы файлов
                JPanel filePanel = new JPanel();
                filePanel.addMouseListener(popupListener);
                filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
                filePanel.setBackground(status ? new Color(173, 216, 230) : new Color(255, 182, 193));
                filePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                filePanel.setPreferredSize(new Dimension(400, 100));

                // Добавляет имя файла
                JLabel label = new JLabel(changingVariabl.removeParts(data.getName(), "out/", ".bin"));
                label.setFont(new Font("Arial", Font.PLAIN, 12));
                label.setForeground(new Color(50, 50, 50));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                filePanel.add(label);
                label.addMouseListener(popupListener);

                // Добавляет тип файла
                JLabel typeLabel = new JLabel("Тип файла: " + type);
                typeLabel.addMouseListener(popupListener);
                typeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                typeLabel.setForeground(new Color(100, 100, 100));
                typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                filePanel.add(typeLabel);

                // Добавляет разделитель
                JSeparator separator = new JSeparator();
                separator.setAlignmentX(Component.LEFT_ALIGNMENT);
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                filePanel.add(separator);

                // Создает контейнер для иконки и прогресс-бара
                JPanel iconProgressPanel = new JPanel();
                iconProgressPanel.addMouseListener(popupListener);
                iconProgressPanel.setLayout(new BoxLayout(iconProgressPanel, BoxLayout.X_AXIS));

                // Загружает и масштабирует иконку
                ImageIcon originalIcon = new ImageIcon("pictures/text.png");
                Image img = originalIcon.getImage();
                Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImg);
                JButton iconButton = new JButton(scaledIcon);
                iconButton.setText("");
                iconButton.setBorder(BorderFactory.createEmptyBorder());
                iconButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                iconButton.addActionListener(e -> {
                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    progressBar.setPreferredSize(new Dimension(250, 20));
                    progressBar.setString("Загрузка...");
                    progressBar.setStringPainted(true);
                    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
                    iconProgressPanel.add(progressBar);
                    filePanel.revalidate();
                    filePanel.repaint();
                    new Thread(() -> {
                        File fileinput = new File(data.getName());
                        try {
                            decryptFile.choosing_cipher_file_oll(encryptAlg, encryptKey, fileinput);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        progressBar.setVisible(false);
                    }).start();
                });
                iconProgressPanel.add(iconButton);
                filePanel.add(iconProgressPanel);
                leftPanel.add(filePanel);
            }

            // Добавляет панель в главную панель
            panel.add(leftPanel, BorderLayout.WEST);
            if (atTop) {
                mainPanel.add(panel, 0);
            } else {
                mainPanel.add(panel);
            }
            mainPanel.revalidate();
            mainPanel.repaint();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Загружает и отображает изображение в панели
    public static void loadImage(String alg, String key, String nameFile, JPanel leftPanel, boolean status, MouseAdapter popupListener, JMenuItem imageDownload) {
        // Создает панели для центрирования изображения
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setPreferredSize(new Dimension(290, 290));
        imagePanel.setBackground(changingVariabl.getPanelBackgroundColor(status));
        imagePanel.addMouseListener(popupListener);
        JPanel imagePanelSmoll = new JPanel(new GridBagLayout());
        imagePanelSmoll.setPreferredSize(new Dimension(280, 280));
        imagePanelSmoll.setBackground(changingVariabl.getPanelBackgroundColor(status));
        imagePanel.addMouseListener(popupListener);
        File destinationFile = new File(nameFile);

        // Запускает загрузку изображения в фоновом потоке
        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return decryptFile.choosing_cipher_image(alg, key, destinationFile);
            }
            @Override
            protected void done() {
                try {
                    byte[] imageData = get();
                    // Добавляет обработчик для скачивания изображения
                    imageDownload.addActionListener(e -> {
                        new Thread(() -> {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Сохранить изображение");
                            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPG Image", "jpg"));
                            fileChooser.setSelectedFile(new File("image.jpg"));
                            int userSelection = fileChooser.showSaveDialog(frame);
                            if (userSelection == JFileChooser.APPROVE_OPTION) {
                                File fileToSave = fileChooser.getSelectedFile();
                                String filePath = fileToSave.getAbsolutePath();
                                if (!filePath.toLowerCase().endsWith(".jpg")) {
                                    filePath += ".jpg";
                                    fileToSave = new File(filePath);
                                }
                                JDialog progressDialog = new JDialog(frame, "Сохранение", true);
                                JProgressBar progressBar = new JProgressBar(0, 100);
                                progressBar.setIndeterminate(true);
                                progressDialog.setLayout(new BorderLayout());
                                progressDialog.add(new JLabel("Сохранение изображения..."), BorderLayout.NORTH);
                                progressDialog.add(progressBar, BorderLayout.CENTER);
                                progressDialog.setSize(300, 100);
                                progressDialog.setLocationRelativeTo(frame);
                                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                                    fos.write(imageData);
                                    SwingUtilities.invokeLater(() -> {
                                        progressDialog.dispose();
                                        JOptionPane.showMessageDialog(frame, "Изображение успешно сохранено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                                    });
                                } catch (IOException ex) {
                                    SwingUtilities.invokeLater(() -> {
                                        progressDialog.dispose();
                                        JOptionPane.showMessageDialog(frame, "Ошибка при сохранении изображения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    });
                                    ex.printStackTrace();
                                }
                            }
                        }).start();
                    });

                    // Создает и масштабирует изображение
                    ImageIcon originalIcon = new ImageIcon(imageData);
                    Image originalImage = originalIcon.getImage();
                    int originalWidth = originalImage.getWidth(null);
                    int originalHeight = originalImage.getHeight(null);
                    int initialHeight = 400;
                    int initialWidth = (originalWidth * initialHeight) / originalHeight;
                    ImageIcon scaledIcon = demonstrationWindows.scaleImage(originalImage, initialWidth, initialHeight);
                    JLabel imageLabel = new JLabel(scaledIcon);
                    imageLabel.addMouseListener(popupListener);
                    imageLabel.setHorizontalAlignment(JLabel.CENTER);
                    imageLabel.setVerticalAlignment(JLabel.CENTER);
                    imageLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            demonstrationWindows.picture(scaledIcon, frame, originalWidth, originalHeight, originalImage);
                        }
                    });

                    // Обновляет панель с изображением
                    imagePanel.removeAll();
                    imagePanelSmoll.add(imageLabel);
                    imagePanel.add(imagePanelSmoll);
                    imagePanel.revalidate();
                    imagePanel.repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
        leftPanel.add(imagePanel);
    }

    // Возвращает высоту панели в зависимости от типа файла
    private int getPanelHeight(String tipy) {
        if (tipy.equals("jpg")) {
            return intpanelHeightMax;
        } else {
            return fileHeight;
        }
    }

    // Переключает воспроизведение и паузу аудио
    private void togglePlayPause(AudioPlayerState state) {
        if (state.clip != null) {
            if (state.isPlaying) {
                state.clip.stop();
                state.playPauseButton.setIcon(state.playIcon);
                state.timer.stop();
            } else {
                state.clip.start();
                state.playPauseButton.setIcon(state.pauseIcon);
                state.timer.start();
            }
            state.isPlaying = !state.isPlaying;
        }
    }

    // Обрабатывает перемотку аудио
    private void seekAudio(AudioPlayerState state) {
        if (state.clip != null && state.progressBar.getValueIsAdjusting()) {
            state.isSeeking = true;
        } else if (state.clip != null && state.isSeeking) {
            int newFramePosition = (int) ((state.progressBar.getValue() / 100.0) * state.clip.getFrameLength());
            state.clip.setFramePosition(newFramePosition);
            state.isSeeking = false;
        }
    }

    // Обрабатывает перемотку аудио при клике на полосу прогресса
    private void seekAudioOnClick(AudioPlayerState state, MouseEvent e) {
        if (state.clip != null) {
            int mouseX = e.getX();
            int newValue = (int) ((mouseX / (double) state.progressBar.getWidth()) * 100);
            state.progressBar.setValue(newValue);
            int newFramePosition = (int) ((newValue / 100.0) * state.clip.getFrameLength());
            state.clip.setFramePosition(newFramePosition);
        }
    }

    // Удаляет панель, если их больше минимального количества
    private void removePanel(boolean removeTop) {
        if (mainPanel.getComponentCount() > MIN_PANELS) {
            if (removeTop) {
                mainPanel.remove(0);
            } else {
                mainPanel.remove(mainPanel.getComponentCount() - 1);
            }
        }
        updateMainPanelSize();
    }

    //Удаление сообщения
    private void deletePanel(String nameFile, JPanel panel) {
        String selectSessionIdQuery = "SELECT session_id FROM messages WHERE file_id = ?";
        String deleteMessagesQuery = "DELETE FROM messages WHERE file_id = ?";
        String deleteSessionsQuery = "DELETE FROM sessions WHERE session_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // Включаем транзакцию

            // 1. Получаем session_id по file_id
            String sessionId = null;
            try (PreparedStatement pstmt = conn.prepareStatement(selectSessionIdQuery)) {
                pstmt.setString(1, nameFile);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getString("session_id");
                    }
                }
            }

            if (sessionId == null) {
                conn.rollback();
                return;
            }

            // 2. Удаляем запись из sessions
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSessionsQuery)) {
                pstmt.setString(1, sessionId);
                int deletedSessions = pstmt.executeUpdate();
                if (deletedSessions > 0) {
                    System.out.println("Удалена сессия с session_id: " + sessionId);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // 3. Удаляем файл
            fileslock.deleteFile(nameFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteMessagesQuery)) {
            pstmt.setString(1, nameFile);
            pstmt.executeUpdate();

            // Устанавливаем флаг загрузки
            isLoading = true;

            // Сохраняем текущую позицию прокрутки и верхний видимый элемент
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int currentScroll = verticalBar.getValue();

            // Вычисляем верхний видимый индекс и его позицию как final переменные
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

            // Находим индекс удаляемой панели
            int panelIndex = -1;
            for (int i = 0; i < components.length; i++) {
                if (components[i] == panel) {
                    panelIndex = i;
                    break;
                }
            }

            // Удаляем панель из интерфейса
            mainPanel.remove(panel);

            int totalPanels = database.getTotalPanels();
            if (totalPanels > 0) {
                // Корректируем индексы
                if (panelIndex >= 0) {
                    int globalIndex = firstVisibleIndex + panelIndex;
                    if (globalIndex <= lastVisibleIndex) {
                        lastVisibleIndex--;
                    }
                    if (panelIndex == 0 && firstVisibleIndex > 0) {
                        firstVisibleIndex--;
                    }
                }

                // Подгружаем панели, если их меньше минимального количества
                int currentCount = mainPanel.getComponentCount();
                if (currentCount < MIN_PANELS && totalPanels > currentCount) {
                    if (firstVisibleIndex > 0) {
                        // Подгрузка сверху
                        int panelsToLoad = Math.min(MIN_PANELS - currentCount, firstVisibleIndex);
                        List<PanelData> newPanels = database.getPanels(firstVisibleIndex - panelsToLoad, firstVisibleIndex - 1);
                        for (int i = newPanels.size() - 1; i >= 0; i--) {
                            addPanel(newPanels.get(i), true);
                        }
                        firstVisibleIndex -= panelsToLoad;
                    } else if (lastVisibleIndex < totalPanels - 1) {
                        // Подгрузка снизу
                        int panelsToLoad = Math.min(MIN_PANELS - currentCount, totalPanels - lastVisibleIndex - 1);
                        List<PanelData> newPanels = database.getPanels(lastVisibleIndex + 1, lastVisibleIndex + panelsToLoad);
                        for (PanelData newPanel : newPanels) {
                            addPanel(newPanel, false);
                        }
                        lastVisibleIndex += panelsToLoad;
                    }
                }

                // Обновляем размер главной панели
                updateMainPanelSize();

                // Восстанавливаем прокрутку, чтобы верхний видимый элемент остался на месте
                SwingUtilities.invokeLater(() -> {
                    if (topVisibleIndex >= 0) {
                        // Вычисляем новую позицию верхнего видимого элемента
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
                // Очищаем интерфейс, если панелей больше нет
                mainPanel.removeAll();
                updateMainPanelSize();
                isLoading = false;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении панели: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            isLoading = false;
        }
    }

    // Возвращает прокручиваемую панель
    public JScrollPane getPanel() {
        return scrollPane;
    }
}
