package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.awt.geom.Ellipse2D;

//Класс, который содержит метод для работы с иконками контактов
public class ImageViewer extends Component {
    // Экземпляр класса для расшифровки файлов
    private static DecryptFile decryptFile = new DecryptFile();
    // Ключ для расшифровки изображений
    private static String keyAcc;

    // Конструктор, инициализирующий ключ для расшифровки
    public ImageViewer(String keyAc) {
        this.keyAcc = keyAc;
    }

    // Загружает изображение из зашифрованного файла (output.bin)
    BufferedImage loadImageFromFile(String image_id) {
        try {
            // Создает объект файла по указанному идентификатору
            File file = new File(image_id);
            // Расшифровывает содержимое файла с использованием AES
            byte[] resultImageByte = decryptFile.playImageFromBinFile(file, "AES", keyAcc);
            // Преобразует массив байтов в изображение
            return ImageIO.read(new ByteArrayInputStream(resultImageByte));
        } catch (IOException ex) {
            // Показывает сообщение об ошибке при неудачной загрузке файла
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки файла output.bin: " + ex.getMessage());
            return null;
        }
    }

    // Создает круглое изображение с черной рамкой
    BufferedImage createCircularImageWithBorder(BufferedImage originalImage) {
        // Проверяет, что исходное изображение не null
        if (originalImage == null) {
            return null;
        }

        // Задает диаметр круглого изображения
        int diameter = 65;
        // Задает толщину черной рамки
        int borderThickness = 4;
        // Вычисляет размер изображения с учетом рамки
        int sizeWithBorder = diameter + borderThickness * 2;

        // Создает новое изображение с прозрачным фоном
        BufferedImage circularImage = new BufferedImage(sizeWithBorder, sizeWithBorder, BufferedImage.TYPE_INT_ARGB);
        // Получает графический контекст для рисования
        Graphics2D g2d = circularImage.createGraphics();

        // Включает сглаживание для улучшения качества изображения
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисует черный круг для рамки
        g2d.setColor(Color.BLACK);
        g2d.fillOval(0, 0, sizeWithBorder, sizeWithBorder);

        // Устанавливает область отсечения в виде круга для изображения
        g2d.setClip(new Ellipse2D.Double(borderThickness, borderThickness, diameter, diameter));

        // Вычисляет масштаб для пропорционального уменьшения изображения
        double scale = Math.max((double) diameter / originalImage.getWidth(), (double) diameter / originalImage.getHeight());
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);

        // Центрирует изображение внутри круга
        int x = (sizeWithBorder - newWidth) / 2;
        int y = (sizeWithBorder - newHeight) / 2;

        // Рисует масштабированное изображение
        g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
        // Освобождает ресурсы графического контекста
        g2d.dispose();

        return circularImage;
    }
}
