package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

// Класс для создания панели, отображающей информацию о заблокированном пользователе с возможностью разблокировки
public class PanelFrandsBlock extends JPanel {

    // Конструктор панели
    public PanelFrandsBlock(String friendID, String recordsID, ObjectOutputStream out, String myID,
                            DynamicPanelScrollNotifications dynamicPanelScrollNotifications, JPanel panel) {
        // Установка компоновки и стилей главной панели
        setLayout(new BorderLayout(10, 10)); // BorderLayout с отступами 10 пикселей
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Внешние отступы
        setBackground(new Color(245, 245, 245)); // Светло-серый фон
        Dimension panelSize = new Dimension(500, 230); // Фиксированный размер панели
        setPreferredSize(panelSize); // Предпочтительный размер
        setMinimumSize(panelSize); // Минимальный размер
        setMaximumSize(panelSize); // Максимальный размер

        // Левая панель для текста и кнопки
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10)); // BorderLayout с отступами
        leftPanel.setBackground(new Color(245, 245, 245)); // Светло-серый фон
        Dimension leftPanelSize = new Dimension(340, 210); // Размер левой панели
        leftPanel.setPreferredSize(leftPanelSize); // Предпочтительный размер
        leftPanel.setMinimumSize(leftPanelSize); // Минимальный размер
        leftPanel.setMaximumSize(leftPanelSize); // Максимальный размер

        // Панель заголовка с информацией о пользователе и кнопкой
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 5)); // GridLayout с 3 строками и отступами
        headerPanel.setBackground(new Color(245, 245, 245)); // Светло-серый фон
        Dimension headerSize = new Dimension(320, 80); // Размер заголовка
        headerPanel.setPreferredSize(headerSize); // Предпочтительный размер
        headerPanel.setMinimumSize(headerSize); // Минимальный размер
        headerPanel.setMaximumSize(headerSize); // Максимальный размер

        // Метка с текстом "Заблокирован пользователь"
        JLabel senderLabel = new JLabel("Заблокирован пользователь:");
        senderLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Жирный шрифт
        senderLabel.setForeground(new Color(33, 150, 243)); // Синий цвет текста
        headerPanel.add(senderLabel); // Добавление метки

        // Метка с ID заблокированного пользователя
        JLabel statusLabelId = new JLabel(friendID);
        statusLabelId.setFont(new Font("Arial", Font.PLAIN, 14)); // Обычный шрифт
        statusLabelId.setForeground(new Color(66, 66, 66)); // Темно-серый цвет текста
        headerPanel.add(statusLabelId); // Добавление метки

        // Кнопка "Разблокировать"
        JButton button = new JButton("Разблокировать");
        button.setPreferredSize(new Dimension(150, 30)); // Фиксированный размер кнопки

        // Обработчик нажатия на кнопку
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Отправка команды на сервер для разблокировки клиента
                    out.writeObject("NOT_BLOCK_KLIENT " + friendID + " " + myID);
                    // Удаление записи о заблокированном клиенте из интерфейса
                    dynamicPanelScrollNotifications.deleteBlockClient(panel, recordsID);
                } catch (IOException ex) {
                    throw new RuntimeException(ex); // Обработка ошибок ввода-вывода
                }
            }
        });

        headerPanel.add(button); // Добавление кнопки в заголовок

        leftPanel.add(headerPanel, BorderLayout.NORTH); // Добавление заголовка в левую панель

        // Правая панель (пустая, для согласованности с другими панелями, например, FriendRequestPanel)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 70)); // FlowLayout с отступами
        rightPanel.setBackground(new Color(245, 245, 245)); // Светло-серый фон
        Dimension rightPanelSize = new Dimension(120, 210); // Размер правой панели
        rightPanel.setPreferredSize(rightPanelSize); // Предпочтительный размер
        rightPanel.setMinimumSize(rightPanelSize); // Минимальный размер
        rightPanel.setMaximumSize(rightPanelSize); // Максимальный размер

        // Добавление панелей на главную панель
        add(leftPanel, BorderLayout.WEST); // Левая панель слева
        add(rightPanel, BorderLayout.EAST); // Правая панель справа
    }

    // Статический метод для создания и возврата новой панели
    public static PanelFrandsBlock createPanel(String friendID, String recordsID, ObjectOutputStream out,
                                               String myID, DynamicPanelScrollNotifications dynamicPanelScrollNotifications,
                                               JPanel panel) {
        return new PanelFrandsBlock(friendID, recordsID, out, myID, dynamicPanelScrollNotifications, panel);
    }
}
