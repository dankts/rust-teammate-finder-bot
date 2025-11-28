package dan.kts.rustfinderplayer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class SendMessageBot {

    private final TelegramClient telegramClient;

    public SendMessageBot(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessage(Long chatId, String text) {
        executeSafe(SendMessage.builder()
                .parseMode("HTML")
                .chatId(chatId)
                .text(text)
                .build());
    }

    public void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        executeSafe(SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(text)
                .replyMarkup(inlineKeyboardMarkup)
                .build());
    }

    public void executeSafe(SendMessage sendMessage) {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения");
        }
    }

    public void executeSafe(EditMessageText editMessageText) {
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения");
        }
    }
}
