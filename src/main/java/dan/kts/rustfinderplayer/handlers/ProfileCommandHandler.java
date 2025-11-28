package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.entity.enums.Role;
import dan.kts.rustfinderplayer.entity.states.UserStates;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.service.UserStateService;
import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCommandHandler {

    private final UserService userService;
    private final UserStateService userStateService;
    private final SendMessageBot sendMessageBot;

    public void getMyProfile(CallbackQuery callbackQuery) {
        sendMessageBot.executeSafe(EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .parseMode(ParseMode.HTML)
                .text(userService.getUserProfile(callbackQuery.getMessage().getChatId()))
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(keyboardProfile(callbackQuery.getMessage().getChatId()))
                .build());
    }

    public void getMyProfileAsNewMessage(Long chatId) {
        sendMessageBot.sendMessageWithInlineKeyboard(chatId, userService.getUserProfile(chatId), keyboardProfile(chatId));
    }

    public void changeRole(Update update) {
        Long chatId = update.getMessage().getChatId();

        if (!userStateService.isUserInState(chatId, UserStates.AWAITING_ROLE_SELECTION)) {
            return;
        }

        String text = update.getMessage().getText();
        Role role = switch (text) {
            case "Комбатер/Стрелок" -> Role.PvP;
            case "Электрик" -> Role.Electrician;
            case "Билдер" -> Role.Builder;
            case "Фермер" -> Role.Farmer;
            default -> null;
        };

        if (role != null) {
            userService.updateRole(chatId, role);
            userStateService.clearUserState(chatId);
            sendMessageBot.sendMessage(chatId, "Роль изменена!");
            sendMessageBot.sendMessageWithInlineKeyboard(chatId, userService.getUserProfile(chatId), keyboardProfile(chatId));
        } else {
            sendMessageBot.sendMessage(chatId, "Неверная роль! Выберите роль из меню ниже.");
        }
    }


    public void getSteamLinkAndSave(Update update) {
        if (update.getMessage().getText().contains("steamcommunity.com")) {
            userService.inLinkSteam(update.getMessage().getChatId(), update.getMessage().getText());
            sendMessageBot.sendMessage(update.getMessage().getChatId(), "Ваш профиль сохранен!");
            getMyProfileAsNewMessage(update.getMessage().getChatId());
        }
        userStateService.clearUserState(update.getMessage().getChatId());
    }

    private InlineKeyboardMarkup keyboardProfile(Long chatId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(InlineKeyboardButton.builder()
                .text("Привязать Steam")
                .callbackData("sign_steam")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("Сменить роль")
                .callbackData("change_role")
                .build());

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("return_to_main_menu")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text(userService.getIsFindNow(chatId) ? "Выключить поиск" : "Включить поиск")
                .callbackData(userService.getIsFindNow(chatId) ? "turn_off_find" : "turn_on_find")
                .build());

        rows.add(row1);
        rows.add(row2);
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public void handleChangeRole(Long chatId) {
        if (!userService.isRegisteredUser(chatId)) {
            userStateService.setUserState(chatId, UserStates.REGISTRATION_ROLE);
        } else {
            userStateService.setUserState(chatId, UserStates.AWAITING_ROLE_SELECTION);

        }
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(KeyboardButton.builder().text("Комбатер/Стрелок").build());
        keyboardRow1.add(KeyboardButton.builder().text("Электрик").build());
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add(KeyboardButton.builder().text("Билдер").build());
        keyboardRow2.add(KeyboardButton.builder().text("Фермер").build());
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(keyboardRow1);
        keyboard.add(keyboardRow2);

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
        sendMessageBot.executeSafe(SendMessage.builder()
                .chatId(chatId)
                .text("Выберите роль")
                .replyMarkup(replyKeyboardMarkup)
                .build());
    }
}
