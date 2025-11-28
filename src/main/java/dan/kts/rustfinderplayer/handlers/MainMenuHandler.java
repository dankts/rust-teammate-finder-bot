package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component
@RequiredArgsConstructor
public class MainMenuHandler {

    private final SendMessageBot sendMessageBot;

    public InlineKeyboardMarkup createMainMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Мой профиль").callbackData("my_profile").build(),
                        InlineKeyboardButton.builder().text("\uD83D\uDD0D Найти тиммейта").callbackData("find_teammate").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("📬 Входящие заявки")
                                .callbackData("incoming_requests")
                                .build()
                )).build();
    }

    public void getMenu(Long chatId) {
        sendMessageBot.sendMessageWithInlineKeyboard(chatId, "🏠 Главное меню:\nВыбери, что тебя интересует:", createMainMenu());
    }

    public void getMenuFromReturn(CallbackQuery callbackQuery) {
        sendMessageBot.executeSafe(EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("🏠 Главное меню:\nВыбери, что тебя интересует:")
                        .replyMarkup(createMainMenu())
                .build());
    }
}
