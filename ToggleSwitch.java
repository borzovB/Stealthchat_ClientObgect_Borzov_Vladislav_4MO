package org.face_recognition;

// ToggleSwitch — это компонент для смены темы интерфейса (тёмная/светлая)
// Он применяет соответствующие цвета ко всем связанным элементам UI
// Также анимирует переключатель и сохраняет состояние темы в базу данных

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class ToggleSwitch extends JPanel {
    private final JPanel panel;
    private boolean isOn = false;
    private int circleX;
    private int lightenFactor = 20; // Степень осветления для тёмной темы
    private Color darkBackground = Color.DARK_GRAY;
    private Color darkTextColor = Color.WHITE;
    private Color lightTextColor = Color.BLACK;
    private Color buttonPanelDarkColor = new Color(80, 80, 80);
    private Color lightBackground = Color.WHITE;
    private Color buttonPanelLightColor = Color.LIGHT_GRAY;
    private JFrame frame;
    private static JTextArea textsClient;
    private JTextArea textMesseng;
    private DynamicPanelScroll dynamicPanel;
    private static Color background;
    private static Color textColor;
    private boolean isAnimating = false; // Предотвращает одновременные анимации
    private static Database database = new Database();
    private static JLabel nameLabel;

    public ToggleSwitch(JFrame frame, JPanel panel, JTextArea textsClient, JTextArea textMesseng,
                        DynamicPanelScroll dynamicPanel, String accaunt_id, JLabel nameLabel) throws SQLException {
        this.frame = frame;
        this.panel = panel;
        this.dynamicPanel = dynamicPanel;
        this.textsClient = textsClient;
        this.textMesseng = textMesseng;
        this.nameLabel = nameLabel;

        setPreferredSize(new Dimension(170, 50)); // Размер компонента
        setBackground(Color.WHITE); // Начальный фон

        isOn = database.getLite(accaunt_id); // Получаем сохранённое состояние темы

        circleX = isOn ? 145 : 5; // Положение круга в зависимости от состояния

        toggleTheme(isOn); // Применяем тему при запуске

        // Обработка клика по переключателю
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isAnimating) {
                    try {
                        isOn = !isOn; // Переключение состояния
                        toggleTheme(isOn); // Применение новой темы
                        animateSwitch(); // Запуск анимации
                        database.getLiteNew(accaunt_id, isOn); // Сохранение состояния в базе
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    // Применяет тему ко всем связанным компонентам
    public void toggleTheme(boolean isDarkMode) {
        background = isDarkMode ? darkBackground : lightBackground;
        textColor = isDarkMode ? darkTextColor : lightTextColor;
        Color buttonPanelColor = isDarkMode ? buttonPanelDarkColor : buttonPanelLightColor;

        panel.setBackground(background);
        textsClient.setBackground(background);
        textsClient.setForeground(textColor);
        textMesseng.setBackground(background);
        textMesseng.setForeground(textColor);

        // Обновление цвета всех кнопок внутри панели
        if (panel.getComponentCount() > 0) {
            Component[] buttonComponents = ((JPanel) ((JScrollPane) panel.getComponent(0)).getViewport().getView()).getComponents();
            for (Component comp : buttonComponents) {
                if (comp instanceof JButton button) {
                    button.setBackground(buttonPanelColor);
                    for (Component innerComp : button.getComponents()) {
                        if (innerComp instanceof JPanel innerPanel) {
                            for (Component subComp : innerPanel.getComponents()) {
                                if (subComp instanceof JLabel label) {
                                    label.setForeground(textColor);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (dynamicPanel != null) {
            dynamicPanel.updatePanelColor(background); // Обновление цвета вложенной панели
        }

        frame.repaint(); // Перерисовка интерфейса
    }

    // Отрисовка переключателя
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(isOn ? Color.LIGHT_GRAY : Color.DARK_GRAY); // Цвет фона переключателя
        g.fillRoundRect(0, 10, getWidth() - 1, 30, 30, 30); // Рисуем фон

        g.setColor(Color.WHITE);
        g.fillOval(circleX, 15, 20, 20); // Рисуем круг

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String modeText = isOn ? "ТЁМНАЯ ТЕМА" : "СВЕТЛАЯ ТЕМА"; // Текст темы
        g.drawString(modeText, getWidth() / 2 - 30, 30); // Отображение текста
    }

    // Анимация движения круга переключателя
    private void animateSwitch() {
        isAnimating = true;

        Timer timer = new Timer(10, e -> {
            if (isOn && circleX < getWidth() - 25) {
                circleX += 2; // Движение вправо
                repaint();
            } else if (!isOn && circleX > 5) {
                circleX -= 2; // Движение влево
                repaint();
            } else {
                ((Timer) e.getSource()).stop(); // Остановка анимации
                isAnimating = false;
            }
        });

        timer.setInitialDelay(0);
        timer.start(); // Запуск таймера
    }

    // Установка панели для обновления цвета
    public void setDynamicPanel(DynamicPanelScroll dynamicPanel) {
        this.dynamicPanel = dynamicPanel;

        if (this.dynamicPanel != null) {
            this.dynamicPanel.updatePanelColor(background);
        }
    }

    // Установка метки имени
    public void setDynamicJLay(JLabel nameLabel) {
        this.nameLabel = nameLabel;
    }

    // Получение текущего цвета панели
    public Color getPanelColor() {
        if (isOn) {
            Color darkGray = Color.DARK_GRAY;
            int r = Math.min(255, darkGray.getRed() + lightenFactor);
            int g = Math.min(255, darkGray.getGreen() + lightenFactor);
            int b = Math.min(255, darkGray.getBlue() + lightenFactor);
            return new Color(r, g, b);
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    // Получение текущего цвета текста
    public Color getTextColor() {
        return isOn ? Color.WHITE : Color.BLACK;
    }
}
