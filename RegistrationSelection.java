package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Этот класс предназначен для отображения окна выбора способа регистрации
// с обязательным подтверждением согласия на обработку персональных данных.
// Пользователь не может продолжить регистрацию без установки соответствующей галочки.

public class RegistrationSelection {

    // IP-адрес сервера
    private static String IP = null;

    // Главное окно приложения
    private static JFrame frame;

    // Класс для незавершённой регистрации
    private static RegistrationIncomplete registrationIncomplete;

    // Порт сервера
    private static int PORT;

    // Текущие размеры окна
    private static int width;
    private static int height;

    // Конструктор: сохраняет параметры подключения и размеры окна
    public RegistrationSelection(String IP, int PORT, int width, int height) {
        this.IP = IP;
        this.PORT = PORT;
        this.width = width;
        this.height = height;
    }

    // Метод запуска окна выбора регистрации
    public static void startRegistration() {
        frame = new JFrame("StealthChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Устанавливаем размер окна
        if (width > 740 && height > 750) {
            frame.setSize(width, height);
        } else {
            frame.setSize(700, 750);
        }

        frame.setMinimumSize(new Dimension(740, 810));
        frame.setLocationRelativeTo(null); // Центрируем окно

        // Обновляем размеры при изменении окна
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Создаем объект для регистрации на устройстве
        registrationIncomplete = new RegistrationIncomplete(IP, width, height);

        // Главная панель с вертикальным размещением
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.add(Box.createVerticalGlue()); // Пространство сверху

        // Внутренняя панель для кнопок и формы
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 63, 65));

        // Уменьшенные размеры панели
        panel.setPreferredSize(new Dimension(400, 380));
        panel.setMinimumSize(new Dimension(400, 380));
        panel.setMaximumSize(new Dimension(400, 380));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Внутренние отступы

        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Отступ перед заголовком

