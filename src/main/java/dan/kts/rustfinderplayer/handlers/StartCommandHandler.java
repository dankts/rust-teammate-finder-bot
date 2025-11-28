package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.entity.enums.Role;
import dan.kts.rustfinderplayer.entity.User;
import dan.kts.rustfinderplayer.entity.states.UserStates;
import dan.kts.rustfinderplayer.service.BotService;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.service.UserStateService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dan.kts.rustfinderplayer.util.SendMessageBot.sendMessage;

@Component
@RequiredArgsConstructor
public class StartCommandHandler {

    private final UserService userService;
    private final MainMenuHandler mainMenuHandler;
    private final BotService botService;
    private final UserStateService userStateService;
    private final ProfileCommandHandler profileCommandHandler;
    private final Map<Long, PartialUser> buffer = new ConcurrentHashMap<>();

    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        if (text.equals("/start")) {
            startCommand(chatId);
        } else {
            if (buffer.containsKey(chatId)) {
                register(update);
            }
        }
    }

    public void startCommand(Long chatId) {
        if (userService.isRegisteredUser(chatId)) {
            mainMenuHandler.getMenu(chatId);
        } else {
            userStateService.setUserState(chatId, UserStates.REGISTRATION_NICKNAME);
            sendMessage(chatId, botService.startCommand());
            buffer.putIfAbsent(chatId, new PartialUser());
            sendMessage(chatId, "\uD83D\uDC64 Напишите мне свой игровой никнейм, который будут видеть все.\n");
        }
    }

    public void register(Update update) {
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();
        PartialUser userBuffer = buffer.get(chatId);
        if (userBuffer == null) {
            userBuffer = new PartialUser();
            buffer.put(chatId, userBuffer);
        }

        switch (userStateService.getUserState(chatId)) {
            case REGISTRATION_NICKNAME -> {
                userBuffer.setUsername(text);
                userStateService.setUserState(chatId, UserStates.REGISTRATION_AGE);
                sendMessage(chatId, "⏱\uFE0F Сколько тебе лет?\n");

            }
            case REGISTRATION_AGE -> {
                userBuffer.setAge(Integer.valueOf(text));
                userStateService.setUserState(chatId, UserStates.REGISTRATION_HOURS);
                sendMessage(chatId, "⏱\uFE0F Сколько у тебя часов в Rust?\n");
            }
            case REGISTRATION_HOURS -> {
                userBuffer.setHours(Integer.valueOf(text));
                userStateService.setUserState(chatId, UserStates.REGISTRATION_ROLE);
                profileCommandHandler.handleChangeRole(chatId);
            }
            case REGISTRATION_ROLE -> {
                userBuffer.setRole(Role.getFromdisplayName(text));
                userStateService.setUserState(chatId, UserStates.REGISTRATION_FIND);
                sendMessage(chatId, "Ищите ли вы сейчас тиммейта? (да, нет)");            }
            case REGISTRATION_FIND -> {
                userBuffer.setFind(text.equalsIgnoreCase("да"));
                User user = User.builder()
                        .username(username)
                        .nickname(userBuffer.getUsername())
                        .hours(userBuffer.getHours())
                        .isFindNow(userBuffer.isFind())
                        .age(userBuffer.getAge())
                        .role(userBuffer.getRole())
                        .chatId(chatId)
                        .build();
                userService.registerUser(user);
                userStateService.clearUserState(chatId);
                sendMessage(chatId, "Регистрация прошла успешно!");
                buffer.remove(chatId);
                mainMenuHandler.getMenu(chatId);
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    private static class PartialUser {

        private String username;
        private Integer hours;
        private Integer age;
        private boolean find;
        private Role role;
    }
}