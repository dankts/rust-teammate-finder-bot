package dan.kts.rustfinderplayer.bot;

import dan.kts.rustfinderplayer.entity.states.UserStates;
import dan.kts.rustfinderplayer.handlers.*;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.service.UserStateService;
import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCustomer implements LongPollingSingleThreadUpdateConsumer {

    private final HandleCommandCallbackQuery handleCommandCallbackQuery;
    private final AdminCommandHandler adminCommandHandler;
    private final ProfileCommandHandler profileCommandHandler;
    private final StartCommandHandler startCommandHandler;
    private final UserStateService userStateService;
    private final UserService userService;
    private final SendMessageBot sendMessageBot;

    @Value("${bot.admin.chatid}")
    private Long adminChatId;


    @Override
    public void consume(Update update) {
        Long chatId = getChatId(update);
        if (userService.isBanned(chatId)) {
            sendMessageBot.sendMessage(chatId, "Вы забанены");
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (Objects.equals(chatId, adminChatId)) {
                adminCommandHandler.handleCommand(update);
            }

            UserStates userStates = userStateService.getUserState(chatId);
            switch (userStates) {
                case AWAITING_ROLE_SELECTION:
                    profileCommandHandler.changeRole(update);
                    break;
                case AWAITING_STEAM_LINK:
                    profileCommandHandler.getSteamLinkAndSave(update);
                    break;
            }
            startCommandHandler.handle(update);
        } else if (update.hasCallbackQuery()) {
            handleCommandCallbackQuery.handleCallbackQuery(update);
        }
    }

    private Long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom().getId();
        }
        return null;
    }
}
