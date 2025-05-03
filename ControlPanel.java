package org.face_recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultEditorKit;

// Предоставляет функциональность для настройки текстовых полей (JTextField и JPasswordField)
// Добавляет контекстное меню с действиями копирования и вставки
// Поддерживает горячие клавиши Ctrl+C и Ctrl+V для соответствующих действий
// Используется в интерфейсе пользователя для упрощения работы с текстовыми компонентами

public class ControlPanel {

    // Полная настройка поля JPasswordField: копирование и вставка
    void configurePasswordField(JPasswordField passwordField) {
        configureCopyMenu(passwordField);
        configurePasteMenu(passwordField);
    }

    // Полная настройка поля JTextField: копирование и вставка
    void configureTextField(JTextField textField) {
        configureCopyMenu(textField);
        configurePasteMenu(textField);
    }

    // Настройка только копирования для текстового компонента
    void configureCopyMenu(JTextComponent textComponent) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem copyItem = createCopyItem(textComponent);
        contextMenu.add(copyItem);
        addContextMenu(textComponent, contextMenu);
        addCopyShortcut(textComponent);
    }

    // Настройка копирования и удаления для текстового компонента
    void configureCopyMenuEndDell(JTextComponent textComponent, JMenuItem deleteItem) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem copyItem = createCopyItem(textComponent);
        contextMenu.add(copyItem);
        contextMenu.add(deleteItem);
        addContextMenu(textComponent, contextMenu);
        addCopyShortcut(textComponent);
    }

    // Настройка только вставки для текстового компонента
    void configurePasteMenu(JTextComponent textComponent) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem pasteItem = createPasteItem(textComponent);
        contextMenu.add(pasteItem);
        addContextMenu(textComponent, contextMenu);
        addPasteShortcut(textComponent);
    }

    // Создание пункта меню "Копировать" с оформлением
    private JMenuItem createCopyItem(JTextComponent textComponent) {
        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.setFont(new Font("Arial", Font.PLAIN, 14));
        copyItem.setBackground(Color.WHITE);
        copyItem.setForeground(Color.BLACK);
        copyItem.addActionListener(e -> {
            String text = textComponent instanceof JPasswordField
                    ? new String(((JPasswordField) textComponent).getPassword())
                    : textComponent.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(text), null
            );
        });
        return copyItem;
    }

    // Создание пункта меню "Вставить" с оформлением
    private JMenuItem createPasteItem(JTextComponent textComponent) {
        JMenuItem pasteItem = new JMenuItem("Вставить");
        pasteItem.setFont(new Font("Arial", Font.PLAIN, 14));
        pasteItem.setBackground(Color.WHITE);
        pasteItem.setForeground(Color.BLACK);
        pasteItem.addActionListener(e -> textComponent.paste());
        return pasteItem;
    }

    // Добавление контекстного меню к текстовому компоненту
    private void addContextMenu(JTextComponent textComponent, JPopupMenu contextMenu) {
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(textComponent, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(textComponent, e.getX(), e.getY());
                }
            }
        });
    }

    // Добавление горячей клавиши Ctrl+C для копирования
    private void addCopyShortcut(JTextComponent textComponent) {
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String text = textComponent instanceof JPasswordField
                        ? new String(((JPasswordField) textComponent).getPassword())
                        : textComponent.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(text), null
                );
            }
        };

        InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textComponent.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
        actionMap.put("copy", copyAction);
    }

    // Добавление горячей клавиши Ctrl+V для вставки
    private void addPasteShortcut(JTextComponent textComponent) {
        // Используем стандартное действие вставки из DefaultEditorKit
        Action pasteAction = textComponent.getActionMap().get(DefaultEditorKit.pasteAction);

        if (pasteAction != null) {
            InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = textComponent.getActionMap();

            inputMap.put(KeyStroke.getKeyStroke("ctrl V"), DefaultEditorKit.pasteAction);
            actionMap.put(DefaultEditorKit.pasteAction, pasteAction);
        }
    }
}
