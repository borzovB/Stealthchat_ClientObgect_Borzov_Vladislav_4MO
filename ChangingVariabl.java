package org.face_recognition;

import javax.swing.*;
import java.awt.*;

// Класс ChangingVariabl предоставляет утилитные методы для работы с цветами панелей,
// рекурсивного обновления их фона и обработки строк в приложении
public class ChangingVariabl {

    // Метод для получения фона панели в зависимости от статуса
    static Color getPanelBackgroundColor(boolean status) {
        return status ? new Color(173, 216, 230) : new Color(255, 182, 193);
    }

    // Метод removeParts удаляет указанные подстроки из входной строки
    // Проверяет, не являются ли входные параметры null
    // Удаляет каждую указанную подстроку из входной строки
    // Возвращает результирующую строку
    public static String removeParts(String input, String... partsToRemove) {
        if (input == null || partsToRemove == null) {
            return input;
        }
        for (String part : partsToRemove) {
            input = input.replace(part, ""); // Убираем каждую часть из строки
        }
        return input;
    }

    // Метод для обновления цвета родительской панели на n уровней выше
    private void updateParentPanelColor(JPanel panel, Color backgroundColor, int levelsUp) {
        // Ищем родительскую панель на заданное количество уровней вверх
        Container targetPanel = panel;
        for (int i = 0; i < levelsUp; i++) {
            if (targetPanel != null) {
                // Переходим на уровень вверх
                targetPanel = targetPanel.getParent();
            } else {
                return; // Если достигли вершины и родительской панели нет — выходим
            }
        }

        // Если нашли панель на нужном уровне, обновляем её цвет
        if (targetPanel instanceof JPanel parentPanel) {
            parentPanel.setBackground(backgroundColor); // Изменяем цвет родительской панели
        }
    }

    // Метод updatePanelColorsRecursively рекурсивно ищет JScrollPane с JTextArea
    // внутри панели и обновляет цвет фона родительской панели
    boolean updatePanelColorsRecursively(JPanel panel, Color backgroundColor) {
        boolean found = false; // Флаг для отслеживания, нашли ли мы нужный компонент

        // Перебираем все компоненты в текущей панели
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel innerPanel = (JPanel) comp;

                // Рекурсивно вызываем для вложенной панели
                if (updatePanelColorsRecursively(innerPanel, backgroundColor)) {
                    found = true; // Если в дочерней панели найден нужный компонент, ставим флаг в true
                }

                // Проверяем наличие JScrollPane внутри панели
                for (Component innerComp : innerPanel.getComponents()) {
                    if (innerComp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) innerComp;

                        // Проверяем, есть ли в JScrollPane JTextArea
                        for (Component scrollPaneComp : scrollPane.getViewport().getComponents()) {
                            if (scrollPaneComp instanceof JTextArea) {
                                updateParentPanelColor(innerPanel, backgroundColor, 1);
                                found = true; // Устанавливаем флаг в true
                                break; // Выходим из цикла, так как нашли нужный компонент
                            }
                        }
                    }
                }
            }
        }

        return found; // Возвращаем true, если нашли JScrollPane с JTextArea, иначе false
    }

}
