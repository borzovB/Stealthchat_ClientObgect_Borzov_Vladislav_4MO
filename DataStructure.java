package org.face_recognition;

// Класс DataStructure предоставляет методы для обработки и парсинга строковых данных,
// включая разбор ответа синхронизации чатов и разделение строки на элементы
import java.util.ArrayList;
import java.util.List;

public class DataStructure {

    // Парсит ответ синхронизации чатов, извлекая данные о контактах
    public String[] parseSyncResponse(String response) {
        // Проверяет, является ли ответ null или не начинается с ожидаемого префикса
        if (response == null || !response.startsWith("SYNCHRONY_OLL_CHAT_PLUSS")) {
            return new String[0]; // Возвращает пустой массив в случае ошибки
        }

        // Удаляет префикс "SYNCHRONY_OLL_CHAT_PLUSS" и обрезает пробелы
        response = response.substring("SYNCHRONY_OLL_CHAT_PLUSS".length()).trim();

        // Если строка пустая после удаления префикса, возвращает пустой массив
        if (response.isEmpty()) {
            return new String[0];
        }

        // Разделяет строку на части по разделителю " | "
        String[] parts = response.split(" \\| ");

        List<String> result = new ArrayList<>();

        // Обрабатывает каждую часть
        for (String part : parts) {
            String[] contactInfo = part.split(" "); // Разделяет часть на элементы
            // Проверяет, что часть содержит минимум 4 элемента (userId, contactId, contactName, sessionKeyReserve)
            if (contactInfo.length >= 4) {
                result.add(part); // Добавляет часть в результат в исходном формате
            }
        }

        // Преобразует список в массив и возвращает
        return result.toArray(new String[0]);
    }

    // Разделяет входную строку на элементы по пробелу
    public static String[] splitString(String input) {
        return input.split(" ");
    }
}
