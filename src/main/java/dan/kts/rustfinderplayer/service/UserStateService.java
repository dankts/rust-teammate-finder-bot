package dan.kts.rustfinderplayer.service;

import dan.kts.rustfinderplayer.entity.states.UserStates;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {

    private final Map<Long, UserStates> userStatesMap  = new ConcurrentHashMap<>();

    public void setUserState(Long chatId, UserStates userState) {
        if (userState == UserStates.IDLE) {
            userStatesMap.remove(chatId);
        } else {
            userStatesMap.put(chatId, userState);
        }
    }

    public UserStates getUserState(Long chatId) {
        return userStatesMap.getOrDefault(chatId, UserStates.IDLE);
    }

    public boolean isUserInState(Long chatId, UserStates userState) {
        return getUserState(chatId) == userState;
    }

    public void clearUserState(Long chatId) {
        userStatesMap.remove(chatId);
    }
}
