package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Random;

// Класс ChangingContacts отвечает за интерфейс изменения иконки и имени контакта в приложении
// Поддерживает загрузку, масштабирование, перемещение и обрезку изображения для иконки контакта
// Позволяет изменять имя контакта с шифрованием и отправкой данных на сервер
// Использует базу данных для хранения информации и SSL-соединение для передачи данных
public class ChangingСontacts {

    // Поля класса для хранения состояния и компонентов
    private static ObjectOutputStream out; // Поток вывода для отправки данных на сервер
    private static String myID; // Идентификатор текущего пользователя
    private static JFrame frame; // Основное окно приложения
    private static String keyAc; // Ключ аккаунта для шифрования
    private static String accaunt_id; // Идентификатор аккаунта
    private static EncryptionAccaunt encryption = new EncryptionAccaunt(); // Объект для шифрования
    private static KeyGet keyGet = new KeyGet(); // Объект для работы с ключами
    private static Database database = new Database(); // Объект для работы с базой данных
    private JPanel imagePanel; // Панель для отображения изображения
    private BufferedImage scaledImage; // Масштабированное изображение
    private double scale = 1.0; // Коэффициент масштабирования
    private int xOffset = 0; // Смещение по оси X
    private int yOffset = 0; // Смещение по оси Y
    private Point lastPoint; // Последняя точка для перемещения изображения
    private BufferedImage originalImage; // Оригинальное изображение
    private static String fileName; // Имя файла для сохранения изображения
    private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Символы для генерации имени файла
    private static Random random = new Random(); // Генератор случайных чисел
    private static ImageViewer imageViewer; // Объект для обработки изображений

    // Конструктор класса, инициализирующий параметры
    public ChangingСontacts(JFrame frame, String myID, ObjectOutputStream out, String keyAc, String accaunt_id) {
        this.frame = frame;
        this.myID = myID;
        this.out = out;
        this.keyAc = keyAc;
        this.accaunt_id = accaunt_id;
    }

    // Метод updateScaledImage обновляет масштабированное изображение
    // Создаёт новое изображение с учётом текущего коэффициента масштабирования
    // Применяет билинейную интерполяцию для повышения качества
    private void updateScaledImage() {
        if (originalImage != null) {
            int newWidth = (int) (originalImage.getWidth() * scale);
            int newHeight = (int) (originalImage.getHeight() * scale);
            scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
        }
    }

    // Метод dialogPanel создаёт интерфейс для изменения иконки контакта
    // Отображает диалоговое окно с панелью для изображения
    // Поддерживает загрузку, масштабирование и перемещение изображения
    // Добавляет кнопки для загрузки, очистки, сохранения и закрытия
    public void dialogPanel(JButton button, String senderId) {
        // Создаём модальное диалоговое окно
        JDialog dialog = new JDialog(frame, "Изменить иконку контакта", true);
        dialog.setSize(400, 440);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);

