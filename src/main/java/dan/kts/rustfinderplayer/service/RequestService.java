package dan.kts.rustfinderplayer.service;


import dan.kts.rustfinderplayer.entity.Request;
import dan.kts.rustfinderplayer.entity.User;
import dan.kts.rustfinderplayer.entity.enums.RequestStatus;
import dan.kts.rustfinderplayer.exceptions.UserNotFoundException;
import dan.kts.rustfinderplayer.repository.RequestRepository;
import dan.kts.rustfinderplayer.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final PaginationStateService paginationStateService;

    public void saveRequest(Long toChatId, Long fromChatId) {
        User toUser = userRepository.getUserByChatId(toChatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        User fromUser = userRepository.getUserByChatId(fromChatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        boolean exist = requestRepository.existsByFromUserAndToUserAndStatus(fromUser, toUser, RequestStatus.PENDING);

        if (exist) {
            return;
        }

        requestRepository.save(Request.builder()
                .toUser(toUser)
                .fromUser(fromUser)
                .status(RequestStatus.PENDING)
                .build());
    }

    public Request getCurrentRequest(Long chatId, RequestStatus status) {
        List<Request> requests = getRequestsWhenStatus(chatId, status);

        if (requests.isEmpty()) {
            throw new IllegalStateException("No requests");
        }

        int currentPage = paginationStateService.getCurrentPage(chatId);

        if (currentPage < 0) {
            currentPage = 0;
            paginationStateService.setCurrentPage(chatId, currentPage);
        } else if (currentPage >= requests.size()) {
            currentPage = requests.size() - 1;
            paginationStateService.setCurrentPage(chatId, currentPage);
        }
        return requests.get(currentPage);
    }

    public List<Request> getRequestsWhenStatus(Long chatId, RequestStatus status) {
        User user = userRepository.getUserByChatId(chatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        return requestRepository.findAllByToUserAndStatus(user, status);
    }

    @Transactional
    public void updateRequest(Long chatId, Long chatIdOwnerRequest, RequestStatus requestStatus) {
        User fromUser = userRepository.getUserByChatId(chatIdOwnerRequest).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        User toUser = userRepository.getUserByChatId(chatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        Request request = requestRepository.findAByFromUserAndToUser(fromUser, toUser).orElse(null);

        Objects.requireNonNull(request).setStatus(requestStatus);
        requestRepository.save(request);
    }

    public boolean isRequestExists(Long chatId, Long chatIdTeammate) {
        User fromUser = userRepository.getUserByChatId(chatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        User toUser = userRepository.getUserByChatId(chatIdTeammate).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        return requestRepository.existsByFromUserAndToUser(fromUser, toUser);
    }
}
