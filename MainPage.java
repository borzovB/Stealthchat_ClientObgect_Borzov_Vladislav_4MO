package org.face_recognition;

// Главная страница для переписки
// Этот класс представляет основную панель приложения StealthChat, обеспечивающую функциональность чата
// Управляет отображением списка контактов, отправкой и получением сообщений (текст, файлы, голосовые),
// взаимодействием с сервером через SSL-соединение
// Поддерживает динамическую загрузку контактов, шифрование сообщений (RSA, AES, Serpent),
// управление уведомлениями и настройками учетной записи через меню

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.awt.event.*;

public class MainPage extends Component {

    // Объект базы данных для работы с SQLite
    private static Database database;
    // Поток вывода объектов для отправки данных на сервер
    private static ObjectOutputStream out;
    // Поток ввода объектов для получения данных от сервера
    private static ObjectInputStream in;
    // IP-адрес клиента
    private static String myIP;
    // Количество добавленных элементов при прокрутке
    private static int slider;
    // Параметры SSL-соединения
    private static String connect;
    // Порт для соединения с сервером
    private static int PORT;
    // Идентификатор текущего пользователя
    public static String myID;
    // Главное окно приложения
    private static JFrame frame;
    // Основная панель интерфейса
    private static JPanel panel = new JPanel();
    // SSL-сокет для безопасного соединения с сервером
    private static SSLSocket socket;
    // Панель для отображения кнопок контактов
    private static JPanel buttonPanel;
    // Цвет фона интерфейса (по умолчанию белый)
    private static Color lightBackground = Color.WHITE;
    // Цвет фона панели контактов
    private static Color panelColor;
    // Переключатель тем (светлая/темная)
    private static ToggleSwitch toggleSwitch;
    // Цвет текста кнопок
    private static Color textButton;
    // Цвет фона панели кнопок в светлой теме
    private static Color buttonPanelLightColor;
    // Текстовое поле для отображения сообщений клиента (не используется)
    private static JTextArea textsClient;
    // Текстовое поле для ввода сообщений
    private static JTextArea textMesseng;
    // Асинхронный результат проверки статуса контакта (онлайн/оффлайн)
    private static CompletableFuture<Boolean> futureResponse;
    // Асинхронный результат проверки наличия контакта для планирования чата
    private static CompletableFuture<Boolean> contactChat;
    // Асинхронный результат получения данных нового контакта
    private static CompletableFuture<String> newFreand;
    // Асинхронный сигнал завершения обработки перетаскивания файла
    private static CompletableFuture<Void> start_input;
    // Идентификатор текущего контакта для чата
    public static String ipContact = null;
    // Имя получателя сообщения
    public static String recipientName;
    // Лимит контактов для загрузки за один запрос
    private static final int limit = 7;
    // Смещение для SQL-запросов при динамической загрузке контактов
    private static int offset = 0;
    // Имя файла базы данных SQLite
    private static final String DATABASE_NAME = "user_accounts.db";
    // URL для подключения к базе данных
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    // Имя таблицы для хранения чатов
    private static final String TABLE_NAME_NUMBER_2 = "chats";
    // Объект для работы с файлами (выбор, отправка, удаление)
    private static Fileslock fileslock = new Fileslock();
    // Объект для управления контактами и их отображением
    private static ContactPanel contactPanel = new ContactPanel();
    // Объект для получения конфигурации сервера
    private static Config config = new Config();
    // Массив параметров конфигурации сервера
    private static String[] conf;
    // Флаг выполнения загрузки контактов
    private static boolean isLoading = false;
    // Счетчик прокрутки для управления загрузкой контактов
    private static int scroling = 0;
    // Счетчик добавленных контактов
    private static int plussContact = 0;
    // Идентификатор учетной записи
    private static String accaunt_id;
    // Объект для генерации уникальных идентификаторов
    private static Safety safety = new Safety();
    // Асинхронный результат получения ключа шифрования
    private static CompletableFuture<String> queueKey;
    // Генератор случайных чисел
    private static Random random = new Random();
    // Набор символов для генерации уникальных имен файлов
    private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // Пара ключей RSA для шифрования
    private static KeyPair keyPair;
    // Объект для генерации и обработки ключей шифрования
    private static KeyGet keyGet = new KeyGet();
    // Закрытый ключ в строковом формате
    private static String privateKeyStr;
    // Панель прокрутки для текстового поля сообщений
    private static JScrollPane textAreaScroll;
    // Панель для динамического отображения сообщений чата
    private static DynamicPanelScroll dynamicPanel;
    // Текстовое поле для отображения сообщений
    private static JTextArea messageField;
    // Панель прокрутки для динамической панели сообщений
    public static JScrollPane dynamicPanelScrollPane;
    // Флаг состояния записи аудио
    private static boolean isRecording = false;
    // Иконка кнопки записи голосового сообщения (активное состояние)
    private static ImageIcon iconToggled;
    // Иконка кнопки записи голосового сообщения (неактивное состояние)
    private static ImageIcon iconNormal;
    // Кнопка для записи и отправки голосового сообщения
    private static JButton sendButtonVois;
    // Буфер для записи аудиоданных
    private static ByteArrayOutputStream audioBuffer;
    // Микрофон для записи голосовых сообщений
    private static TargetDataLine microphone;
    // Поток для записи аудио
    private static Thread recordingThread;
    // Путь к файлу для отправки
    private static String filePath;
    // Объект меню учетной записи
    private static MenuAccaunt menuAccaunt;
    // Объект для настройки элементов интерфейса
    private static ControlPanel controlPanel = new ControlPanel();
    // Панель прокрутки для списка контактов
    public static JScrollPane scrollPane;
    // Метка для отображения имени контакта
    private static JLabel nameLabel;
    // Объект для работы с изображениями (аватары контактов)
    private static ImageViewer imageViewer;
    // Объект для шифрования данных учетной записи
    private static EncryptionAccaunt encryption = new EncryptionAccaunt();
    // Флаг динамической загрузки контактов
    private static boolean isDynamicLoading = false;
    // Очередь для асинхронной отправки сообщений на сервер
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    // Асинхронный результат синхронизации контактов
    private static CompletableFuture<String> contact;
    // Объект для расшифровки файлов
    private static DecryptFile decryptFil = new DecryptFile();
    // Объект для обработки уведомлений
    private static NotificationDaemon notificationDaemon;
    // Ключ шифрования учетной записи
    private static String keyAc;
    // Ширина окна приложения
    private static int width;
    // Высота окна приложения
    private static int height;

    // Конструктор класса, инициализирующий основные параметры
    public MainPage(String myIP, String myID, int PORT, String keyAc, String connect,
                    String accaunt_id, int height, int width) {
        this.myID = myID; // Установка идентификатора пользователя
        this.myIP = myIP; // Установка IP-адреса клиента
        this.PORT = PORT; // Установка порта
        conf = config.configServer(); // Получение конфигурации сервера
        MainPage.keyAc = keyAc; // Установка ключа шифрования
        this.connect = connect; // Установка параметров SSL-соединения
        this.accaunt_id = accaunt_id; // Установка идентификатора учетной записи
        this.width = width; // Установка ширины окна
        this.height = height; // Установка высоты окна
    }

    // Обновляет ключ шифрования учетной записи и перезапускает интерфейс, после обновления ключей
    // безопасности и алгоритмы
    public static void updateKeyAc(String newKey) {
        // Останавливает уведомления
        notificationDaemon.stop();

        // Закрывает соединение с сервером
        Network network = new Network();
        network.reloadClient(out, in, socket); // Вызывает метод для корректного закрытия соединения

        // Сбрасывает статические компоненты перед перерисовкой
        resetStaticComponents();

        // Закрывает текущее окно, если оно существует
        if (frame != null) {
            frame.dispose();
        }

        // Обновляет ключ шифрования
        keyAc = newKey;

        // Перезапускает интерфейс
        showMainPage();
    }

