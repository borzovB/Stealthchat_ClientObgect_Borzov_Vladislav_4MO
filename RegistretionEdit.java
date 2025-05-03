package org.face_recognition;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Random;

// Класс для добавления контактов в друзья с диалоговыми окнами для ввода данных
public class RegistretionEdit {

    // Статические переменные для генерации случайных символов и работы с ключами
    private static Random random = new Random();
    private static KeyGet keyGet = new KeyGet();
    private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static String name; // Имя контакта
    private static String id; // Идентификатор контакта
    private BufferedImage originalImage; // Оригинальное изображение
    private BufferedImage scaledImage; // Масштабированное изображение
    private double scale = 1.0; // Масштаб изображения
    private int xOffset = 0; // Смещение по X
    private int yOffset = 0; // Смещение по Y
    private Point lastPoint; // Последняя точка нажатия мыши
    private JPanel imagePanel; // Панель для отображения изображения
    private JTextField nameField; // Поле для ввода имени
    private JTextField idField; // Поле для ввода идентификатора
    private static JFrame frame; // Главное окно приложения
    private static String fileName; // Имя файла для сохранения изображения
    private static String keyAc; // Ключ для шифрования
    private static ControlPanel controlPanel = new ControlPanel(); // Панель управления

    // Конструктор класса
    public RegistretionEdit(JFrame frame, String keyAc) {
        this.frame = frame;
        this.keyAc = keyAc;
    }

    // Метод для диалогового окна при принятии заявки в друзья
    String[] dialogPanelReceived(String myID, String senderId) {
        // Создание диалогового окна
        JDialog dialog = new JDialog(frame, "Ввод данных контакта", true);
        dialog.setSize(600, 500);
        dialog.setMinimumSize(new Dimension(690, 700));
        dialog.setLocationRelativeTo(frame);

        // Панель для ввода текста
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Шрифты для меток и полей
        Font labelFont = new Font("Arial", Font.PLAIN, 16);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Метка и поле для имени
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Введите имя:");
        nameLabel.setFont(labelFont);
        textPanel.add(nameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        nameField.setPreferredSize(new Dimension(200, 40));
        controlPanel.configurePasteMenu(nameField);
        textPanel.add(nameField, gbc);

        // Панель для изображения
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Белый фон
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Отрисовка изображения с учетом масштаба и смещения
                if (scaledImage != null) {
                    g2d.drawImage(scaledImage, xOffset, yOffset, null);

                    // Затемнение области вне круга
                    g2d.setColor(new Color(0, 0, 0, 100));
                    Shape outerArea = new Rectangle(0, 0, getWidth(), getHeight());
                    Shape innerCircle = new java.awt.geom.Ellipse2D.Double(0, 0, getWidth(), getHeight());
                    Area area = new Area(outerArea);
                    area.subtract(new Area(innerCircle));
                    g2d.fill(area);

                    // Рамка обрезки
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new java.awt.geom.Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
                } else {
                    // Текст до загрузки изображения
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "Вставьте изображение";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    g2d.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - fm.getDescent());
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(200, 200));
        imagePanel.setBackground(Color.WHITE);
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2);
        imagePanel.setBorder(blackBorder);

