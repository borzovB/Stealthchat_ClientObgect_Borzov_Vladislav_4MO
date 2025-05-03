package org.face_recognition;

// Toggle — компонент переключателя для параметров пользователя (например, синхронизация, шифрование, доступ)
// Поддерживает анимацию и сохраняет состояние параметра в базе данных

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

class Toggle extends JPanel {
    private boolean isOn; // Текущее состояние переключателя
    private int circleX; // Координата круга
    private static Database database = new Database(); // Работа с базой данных
    private boolean isAnimating = false; // Флаг выполнения анимации
    public String str; // Идентификатор параметра
    private static final int PADDING = 20; // Отступы слева и справа
    private static final int CIRCLE_SIZE = 20; // Размер круга
    private static final int TRACK_HEIGHT = 20; // Высота трека

    protected Toggle(String accauntId, String str, ObjectOutputStream out, String myID) throws SQLException {
        this.str = str;

        // Получение текущего состояния переключателя из базы данных
        if (str.equals("1a")) {
            this.isOn = database.checkAccount(accauntId);
        } else {
            if (str.equals("2a")) {
                this.isOn = database.getSynchronizationLocks(accauntId);
            } else {
                this.isOn = database.getEncryptionData(accauntId);
            }
        }

        setPreferredSize(new Dimension(110, 40)); // Размер компонента
        setBackground(Color.WHITE); // Цвет фона

        // Обработка клика по переключателю
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isAnimating) {
                    isOn = !isOn; // Инверсия состояния
                    animateSwitch(); // Запуск анимации

                    try {
                        // Обработка по типу параметра
                        if (str.equals("1a")) {
                            database.updateAccountStatus(accauntId, isOn); // Обновление аккаунта
                            if (isOn) {
                                MainPage.dataReload(); // Обновление данных
                            }
                        } else {
                            if (str.equals("2a")) {
                                database.setSynchronizationLocks(accauntId, isOn); // Обновление синхронизации

                                if (isOn) {
                                    String[] blockFriend = database.getClientBlockArray(accauntId, myID);
                                    out.writeObject(blockFriend); // Отправка списка блокировки
                                }
                            } else {
                                database.setEncryptionData(accauntId, isOn); // Обновление шифрования
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    // Геттер состояния переключателя
    public boolean isOn() {
        return isOn;
    }

    // Отрисовка компонента
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int trackWidth = getWidth() - 2 * PADDING; // Ширина трека
        int trackY = (getHeight() - TRACK_HEIGHT) / 2; // Y-позиция трека

        g.setColor(isOn ? Color.LIGHT_GRAY : Color.DARK_GRAY); // Цвет трека
        g.fillRoundRect(PADDING, trackY, trackWidth, TRACK_HEIGHT, 20, 20); // Рисуем трек

        int minX = PADDING;
        int maxX = getWidth() - PADDING - CIRCLE_SIZE;

        if (circleX == 0 && !isAnimating) {
            circleX = isOn ? maxX : minX; // Начальное положение круга
        }

        g.setColor(Color.BLUE);
        g.fillOval(circleX, trackY, CIRCLE_SIZE, CIRCLE_SIZE); // Рисуем круг

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String modeText = isOn ? "ДА" : "НЕТ"; // Подпись режима
        g.drawString(modeText, getWidth() / 2 - 10, trackY + TRACK_HEIGHT / 2 + 5); // Отображение текста
    }

    // Анимация переключателя
    private void animateSwitch() {
        isAnimating = true;

        int targetX = isOn ? getWidth() - PADDING - CIRCLE_SIZE : PADDING;

        Timer timer = new Timer(10, e -> {
            if ((isOn && circleX < targetX) || (!isOn && circleX > targetX)) {
                circleX += isOn ? 2 : -2; // Движение круга
                repaint();
            } else {
                circleX = targetX; // Установка финального положения
                ((Timer) e.getSource()).stop(); // Остановка таймера
                isAnimating = false;
            }
        });

        timer.start(); // Запуск анимации
    }
}