        // Создаём панель для изображения
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Рисуем белый фон
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if (scaledImage != null) {
                    // Рисуем масштабированное изображение с учётом смещения
                    g2d.drawImage(scaledImage, xOffset, yOffset, null);

                    // Затемняем область вне круга
                    g2d.setColor(new Color(0, 0, 0, 100));
                    Shape outerArea = new Rectangle(0, 0, getWidth(), getHeight());
                    Shape innerCircle = new java.awt.geom.Ellipse2D.Double(0, 0, getWidth(), getHeight());
                    Area area = new Area(outerArea);
                    area.subtract(new Area(innerCircle));
                    g2d.fill(area);

                    // Рисуем красную границу круга
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new java.awt.geom.Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
                } else {
                    // Отображаем текст, если изображение не загружено
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

        // Настраиваем поддержку перетаскивания файлов
        imagePanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        loadImageFromFile(files.get(0));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка загрузки изображения: " + ex.getMessage());
                }
                return true;
            }
        });

        // Обработчик перемещения изображения
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

        // Обработчик масштабирования колёсиком мыши
        imagePanel.addMouseWheelListener(e -> {
            if (originalImage != null) {
                scale += e.getWheelRotation() * -0.1;
                scale = Math.max(0.1, Math.min(scale, 5.0));
                updateScaledImage();
                imagePanel.repaint();
            }
        });

        // Создаём панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));

        int buttonSize = 40;

        // Кнопка "Загрузить изображение"
        JButton loadButton = new JButton();
        ImageIcon originalIcon = new ImageIcon("pictures/load.png");
        Image scaledImageButton = originalIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        loadButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadButton.setIcon(new ImageIcon(scaledImageButton));
        loadButton.setToolTipText("Загрузить изображение");
        loadButton.addActionListener(e -> loadImage(imagePanel));

        // Кнопка "Очистить"
        JButton clearButton = new JButton();
        ImageIcon clearIcon = new ImageIcon("pictures/clearingChat.png");
        Image scaledClearImage = clearIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        clearButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        clearButton.setIcon(new ImageIcon(scaledClearImage));
        clearButton.setToolTipText("Очистить");
        clearButton.addActionListener(e -> clearImage(imagePanel));

        // Кнопка "Готово"
        JButton saveButton = new JButton();
        ImageIcon saveIcon = new ImageIcon("pictures/save.png");
        Image scaledSaveImage = saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        saveButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveButton.setIcon(new ImageIcon(scaledSaveImage));
        saveButton.setToolTipText("Готово");
        saveButton.addActionListener(e -> {
            // Генерируем имя файла с текущей датой и случайным символом
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = dateFormat.format(new java.util.Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
            fileName = "out/contact_images/" + myID + "/" + senderId + "/" + formattedDate + ".bin";

            // Обновляем путь к изображению в базе данных
            database.updateContactImage(fileName, senderId, accaunt_id);

            // Создаём директории для файла
            File file = new File(fileName);
            file.getParentFile().mkdirs();

            // Сохраняем изображение
            saveImage(imagePanel, button);

            // Очищаем панель и закрываем диалог
            clearImage(imagePanel);
            dialog.dispose();
        });

        // Кнопка "Выход"
        JButton exitButton = new JButton();
        ImageIcon exitIcon = new ImageIcon("pictures/exitIcon.png");
        Image scaledExitImage = exitIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        exitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        exitButton.setIcon(new ImageIcon(scaledExitImage));
        exitButton.setToolTipText("Закрыть");
        exitButton.addActionListener(e -> dialog.dispose());

        // Добавляем кнопки на панель
        buttonPanel.add(clearButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        // Создаём основную панель контента
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.LIGHT_GRAY);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(imagePanel, BorderLayout.CENTER);

        // Добавляем панель в диалог
        dialog.add(contentPanel);

        // Очищаем изображение при закрытии окна
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clearImage(imagePanel);
            }
        });

        // Отображаем диалог
        dialog.setVisible(true);
    }

    // Метод saveImage сохраняет обрезанное изображение и обновляет иконку кнопки
    // Если изображение не загружено, копирует дефолтное изображение
    // Обрезает изображение по кругу и шифрует его
    // Обновляет изображение на кнопке контакта
    private void saveImage(JPanel panel, JButton button) {
        byte[] imageBytes = null;

        if (scaledImage == null) {
            // Используем дефолтное изображение
            try {
                File file = new File("pictures/defolt.bin");
                byte[] fileBytes = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileBytes);
                }
                imageBytes = fileBytes;
                keyGet.encryptImageIcon(fileBytes, fileName, keyAc, "AES");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка копирования файла: " + ex.getMessage());
                return;
            }
        } else {
            // Обрезаем изображение по кругу
            try {
                int diameter = Math.min(panel.getWidth(), panel.getHeight());
                BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = output.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, diameter, diameter));
                g2d.drawImage(scaledImage, xOffset, yOffset, null);
                g2d.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                boolean success = ImageIO.write(output, "jpg", baos);
                if (!success) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: ImageIO.write не смог записать изображение. Проверьте поддержку формата JPG.");
                    return;
                }

                imageBytes = baos.toByteArray();
                if (imageBytes.length == 0) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: массив байтов пустой");
                    return;
                }

                keyGet.encryptImageIcon(imageBytes, fileName, keyAc, "AES");

                File file = new File(fileName);
                if (file.length() == 0) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: файл output.bin пустой после записи");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения: " + ex.getMessage());
                return;
            }
        }

        // Обновляем изображение на кнопке
        try {
            imageViewer = new ImageViewer(keyAc);
            BufferedImage img = imageViewer.createCircularImageWithBorder(ImageIO.read(new ByteArrayInputStream(imageBytes)));
            JLabel newImageLabel = new JLabel(new ImageIcon(img));

            JPanel leftPanelContacts = null;
            for (Component comp : button.getComponents()) {
                if (comp instanceof Container) {
                    Container container = (Container) comp;
                    if (container.getLayout() instanceof FlowLayout) {
                        leftPanelContacts = (JPanel) container;
                        break;
                    }
                }
            }

            if (leftPanelContacts != null) {
                leftPanelContacts.removeAll();
                leftPanelContacts.add(newImageLabel);
                button.revalidate();
                button.repaint();
            } else {
                System.out.println("Не удалось найти leftPanelContacts для обновления изображения.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при обновлении изображения на кнопке: " + ex.getMessage());
        }
    }

    // Метод clearImage очищает изображение на панели
    // Сбрасывает оригинальное и масштабированное изображения
    // Сбрасывает параметры масштабирования и смещения
    private void clearImage(JPanel panel) {
        originalImage = null;
        scaledImage = null;
        scale = 1.0;
        xOffset = 0;
        yOffset = 0;
        panel.repaint();
    }

    // Метод loadImageFromFile загружает изображение из файла
    // Читает изображение с помощью ImageIO
    // Сбрасывает параметры масштабирования и смещения
    // Обновляет масштабированное изображение
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

    // Метод loadImage открывает диалог выбора файла для загрузки изображения
    // Показывает JFileChooser с фильтром для JPG-файлов
    // Загружает выбранное изображение
    private void loadImage(JPanel panel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPG Images", "jpg"));
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

    // Метод dialogPanelReceived создаёт интерфейс для изменения имени контакта
    // Отображает диалоговое окно с полем для ввода нового имени
    // Добавляет кнопки для сохранения и закрытия
    // Шифрует и отправляет новое имя на сервер
    void dialogPanelReceived(String myID, String senderId, JButton button) {
        // Создаём модальное диалоговое окно
        JDialog dialog = new JDialog(frame, "Изменить имя контакта", true);
        dialog.setSize(400, 250);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);

        // Создаём панель для ввода текста
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Устанавливаем шрифты
        Font labelFont = new Font("Arial", Font.PLAIN, 16);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Добавляем метку для имени
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Введите новое имя контакта:");
        nameLabel.setFont(labelFont);
        textPanel.add(nameLabel, gbc);

        // Добавляем поле для ввода имени
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        nameField.setPreferredSize(new Dimension(200, 40));
        textPanel.add(nameField, gbc);

        // Создаём панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));

        int buttonSize = 40;

        // Кнопка "Готово"
        JButton saveButton = new JButton();
        ImageIcon saveIcon = new ImageIcon("pictures/save.png");
        Image scaledSaveImage = saveIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        saveButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveButton.setIcon(new ImageIcon(scaledSaveImage));
        saveButton.setToolTipText("Готово");
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                // Проверяем длину имени
                if (name.length() > 16) {
                    new Thread(() ->
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(frame,
                                            "Имя слишком длинное. Максимум 16 символов.",
                                            "Информация",
                                            JOptionPane.INFORMATION_MESSAGE)
                            )
                    ).start();
                    return;
                }

                // Обновляем имя на кнопке
                redrawContactButton(button, name);

                try {
                    // Шифруем имя
                    String nameNew = encryption.chaha20Encript(keyAc, name);

                    // Обновляем имя в базе данных
                    database.upContactNew(accaunt_id, senderId, nameNew);

                    // Проверяем существование аккаунта
                    boolean result = database.checkAccount(accaunt_id);

                    if (result) {
                        // Шифруем имя для отправки на сервер
                        byte[] resultEn = keyGet.encryptBlock("AES", null, keyAc, name.getBytes());
                        String nameEnc = Base64.getEncoder().encodeToString(resultEn);

                        // Отправляем новое имя на сервер
                        out.writeObject("UP_CONTACT_NEW " + myID + " " + senderId + " " + nameEnc);
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                dialog.dispose();
            } else {
                // Показываем сообщение, если имя пустое
                new Thread(() ->
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame,
                                        "Имя не введено или пустое.",
                                        "Информация",
                                        JOptionPane.INFORMATION_MESSAGE)
                        )
                ).start();
            }
        });

        // Кнопка "Выход"
        JButton exitButton = new JButton();
        ImageIcon exitIcon = new ImageIcon("pictures/exitIcon.png");
        Image scaledExitImage = exitIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
        exitButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        exitButton.setIcon(new ImageIcon(scaledExitImage));
        exitButton.setToolTipText("Закрыть");
        exitButton.addActionListener(e -> dialog.dispose());

        // Добавляем кнопки на панель
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        // Создаём основную панель контента
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.LIGHT_GRAY);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(textPanel, BorderLayout.CENTER);

        // Добавляем панель в диалог
        dialog.add(contentPanel);

        // Отображаем диалог
        dialog.setVisible(true);
    }

    // Метод redrawContactButton обновляет имя на кнопке контакта
    // Находит JLabel с именем и обновляет текст
    // Перерисовывает кнопку
    private static void redrawContactButton(JButton button, String newName) {
        JLabel nameLabel = (JLabel) button.getClientProperty("nameLabel");
        if (nameLabel != null) {
            nameLabel.setText(newName);
        } else {
            // Ищем JLabel в rightPanel
            JPanel rightPanel = (JPanel) button.getComponent(1);
            for (Component comp : rightPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setText(newName);
                    break;
                }
            }
        }
        button.revalidate();
        button.repaint();
    }

}
