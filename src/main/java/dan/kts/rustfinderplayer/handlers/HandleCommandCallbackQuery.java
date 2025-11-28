package dan.kts.rustfinderplayer.handlers;

import dan.kts.rustfinderplayer.entity.Request;
import dan.kts.rustfinderplayer.entity.User;
import dan.kts.rustfinderplayer.entity.enums.RequestStatus;
import dan.kts.rustfinderplayer.entity.states.UserStates;
import dan.kts.rustfinderplayer.service.PaginationStateService;
import dan.kts.rustfinderplayer.service.RequestService;
import dan.kts.rustfinderplayer.service.UserService;
import dan.kts.rustfinderplayer.service.UserStateService;
import dan.kts.rustfinderplayer.util.SendMessageBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleCommandCallbackQuery {

    private final UserService userService;
    private final MainMenuHandler mainMenuHandler;
    private final ProfileCommandHandler profileCommandHandler;
    private final UserStateService userStateService;
    private final RequestService requestService;
    private final PaginationStateService paginationStateService;
    private final SendMessageBot sendMessageBot;


    public void handleCallbackQuery(Update update) {
        String data = update.getCallbackQuery().getData();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        switch (data) {
            case "find_teammate": {
                findTeammate(messageId, chatId);
                break;
            }
            case "return_to_main_menu": {
                mainMenuHandler.getMenuFromReturn(callbackQuery);
                break;
            }
            case "my_profile": {
                profileCommandHandler.getMyProfile(callbackQuery);
                break;
            }
            case "turn_off_find": {
                userService.setFindNow(chatId, false);
                profileCommandHandler.getMyProfile(callbackQuery);
                break;
            }
            case "turn_on_find": {
                userService.setFindNow(chatId, true);
                profileCommandHandler.getMyProfile(callbackQuery);
                break;
            }
            case "change_role": {
                profileCommandHandler.handleChangeRole(chatId);
                break;
            }
            case "sign_steam": {
                userStateService.setUserState(chatId, UserStates.AWAITING_STEAM_LINK);
                sendMessageBot.sendMessage(chatId, "Введите аккаунт Steam");
                break;
            }
            case "incoming_requests": {
                handleIncomingRequests(chatId, messageId);
                break;
            }
            default: {
                if (data.startsWith("view_profile_")) {
                    handleViewProfile(data, callbackQuery, chatId);
                } else if (data.startsWith("send_request_")) {
                    sendMessageBot.sendMessage(chatId, "Заявка отправлена!");
                    String[] split = data.split("_");
                    Long chatIdOwnerProfile = Long.parseLong(split[2]);
                    sendMessageBot.sendMessage(chatIdOwnerProfile, "У вас новая заявка от игрока <b>" + userService.getUser(chatId).getNickname() + "</b>");
                    requestService.saveRequest(chatIdOwnerProfile, chatId);
                } else if (data.startsWith("accept_request_")) {
                    handleAcceptRequest(data, chatId, messageId);
                } else if (data.startsWith("decline_request_")) {
                    handleDeclineRequest(data, chatId, messageId);
                } else if (data.startsWith("next_page_")) {
                    int nextPage = Integer.parseInt(data.substring(10));
                    handleNextPage(chatId, nextPage, messageId);
                } else if (data.startsWith("previous_page_")) {
                    int previousPage = Integer.parseInt(data.substring(14));
                    handlePreviousPage(chatId, previousPage, messageId);
                } else {
                    mainMenuHandler.getMenuFromReturn(callbackQuery);
                }
            }
        }
    }

    private void handleIncomingRequests(Long chatId, Integer messageId) {
        List<Request> requests = requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING);
        int sizeRequests = requests.size();

        if (requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING).isEmpty()) {
            sendMessageBot.sendMessage(chatId, "📭 Нет входящих заявок");
            mainMenuHandler.getMenu(chatId);
            return;
        }

        Request currentRequest = requestService.getCurrentRequest(chatId, RequestStatus.PENDING);

        if (currentRequest == null) {
            paginationStateService.setCurrentPage(chatId, 0);
            currentRequest = requestService.getCurrentRequest(chatId, RequestStatus.PENDING);
        }

        User fromUser = currentRequest.getFromUser();
        int currentPage = paginationStateService.getCurrentPage(chatId);

        String text = "📬 <b>Входящие заявки</b> — " + sizeRequests + "\n\n" + """
                <b>%d. %s</b>
                🛠 Роль: <code>%s</code>
                ⏱ Играет: <code>%d ч</code>
                🌐 <b>Steam:</b> <a href="%s">%s</a>
                ────────────────────
                """.formatted(
                currentPage + 1,
                fromUser.getNickname(),
                fromUser.getRole().getDisplayName(),
                fromUser.getHours(),
                fromUser.getSteamLink(),
                fromUser.getSteamLink() == null ? "Не указан профиль" : "Открыть профиль");

        sendMessageBot.executeSafe(EditMessageText.builder()
                .parseMode("HTML")
                .text(text)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(getKeyboard(chatId, fromUser.getChatId()))
                .build());
    }

    private void handleViewProfile(String data, CallbackQuery callbackQuery, Long chatId) {
        String[] split = data.split("_");
        sendMessageBot.executeSafe(EditMessageText.builder()
                .parseMode("HTML")
                .text(userService.getTeammateProfile(Long.parseLong(split[2])))
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("Отправить заявку")
                                        .callbackData("send_request_" + split[2])
                                        .build()
                        ))
                        .keyboardRow(new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("Назад")
                                        .callbackData("find_teammate")
                                        .build()
                        ))
                        .build())
                .chatId(chatId)
                .build());
    }

    private void handleDeclineRequest(String data, Long chatId, Integer messageId) {
        String[] split = data.split("_");
        Long chatIdOwnerRequest = Long.parseLong(split[2]);
        requestService.updateRequest(chatId, chatIdOwnerRequest, RequestStatus.DECLINED);
        adjustPaginationAfterAction(chatId);
        handleIncomingRequests(chatId, messageId);
    }

    private void handleAcceptRequest(String data, Long chatId, Integer messageId) {
        String[] split = data.split("_");
        Long chatIdOwnerRequest = Long.parseLong(split[2]);
        requestService.updateRequest(chatId, chatIdOwnerRequest, RequestStatus.ACCEPTED);
        adjustPaginationAfterAction(chatId);
        handleIncomingRequests(chatId, messageId);
        sendMessageBot.sendMessage(chatId, """
        ✅ <b>Отлично! Вы приняли заявку от %s</b>
        
        📞 <b>Что делать дальше?</b>
        1. Напишите игроку в Telegram
        2. Обсудите время игры
        3. Добавьтесь в друзья в Steam
        4. Созванивайтесь в голосовом чате
        
        🎯 <b>Быстрая связь:</b>
        👉 <a href="tg://user?id=%d">Написать в Telegram</a>
        🌐 <a href="%s">Профиль Steam</a>
        
        Приятной игры! 🎮
        """.formatted(
                userService.getUser(chatIdOwnerRequest).getNickname(),
                chatIdOwnerRequest,
                userService.getUser(chatIdOwnerRequest).getSteamLink()
        ));
        sendMessageBot.sendMessage(chatIdOwnerRequest, """
                🎉 <b>Заявка принята!</b>
                
                Игрок <b>%s</b> принял вашу заявку на команду! 🤝
                
                💬 Теперь вы можете связаться:
                👉 <a href="tg://user?id=%d">%s</a>
                
                Удачной игры и побед! 🏆
                """.formatted(
                userService.getUser(chatId).getNickname(),
                chatId,
                userService.getUser(chatId).getNickname()
        ));
    }

    private void adjustPaginationAfterAction(Long chatId) {
        List<Request> pendingRequests = requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING);
        int currentPage = paginationStateService.getCurrentPage(chatId);

        if (pendingRequests.isEmpty()) {
            paginationStateService.setCurrentPage(chatId, 0);
        } else if (currentPage >= pendingRequests.size()) {
            paginationStateService.setCurrentPage(chatId, pendingRequests.size() - 1);
        }
    }

    private void handlePreviousPage(Long chatId, int previousPage, Integer messageId) {
        List<Request> pendingRequests = requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING);
        if (previousPage < 0 || previousPage >= pendingRequests.size()) {
            return;
        }
        paginationStateService.setCurrentPage(chatId, previousPage);
        handleIncomingRequests(chatId, messageId);
    }

    private void handleNextPage(Long chatId, int nextPage, Integer messageId) {
        List<Request> pendingRequests = requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING);
        if (nextPage < 0 || nextPage >= pendingRequests.size()) {
            return;
        }
        paginationStateService.setCurrentPage(chatId, nextPage);
        handleIncomingRequests(chatId, messageId);
    }


    private InlineKeyboardMarkup getKeyboard(Long chatId, Long chatIdOwnerRequest) {
        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        int currentPage = paginationStateService.getCurrentPage(chatId);
        int maxPage = requestService.getRequestsWhenStatus(chatId, RequestStatus.PENDING).size();

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(InlineKeyboardButton.builder()
                .text("Принять заявку")
                .callbackData("accept_request_" + chatIdOwnerRequest)
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("Отклонить заявку")
                .callbackData("decline_request_" + chatIdOwnerRequest)
                .build());

        InlineKeyboardRow navigationRow = new InlineKeyboardRow();
        if (maxPage > 1 && currentPage < maxPage - 1) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Вперёд")
                    .callbackData("next_page_" + (currentPage + 1))
                    .build());
        }
        if (currentPage > 0) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Назад")
                    .callbackData("previous_page_" + (currentPage - 1))
                    .build());
        }

        if (!navigationRow.isEmpty()) {
            inlineKeyboardRows.add(navigationRow);
        }

        InlineKeyboardRow row3 = new InlineKeyboardRow();
        row3.add(InlineKeyboardButton.builder()
                .text("В главное меню")
                .callbackData("return_to_main_menu")
                .build());
        inlineKeyboardRows.add(row1);
        inlineKeyboardRows.add(row3);
        return InlineKeyboardMarkup.builder()
                .keyboard(inlineKeyboardRows)
                .build();
    }

    private void findTeammate(Integer messageId, Long chatId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Long chatIdTeammate : userService.getUsersChatId()) {
            if (chatIdTeammate.equals(chatId)) {
                continue;
            }
            if (!requestService.isRequestExists(chatId, chatIdTeammate)) {
                InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
                if (userService.getIsFindNow(chatIdTeammate)) {
                    User user = userService.getUser(chatIdTeammate);
                    inlineKeyboardRow.add(InlineKeyboardButton.builder()
                            .text("👤 " + user.getNickname() + " | " + user.getRole().getDisplayName() + " | ⏱" + user.getHours() + "ч")
                            .callbackData("view_profile_" + chatIdTeammate)
                            .build());
                    rows.add(inlineKeyboardRow);
                }
            }
        }
        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
        inlineKeyboardRow.add(InlineKeyboardButton.builder()
                .text("Назад").callbackData("return_to_main_menu").build());
        rows.add(inlineKeyboardRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
        sendMessageBot.executeSafe(EditMessageText.builder()
                .text("Выбери кого ищешь:")
                .messageId(messageId)
                .replyMarkup(inlineKeyboardMarkup)
                .chatId(chatId)
                .build());
    }
}
