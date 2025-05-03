package org.face_recognition;

//Класс для ввода IP
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ChatClientWithIP {

    public static String myIP;
    private static Menu menu;
    private static int width;
    private static int height;
    public ChatClientWithIP() {
        // Создаем окно
        JFrame frame = new JFrame("IP Window");
        frame.setSize(290, 200);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Создаем метку
        JLabel label = new JLabel("Введите IP:");
        label.setBounds(20, 20, 100, 30);
        frame.add(label);

        // Создаем текстовое поле
        JTextField textField = new JTextField();
        textField.setBounds(120, 20, 150, 30);
        frame.add(textField);

        // Создаем кнопку "ОК"
        JButton okButton = new JButton("OK");
        okButton.setBounds(100, 70, 80, 30);
        frame.add(okButton);

        // Добавляем обработчик нажатия кнопки
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myIP = textField.getText();
                frame.dispose(); // Закрываем окно
                menu = new Menu(myIP, width, height);
                menu.start();
            }
        });

        // Добавляем слушатель для изменения размера окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();  // Получаем ширину окна
                height = frame.getHeight(); // Получаем высоту окна
            }
        });

        // Отображаем окно
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientWithIP());
    }

}