        // Заголовок
        JLabel label = new JLabel("Регистрация");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 19));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Кнопка полной регистрации
        JButton fullRegistration = new JButton("Полная регистрация");
        customizeButton(fullRegistration);
        panel.add(fullRegistration);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Кнопка регистрации на устройстве
        JButton deviceRegistration = new JButton("Регистрация на устройстве");
        customizeButton(deviceRegistration);
        panel.add(deviceRegistration);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Кнопка возврата в меню
        JButton mainMenu = new JButton("Панель входа");
        customizeButton(mainMenu);
        panel.add(mainMenu);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Чекбокс согласия на обработку данных
        JCheckBox agreementCheckBox = new JCheckBox("Я принимаю условия");
        agreementCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        agreementCheckBox.setBackground(new Color(60, 63, 65));
        agreementCheckBox.setForeground(Color.WHITE);
        agreementCheckBox.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(agreementCheckBox);

        // Панель для ссылки с отступами (top = 5, left = 5, bottom = 10)
        JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        linkPanel.setBackground(new Color(60, 63, 65));
        linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 0));

        // Метка-ссылка
        JLabel linkLabel = new JLabel("<html><u>Согласие на обработку персональных данных</u></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.setForeground(Color.WHITE);
        linkLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        linkPanel.add(linkLabel);
        panel.add(linkPanel);

// Отображаем окно с текстом согласия при нажатии на ссылку
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTextArea textArea = new JTextArea(
                        "Соглашение об обработке персональных данных\n\n" +
                                "Настоящее Соглашение об обработке персональных данных разработано в соответствии с Федеральным законом от 27.07.2006 № 152-ФЗ «О персональных данных» и регулирует порядок обработки персональных данных пользователей мессенджера «StealthChat», предоставляемого разработчиком.\n\n" +
                                "1. Общие положения\n" +
                                "1.1. Настоящее Соглашение определяет условия обработки персональных данных, предоставляемых пользователем при регистрации и использовании «StealthChat», а также порядок их распространения в рамках чатов «StealthChat».\n" +
                                "1.2. Под персональными данными понимается любая информация, относящаяся к прямо или косвенно определенному или определяемому физическому лицу (субъекту персональных данных), включая имя, логин, пароль, адрес электронной почты, кодового слова и иные данные, предоставляемые пользователем в процессе использования «StealthChat».\n" +
                                "1.3. Персональные данные, предоставляемые пользователем в чатах «StealthChat», считаются общедоступными в рамках конкретного чата между собеседниками, но их распространение за пределы чата возможно только с согласия субъекта персональных данных (пользователя-собеседника).\n" +
                                "1.4. Принимая условия настоящего Соглашения, пользователь дает согласие на обработку своих персональных данных в объеме и на условиях, предусмотренных настоящим Соглашением.\n\n" +
                                "2. Цели обработки персональных данных\n" +
                                "2.1. Оператор осуществляет обработку персональных данных в следующих целях:\n" +
                                "- Регистрация и идентификация пользователя в системе мессенджера (обработка имени, логина, пароля, адреса электронной почты, кодового слова).\n" +
                                "- Обеспечение функционирования мессенджера, включая предоставление возможности общения в чатах.\n" +
                                "- Обеспечение безопасности и защиты учетных записей пользователей.\n" +
                                "- Выполнение требований законодательства Российской Федерации.\n" +
                                "2.2. Персональные данные, размещенные пользователем в чатах, обрабатываются для обеспечения обмена сообщениями между пользователями в рамках конкретного чата.\n\n" +
                                "3. Объем обрабатываемых персональных данных\n" +
                                "3.1. Оператор обрабатывает следующие персональные данные:\n" +
                                "- Имя пользователя.\n" +
                                "- Логин.\n" +
                                "- Пароль.\n" +
                                "- Адрес электронной почты.\n" +
                                "- Кодовое слово.\n" +
                                "- Персональные данные, предоставленные пользователем в сообщениях чата, которые считаются общедоступными в рамках чата между собеседниками.\n" +
                                "3.2. Персональные данные, предоставленные в чатах, обрабатываются исключительно в рамках технического обеспечения функционирования чата и не передаются третьим лицам без согласия субъекта данных.\n\n" +
                                "4. Условия обработки персональных данных\n" +
                                "4.1. Обработка персональных данных осуществляется с соблюдением принципов и правил, установленных Федеральным законом № 152-ФЗ «О персональных данных».\n" +
                                "4.2. Обработка персональных данных осуществляется с согласия субъекта персональных данных, за исключением случаев, предусмотренных законодательством Российской Федерации.\n" +
                                "4.3. Персональные данные, размещенные пользователем в чатах, считаются общедоступными в рамках конкретного чата. Распространение таких данных за пределы чата (например, пересылка, публикация в иных источниках) допускается только с согласия пользователя, чьи данные распространяются.\n" +
                                "4.4. Оператор принимает необходимые организационные и технические меры для защиты персональных данных от неправомерного или случайного доступа, уничтожения, изменения, блокирования, копирования, распространения, а также от иных неправомерных действий третьих лиц.\n" +
                                "4.5. Пароль, логин, адрес электронной почты и кодовое слово пользователя хранятся в хэшированном виде с использованием современных криптографических методов. Сообщения в чатах защищены сквозным шифрованием, что исключает доступ к их содержимому со стороны Оператора или третьих лиц.\n" +
                                "4.6. Данные о контактах, хранящиеся на сервере по выбору пользователя, защищены шифрованием, а ключи шифрования хранятся исключительно на устройстве пользователя.\n\n" +
                                "5. Передача персональных данных\n" +
                                "5.1. Оператор не передает персональные данные третьим лицам, за исключением случаев:\n" +
                                "- Получения письменного согласия субъекта персональных данных.\n" +
                                "- Выполнения требований законодательства Российской Федерации.\n" +
                                "- Необходимости передачи данных для обеспечения функционирования Мессенджера (например, хостинг-провайдерам, обеспечивающим техническую поддержку сервиса).\n" +
                                "5.2. Персональные данные, размещенные в чатах, не передаются за пределы чата без согласия субъекта данных, за исключением случаев, предусмотренных законодательством.\n\n" +
                                "6. Права субъекта персональных данных\n" +
                                "6.1. Пользователь имеет право:\n" +
                                "- Запрашивать у Оператора информацию об обработке его персональных данных.\n" +
                                "- Требовать уточнения, блокирования или уничтожения своих персональных данных в случае, если они являются неполными, устаревшими, неточными, незаконно полученными или не являются необходимыми для заявленных целей обработки.\n" +
                                "- Отозвать свое согласие на обработку персональных данных, направив письменное заявление Оператору.\n\n" +
                                "7. Срок обработки персональных данных\n" +
                                "7.1. Персональные данные обрабатываются в течение срока, необходимого для достижения целей обработки, указанных в разделе 2 настоящего Соглашения, или до момента отзыва согласия субъектом персональных данных.\n" +
                                "7.2. При удалении учетной записи пользователя его персональные данные подлежат уничтожению в порядке, установленном законодательством, за исключением случаев, когда их хранение требуется в соответствии с законом.\n\n" +
                                "8. Ответственность\n" +
                                "8.1. Оператор несет ответственность за обеспечение безопасности персональных данных в соответствии с требованиями Федерального закона № 152-ФЗ.\n" +
                                "8.2. Пользователь несет ответственность за достоверность предоставляемых персональных данных и за соблюдение условий настоящего Соглашения при распространении персональных данных других пользователей за пределы чата.\n" +
                                "8.3. Пользователь несет ответственность за обеспечение безопасности своего устройства, включая, но не ограничиваясь, установку и использование средств защиты (антивирусных программ, паролей, средств блокировки устройства), а также за действия, которые могут привести к несанкционированному доступу третьих лиц к данным мессенджера (например, оставление устройства без надзора с открытым приложением «StealthChat» или передача устройства третьим лицам).\n" +
                                "8.4. Ответственность за безопасность данных на устройстве пользователя:\n" +
                                "8.4.1. Все сообщения и данные чатов хранятся исключительно на устройстве пользователя в зашифрованном виде и передаются с использованием сквозного шифрования. Оператор не имеет доступа к содержимому сообщений и не осуществляет их хранение на своих серверах.\n" +
                                "8.4.2. Пользователь самостоятельно несет ответственность за обеспечение безопасности своего устройства, включая защиту от несанкционированного доступа.\n" +
                                "8.4.3. Оператор не несет ответственности за утрату, утечку или несанкционированное использование персональных данных и сообщений, хранящихся на устройстве пользователя, если такие события произошли вследствие действий или бездействия пользователя, включая отсутствие надлежащих мер защиты устройства или предоставление доступа к устройству третьим лицам.\n\n" +
                                "9. Заключительные положения\n" +
                                "9.1. Настоящее Соглашение вступает в силу с момента его принятия пользователем (путем проставления отметки о согласии при регистрации в «StealthChat») и действует в течение всего периода использования «StealthChat».\n" +
                                "9.2. Оператор вправе вносить изменения в настоящее Соглашение, уведомляя пользователей через интерфейс «StealthChat» или иным доступным способом.\n" +
                                "9.3. Все споры, связанные с обработкой персональных данных, разрешаются в соответствии с законодательством Российской Федерации."
                );
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                textArea.setBackground(new Color(240, 240, 240));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 200));
                JOptionPane.showMessageDialog(frame, scrollPane, "Согласие на обработку данных", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Блокируем кнопки до согласия
        fullRegistration.setEnabled(false);
        deviceRegistration.setEnabled(false);

        // Активируем кнопки при включении чекбокса
        agreementCheckBox.addActionListener(e -> {
            boolean agreed = agreementCheckBox.isSelected();
            fullRegistration.setEnabled(agreed);
            deviceRegistration.setEnabled(agreed);
        });

        // Добавляем действия на кнопки
        fullRegistration.addActionListener(e -> new Thread(() -> full(fullRegistration)).start());
        deviceRegistration.addActionListener(e -> new Thread(() -> device(deviceRegistration)).start());
        mainMenu.addActionListener(e -> new Thread(() -> menu(mainMenu)).start());

        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 10));
        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // Переход в главное меню
    private static void menu(JButton mainMenu) {
        SwingUtilities.invokeLater(() -> {
            mainMenu.setEnabled(false);
            mainMenu.setText("Переход в главное меню...");
        });

        Menu menu = new Menu(IP, width, height);
        menu.start();
        frame.dispose();

        SwingUtilities.invokeLater(() -> {
            mainMenu.setEnabled(true);
            mainMenu.setText("Вернуться в главное меню");
        });
    }

    // Действие при полной регистрации
    private static void full(JButton button) {
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false);
            button.setText("Полная регистрация...");
        });

        RegistrationIncompleteFull registration = new RegistrationIncompleteFull(IP, PORT, width, height);
        registration.startDFullPassword();
        frame.dispose();

        SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("Полная регистрация");
        });
    }

    // Действие при регистрации на устройстве
    private static void device(JButton button) {
        SwingUtilities.invokeLater(() -> {
            button.setEnabled(false);
            button.setText("Регистрация на устройстве...");
        });

        registrationIncomplete.startDevPassword();
        frame.dispose();

        SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("Регистрация на устройстве");
        });
    }

    // Метод стилизации кнопок
    private static void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(75, 110, 175));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(250, 60));
        button.setMinimumSize(new Dimension(150, 40));
        button.setMaximumSize(new Dimension(250, 40));
    }
}
