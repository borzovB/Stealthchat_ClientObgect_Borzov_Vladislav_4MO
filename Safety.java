package org.face_recognition;

import java.util.UUID;

// Класс для обеспечения безопасности
// Содержит методы для проверки пароля и генерации уникального идентификатора
public class Safety {

    // Метод проверяет, соответствует ли пароль требованиям безопасности
    // Требования: хотя бы одна буква (латиница или кириллица), одна цифра, один спецсимвол, не менее 8 символов
    boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z\\p{IsCyrillic}])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
        return password.matches(passwordPattern);
    }

    // Метод генерирует уникальный идентификатор UUID
    public static String generateUniqueId() {
        UUID uniqueId = UUID.randomUUID();
        return uniqueId.toString();
    }
}
