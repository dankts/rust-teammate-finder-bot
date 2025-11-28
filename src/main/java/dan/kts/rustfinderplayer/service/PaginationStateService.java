package dan.kts.rustfinderplayer.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaginationStateService {

    private final Map<Long, Integer> userPageStates = new ConcurrentHashMap<>();

    public void setCurrentPage(Long chatId, Integer page) {
        userPageStates.put(chatId, page);
    }

    public int getCurrentPage(Long chatId) {
        return userPageStates.getOrDefault(chatId, 0);
    }

    public void clear(Long chatId) {
        userPageStates.remove(chatId);
    }
}