        // Обработчик перетаскивания изображения
        imagePanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        File file = files.get(0);
                        loadImageFromFile(file);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка загрузки изображения: " + ex.getMessage());
                }
                return true;
            }
        });

        // Перемещение изображения
        imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    xOffset += e.getX() - lastPoint.x;
                    yOffset += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    imagePanel.repaint();
                }
            }
        });

        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        // Масштабирование изображения колесиком мыши
        imagePanel.addMouseWheelListener(e -> {
            if (originalImage != null) {
                scale += e.getWheelRotation() * -0.1;
                scale = Math.max(0.1, Math.min(scale, 5.0));
                updateScaledImage();
                imagePanel.repaint();
            }
        });

        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)),
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220))
        ));

        // Размер кнопок
        int buttonSize = 40;

        // Кнопка загрузки изображения
        JButton loadButton = new JButton();
        ImageIcon originalIcon = new ImageIcon("pictures/load.png");
        Image scaledImageButton = originalIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImageButton);
        loadButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadButton.setIcon(scaledIcon);
        loadButton.setToolTipText("Загрузить изображение");
        loadButton.addActionListener(e -> loadImage(imagePanel));

        // Кнопка очистки изображения
        JButton clearButton = new JButton();
        ImageIcon clearIcon = new ImageIcon("pictures/clearingChat.png");
        Image scaledClearImage = clearIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        clearButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        clearButton.setIcon(new ImageIcon(scaledClearImage));
        clearButton.setToolTipText("Очистить");
        clearButton.addActionListener(e -> clearImage(imagePanel));

        // Кнопка сохранения
        JButton saveButton = new JButton();
        ImageIcon saveIcon = new ImageIcon("pictures/save.png");
        Image scaledSaveImage = saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        saveButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveButton.setIcon(new ImageIcon(scaledSaveImage));
        saveButton.setToolTipText("Готово");
        saveButton.addActionListener(e -> {
            name = nameField.getText().trim();

            // Проверка пустого имени
            if (name.isEmpty()) {
                new Thread(() ->
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame,
                                        "Имя не введено или пустое.",
                                        "Информация",
                                        JOptionPane.INFORMATION_MESSAGE)
                        )
                ).start();
                return;
            }

            // Проверка длины имени
            if (name.length() > 16) {
                new Thread(() ->
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame,
                                        "Имя слишком длинное. Максимум 16 символов.",
                                        "Предупреждение",
                                        JOptionPane.WARNING_MESSAGE)
                        )
                ).start();
                return;
            }

            // Форматирование даты для имени файла
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

            // Генерация имени файла
            fileName = "out/contact_images/" + myID + "/" + senderId + "/" + formattedDate + ".bin";

            File file = new File(fileName);
            file.getParentFile().mkdirs();

            // Установка имени по умолчанию
            if (name.isEmpty()) {
                name = senderId;
            }

            saveImage(imagePanel);
            clearImage(imagePanel);
            dialog.dispose();
        });

        // Кнопка выхода
        JButton exitButton = new JButton();
        ImageIcon exitIcon = new ImageIcon("pictures/exitIcon.png");
        Image scaledExitImage = exitIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        exitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        exitButton.setIcon(new ImageIcon(scaledExitImage));
        exitButton.setToolTipText("Закрыть");
        exitButton.addActionListener(e -> {
            name = nameField.getText().trim();

            // Форматирование даты для имени файла
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

            // Генерация имени файла
            fileName = "out/contact_images/" + myID + "/" + senderId + "/" + formattedDate + ".bin";

            File file = new File(fileName);
            file.getParentFile().mkdirs();

            // Установка имени по умолчанию
            if (name.isEmpty()) {
                name = senderId;
            }

            saveImage(imagePanel);
            clearImage(imagePanel);
            dialog.dispose();
        });

        // Добавление кнопок на панель
        buttonPanel.add(clearButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        // Панель для изображения
        JPanel staticPanel = new JPanel();
        staticPanel.setBackground(Color.LIGHT_GRAY);
        staticPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        staticPanel.add(imagePanel, BorderLayout.CENTER);

        // Основная панель контента
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(500, 400));
        contentPanel.setBackground(Color.LIGHT_GRAY);

        // Добавление компонентов в contentPanel
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(textPanel, BorderLayout.CENTER);
        contentPanel.add(staticPanel, BorderLayout.SOUTH);

        // Панель-обертка для центрирования
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(Color.GRAY);
        GridBagConstraints gbcWrapper = new GridBagConstraints();
        gbcWrapper.gridx = 0;
        gbcWrapper.gridy = 0;
        gbcWrapper.anchor = GridBagConstraints.CENTER;
        wrapperPanel.add(contentPanel, gbcWrapper);

        // Добавление wrapperPanel в диалог
        dialog.add(wrapperPanel);

        // Обработчик закрытия окна
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                name = nameField.getText().trim();

                // Форматирование даты для имени файла
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

                // Генерация имени файла
                fileName = "out/contact_images/" + myID + "/" + senderId + "/" + formattedDate + ".bin";

                File file = new File(fileName);
                file.getParentFile().mkdirs();

                // Установка имени по умолчанию
                if (name.isEmpty()) {
                    name = senderId;
                    saveImage(imagePanel);
                    clearImage(imagePanel);
                    dialog.dispose();
                }

                saveImage(imagePanel);
                clearImage(imagePanel);
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        return new String[]{name, fileName};
    }

    // Метод для диалогового окна при отправке заявки в друзья
    String[] dialogPanel(String myID) {
        // Создание диалогового окна
        JDialog dialog = new JDialog(frame, "Ввод данных контакта", true);
        dialog.setSize(600, 500);
        dialog.setMinimumSize(new Dimension(690, 700));
        dialog.setLocationRelativeTo(frame);

        // Панель для ввода текста
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Шрифты для меток и полей
        Font labelFont = new Font("Arial", Font.PLAIN, 16);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Метка и поле для имени
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Введите имя:");
        nameLabel.setFont(labelFont);
        textPanel.add(nameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        nameField.setPreferredSize(new Dimension(200, 40));
        controlPanel.configurePasteMenu(nameField);
        textPanel.add(nameField, gbc);

        // Метка и поле для идентификатора
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel idLabel = new JLabel("Введите идентификатор:");
        idLabel.setFont(labelFont);
        textPanel.add(idLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        idField = new JTextField(20);
        idField.setFont(fieldFont);
        idField.setPreferredSize(new Dimension(200, 40));
        controlPanel.configurePasteMenu(idField);
        textPanel.add(idField, gbc);

        // Панель для изображения
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Белый фон
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Отрисовка изображения
                if (scaledImage != null) {
                    g2d.drawImage(scaledImage, xOffset, yOffset, null);

                    // Затемнение области вне круга
                    g2d.setColor(new Color(0, 0, 0, 100));
                    Shape outerArea = new Rectangle(0, 0, getWidth(), getHeight());
                    Shape innerCircle = new java.awt.geom.Ellipse2D.Double(0, 0, getWidth(), getHeight());
                    Area area = new Area(outerArea);
                    area.subtract(new Area(innerCircle));
                    g2d.fill(area);

                    // Рамка обрезки
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new java.awt.geom.Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
                } else {
                    // Текст до загрузки изображения
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "Вставьте изображение";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    g2d.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - fm.getDescent());
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(200, 200));
        imagePanel.setBackground(Color.WHITE);
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2);
        imagePanel.setBorder(blackBorder);

        // Обработчик перетаскивания изображения
        imagePanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        File file = files.get(0);
                        loadImageFromFile(file);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка загрузки изображения: " + ex.getMessage());
                }
                return true;
            }
        });

        // Перемещение изображения
        imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    xOffset += e.getX() - lastPoint.x;
                    yOffset += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    imagePanel.repaint();
                }
            }
        });

        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        // Масштабирование изображения
        imagePanel.addMouseWheelListener(e -> {
            if (originalImage != null) {
                scale += e.getWheelRotation() * -0.1;
                scale = Math.max(0.1, Math.min(scale, 5.0));
                updateScaledImage();
                imagePanel.repaint();
            }
        });

        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)),
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220))
        ));

        // Размер кнопок
        int buttonSize = 40;

        // Кнопка загрузки изображения
        JButton loadButton = new JButton();
        ImageIcon originalIcon = new ImageIcon("pictures/load.png");
        Image scaledImageButton = originalIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImageButton);
        loadButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadButton.setIcon(scaledIcon);
        loadButton.setToolTipText("Загрузить изображение");
        loadButton.addActionListener(e -> loadImage(imagePanel));

        // Кнопка очистки изображения
        JButton clearButton = new JButton();
        ImageIcon clearIcon = new ImageIcon("pictures/clearingChat.png");
        Image scaledClearImage = clearIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        clearButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        clearButton.setIcon(new ImageIcon(scaledClearImage));
        clearButton.setToolTipText("Очистить");
        clearButton.addActionListener(e -> clearImage(imagePanel));

        // Кнопка сохранения
        JButton saveButton = new JButton();
        ImageIcon saveIcon = new ImageIcon("pictures/save.png");
        Image scaledSaveImage = saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        saveButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveButton.setIcon(new ImageIcon(scaledSaveImage));
        saveButton.setToolTipText("Готово");
        saveButton.addActionListener(e -> new Thread(() -> {
            name = nameField.getText().trim();
            id = idField.getText().trim();

            // Проверка пустого имени
            if (name.isEmpty()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Имя не введено или пустое.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                id = null;
                return;
            }

            // Проверка отправки заявки самому себе
            if (id.equals(myID)) {
                new Thread(() ->
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame,
                                        "Вы не можете отправить себе запрос в друзья.",
                                        "Информация",
                                        JOptionPane.INFORMATION_MESSAGE)
                        )
                ).start();
                return;
            }

            // Проверка длины имени
            if (name.length() > 16) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Имя слишком длинное. Максимум 16 символов.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                id = null;
                return;
            }

            // Проверка пустого идентификатора
            if (id.isEmpty()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Идентификатор не введен или пустой.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                id = null;
                return;
            }

            // Проверка существования пользователя
            String[] result = MainPage.contactPluss(id);
            if (!Boolean.parseBoolean(result[1])) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Такого пользователя не существует.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                id = null;
                return;
            }

            // Проверка блокировки пользователем
            if (Boolean.parseBoolean(result[2])) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Вы были заблокированы данным пользователем.",
                                "Информация",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                id = null;
                return;
            }

            // Проверка наличия контакта
            if (MainPage.contactOut(id)) {
                try {
                    SwingUtilities.invokeAndWait(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "У вас уже есть такой контакт, но возможно его нет у вашего получателя.",
                                    "Информация",
                                    JOptionPane.INFORMATION_MESSAGE)
                    );
                } catch (Exception er) {
                    er.printStackTrace();
                }
            }

            // Форматирование даты для имени файла
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

            // Генерация имени файла
            fileName = "out/contact_images/" + myID + "/" + id + "/" + formattedDate + ".bin";

            File file = new File(fileName);
            file.getParentFile().mkdirs();

            saveImage(imagePanel);
            clearImage(imagePanel);

            // Закрытие диалога
            SwingUtilities.invokeLater(dialog::dispose);
        }).start());

        // Кнопка выхода
        JButton exitButton = new JButton();
        ImageIcon exitIcon = new ImageIcon("pictures/exitIcon.png");
        Image scaledExitImage = exitIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        exitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        exitButton.setIcon(new ImageIcon(scaledExitImage));
        exitButton.setToolTipText("Закрыть");
        exitButton.addActionListener(e -> dialog.dispose());

        // Добавление кнопок на панель
        buttonPanel.add(clearButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        // Панель для изображения
        JPanel staticPanel = new JPanel();
        staticPanel.setBackground(Color.LIGHT_GRAY);
        staticPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        staticPanel.add(imagePanel, BorderLayout.CENTER);

        // Основная панель контента
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(500, 480));
        contentPanel.setBackground(Color.LIGHT_GRAY);

        // Добавление компонентов в contentPanel
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(textPanel, BorderLayout.CENTER);
        contentPanel.add(staticPanel, BorderLayout.SOUTH);

        // Панель-обертка для центрирования
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(Color.GRAY);
        GridBagConstraints gbcWrapper = new GridBagConstraints();
        gbcWrapper.gridx = 0;
        gbcWrapper.gridy = 0;
        gbcWrapper.anchor = GridBagConstraints.CENTER;
        wrapperPanel.add(contentPanel, gbcWrapper);

        // Добавление wrapperPanel в диалог
        dialog.add(wrapperPanel);

        // Обработчик закрытия окна
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clearImage(imagePanel);
            }
        });

        dialog.setVisible(true);

        return new String[]{name, id, fileName};
    }

    // Загрузка изображения из файла
    private void loadImageFromFile(File file) {
        try {
            originalImage = ImageIO.read(file);
            if (originalImage == null) {
                JOptionPane.showMessageDialog(frame, "Не удалось загрузить изображение");
                return;
            }
            scale = 1.0;
            xOffset = 0;
            yOffset = 0;
            updateScaledImage();
            imagePanel.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка загрузки изображения: " + ex.getMessage());
        }
    }

    // Загрузка изображения через диалог выбора файла
    private void loadImage(JPanel panel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JPG Images", "jpg"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(chooser.getSelectedFile());
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(frame, "Не удалось загрузить изображение");
                    return;
                }
                scale = 1.0;
                xOffset = 0;
                yOffset = 0;
                updateScaledImage();
                panel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка загрузки изображения: " + ex.getMessage());
            }
        }
    }

    // Очистка изображения
    private void clearImage(JPanel panel) {
        originalImage = null;
        scaledImage = null;
        scale = 1.0;
        xOffset = 0;
        yOffset = 0;
        panel.repaint();
    }

    // Обновление масштабированного изображения
    private void updateScaledImage() {
        if (originalImage != null) {
            int newWidth = (int) (originalImage.getWidth() * scale);
            int newHeight = (int) (originalImage.getHeight() * scale);
            scaledImage = new BufferedImage(newWidth, newHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
        }
    }

    // Сохранение изображения
    private void saveImage(JPanel panel) {
        if (scaledImage == null) {
            // Сохранение изображения по умолчанию
            try {
                File file = new File("pictures/defolt.bin");
                byte[] fileBytes = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileBytes);
                }

                // Шифрование и сохранение изображения
                keyGet.encryptImageIcon(fileBytes, fileName, keyAc, "AES");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка копирования файла: " + ex.getMessage());
            }
            return;
        }

        try {
            // Создание круглого изображения
            int diameter = Math.min(panel.getWidth(), panel.getHeight());
            BufferedImage output = new BufferedImage(diameter, diameter,
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = output.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, diameter, diameter));
            g2d.drawImage(scaledImage, xOffset, yOffset, null);
            g2d.dispose();

            // Преобразование изображения в массив байтов
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = ImageIO.write(output, "jpg", baos);
            if (!success) {
                JOptionPane.showMessageDialog(frame,
                        "Ошибка: ImageIO.write не смог записать изображение. Проверьте поддержку формата JPG.");
                return;
            }

            byte[] imageBytes = baos.toByteArray();
            if (imageBytes.length == 0) {
                JOptionPane.showMessageDialog(frame, "Ошибка: массив байтов пустой");
                return;
            }

            // Шифрование и сохранение изображения
            keyGet.encryptImageIcon(imageBytes, fileName, keyAc, "AES");

            // Проверка размера файла
            File file = new File(fileName);
            if (file.length() == 0) {
                JOptionPane.showMessageDialog(frame, "Ошибка: файл output.bin пустой после записи");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка сохранения: " + ex.getMessage());
        }
    }

}