    // Инициализирует и отображает главную панель приложения
    static void showMainPage() {
        try {

            // Устанавливает цвета текста и панели в зависимости от темы
            if(!database.getLite(accaunt_id)){
                textButton = Color.BLACK;
                buttonPanelLightColor = Color.LIGHT_GRAY;
            }else {
                textButton = Color.WHITE;
                buttonPanelLightColor = new Color(80, 80, 80);
            }

            // Инициализирует демон уведомлений
            notificationDaemon = new NotificationDaemon(accaunt_id, keyAc);

            // Настраивает параметры SSL-соединения
            System.setProperty(conf[0], conf[1]);
            System.setProperty(conf[2], connect);

            // Инициализирует объект для работы с изображениями
            imageViewer = new ImageViewer(keyAc);

            // Устанавливает SSL-соединение с сервером
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) ssf.createSocket(myIP, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Инициализирует объект базы данных
            database = new Database();

            //Удаление сообщений у которых нет файла
            database.delMessangNot();

            // Отправляет имя пользователя на сервер
            out.writeObject("NAME " + myID);

            // Запускает поток для обработки ответов от сервера
            new Thread(() -> {
                try {
                    handleServerResponses();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            // Настройка фрейма и панели
            frame = new JFrame("Client");
            frame.setSize(1000, 600);

            // Устанавливаем минимальный размер окна
            Dimension minSize = new Dimension(1040, 640);
            frame.setMinimumSize(minSize);

            // Размещаем окно по центру экрана
            frame.setLocationRelativeTo(null);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Заменяем EXIT_ON_CLOSE на DISPOSE_ON_CLOSE

            // Добавляет обработчик закрытия окна
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        // Сохраняет данные чатов, если учетная запись активна
                        boolean result = database.checkAccount(accaunt_id);
                        if (result) {
                            String[] keyResult = database.getChatsInfo(accaunt_id, myID, keyAc);
                            if (keyResult.length > 2) {
                                out.writeObject(keyResult);
                                out.flush(); // Гарантирует отправку данных
                            }
                        }

                        // Останавливает уведомления
                        notificationDaemon.stop();

                        // Закрывает сетевое соединение
                        Network network = new Network();
                        network.exitClient(out, in, socket, myID);

                        // Закрывает текущее окно
                        frame.dispose();

                        // Сбрасывает статические компоненты
                        resetStaticComponents();

                        // Запускает главное меню в EDT
                        SwingUtilities.invokeLater(() -> {
                            Menu menu = new Menu(myIP, 800, 900);
                            menu.start();
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException("Ошибка при закрытии окна: " + ex.getMessage());
                    }
                }
            });

            // Настраивает основную панель с BorderLayout
            panel.setLayout(new BorderLayout());
            panel.setBackground(lightBackground);

            // Создает панель для кнопок контактов
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            buttonPanel.setBackground(Color.WHITE);

            // Настраивает панель прокрутки для списка контактов
            scrollPane = new JScrollPane(buttonPanel);
            scrollPane.setPreferredSize(new Dimension(450, 0));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            // Фоновый поток для обработки сообщений из очереди и синхронизации контактов с сервером
            Thread daemonThread = new Thread(() -> {
                while (true) {
                    try {
                        // Ожидает и извлекает сообщение из очереди (блокируется, пока очередь пуста)
                        String messangFromServer = queue.take();
                        // Отправляет сообщение на сервер
                        out.writeObject(messangFromServer);
                    } catch (InterruptedException e) {
                        // Прерывает поток при получении сигнала прерывания
                        Thread.currentThread().interrupt();
                        break;
                    } catch (IOException e) {
                        // Выбрасывает исключение при ошибке ввода-вывода
                        throw new RuntimeException(e);
                    }
                }
            });
            // Устанавливает поток как фоновый (демон)
            daemonThread.setDaemon(true);
            // Запускает поток
            daemonThread.start();

            // Синхронизирует контакты с сервером
            Thread treadContact = new Thread(() -> {
                try {
                    boolean result = database.checkAccount(accaunt_id);

                    if(!result){

                        String [] resultString = database.getSyncCommands(accaunt_id, myID);

                        out.writeObject(resultString);

                    }else {

                        connectAllContact();

                    }
                } catch (IOException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            treadContact.start();
            treadContact.join();

            // Добавляет обработчик прокрутки для динамической загрузки контактов
            scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
                if (!e.getValueIsAdjusting() && !isLoading) {
                    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                    isLoading = true;
                    try {
                        // Загружает контакты снизу при достижении конца прокрутки
                        if (contactPanel.isEndOfScroll(scrollPane) && contactPanel.canLoadMoreFromBottom(offset, accaunt_id)) {
                            panelColor = toggleSwitch.getPanelColor();
                            textButton = toggleSwitch.getTextColor();
                            int previousValue = verticalScrollBar.getValue();
                            loadMoreContacts(false);
                            int newValue = previousValue - contactPanel.getAddedElementsHeight(slider);
                            verticalScrollBar.setValue(Math.max(newValue, 5));
                            scroling++;
                            plussContact++;
                            // Загружает контакты сверху при достижении начала прокрутки
                        } else if (contactPanel.isStartOfScroll(scrollPane) && contactPanel.canLoadMoreFromTop(offset, accaunt_id)) {
                            int maximum = verticalScrollBar.getMaximum();
                            int visibleAmount = verticalScrollBar.getVisibleAmount();
                            int maxScrollable = maximum - visibleAmount;
                            int number = maxScrollable / limit;
                            int pluss = 0;
                            int big = 0;
                            if (scroling + 1 == contactPanel.getRemainingMin(accaunt_id)) {
                                pluss = number * contactPanel.getRemainingRecords(accaunt_id);
                            }
                            if (scroling < contactPanel.getRemainingMin(accaunt_id) && !(scroling + 1 < 0)) {
                                big = maxScrollable - 3;
                            } else if (scroling + 1 < 0) {
                                big = 0;
                            } else if (pluss == 0) {
                                big = number * contactPanel.getRemainingRecords(accaunt_id);
                            } else {
                                big = maxScrollable - 3 - pluss;
                            }
                            if (big == 0 && pluss == 0 && contactPanel.getRemainingRecords(accaunt_id) != 0) {
                                big = maxScrollable - 3;
                            }
                            if (big == maxScrollable - 3 && pluss != 0 && scroling > contactPanel.canLoadMoreFrom(accaunt_id)) {
                                big = pluss;
                            }
                            panelColor = toggleSwitch.getPanelColor();
                            textButton = toggleSwitch.getTextColor();
                            loadMoreContacts(true);
                            buttonPanel.revalidate();
                            buttonPanel.repaint();
                            verticalScrollBar.setValue(Math.max(big, 5));
                            scroling--;
                            plussContact--;
                        }
                    } finally {
                        isLoading = false;
                    }
                }
            });

            // Создаем текстовое поле
            messageField = new JTextArea();
            messageField.setLineWrap(true);
            messageField.setWrapStyleWord(true);
            messageField.setFont(new Font("Arial", Font.PLAIN, 14));

            // Создаем JScrollPane для текстового поля (скрываем скроллбар)
            textAreaScroll = new JScrollPane(messageField);
            textAreaScroll.setPreferredSize(new Dimension(400, 30));
            textAreaScroll.setMaximumSize(new Dimension(400, 30));
            textAreaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            textAreaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            // Создаем текстовое поле
            textMesseng = new JTextArea(1, 30);
            textMesseng.setLineWrap(true);
            textMesseng.setWrapStyleWord(true);
            textMesseng.setFont(new Font("Arial", Font.PLAIN, 14));
            controlPanel.configurePasteMenu(textMesseng);

            // Создаем JScrollPane для текстового поля (скрываем скроллбар)
            JScrollPane scrollPanemessang = new JScrollPane(textMesseng);
            scrollPanemessang.setPreferredSize(new Dimension(400, 40));
            scrollPanemessang.setMaximumSize(new Dimension(400, 40));
            scrollPanemessang.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPanemessang.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

            // Создает кнопку отправки текстового сообщения
            JButton sendButton = new JButton(new ImageIcon(new ImageIcon("pictures/messag.png")
                    .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
            sendButton.setPreferredSize(new Dimension(40, 40));
            sendButton.setToolTipText("Отправить сообщение");

            // Создает кнопку отправки файла
            JButton sendButtonFile = new JButton(new ImageIcon(new ImageIcon("pictures/file.png")
                    .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
            sendButtonFile.setPreferredSize(new Dimension(40, 40));
            sendButtonFile.setToolTipText("Отправить файл");

            // Загружает иконки для кнопки записи голосового сообщения
            iconNormal = new ImageIcon(new ImageIcon("pictures/vois.png")
                    .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            iconToggled = new ImageIcon(new ImageIcon("pictures/vois_active.png")
                    .getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

            // Создает кнопку записи и отправки голосового сообщения
            sendButtonVois = new JButton(iconNormal);
            sendButtonVois.setPreferredSize(new Dimension(40, 40));
            sendButtonVois.setToolTipText("Отправить голосовое");

            // Добавляет обработчик для записи и отправки голосового сообщения
            sendButtonVois.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Thread thread = new Thread(() -> {
                        try {
                            if(!isRecording) {
                                recipientName = ipContact;
                                futureResponse = new CompletableFuture<>();
                                //Проверка онлайн ли получатель, а также списка наличия пользователя в контактах получателя
                                out.writeObject("ONLINE " + ipContact + " " + myID);
                                out.flush();
                            }
                            boolean response = futureResponse.get();
                            if (response && !myID.equals(ipContact)){

                                if (!isRecording) {

                                    audioBuffer = new ByteArrayOutputStream();

                                    startRecording();
                                    sendButtonVois.setIcon(iconToggled);
                                } else {

                                    stopRecording();
                                    sendButtonVois.setIcon(iconNormal);

                                    keyPair = keyGet.generateRSAKeyPair();
                                    String publicKeyStr = keyGet.encodeKeyToString(keyPair.getPublic());
                                    privateKeyStr = keyGet.encodeKeyToString(keyPair.getPrivate());

                                    out.writeObject("KEY " + myID + " " + ipContact + " " + publicKeyStr + " " + "wav");
                                    out.flush();

                                }

                            }

                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    thread.start();

                    try {
                        thread.join();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            });

            // Добавляет обработчик для отправки файла
            sendButtonFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    recipientName = ipContact;
                    futureResponse = new CompletableFuture<>();

                    try {
                        out.writeObject("ONLINE " + ipContact + " " + myID);
                        out.flush();
                        boolean response = futureResponse.get();
                        if (response && !myID.equals(ipContact)) {
                            File file = fileslock.chooseFile();

                            Thread thread = new Thread(() -> {
                                try {
                                    if (file != null) {
                                        filePath = fileslock.getFilePath(file);
                                        String typeFile = keyGet.getFileExtension(file);
                                        if(typeFile.equals("wav")){
                                            audioBuffer = new ByteArrayOutputStream();
                                            recordFromWavFile(filePath);
                                        }
                                        keyPair = keyGet.generateRSAKeyPair();
                                        String publicKeyStr = keyGet.encodeKeyToString(keyPair.getPublic());
                                        privateKeyStr = keyGet.encodeKeyToString(keyPair.getPrivate());

                                        out.writeObject("KEY " + myID + " " + ipContact + " " + publicKeyStr + " " + typeFile);
                                        out.flush();
                                    } else {
                                        System.out.println("Файл не выбран.");
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            });

                            thread.start();
                        } else {
                            textMesseng.setText("");
                        }
                    } catch (IOException | InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            // Добавляет обработчик для отправки текстового сообщения
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Thread thread = new Thread(() -> {
                        try {
                            recipientName = ipContact;
                            futureResponse = new CompletableFuture<>();
                            out.writeObject("ONLINE " + ipContact + " " + myID);
                            out.flush();
                            boolean response = futureResponse.get();
                            if (response && !myID.equals(ipContact)) {
                                byte[] inputData = textMesseng.getText().getBytes();

                                if (inputData.length < 4096) {
                                    keyPair = keyGet.generateRSAKeyPair();
                                    String publicKeyStr = keyGet.encodeKeyToString(keyPair.getPublic());
                                    privateKeyStr = keyGet.encodeKeyToString(keyPair.getPrivate());

                                    out.writeObject("KEY " + myID + " " + ipContact + " " + publicKeyStr + " " + "texts");
                                    out.flush();
                                } else {
                                    textMesseng.setText("");
                                }
                            } else {
                                textMesseng.setText("");
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    thread.start();

                }
            });

            // Добавляет поддержку перетаскивания файлов
            new DropTarget(frame, new DropTargetAdapter() {
                @Override
                public void drop(DropTargetDropEvent event) {

                    try {

                        event.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable transferable = event.getTransferable();
                        DataFlavor[] flavors = transferable.getTransferDataFlavors();

                        recipientName = ipContact;
                        futureResponse = new CompletableFuture<>();

                        try {
                            out.writeObject("ONLINE " + ipContact + " " + myID);
                            out.flush();
                            boolean response = futureResponse.get();
                            if (response && !myID.equals(ipContact)) {
                                for (DataFlavor flavor : flavors) {
                                    if (flavor.isFlavorJavaFileListType()) {
                                        List<File> files = (List<File>) transferable.getTransferData(flavor);

                                        start_input = new CompletableFuture<>();

                                        Thread daemonThread = new Thread(() -> {
                                            try {
                                                for (File file : files) {
                                                    String fileName = file.getName();
                                                    String fileExtension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "Неизвестный";

                                                    if (file != null && !fileExtension.equals("Неизвестный")) {
                                                        if(fileExtension.equals("wav")){
                                                            audioBuffer = new ByteArrayOutputStream();
                                                            String filePathWAV = fileslock.getFilePath(file);
                                                            recordFromWavFile(filePathWAV);
                                                        }
                                                        filePath = fileslock.getFilePath(file);
                                                        String typeFile = keyGet.getFileExtension(file);

                                                        keyPair = keyGet.generateRSAKeyPair();
                                                        String publicKeyStr = keyGet.encodeKeyToString(keyPair.getPublic());
                                                        privateKeyStr = keyGet.encodeKeyToString(keyPair.getPrivate());

                                                        out.writeObject("KEY " + myID + " " + ipContact + " " + publicKeyStr + " " + typeFile);
                                                        out.flush();
                                                    } else {
                                                        System.out.println("Файл не выбран.");
                                                    }

                                                    if (!fileExtension.equals("Неизвестный")) {
                                                        start_input.get(); // Блокируется до вызова sendSignal()
                                                    }

                                                    // После обработки файла создаем новый future для следующего ожидания
                                                    start_input = new CompletableFuture<>();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        });

                                        // Делаем поток фоновым (демоном) и запускаем
                                        daemonThread.setDaemon(true);
                                        daemonThread.start();
                                    }
                                }
                            } else {
                                textMesseng.setText("");
                            }
                        } catch (IOException | InterruptedException | ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Создает панель для ввода и кнопок
            JPanel inputPanel = new JPanel(new BorderLayout());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // Кнопки в линию справа

            // Добавляет кнопки в панель
            buttonPanel.add(sendButton);
            buttonPanel.add(sendButtonVois);
            buttonPanel.add(sendButtonFile);

            // Настраивает панель ввода
            inputPanel.add(scrollPanemessang, BorderLayout.CENTER);
            inputPanel.add(buttonPanel, BorderLayout.EAST); // Кнопки справа
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Создает панель для кнопок управления
            JPanel buttonContacts = new JPanel(new BorderLayout());
            buttonContacts.setBackground(lightBackground);

            // Панель для кнопок слева
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            leftPanel.setBackground(lightBackground);

            // Инициализирует меню учетной записи
            menuAccaunt = new MenuAccaunt(accaunt_id, out, myID, frame, keyAc);

            // Панель для кнопок справа
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rightPanel.setBackground(lightBackground);

            // Добавляет меню в левую панель
            JMenuBar menuBar = menuAccaunt.createMenuBar();
            leftPanel.add(menuBar);

            // Инициализирует переключатель тем
            toggleSwitch = new ToggleSwitch(frame, panel, messageField, textMesseng, null, accaunt_id,
                    nameLabel);
            panelColor = toggleSwitch.getPanelColor();
            textButton = toggleSwitch.getTextColor();

            // Загружает начальный список контактов
            loadContacts( false);

            // Создает кнопку для добавления нового контакта
            JButton buttonNewContacts = contactPanel.createIconButton("pictures/iconNewContacts.png", "Добавить новый контакт");

            // Добавляет обработчик для добавления нового контакта
            buttonNewContacts.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Запускаем обработку в отдельном потоке
                    new Thread(() -> {
                        try {

                            RegistretionEdit registretionEdit = new RegistretionEdit(frame, keyAc);

                            String [] date_contact = registretionEdit.dialogPanel(myID);

                            if(date_contact[0] != null && date_contact[1] != null && date_contact[2] != null){

                                // Генерируем ключи
                                keyPair = keyGet.generateRSAKeyPair();
                                String publicKey = keyGet.encodeKeyToString(keyPair.getPublic());
                                String privateKey = keyGet.encodeKeyToString(keyPair.getPrivate());

                                // Создаем поля для записи
                                Map<String, Object> fields = new HashMap<>();
                                String rec_id = safety.generateUniqueId();
                                fields.put("record_id", rec_id);
                                fields.put("account_id", accaunt_id);
                                fields.put("contact_id", date_contact[1]);
                                fields.put("contact_name", encryption.chaha20Encript(keyAc, date_contact[0]));
                                fields.put("contact_private_key", encryption.chaha20Encript(keyAc, privateKey));
                                fields.put("contact_image_id", date_contact[2]);

                                // Добавляем запись в базу данных
                                database.addRecord("potential_contacts", fields);

                                // Отправляем запрос на добавление друга
                                out.writeObject("FRAND_ADD " + date_contact[1] + " " + myID + " " + publicKey + " " + rec_id);

                            }

                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(frame,
                                            "Неизвестная ошибка: " + ex.getMessage(),
                                            "Ошибка",
                                            JOptionPane.ERROR_MESSAGE)
                            );
                        }
                    }).start();
                }
            });

            // Создает кнопку выхода
            JButton buttonNewExit = contactPanel.createIconButton("pictures/exitIcon.png", "Выход");

            // Добавляет обработчик для выхода из приложения
            buttonNewExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    boolean result = database.checkAccount(accaunt_id);
                    if (result){
                        String[] keyResult = database.getChatsInfo(accaunt_id, myID, keyAc);
                        if(keyResult.length > 2){
                            try {
                                out.writeObject(keyResult);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }

                    notificationDaemon.stop();

                    Network network = new Network();
                    network.exitClient(out, in, socket, myID); // Вызов метода выхода
                    frame.dispose(); // Закрытие окна

                    Menu menu = new Menu(myIP, width, height);
                    menu.start();

                    resetStaticComponents();

                }
            });

            // Создает кнопку очистки чата
            JButton clearingChat = contactPanel.createIconButton("pictures/clearingChat.png", "Очистить чат");

            // Добавляет обработчик для очистки чата
            clearingChat.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if(ipContact!=null){
                        contactPanel.deleteMessagesAndSessionsByContactId(ipContact, accaunt_id);
                        if(dynamicPanel!=null){
                            dynamicPanel.loadInitialPanels();
                        }
                    }

                }
            });

            // Создает кнопку планирования чата
            JButton buttonChatPlan = contactPanel.createIconButton("pictures/chats.png", "Запланировать чат");

            // Добавляет обработчик для планирования чата
            buttonChatPlan.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Thread contactTread = new Thread(() ->{

                        contactChat = new CompletableFuture<>();

                        if(ipContact != null){

                            try {

                                out.writeObject("CONTACT_CTAT_DB " + myID + " " + ipContact);

                                boolean response = contactChat.get();

                                if(response){

                                    // Создание диалогового окна
                                    JDialog dialog = new JDialog(frame, "Отправить уведомление", true);
                                    dialog.setSize(520, 590);
                                    dialog.setResizable(false);
                                    dialog.setLocationRelativeTo(frame);

                                    ChatPlanningPanel schedulePanel = new ChatPlanningPanel(dialog, myID, ipContact, out, keyAc, accaunt_id);
                                    dialog.add(schedulePanel.getPanel());

                                    dialog.setVisible(true);

                                }else {

                                    SwingUtilities.invokeLater(() ->

                                            JOptionPane.showMessageDialog(frame,
                                                    "У этого пользователя нет вашего контакта, необходимо добавиться ему в друзья.",
                                                    "Информация",
                                                    JOptionPane.INFORMATION_MESSAGE)

                                    );

                                }

                            } catch (InterruptedException | ExecutionException ex) {
                                throw new RuntimeException(ex);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }

                        }else{


                            SwingUtilities.invokeLater(() ->

                                    JOptionPane.showMessageDialog(frame,
                                            "Выберите контакт.",
                                            "Информация",
                                            JOptionPane.INFORMATION_MESSAGE)

                            );

                        }
                    });

                    contactTread.start();

                }
            });

            // Добавляет элементы управления в правую панель
            rightPanel.add(toggleSwitch);
            rightPanel.add(buttonChatPlan);
            rightPanel.add(clearingChat);
            rightPanel.add(buttonNewContacts);
            rightPanel.add(buttonNewExit);

            // Настраивает панель кнопок управления
            buttonContacts.add(leftPanel, BorderLayout.WEST);  // Левая панель слева
            buttonContacts.add(rightPanel, BorderLayout.EAST); // Правая панель справа

            // Добавляет компоненты в основную панель
            panel.add(scrollPane, BorderLayout.WEST);
            panel.add(textAreaScroll, BorderLayout.CENTER);
            panel.add(inputPanel, BorderLayout.SOUTH);
            panel.add(buttonContacts, BorderLayout.NORTH);

            // Добавляет основную панель в окно и отображает его
            frame.add(panel, BorderLayout.CENTER);
            frame.setVisible(true);

            // Отправляет список заблокированных контактов, если требуется синхронизация
            String [] blockFriend = database.getClientBlockArray(accaunt_id, myID);

            if(database.getSynchronizationLocks(accaunt_id)){

                out.writeObject(blockFriend);

            }


        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Перезагружает данные контактов и обновляет интерфейс
    public static void dataReload() {
        try {
            // Синхронизирует все контакты с сервером
            connectAllContact();
            // Удаляет все компоненты с панели контактов
            buttonPanel.removeAll();
            // Перерисовывает панель для отображения изменений
            buttonPanel.repaint();
            // Загружает контакты из базы данных
            loadContacts(false);
        } catch (IOException | ExecutionException | InterruptedException e) {
            // Выбрасывает исключение при ошибке синхронизации или загрузки
            throw new RuntimeException(e);
        }
    }

    // Синхронизирует все контакты с сервером и обновляет локальную базу данных
    public static void connectAllContact() throws IOException, ExecutionException, InterruptedException {
        // Получает список контактов из базы данных
        String[] contacts = database.getChatList(accaunt_id, myID, keyAc);

        // Инициализирует асинхронный результат синхронизации
        contact = new CompletableFuture<>();

        // Отправляет список контактов на сервер
        out.writeObject(contacts);

        // Ожидает ответа от сервера
        String resultMy = contact.get();

        // Создает объект для обработки структуры данных
        DataStructure dataStructure = new DataStructure();

        // Парсит ответ сервера в массив контактов
        String[] contact = dataStructure.parseSyncResponse(resultMy);

        // Вставляет полученные данные контактов в базу данных
        String[] ret = database.insertDateContact(contact, keyAc, accaunt_id);

        // Если есть данные для отправки, отправляет их на сервер
        if (ret.length > 1) {
            out.writeObject(ret);
        }

        // Получает команды синхронизации из базы данных
        String[] resultString = database.getSyncCommands(accaunt_id, myID);

        // Отправляет команды синхронизации на сервер
        out.writeObject(resultString);
    }

    // Добавляет новый контакт в базу данных и обновляет интерфейс
    public static void reloadContacts(String friendId, String friendName, String nameFileProf, String secretKeyStr, boolean serverResponse) {
        try {
            // Устанавливает цвета текста и панели в зависимости от темы
            if (!database.getLite(accaunt_id)) {
                textButton = Color.BLACK;
                buttonPanelLightColor = Color.LIGHT_GRAY;
            } else {
                textButton = Color.WHITE;
                buttonPanelLightColor = new Color(80, 80, 80);
            }

            // Проверяет статус учетной записи
            boolean result = database.checkAccount(accaunt_id);

            // Если учетная запись не активна, добавляет команду для синхронизации контакта
            if (!result) {
                queue.add("CONNECT_CONTACTS " + friendId + " " + myID);
            } else {
                // Шифрует имя контакта с использованием AES
                byte[] resultEn = keyGet.encryptBlock("AES", null, keyAc, friendName.getBytes());
                String nameEnc = Base64.getEncoder().encodeToString(resultEn);

                // Шифрует секретный ключ с использованием AES
                byte[] resultEnKey = keyGet.encryptBlock("AES", null, keyAc, secretKeyStr.getBytes());
                String nameEncKey = Base64.getEncoder().encodeToString(resultEnKey);

                // Добавляет команду для синхронизации контакта с зашифрованными данными
                queue.add("CONNECT_CONTACTS_PLUSS " + friendId + " " + myID + " " + nameEnc + " " + nameEncKey);
            }

            // Добавляет контакт в базу данных с зашифрованными данными
            addUserToDatabase(friendId, encryption.chaha20Encript(keyAc, friendName),
                    nameFileProf, encryption.chaha20Encript(keyAc, secretKeyStr));

            if (serverResponse){
                // Загружает последние контакты в интерфейс
                loadLastContacts(limit);

                // Обновляет смещение и счетчик прокрутки
                offset = limit * contactPanel.getRemainingMin(accaunt_id);
                scroling = contactPanel.getRemainingMin(accaunt_id);

                // Прокручивает список контактов вниз в потоке EDT
                SwingUtilities.invokeLater(() ->
                        fileslock.scrollToBottom(scrollPane)
                );
            }

        } catch (Exception e) {
            // Выбрасывает исключение при ошибке
            throw new RuntimeException(e);
        }
    }

    // Запрашивает данные нового контакта у сервера и возвращает их в виде массива
    public static String[] contactPluss(String id_new_contact) {
        String[] result = null;
        try {
            // Инициализирует асинхронный результат для получения данных контакта
            newFreand = new CompletableFuture<>();
            // Отправляет запрос на сервер для получения данных нового контакта
            out.writeObject("NEW_CONTACT " + myID + " " + id_new_contact);
            // Ожидает ответа от сервера
            String resultContact = newFreand.get();
            // Разбивает ответ на массив строк
            result = resultContact.split(" ");
        } catch (IOException | ExecutionException | InterruptedException e) {
            // Выбрасывает исключение при ошибке
            throw new RuntimeException(e);
        }
        return result;
    }

    // Проверяет, существует ли контакт в базе данных
    public static boolean contactOut(String id_new_contact) {
        // Возвращает true, если контакт существует, иначе false
        boolean result = database.isContactExists(id_new_contact, accaunt_id);
        return result;
    }

    // Читает и записывает данные из WAV-файла в буфер для отправки
    public static void recordFromWavFile(String wavFilePath) {
        try {
            // Открывает WAV-файл как поток аудиоданных
            File wavFile = new File(wavFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);

            // Определяет ожидаемый формат аудио (44100 Гц, 16 бит, моно)
            AudioFormat expectedFormat = new AudioFormat(44100.0f, 16, 1, true, true);
            AudioFormat fileFormat = audioInputStream.getFormat();

            // Проверяет соответствие формата файла ожидаемому
            if (!fileFormat.matches(expectedFormat)) {
                // Конвертирует аудиопоток в ожидаемый формат, если необходимо
                audioInputStream = AudioSystem.getAudioInputStream(expectedFormat, audioInputStream);
            }

            // Инициализирует буфер для аудиоданных, если он еще не создан
            if (audioBuffer == null) {
                audioBuffer = new ByteArrayOutputStream();
            }

            // Создает буфер для чтения данных
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Читает данные из файла и записывает в буфер
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                audioBuffer.write(buffer, 0, bytesRead);
            }

            // Закрывает аудиопоток
            audioInputStream.close();
        } catch (UnsupportedAudioFileException e) {
            // Обрабатывает ошибку неподдерживаемого формата файла
            e.printStackTrace();
        } catch (IOException e) {
            // Обрабатывает ошибку ввода-вывода
            e.printStackTrace();
        } catch (Exception e) {
            // Обрабатывает прочие исключения
            e.printStackTrace();
        }
    }

    // Запускает запись аудио с микрофона
    private static void startRecording() {
        try {
            // Задает формат аудио (44100 Гц, 16 бит, моно)
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            // Получает доступ к микрофону
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            isRecording = true;

            // Запускает поток для записи аудио
            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (isRecording) {
                    bytesRead = microphone.read(buffer, 0, buffer.length);
                    audioBuffer.write(buffer, 0, bytesRead);
                }
            });
            recordingThread.start();
        } catch (LineUnavailableException ex) {
            // Обрабатывает ошибку недоступности микрофона
            ex.printStackTrace();
        }
    }

    // Останавливает запись аудио
    private static void stopRecording() {
        // Сбрасывает флаг записи
        isRecording = false;
        if (microphone != null) {
            // Останавливает и закрывает микрофон
            microphone.stop();
            microphone.close();
        }
        try {
            if (recordingThread != null) {
                // Ожидает завершения потока записи
                recordingThread.join();
            }
        } catch (InterruptedException ex) {
            // Обрабатывает ошибку прерывания потока
            ex.printStackTrace();
        }
    }

    // Загружает последние контакты из базы данных и отображает их
    private static void loadLastContacts(int count) {
        // SQL-запрос для получения последних контактов
        String sqlQuery = "SELECT contact_name, contact_id, contact_image_id FROM chats WHERE account_id = ? " +
                "ORDER BY ROWID DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            // Устанавливает параметры запроса
            pstmt.setString(1, accaunt_id);
            pstmt.setInt(2, count);

            ResultSet rs = pstmt.executeQuery();

            // Список для хранения кнопок контактов
            ArrayList<JButton> newContacts = new ArrayList<>();

            // Обрабатывает результаты запроса
            while (rs.next()) {
                String contactName = encryption.chaha20Decrypt(keyAc, rs.getString("contact_name"));
                String contactId = rs.getString("contact_id");
                String image_id = rs.getString("contact_image_id");

                // Создает кнопку для контакта
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(100, 100));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                button.setBackground(new Color(211, 211, 211));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(true);
                button.setMargin(new Insets(10, 10, 10, 10));
                button.setBackground(panelColor);
                button.setForeground(textButton);
                button.setLayout(new BorderLayout());

                // Создает панель для аватара контакта
                JPanel leftPanelContacts = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                leftPanelContacts.setPreferredSize(new Dimension(70, 100));
                leftPanelContacts.setOpaque(false);
                BufferedImage img = imageViewer.createCircularImageWithBorder(imageViewer.loadImageFromFile(image_id));
                if (img != null) {
                    JLabel imageLabel = new JLabel(new ImageIcon(img));
                    leftPanelContacts.add(imageLabel);
                }

                // Создает панель для имени контакта
                JPanel rightPanel = new JPanel(new GridBagLayout());
                rightPanel.setOpaque(false);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 90, 0, 0);

                // Настраивает метку с именем контакта
                nameLabel = new JLabel(contactName);
                toggleSwitch.setDynamicJLay(nameLabel);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 27));
                nameLabel.setForeground(textButton);
                nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                rightPanel.add(nameLabel, gbc);

                button.add(leftPanelContacts, BorderLayout.WEST);
                button.add(rightPanel, BorderLayout.CENTER);

                final String currentContactId = contactId;
                final JButton currentButton = button;

                // Создает контекстное меню для удаления и изменения контакта
                JPopupMenu popup = new JPopupMenu();
                JMenuItem deleteItem = new JMenuItem("Удалить");
                deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
                deleteItem.setBackground(Color.WHITE);
                deleteItem.setForeground(Color.BLACK);
                deleteItem.addActionListener(e -> {
                    deleteContact(currentContactId, currentButton);
                });
                popup.add(deleteItem);

                ChangingСontacts changingСontacts = new ChangingСontacts(frame, myID, out, keyAc, accaunt_id);

                JMenuItem upName = new JMenuItem("Изменить имя");
                upName.setFont(new Font("Arial", Font.PLAIN, 14));
                upName.setBackground(Color.WHITE);
                upName.setForeground(Color.BLACK);
                upName.addActionListener(e -> {
                    changingСontacts.dialogPanelReceived(myID, contactId, button);
                });
                popup.add(upName);

                JMenuItem upImage = new JMenuItem("Изменить иконку контакта");
                upImage.setFont(new Font("Arial", Font.PLAIN, 14));
                upImage.setBackground(Color.WHITE);
                upImage.setForeground(Color.BLACK);
                upImage.addActionListener(e -> {
                    changingСontacts.dialogPanel(button, contactId);
                });
                popup.add(upImage);

                // Добавляет обработчик для вызова контекстного меню
                MouseAdapter popupListener = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                };
                button.addMouseListener(popupListener);

                // Добавляет обработчик для открытия чата
                button.addActionListener(e -> {
                    ipContact = contactId;
                    dynamicPanel = new DynamicPanelScroll(contactId, frame, keyAc, accaunt_id);
                    toggleSwitch.setDynamicPanel(dynamicPanel);
                    if (dynamicPanelScrollPane != null) {
                        panel.remove(dynamicPanelScrollPane);
                    }
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            dynamicPanel.loadInitialPanels();
                            return null;
                        }
                        @Override
                        protected void done() {
                            dynamicPanelScrollPane = dynamicPanel.getPanel();
                            SwingUtilities.invokeLater(() -> {
                                panel.remove(textAreaScroll);
                                panel.add(dynamicPanelScrollPane, BorderLayout.CENTER);
                                frame.revalidate();
                                frame.repaint();
                            });
                        }
                    };
                    worker.execute();
                });

                newContacts.add(button);
            }

            // Очищает панель и добавляет новые контакты
            buttonPanel.removeAll();
            for (int i = newContacts.size() - 1; i >= 0; i--) {
                buttonPanel.add(newContacts.get(i));
            }

            buttonPanel.revalidate();
            buttonPanel.repaint();
        } catch (SQLException e) {
            // Обрабатывает ошибку SQL
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Ошибка загрузки контактов из базы данных.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Обрабатывает прочие исключения
            throw new RuntimeException(e);
        }
    }

    // Загружает контакты из базы данных с учетом смещения
    static void loadContacts(boolean isTopLoading) {
        // SQL-запрос для получения контактов с ограничением и смещением
        String sqlQuery = "SELECT contact_name, contact_id, contact_image_id FROM chats WHERE account_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            // Устанавливает параметры запроса
            pstmt.setString(1, accaunt_id);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);

            ResultSet rs = pstmt.executeQuery();

            // Список для хранения кнопок контактов
            ArrayList<JButton> newContacts = new ArrayList<>();
            while (rs.next()) {
                String contactName = encryption.chaha20Decrypt(keyAc, rs.getString("contact_name"));
                String contactId = rs.getString("contact_id");
                String image_id = rs.getString("contact_image_id");

                // Создает кнопку для контакта
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(100, 100));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                button.setBackground(new Color(211, 211, 211));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(true);
                button.setMargin(new Insets(10, 10, 10, 10));
                button.setBackground(panelColor);
                button.setForeground(textButton);
                button.setLayout(new BorderLayout());

                // Создает панель для аватара контакта
                JPanel leftPanelContacts = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                leftPanelContacts.setPreferredSize(new Dimension(70, 100));
                leftPanelContacts.setOpaque(false);
                BufferedImage img = imageViewer.createCircularImageWithBorder(imageViewer.loadImageFromFile(image_id));
                if (img != null) {
                    JLabel imageLabel = new JLabel(new ImageIcon(img));
                    leftPanelContacts.add(imageLabel);
                }

                // Создает панель для имени контакта
                JPanel rightPanel = new JPanel(new GridBagLayout());
                rightPanel.setOpaque(false);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 90, 0, 0);

