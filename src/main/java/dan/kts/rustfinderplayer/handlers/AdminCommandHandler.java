package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.exceptions.UserNotFoundException;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static dan.kts.rustfinderplayer.util.SendMessageBot.sendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminCommandHandler {

    private final UserService userService;

    public void handleCommand(Update update) {
        String text = update.getMessage().getText();
        Long adminChatId = update.getMessage().getChatId();

        if (text.startsWith("/banUser")) {
            String[] split = text.split(" ");
            Long chatId = Long.parseLong(split[1]);
            banUser(adminChatId, chatId);
        } else if (text.startsWith("/unBanUser")) {
            String[] split = text.split(" ");
            Long chatId = Long.parseLong(split[1]);
            unBanUser(adminChatId, chatId);
        } else if (text.startsWith("/getAllUserMessage")) {
            String[] split = text.split(" ");
            String message = split[1];
            sendAllUserMessage(message);
        }
    }

    private void banUser(Long adminChatId, Long chatId) {
        try {
            userService.banUser(chatId);
            sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " забанен!");
        } catch (UserNotFoundException e) {
            sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " не найден!");
        }
    }

    private void unBanUser(Long adminChatId, Long chatId) {
        try {
            userService.unBanUser(chatId);
            sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " разбанен!");
        } catch (UserNotFoundException e) {
            sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " не найден!");
        }
    }

    private void sendAllUserMessage(String message) {
        List<Long> usersChatId = userService.getUsersChatId();
        for (Long l : usersChatId) {
            SendMessageBot.sendMessage(l, message);
        }
    }
}
