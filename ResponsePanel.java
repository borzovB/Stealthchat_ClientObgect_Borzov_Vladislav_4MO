package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

// Класс представляет панель, где отображается ответ на заявку в друзья
public class ResponsePanel extends JPanel {

    // Конструктор панели, принимает ID отправителя, статус заявки, флаг блокировки и слушатель мыши
    public ResponsePanel(String senderId, boolean requestStatus, boolean lockFlag, MouseAdapter popupListener) {
        // Главная панель с отступами и фиксированным размером
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));
        Dimension panelSize = new Dimension(500, 230);
        setPreferredSize(panelSize);
        setMinimumSize(panelSize);
        setMaximumSize(panelSize);

        // Левая панель, где располагается текстовая информация
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.addMouseListener(popupListener);
        leftPanel.setBackground(new Color(245, 245, 245));
        Dimension leftPanelSize = new Dimension(340, 210);
        leftPanel.setPreferredSize(leftPanelSize);
        leftPanel.setMinimumSize(leftPanelSize);
        leftPanel.setMaximumSize(leftPanelSize);

        // Панель заголовка с информацией об отправителе
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.addMouseListener(popupListener);
        headerPanel.setBackground(new Color(245, 245, 245));
        Dimension headerSize = new Dimension(320, 50);
        headerPanel.setPreferredSize(headerSize);
        headerPanel.setMinimumSize(headerSize);
        headerPanel.setMaximumSize(headerSize);

        // Метка заголовка
        JLabel senderLabel = new JLabel("Получен ответ от:");
        senderLabel.addMouseListener(popupListener);
        senderLabel.setFont(new Font("Arial", Font.BOLD, 16));
        senderLabel.setForeground(new Color(33, 150, 243));
        headerPanel.add(senderLabel);

        // Метка с ID отправителя
        JLabel statusLabelId = new JLabel(senderId);
        statusLabelId.addMouseListener(popupListener);
        statusLabelId.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabelId.setForeground(new Color(66, 66, 66));
        headerPanel.add(statusLabelId);

        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // Панель со статусами
        JPanel statusPanel = new JPanel();
        statusPanel.addMouseListener(popupListener);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(new Color(245, 245, 245));
        Dimension statusSize = new Dimension(320, 140);
        statusPanel.setPreferredSize(statusSize);
        statusPanel.setMinimumSize(statusSize);
        statusPanel.setMaximumSize(statusSize);

        // Заголовок статуса заявки
        JLabel requestStatusHeader = new JLabel("Статус заявки:");
        requestStatusHeader.addMouseListener(popupListener);
        requestStatusHeader.setFont(new Font("Arial", Font.BOLD, 16));
        requestStatusHeader.setForeground(new Color(33, 150, 243));
        statusPanel.add(requestStatusHeader);
        statusPanel.add(Box.createVerticalStrut(5)); // Отступ перед значением

        // Отображение текста в зависимости от статуса заявки
        JLabel statusLabel = new JLabel(requestStatus ? "Заявка принята" : "Заявка не принята");
        statusLabel.addMouseListener(popupListener);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(66, 66, 66));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(10)); // Отступ между секциями

        // Заголовок статуса блокировки
        JLabel lockStatusHeader = new JLabel("Статус блокировки:");
        lockStatusHeader.addMouseListener(popupListener);
        lockStatusHeader.setFont(new Font("Arial", Font.BOLD, 16));
        lockStatusHeader.setForeground(new Color(33, 150, 243));
        statusPanel.add(lockStatusHeader);
        statusPanel.add(Box.createVerticalStrut(5)); // Отступ перед значением

        // Отображение текста в зависимости от флага блокировки
        JLabel lockLabel = new JLabel(lockFlag ? "Вы были заблокированы" : "Пользователю доступ разрешён");
        lockLabel.addMouseListener(popupListener);
        lockLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lockLabel.setForeground(new Color(66, 66, 66));
        statusPanel.add(lockLabel);

        leftPanel.add(statusPanel, BorderLayout.CENTER);

        // Правая панель для выравнивания с другими панелями, может быть пустой
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 70));
        rightPanel.setBackground(new Color(245, 245, 245));
        Dimension rightPanelSize = new Dimension(120, 210);
        rightPanel.setPreferredSize(rightPanelSize);
        rightPanel.setMinimumSize(rightPanelSize);
        rightPanel.setMaximumSize(rightPanelSize);
        rightPanel.addMouseListener(popupListener);

        // Добавляем левые и правые панели в основную панель
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    // Метод для удобного создания экземпляра панели
    public static ResponsePanel createPanel(String senderId, boolean requestStatus, boolean lockFlag, MouseAdapter popupListener) {
        return new ResponsePanel(senderId, requestStatus, lockFlag, popupListener);
    }

}
