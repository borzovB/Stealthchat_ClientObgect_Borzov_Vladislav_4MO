package org.face_recognition;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Класс для загрузки конфигурационных параметров из файла конфигурации config.properties
public class Config {

    // Метод для загрузки настроек сервера
    public static String[] configServer() {
        Properties properties = new Properties();
        String[] configServer = new String[5]; // Массив для 5 параметров

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            // Загружаем данные из файла
            properties.load(fis);

            // Читаем параметры сервера и заполняем массив
            configServer[0] = properties.getProperty("db.name_trast");           // Порт сервера
            configServer[1] = properties.getProperty("db.client");               // Имя ключа
            configServer[2] = properties.getProperty("db.name_ssl");                 // Адрес сервера

        } catch (IOException e) {
            System.err.println("Ошибка при загрузке файла конфигурации: " + e.getMessage());
        }

        return configServer;
    }

    // Метод для получения порта сервера
    public static int configPort() {
        Properties properties = new Properties();
        int PORT = 0;

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            // Загружаем данные из файла
            properties.load(fis);

            // Читаем параметры сервера и заполняем массив
            String configServer = properties.getProperty("db.port_server"); // Порт сервера
            PORT = Integer.parseInt(configServer);

        } catch (IOException e) {
            System.err.println("Ошибка при загрузке файла конфигурации: " + e.getMessage());
        }

        return PORT;
    }

}