                // Настраивает метку с именем контакта
                nameLabel = new JLabel(contactName);
                toggleSwitch.setDynamicJLay(nameLabel);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 27));
                nameLabel.setForeground(textButton);
                nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                rightPanel.add(nameLabel, gbc);

                button.add(leftPanelContacts, BorderLayout.WEST);
                button.add(rightPanel, BorderLayout.CENTER);

                final String currentContactId = contactId;
                final JButton currentButton = button;

                // Создает контекстное меню для удаления и изменения контакта
                JPopupMenu popup = new JPopupMenu();
                JMenuItem deleteItem = new JMenuItem("Удалить");
                deleteItem.setFont(new Font("Arial", Font.PLAIN, 14));
                deleteItem.setBackground(Color.WHITE);
                deleteItem.setForeground(Color.BLACK);
                deleteItem.addActionListener(e -> {
                    deleteContact(currentContactId, currentButton);
                });
                popup.add(deleteItem);

                JMenuItem upName = new JMenuItem("Изменить имя");
                upName.setFont(new Font("Arial", Font.PLAIN, 14));
                upName.setBackground(Color.WHITE);
                upName.setForeground(Color.BLACK);
                ChangingСontacts changingСontacts = new ChangingСontacts(frame, myID, out, keyAc, accaunt_id);
                upName.addActionListener(e -> {
                    changingСontacts.dialogPanelReceived(myID, currentContactId, currentButton);
                });
                popup.add(upName);

                JMenuItem upImage = new JMenuItem("Изменить иконку контакта");
                upImage.setFont(new Font("Arial", Font.PLAIN, 14));
                upImage.setBackground(Color.WHITE);
                upImage.setForeground(Color.BLACK);
                upImage.addActionListener(e -> {
                    changingСontacts.dialogPanel(currentButton, currentContactId);
                });
                popup.add(upImage);

                // Добавляет обработчик для вызова контекстного меню
                MouseAdapter popupListener = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                };
                button.addMouseListener(popupListener);

                // Добавляет обработчик для открытия чата
                button.addActionListener(e -> {
                    ipContact = contactId;
                    dynamicPanel = new DynamicPanelScroll(contactId, frame, keyAc, accaunt_id);
                    toggleSwitch.setDynamicPanel(dynamicPanel);
                    if (dynamicPanelScrollPane != null) {
                        panel.remove(dynamicPanelScrollPane);
                    }
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            dynamicPanel.loadInitialPanels();
                            return null;
                        }
                        @Override
                        protected void done() {
                            dynamicPanelScrollPane = dynamicPanel.getPanel();
                            SwingUtilities.invokeLater(() -> {
                                panel.remove(textAreaScroll);
                                panel.add(dynamicPanelScrollPane, BorderLayout.CENTER);
                                frame.revalidate();
                                frame.repaint();
                            });
                        }
                    };
                    worker.execute();
                });

                newContacts.add(button);
            }
            // Сохраняет количество добавленных контактов
            slider = newContacts.size();

            // Добавляет контакты в панель в зависимости от направления загрузки
            if (isTopLoading) {
                for (int i = newContacts.size() - 1; i >= 0; i--) {
                    buttonPanel.add(newContacts.get(i), 0);
                }
            } else {
                for (JButton contactButton : newContacts) {
                    buttonPanel.add(contactButton);
                }
            }

            // Удаляет лишние контакты для соблюдения лимита
            while (buttonPanel.getComponentCount() > limit) {
                if (isTopLoading) {
                    buttonPanel.remove(buttonPanel.getComponentCount() - 1);
                } else {
                    buttonPanel.remove(0);
                }
            }

            // Обновляет и перерисовывает панель
            buttonPanel.revalidate();
            buttonPanel.repaint();
        } catch (SQLException e) {
            // Обрабатывает ошибку SQL
            e.printStackTrace();
        } catch (Exception e) {
            // Обрабатывает прочие исключения
            throw new RuntimeException(e);
        }
    }

    // Удаляет контакт из базы данных и интерфейса
    private static void deleteContact(String contactId, JButton contactButton) {
        try {
            // Удаляет сообщения и сессии, связанные с контактом
            contactPanel.deleteMessagesAndSessionsByContactId(contactId, accaunt_id);

            // Удаляет ответы, связанные с учетной записью и контактом
            database.deleteResponsesByAccountAndContact(accaunt_id, contactId);

            // Если удаляемый контакт активен, обновляет панель сообщений
            if (contactId.equals(ipContact)) {
                if (dynamicPanel != null) {
                    dynamicPanel.loadInitialPanels();
                }
            }

            // Сохраняет текущее положение ползунка прокрутки
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            int scrollPosition = verticalScrollBar.getValue();

            // Получает имя файла, связанного с контактом
            String fileName = contactPanel.getChatData(contactId, accaunt_id);

            // Проверяет статус учетной записи
            boolean result = database.checkAccount(accaunt_id);

            // Добавляет команду удаления контакта в очередь
            if (!result) {
                queue.add("CONNECT_CONTACTS_DELETE " + contactId + " " + myID);
            } else {
                queue.add("CONNECT_CONTACTS_DELETE_PLUSS " + contactId + " " + myID);
            }

            // Удаляет файл, связанный с контактом
            fileslock.deleteFile(fileName);

            // Удаляет контакт из базы данных
            String sqlDelete = "DELETE FROM chats WHERE contact_id = ? AND account_id = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                pstmt.setString(1, contactId);
                pstmt.setString(2, accaunt_id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Контакт с ID " + contactId + " удален из базы данных.");
                } else {
                    System.out.println("Контакт с ID " + contactId + " не найден в базе данных.");
                }
            }

            // Удаляет кнопку контакта из панели
            buttonPanel.remove(contactButton);

            // Обновляет список контактов
            int currentCount = buttonPanel.getComponentCount();
            if (currentCount < limit) {
                if (isDynamicLoading) {
                    // Подгружает дополнительные контакты снизу, если возможно
                    if (contactPanel.canLoadMoreFromBottom(offset, accaunt_id)) {
                        loadMoreContacts(false);
                    } else {
                        loadContacts(false); // Обновляет текущий список
                    }
                } else {
                    // Загружает последние контакты
                    loadLastContacts(limit);
                }
            } else {
                // Обновляет и перерисовывает панель
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }

            // Восстанавливает положение ползунка
            SwingUtilities.invokeLater(() -> {
                verticalScrollBar.setValue(scrollPosition);
            });

            // Сбрасывает активный контакт, если он был удален
            if (contactId.equals(ipContact)) {
                ipContact = null;
            }
        } catch (SQLException e) {
            // Обрабатывает ошибку SQL
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Ошибка при удалении контакта из базы данных.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Обрабатывает прочие исключения
            throw new RuntimeException("Неизвестная ошибка при удалении контакта: " + e.getMessage());
        }
    }

    // Подгружает дополнительные контакты сверху или снизу
    private static void loadMoreContacts(boolean isTopLoading) {
        if (isTopLoading) {
            // Проверяет возможность загрузки контактов сверху
            if (contactPanel.canLoadMoreFromTop(offset, accaunt_id)) {
                // Сдвигает смещение вверх и загружает контакты
                offset -= limit;
                loadContacts(true);
            }
        } else {
            // Сдвигает смещение вниз и загружает контакты
            offset += limit;
            loadContacts(false);
        }
    }

    // Добавляет нового пользователя в таблицу чатов базы данных
    private static void addUserToDatabase(String id, String name, String nameFileProf, String secretKeyStr) {
        // SQL-запрос для добавления записи в таблицу
        String sqlInsert = "INSERT INTO " + TABLE_NAME_NUMBER_2 + " (conversation_id, contact_id, contact_name, account_id," +
                "contact_image_id, session_key_reserve) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                // Выполняет вставку записи
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setString(1, safety.generateUniqueId()); // Генерирует уникальный ID
                    pstmt.setString(2, id); // Устанавливает ID контакта
                    pstmt.setString(3, name); // Устанавливает имя контакта
                    pstmt.setString(4, accaunt_id); // Устанавливает ID учетной записи
                    pstmt.setString(5, nameFileProf); // Устанавливает ID изображения
                    pstmt.setString(6, secretKeyStr); // Устанавливает резервный ключ сессии
                    pstmt.executeUpdate(); // Выполняет запрос
                }
            }
        } catch (SQLException e) {
            // Обрабатывает ошибку SQL
            JOptionPane.showMessageDialog(frame, "Ошибка работы с базой данных.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Отправляет зашифрованный ключ сообщения на сервер
    private static void getKeyMess() {
        // Обрабатывает асинхронный результат получения ключа
        queueKey.thenAccept(elementKey -> {
            try {
                // Отправляет ключ на сервер
                out.writeObject(elementKey);
                out.flush(); // Сбрасывает буфер вывода
            } catch (IOException e) {
                // Обрабатывает ошибку ввода-вывода
                e.printStackTrace();
            }
        });
    }

    // Сбрасывает статические компоненты интерфейса для перезапуска
    private static void resetStaticComponents() {
        // Инициализирует новую основную панель
        panel = new JPanel();
        // Сбрасывает панель кнопок контактов
        buttonPanel = null;
        // Сбрасывает панель прокрутки
        scrollPane = null;
        // Сбрасывает панель прокрутки для динамической панели
        dynamicPanelScrollPane = null;
        // Сбрасывает динамическую панель сообщений
        dynamicPanel = null;
        // Сбрасывает панель прокрутки текстового поля
        textAreaScroll = null;
        // Сбрасывает поле отображения сообщений
        messageField = null;
        // Сбрасывает поле ввода сообщений
        textMesseng = null;
        // Сбрасывает смещение для загрузки контактов
        offset = 0;
        // Сбрасывает счетчик прокрутки
        scroling = 0;
        // Сбрасывает счетчик добавленных контактов
        plussContact = 0;
        // Сбрасывает флаг загрузки
        isLoading = false;
        // Сбрасывает идентификатор текущего контакта
        ipContact = null;
        // Сбрасывает количество добавленных элементов
        slider = 0;
        // Сбрасывает высоту окна
        height = 0;
        // Сбрасывает ширину окна
        width = 0;
    }

    // Обрабатывает ответы от сервера, включая файлы, сообщения, ключи и действия с контактами
    private static void handleServerResponses() throws IOException {
        try {
            Object response;
            // Читает объекты из входного потока, пока они доступны
            while ((response = in.readObject()) != null) {

                try {

                    // Обрабатывает передачу файлов
                    if (response instanceof FileTransferData fileData) {
                        String nameFile = fileData.getFileName();

                        // Обрабатывает фрагмент файла и возвращает флаг завершения
                        boolean print = fileslock.handleFileChunk(fileData, textsClient, nameFile);

                        // Обновляет статус сообщения в базе данных, если файл полностью получен
                        if (print){
                            String dbPath = "user_accounts.db";
                            String tableName = "messages";
                            Object[] searchValues = {nameFile};
                            String[] searchColumns = {"file_id"};
                            String[] updateColumns = {"message_status"};
                            Object[] newValues = {true};
                            database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);
                        }

                        String senderName = fileData.getSenderName();
                        // Обновляет панель сообщений, если файл от текущего контакта
                        if (senderName.equals(ipContact) && print) {
                            String type = contactPanel.findSenderFileTipy(nameFile);
                            String alg = contactPanel.findSenderAlg(nameFile);
                            String key = contactPanel.findSenderKey(nameFile);
                            if(alg!=null && key!=null && type!=null){
                                dynamicPanel.loadInitialPanels();
                            }
                        }

                    }else {
                        // Обрабатывает строковые сообщения
                        if (response instanceof String) {
                            String message = (String) response;
                            String[] parts = message.split(" ", 2);
                            String action = parts[0];
                            switch (action){

                                case "KEY": {

                                    // Обрабатывает запрос ключа шифрования
                                    queueKey = new CompletableFuture<>();
                                    String[] update = message.split(" ");

                                    PublicKey restoredPublicKey = keyGet.decodePublicKeyFromString(update[3]);
                                    // Запускает поток для генерации и шифрования ключа
                                    Thread encryptionThread = new Thread(() -> {
                                        try {

                                            String alg = EncryptionAccaunt.getRandomString();

                                            String secretKeyStr = null;

                                            // Генерирует ключ в зависимости от алгоритма
                                            switch (alg){
                                                case "1a": {
                                                    secretKeyStr = KeyGet.generationKeyAES();
                                                    break;
                                                }
                                                case "1b": {
                                                    secretKeyStr = KeyGet.generationKeyTwoFishAndSerpent("Twofish", "BC");
                                                    break;
                                                }
                                                case "1c": {
                                                    secretKeyStr = KeyGet.generationKeyTwoFishAndSerpent("Serpent", "BC");
                                                    break;
                                                }
                                            }

                                            // Шифрует ключ с помощью публичного ключа
                                            String encryptedText = keyGet.encrypt(secretKeyStr, restoredPublicKey);

                                            // Форматируем текущую дату и время
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                            String formattedDate = dateFormat.format(new Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

                                            // Генерация имени файла с датой, временем и уникальным идентификатором
                                            String fileName = "out/"+update[1]+"/"+ formattedDate + ".bin"; // Используем расширение .txt
                                            // Шифруем данные
                                            String chatID = contactPanel.getConversationId(accaunt_id, update[1]);

                                            // Завершает асинхронный результат с данными ключа
                                            contactPanel.updateSessionKeyReserve(update[1], accaunt_id, encryption.chaha20Encript(keyAc, secretKeyStr));
                                            queueKey.complete("MESS_KEY " + update[2] + " " + update[1] + " " + encryptedText + " " + alg
                                                    + " " + fileName + " " + update[4]);
                                            // Сохраняет зашифрованные данные в базе
                                            String key = encryption.chaha20Encript(keyAc, secretKeyStr);
                                            String algEncrypt = encryption.chaha20Encript(keyAc, alg);
                                            contactPanel.insertMessage(chatID, fileName,false, key, algEncrypt, update[4]);
                                            // Отправляет ключ на сервер
                                            getKeyMess();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    encryptionThread.start();

                                    encryptionThread.join();

                                    break;

                                }

                                case "MESS_KEY": {

                                    // Обрабатывает получение ключа сообщения
                                    String[] keyMai = message.split(" ");

                                    CountDownLatch latch = new CountDownLatch(1);
                                    ConcurrentHashMap<String, String> encryptedDataMap = new ConcurrentHashMap<>();

                                    // Генерирует имя файла с текущей датой и временем
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                    String formattedDate = dateFormat.format(new Date()) + CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
                                    // Генерация имени файла с датой, временем и уникальным идентификатором
                                    String fileName = "out/"+ipContact+"/"+ formattedDate + ".bin";

                                    File file = new File(fileName);

                                    // Создает директории, если они не существуют
                                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                                        if (!file.getParentFile().mkdirs()) {
                                            System.out.println("Не удалось создать директории: " + file.getParentFile().getAbsolutePath());
                                        }
                                    }

                                    if (keyMai[6].equals("texts")){

                                        // Обрабатывает текстовое сообщение
                                        String text = textMesseng.getText();

                                        // Поток для расшифровки ключа
                                        Thread decryptionThread = new Thread(() -> {
                                            try {
                                                String decryptedText = keyGet.decrypt(keyMai[3], keyPair.getPrivate());
                                                String chatID = contactPanel.getConversationId(accaunt_id, ipContact);
                                                String key = encryption.chaha20Encript(keyAc, decryptedText);
                                                contactPanel.insertMessage(chatID, fileName,true, key, encryption.chaha20Encript(keyAc, keyMai[4]), keyMai[6]);
                                                contactPanel.updateSessionKeyReserve(ipContact, accaunt_id, encryption.chaha20Encript(keyAc, decryptedText));
                                                encryptedDataMap.put("keys", decryptedText);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }finally {
                                                latch.countDown();
                                            }
                                        });

                                        // Поток для шифрования и записи текста
                                        Thread writeAndEncription = new Thread(()-> {

                                            try {
                                                latch.await();

                                                String key = encryptedDataMap.get("keys");

                                                // Шифрует текст в зависимости от алгоритма
                                                switch (keyMai[4]){
                                                    case "1a": {
                                                        keyGet.encryptAndWriteToFile("AES", null, key, text, fileName);
                                                        break;
                                                    }
                                                    case "1b": {
                                                        keyGet.encryptAndWriteToFile("Twofish", "BC", key, text, fileName);
                                                        break;
                                                    }
                                                    case "1c": {
                                                        keyGet.encryptAndWriteToFile("Serpent", "BC", key, text, fileName);
                                                        break;
                                                    }
                                                }

                                                // Обновляет статус сообщения в базе данных
                                                String dbPath = "user_accounts.db";
                                                String tableName = "messages";
                                                Object[] searchValues = {fileName};
                                                String[] searchColumns = {"file_id"};
                                                String[] updateColumns = {"message_status"};
                                                Object[] newValues = {true};
                                                database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);

                                                // Обновляет панель сообщений
                                                dynamicPanel.loadInitialPanels();
                                                textMesseng.setText("");
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }

                                        });

                                        decryptionThread.start();
                                        writeAndEncription.start();

                                        decryptionThread.join();
                                        writeAndEncription.join();

                                        // Отправляет текст на сервер
                                        Thread outMesseng = new Thread(()-> {
                                            fileslock.sendText(recipientName, out,myID, frame, textsClient, fileName, keyMai[5]);
                                        });

                                        outMesseng.start();
                                        outMesseng.join();

                                    }else {
                                        // Обрабатывает файлы (аудио, изображения, другие)
                                        // Создаем модальное окно с прогресс-баром
                                        JDialog progressDialog = new JDialog(frame, "Processing...", true);
                                        JProgressBar progressBar = new JProgressBar();

                                        progressBar.setIndeterminate(true); // Анимация загрузки
                                        progressBar.setStringPainted(true);
                                        progressBar.setString("Обработка данных...");

                                        progressDialog.setLayout(new BorderLayout());
                                        progressDialog.add(progressBar, BorderLayout.CENTER);
                                        progressDialog.setSize(300, 100);
                                        progressDialog.setLocationRelativeTo(frame);
                                        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                                        // Запускаем прогресс-бар в отдельном потоке
                                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                                            @Override
                                            protected Void doInBackground() throws InterruptedException {

                                                if(keyMai[6].equals("wav")){
                                                    // Обрабатывает аудиофайл
                                                    Thread decryptionThread = new Thread(() -> {
                                                        try {
                                                            String decryptedText = keyGet.decrypt(keyMai[3], keyPair.getPrivate());
                                                            String chatID = contactPanel.getConversationId(accaunt_id, ipContact);
                                                            String key = encryption.chaha20Encript(keyAc, decryptedText);
                                                            // Форматируем текущую дату и время
                                                            contactPanel.insertMessage(chatID, fileName,true, key, encryption.chaha20Encript(keyAc, keyMai[4]), keyMai[6]);
                                                            contactPanel.updateSessionKeyReserve(ipContact, accaunt_id, encryption.chaha20Encript(keyAc, decryptedText));
                                                            encryptedDataMap.put("keys", decryptedText);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }finally {
                                                            latch.countDown();
                                                        }
                                                    });

                                                    Thread writeAndEncription = new Thread(()-> {

                                                        try {
                                                            latch.await();

                                                            String key = encryptedDataMap.get("keys");

                                                            // Шифрует и сохраняет аудио
                                                            switch (keyMai[4]){
                                                                case "1a": {
                                                                    keyGet.saveEncryptedAudio(audioBuffer.toByteArray(), fileName, key, "AES");
                                                                    break;
                                                                }
                                                                case "1b": {
                                                                    keyGet.saveEncryptedAudio(audioBuffer.toByteArray(), fileName, key, "Twofish");
                                                                    break;
                                                                }
                                                                case "1c": {
                                                                    keyGet.saveEncryptedAudio(audioBuffer.toByteArray(), fileName, key, "Serpent");
                                                                    break;
                                                                }
                                                            }

                                                            // Обновляет статус сообщения
                                                            String dbPath = "user_accounts.db";
                                                            String tableName = "messages";
                                                            Object[] searchValues = {fileName};
                                                            String[] searchColumns = {"file_id"};
                                                            String[] updateColumns = {"message_status"};
                                                            Object[] newValues = {true};
                                                            database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);

                                                            dynamicPanel.loadInitialPanels();

                                                            textMesseng.setText("");
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(e);
                                                        }

                                                    });

                                                    decryptionThread.start();
                                                    writeAndEncription.start();

                                                    decryptionThread.join();
                                                    writeAndEncription.join();
                                                }else {
                                                    if(keyMai[6].equals("jpg")){
                                                        // Обрабатывает изображение
                                                        Thread decryptionThread = new Thread(() -> {
                                                            try {
                                                                String decryptedText = keyGet.decrypt(keyMai[3], keyPair.getPrivate());
                                                                String chatID = contactPanel.getConversationId(accaunt_id, ipContact);
                                                                String key = encryption.chaha20Encript(keyAc, decryptedText);
                                                                // Форматируем текущую дату и время
                                                                contactPanel.insertMessage(chatID, fileName,true, key, encryption.chaha20Encript(keyAc, keyMai[4]), keyMai[6]);
                                                                contactPanel.updateSessionKeyReserve(ipContact, accaunt_id, encryption.chaha20Encript(keyAc, decryptedText));
                                                                encryptedDataMap.put("keys", decryptedText);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }finally {
                                                                latch.countDown();
                                                            }
                                                        });

                                                        Thread writeAndEncription = new Thread(()-> {

                                                            try {
                                                                latch.await();

                                                                String key = encryptedDataMap.get("keys");

                                                                // Шифрует изображение
                                                                switch (keyMai[4]){
                                                                    case "1a": {
                                                                        keyGet.encryptImage(filePath, fileName, key, "AES");
                                                                        break;
                                                                    }
                                                                    case "1b": {
                                                                        keyGet.encryptImage(filePath, fileName, key, "Twofish");
                                                                        break;
                                                                    }
                                                                    case "1c": {
                                                                        keyGet.encryptImage(filePath, fileName, key, "Serpent");
                                                                        break;
                                                                    }
                                                                }

                                                                // Обновляет статус сообщения
                                                                String dbPath = "user_accounts.db";
                                                                String tableName = "messages";
                                                                Object[] searchValues = {fileName};
                                                                String[] searchColumns = {"file_id"};
                                                                String[] updateColumns = {"message_status"};
                                                                Object[] newValues = {true};
                                                                database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);
                                                                dynamicPanel.loadInitialPanels();

                                                                textMesseng.setText("");
                                                            } catch (Exception e) {
                                                                throw new RuntimeException(e);
                                                            }

                                                        });

                                                        decryptionThread.start();
                                                        writeAndEncription.start();

                                                        decryptionThread.join();
                                                        writeAndEncription.join();

                                                    }else {
                                                        // Обрабатывает другие типы файлов
                                                        Thread decryptionThread = new Thread(() -> {
                                                            try {
                                                                String decryptedText = keyGet.decrypt(keyMai[3], keyPair.getPrivate());
                                                                String chatID = contactPanel.getConversationId(accaunt_id, ipContact);
                                                                String key = encryption.chaha20Encript(keyAc, decryptedText);
                                                                // Форматируем текущую дату и время
                                                                contactPanel.updateSessionKeyReserve(ipContact, accaunt_id, encryption.chaha20Encript(keyAc, decryptedText));
                                                                contactPanel.insertMessage(chatID, fileName,true, key, encryption.chaha20Encript(keyAc, keyMai[4]), keyMai[6]);
                                                                encryptedDataMap.put("keys", decryptedText);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }finally {
                                                                latch.countDown();
                                                            }
                                                        });

                                                        Thread writeAndEncription = new Thread(()-> {

                                                            try {
                                                                latch.await();

                                                                String key = encryptedDataMap.get("keys");

                                                                // Шифрует файл
                                                                switch (keyMai[4]){
                                                                    case "1a": {
                                                                        keyGet.encryptFile(filePath, fileName, key, "AES");
                                                                        break;
                                                                    }
                                                                    case "1b": {
                                                                        keyGet.encryptFile(filePath, fileName, key, "Twofish");
                                                                        break;
                                                                    }
                                                                    case "1c": {
                                                                        keyGet.encryptFile(filePath, fileName, key, "Serpent");
                                                                        break;
                                                                    }
                                                                }

                                                                // Обновляет статус сообщения
                                                                String dbPath = "user_accounts.db";
                                                                String tableName = "messages";
                                                                Object[] searchValues = {fileName};
                                                                String[] searchColumns = {"file_id"};
                                                                String[] updateColumns = {"message_status"};
                                                                Object[] newValues = {true};
                                                                database.updateRecords(dbPath, tableName, searchValues, searchColumns, updateColumns, newValues);

                                                                dynamicPanel.loadInitialPanels();

                                                                textMesseng.setText("");
                                                            } catch (Exception e) {
                                                                throw new RuntimeException(e);
                                                            }

                                                        });

                                                        decryptionThread.start();
                                                        writeAndEncription.start();

                                                        decryptionThread.join();
                                                        writeAndEncription.join();
                                                    }
                                                }

                                                // Отправляет файл на сервер
                                                Thread outMesseng = new Thread(()-> {
                                                    fileslock.sendText(recipientName, out,myID, frame, textsClient, fileName, keyMai[5]);

                                                    try {
                                                        if(!keyMai[6].equals("jpg")){
                                                            Thread.sleep(3000);
                                                        }else {
                                                            Thread.sleep(2000);
                                                        }
                                                    } catch (InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                });

                                                outMesseng.start();
                                                outMesseng.join();

                                                return null;
                                            }

                                            @Override
                                            protected void done() {
                                                progressDialog.dispose(); // Закрываем прогресс-бар после завершения
                                            }
                                        };

                                        worker.execute();
                                        progressDialog.setVisible(true); // Показывает прогресс-бар

                                        // Завершает асинхронный результат обработки файла
                                        if(start_input != null){
                                            start_input.complete(null);
                                        }

                                    }

                                    break;

                                }

                                case "CONTACT_CHAT_PLAN": {

                                    // Обрабатывает запрос проверки контакта для планирования чата
                                    String[] chatMai = message.split(" ");

                                    if (chatMai[1].equals("1")){
                                        contactChat.complete(true);
                                    }else {
                                        contactChat.complete(false);
                                    }

                                    break;
                                }

                                case "ONLINE_PLUS_CONTACT": {

                                    // Проверяет статус контакта (онлайн и наличие в списке)
                                    String[] keyMai = message.split(" ");

                                    String online = keyMai[2];

                                    String contact = keyMai[1];

                                    if(online.equals("1") && contact.equals("1")){

                                        futureResponse.complete(true);

                                    }else {

                                        futureResponse.complete(false);

                                        if (online.equals("0")){
                                            SwingUtilities.invokeLater(() ->

                                                    JOptionPane.showMessageDialog(frame,
                                                            "Пользователя нет в сети.",
                                                            "Информация",
                                                            JOptionPane.INFORMATION_MESSAGE)

                                            );
                                        }

                                        if(contact.equals("0")){

                                            SwingUtilities.invokeLater(() ->

                                                    JOptionPane.showMessageDialog(frame,
                                                            "Вас нет в контактах этого пользователя, добавьтесь ему в друзья.",
                                                            "Информация",
                                                            JOptionPane.INFORMATION_MESSAGE)

                                            );

                                        }

                                    }

                                    break;
                                }

                                // Завершает асинхронный результат получения данных нового контакта
                                case "NEW_CONTACT": {
                                    newFreand.complete(message);
                                    break;
                                }

                                // Завершает асинхронный результат синхронизации контактов
                                case "SYNCHRONY_OLL_CHAT_PLUSS": {

                                    contact.complete(message);

                                    break;
                                }

                                //
                                case "YOU_BLOCK": {

                                    //Обработчик передачи заблокированных пользователей
                                    String[] keyMai = message.split(" ");

                                    database.fillClientBlock(safety.generateUniqueId(), accaunt_id, keyMai[1]);

                                    break;

                                }

                                //Обработчик запроса на добавление в друзья
                                case "FRAND_ADD": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("public_key", update[5]);
                                    fields.put("record_ac_id_friend", update[6]);
                                    fields.put("record_ac_id_data", update[7]);

                                    // Добавляем запись в базу данных
                                    database.addRecord("request_responses", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель только зашёл в сеть и ему из хранилища
                                // сервера передается сообщение содержащее начальное время беседы, время окончания беседы и
                                // небольшое сообщение
                                case "CHAT_GET_ONLINE_1_2_3": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("messages", update[3]);
                                    fields.put("start_time", update[4]);
                                    fields.put("end_time", update[5]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель только зашёл в сеть и ему из хранилища
                                // сервера передается сообщение содержащее начальное время беседы, время окончания беседы
                                case "CHAT_GET_ONLINE_1_2": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("start_time", update[3]);
                                    fields.put("end_time", update[4]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель только зашёл в сеть и ему из хранилища
                                // сервера передается сообщение содержащее начальное время беседы и небольшое сообщение
                                case "CHAT_GET_ONLINE_1_3": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("messages", update[3]);
                                    fields.put("start_time", update[4]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель только зашёл в сеть и ему из хранилища
                                // сервера передается сообщение содержащее начальное время беседы
                                case "CHAT_GET_ONLINE_1": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("start_time", update[3]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель онлайн и ему приходит запрос
                                //с временем начала беседы и временем окончания беседы, а также небольшим сообщением
                                case "CHAT_GET_1_2_3": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("messages", update[5]);
                                    fields.put("start_time", update[4]);
                                    fields.put("end_time", update[3]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель онлайн и ему приходит запрос
                                //с временем начала беседы и временем окончания беседы
                                case "CHAT_GET_1_2": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("start_time", update[3]);
                                    fields.put("end_time", update[4]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель онлайн и ему приходит запрос
                                //с временем начала беседы и небольшим сообщением
                                case "CHAT_GET_1_3": {
                                    // Извлечение имени пользователя из сообщения, если оно есть
                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("messages", update[4]);
                                    fields.put("start_time", update[3]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает запросы планирования чата, когда получатель онлайн и ему приходит запрос
                                //только с началом беседы
                                case "CHAT_GET_1": {

                                    String[] update = message.split(" ");

                                    String keyMesOrg = encryption.chaha20Decrypt(keyAc, database.getSessionKeyReserve(accaunt_id, update[2]));

                                    String keyMes = encryption.chaha20Encript(keyAc, keyMesOrg);

                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();
                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("sender_id", update[2]);
                                    fields.put("start_time", update[3]);
                                    fields.put("key_planning", keyMes);

                                    // Добавляем запись в базу данных
                                    database.addRecord("chat_planning", fields);

                                    menuAccaunt.loadInitialMenu();

                                    break;
                                }

                                // Обрабатывает ответ на запрос дружбы
                                case "RESPONSE", "RESPONSE_SERVER": {

                                    boolean serverResponse = false;

                                    if(action.equals("RESPONSE")){
                                        serverResponse = true;
                                    }

                                    String[] update = message.split(" ");
                                    String [] dateContactPotencial = database.getContactDetailsByRecordId(update[4]);

                                    // Создаем поля для записи
                                    Map<String, Object> fields = new HashMap<>();
                                    String rec_id = safety.generateUniqueId();

                                    fields.put("record_id", rec_id);
                                    fields.put("account_id", accaunt_id);
                                    fields.put("name_contact", dateContactPotencial[0]);
                                    fields.put("lock_flag", Boolean.parseBoolean(update[5]));
                                    fields.put("request_status", Boolean.parseBoolean(update[6]));

                                    // Добавляет ответ в базу данных
                                    database.addRecord("application_responses", fields);

                                    menuAccaunt.loadInitialMenu();

                                    if (Boolean.parseBoolean(update[6])){
                                        // Если запрос принят, добавляет или обновляет контакт

                                        PrivateKey privateKey = keyGet.decodePrivateKeyFromString(encryption.chaha20Decrypt(keyAc, dateContactPotencial[1]));

                                        String decryptedText = keyGet.decrypt(update[3], privateKey);

                                        if(!database.isContactExists(update[2], accaunt_id)){

                                            reloadContacts(update[2], encryption.chaha20Decrypt(keyAc, dateContactPotencial[0]), dateContactPotencial[2],
                                                    decryptedText, serverResponse);

                                        }else {

                                            database.updateChatSession(update[2], accaunt_id, encryption.chaha20Encript(keyAc, decryptedText));

                                        }

                                    }else {

                                        // Если запрос отклонен, удаляет файл
                                        database.deleteFile(dateContactPotencial[2]);

                                    }

                                    // Удаляет запрос из базы данных
                                    database.deleteContactByRecordId(update[4]);

                                    break;

                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка в потоке обработки: " + e.getMessage());
                    e.printStackTrace();
                }


            }
        } catch (IOException | ClassNotFoundException e) {

            // Закрывает потоки и сокет при ошибке
            if (in != null){
                in.close();
                out.close();
                socket.close();
            }
        } finally {
            try {
                // Гарантирует закрытие потоков и сокета
                if (in != null){
                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Ошибка при закрытии потока: " + e.getMessage());
            }
        }
    }

}
