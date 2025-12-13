package com.example.demo.bot;

import com.example.demo.config.TelegramBotConfig;
import com.example.demo.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramHealthBot extends TelegramLongPollingBot {
    
    private final TelegramBotConfig botConfig;
    private final HealthReportService healthReportService;
    
    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }
    
    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            if ("/start".equals(messageText)) {
                sendWelcomeMessage(chatId);
            } else if ("📊 Получить отчет".equals(messageText) || "/report".equals(messageText)) {
                sendHealthReport(chatId);
            } else if ("ℹ️ Помощь".equals(messageText) || "/help".equals(messageText)) {
                sendHelpMessage(chatId);
            } else {
                sendUnknownCommand(chatId);
            }
        }
    }
    
    private void sendWelcomeMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🏠 *Добро пожаловать в Smart Home Monitor!*\n\n" +
                "Я бот для мониторинга состояния вашего умного дома.\n" +
                "Используйте кнопки ниже для взаимодействия.");
        
        // Создаем клавиатуру с кнопками
        ReplyKeyboardMarkup keyboardMarkup = createMainKeyboard();
        message.setReplyMarkup(keyboardMarkup);
        
        sendMessage(message);
    }
    
    private void sendHealthReport(long chatId) {
        try {
            String report = healthReportService.generateHealthReport();
            
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(report);
            message.setParseMode("Markdown");
            
            // Добавляем клавиатуру обратно
            ReplyKeyboardMarkup keyboardMarkup = createMainKeyboard();
            message.setReplyMarkup(keyboardMarkup);
            
            sendMessage(message);
            log.info("Health report sent to chat: {}", chatId);
            
        } catch (Exception e) {
            log.error("Error generating health report: {}", e.getMessage());
            sendErrorMessage(chatId);
        }
    }
    
    private void sendHelpMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("*Доступные команды:*\n\n" +
                "📊 Получить отчет - Получить текущий статус умного дома\n" +
                "ℹ️ Помощь - Показать это сообщение\n\n" +
                "*Команды в чате:*\n" +
                "/start - Запустить бота\n" +
                "/report - Получить отчет\n" +
                "/help - Помощь");
        message.setParseMode("Markdown");
        
        sendMessage(message);
    }
    
    private void sendUnknownCommand(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("❌ Неизвестная команда. Используйте кнопки ниже или /help для списка команд.");
        
        ReplyKeyboardMarkup keyboardMarkup = createMainKeyboard();
        message.setReplyMarkup(keyboardMarkup);
        
        sendMessage(message);
    }
    
    private void sendErrorMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("❌ Произошла ошибка при формировании отчета. Попробуйте позже.");
        
        sendMessage(message);
    }
    
    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📊 Получить отчет");
        keyboard.add(row1);
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("ℹ️ Помощь");
        keyboard.add(row2);
        
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
    
    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending Telegram message: {}", e.getMessage());
        }
    }
}