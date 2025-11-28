package dan.kts.rustfinderplayer.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendMessageBot {

    private static TelegramClient telegramClient;

    @Autowired
    private void setTelegramClient(TelegramClient telegramClient) {
        SendMessageBot.telegramClient = telegramClient;
    }

    public static void sendMessage(Long chatId, String text) {
        executeSafe(SendMessage.builder()
                .parseMode("HTML")
                .chatId(chatId)
                .text(text)
                .build());
    }

    public static void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        executeSafe(SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(text)
                .replyMarkup(inlineKeyboardMarkup)
                .build());
    }

    public static void executeSafe(SendMessage sendMessage) {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения");
        }
    }

    public static void executeSafe(EditMessageText editMessageText) {
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения");
        }
    }
}
