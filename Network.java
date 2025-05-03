package org.face_recognition;

// Класс для обеспечения связи с сервером
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Network {

    // Обработка выхода из системы и закрытия сокета
    void exitClient(ObjectOutputStream out, ObjectInputStream in, SSLSocket socket, String myID) {
        try {
            // Проверяем, что все необходимые объекты не равны null
            if (out != null && in != null && socket != null) {
                // Отправляем команду на выход
                out.writeObject("EXIT " + myID);
                out.flush(); // Сбрасываем поток

                // Закрываем все потоки и сокет
                in.close();
                out.close();
                socket.close();
            } else {
                System.err.println("Некоторые объекты равны null. Выход не может быть завершен корректно.");
            }
        } catch (IOException ex) {
            // Логируем ошибку при завершении работы клиента
            System.err.println("Ошибка при выходе клиента: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Закрытие сокета
    void reloadClient(ObjectOutputStream out, ObjectInputStream in, SSLSocket socket) {
        try {
            // Проверяем, что все необходимые объекты не равны null
            if (out != null && in != null && socket != null) {

                // Закрываем все потоки и сокет
                in.close();
                out.close();
                socket.close();
            } else {
                System.err.println("Некоторые объекты равны null. Выход не может быть завершен корректно.");
            }
        } catch (IOException ex) {
            // Логируем ошибку при завершении работы клиента
            System.err.println("Ошибка при выходе клиента: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
