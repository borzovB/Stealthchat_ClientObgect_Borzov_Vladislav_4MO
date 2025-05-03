package org.face_recognition;

// Класс DemonstrationWindows предоставляет методы для отображения диалоговых окон с изображениями
// и полным текстом сообщений, включая масштабирование изображений и настройку контекстного меню
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class DemonstrationWindows {

    // Экземпляр панели управления для настройки контекстного меню
    private static ControlPanel controlPanel = new ControlPanel();

    // Метод для масштабирования изображения
    static ImageIcon scaleImage(Image originalImage, int newWidth, int newHeight) {
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    // Метод для создания диалогового окна
    private static JDialog createDialog(Frame frame) {
        JDialog dialog = new JDialog(frame, "Изображение", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        return dialog;
    }

    // Отображает диалоговое окно с изображением и поддержкой масштабирования
    public static void picture(ImageIcon scaledIcon, Frame frame, int originalWidth, int originalHeight, Image originalImage) {
        // Создает диалоговое окно
        JDialog dialog = createDialog(frame);
        JLabel fullSizeLabel = new JLabel(scaledIcon);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(fullSizeLabel);

        // Начальная высота для масштабирования (400 пикселей)
        final int[] scalePercent = {400};

        // Применяет начальный масштаб
        SwingUtilities.invokeLater(() -> {
            int newHeight = scalePercent[0];
            int newWidth = (originalWidth * newHeight) / originalHeight;
            Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            fullSizeLabel.setIcon(new ImageIcon(resizedImage));
            int dialogHeight = Math.max(newHeight, 400);
            Dimension newSize = new Dimension(newWidth, dialogHeight + 30);
            dialog.setSize(newSize);
            dialog.setLocationRelativeTo(null);
        });

        // Добавляет обработчик колесика мыши для изменения масштаба
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                int oldHeight = scalePercent[0];
                // Ограничивает масштаб высоты от 400 до 800 пикселей
                if (notches < 0) {
                    scalePercent[0] = Math.min(scalePercent[0] + 10, 800); // Увеличение
                } else {
                    scalePercent[0] = Math.max(scalePercent[0] - 10, 400); // Уменьшение
                }
                if (scalePercent[0] == oldHeight) return;
                // Масштабирует изображение
                int newHeight = scalePercent[0];
                int newWidth = (originalWidth * newHeight) / originalHeight;
                Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                fullSizeLabel.setIcon(new ImageIcon(resizedImage));
                // Обновляет размер окна
                SwingUtilities.invokeLater(() -> {
                    int dialogHeight = Math.max(newHeight, 400);
                    Dimension newSize = new Dimension(newWidth, dialogHeight);
                    dialog.setSize(newSize);
                    dialog.setLocationRelativeTo(null);
                });
            }
        });

        // Добавляет панель в диалог и отображает его
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // Отображает диалоговое окно с полным текстом сообщения
    void showFullTextDialog(String fullText, boolean status, Frame frame) {
        JDialog dialog = new JDialog(frame, "Полный текст", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        // Настраивает текстовую область
        Font customFont = new Font("Dialog", Font.PLAIN, 16);
        JTextArea fullTextArea = new JTextArea(fullText);
        fullTextArea.setFont(customFont);
        fullTextArea.setWrapStyleWord(true);
        fullTextArea.setLineWrap(true);
        fullTextArea.setEditable(false);
        fullTextArea.setForeground(new Color(0, 0, 0));
        controlPanel.configureCopyMenu(fullTextArea); // Настраивает контекстное меню для копирования

        // Устанавливает цвета в зависимости от статуса
        if (status) {
            fullTextArea.setBackground(new Color(173, 216, 230)); // Светло-голубой
            fullTextArea.setSelectionColor(new Color(255, 182, 193)); // Светло-розовый для выделения
            fullTextArea.setSelectedTextColor(new Color(0, 0, 0));
        } else {
            fullTextArea.setBackground(new Color(255, 182, 193)); // Светло-розовый
            fullTextArea.setSelectionColor(new Color(173, 216, 230)); // Светло-голубой для выделения
            fullTextArea.setSelectedTextColor(new Color(0, 0, 0));
        }

        // Создает прокручиваемую область
        JScrollPane scrollPane = new JScrollPane(fullTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setPreferredSize(new Dimension(380, 240));

        // Создает панель содержимого
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(status ? new Color(173, 216, 230) : new Color(255, 182, 193));
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(211, 211, 211), 2));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Добавляет панель в диалог и отображает его
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
}
