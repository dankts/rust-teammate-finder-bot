package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.exceptions.UserNotFoundException;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCommandHandler {

    private final UserService userService;
    private final SendMessageBot sendMessageBot;

    public void handleCommand(Update update) {
        String text = update.getMessage().getText();
        Long adminChatId = update.getMessage().getChatId();

        if (text.equals("/admin")) {
            sendMessageBot.sendMessage(adminChatId, """
                            Доступные команды:
                                /banUser chatId - забанить пользователя
                                /unBanUser chatId - разбанить пользователя
                                /getAllUserMessage message - отправить сообщение всем пользователям
                    """);
            return;
        }

        if (text.startsWith("/banUser")) {
            String[] split = text.split(" ");
            Long chatId = Long.parseLong(split[1]);
            banUser(adminChatId, chatId);
        } else if (text.startsWith("/unBanUser")) {
            String[] split = text.split(" ");
            Long chatId = Long.parseLong(split[1]);
            unBanUser(adminChatId, chatId);
        } else if (text.startsWith("/getAllUserMessage")) {
            String message = text.substring(19);
            sendAllUserMessage(message);
        }
    }

    private void banUser(Long adminChatId, Long chatId) {
        try {
            userService.banUser(chatId);
            sendMessageBot.sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " забанен!");
        } catch (UserNotFoundException e) {
            sendMessageBot.sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " не найден!");
        }
    }

    private void unBanUser(Long adminChatId, Long chatId) {
        try {
            userService.unBanUser(chatId);
            sendMessageBot.sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " разбанен!");
        } catch (UserNotFoundException e) {
            sendMessageBot.sendMessage(adminChatId, "Пользователь с чат ид " + chatId + " не найден!");
        }
    }

    private void sendAllUserMessage(String message) {
        List<Long> usersChatId = userService.getUsersChatId();
        log.info("Start send message to all users - {}", usersChatId.size());
        for (Long l : usersChatId) {
            sendMessageBot.sendMessage(l, message);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.warn("Send message to all users interrupted!");
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("End send message to all users");
    }
}
