package org.face_recognition;

// Класс AudioPlayerState хранит состояние аудиоплеера
// Содержит данные о текущем аудиоклипе, элементах управления и их настройках
import javax.sound.sampled.Clip;
import javax.swing.*;


//Состояние аудиоплеера
public class AudioPlayerState {
    Clip clip; // Аудиоклип для воспроизведения
    JButton playPauseButton; // Кнопка для воспроизведения/паузы
    JSlider progressBar; // Ползунок для отображения и управления прогрессом воспроизведения
    boolean isPlaying = false; // Флаг, указывающий, воспроизводится ли аудио
    boolean isSeeking = false; // Флаг, указывающий, выполняется ли перемотка
    Timer timer; // Таймер для обновления прогресса воспроизведения
    ImageIcon playIcon; // Иконка для кнопки "воспроизвести"
    ImageIcon pauseIcon; // Иконка для кнопки "пауза"
    String input; // Входная строка, связанная с аудиофайлом (например, путь или идентификатор)

    // Конструктор, инициализирующий иконки и входную строку
    AudioPlayerState(ImageIcon playIcon, ImageIcon pauseIcon, String input) {
        this.playIcon = playIcon;
        this.pauseIcon = pauseIcon;
        this.input = input;
    }
}
