package dan.kts.rustfinderplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {

    public String startCommand() {
        return "\uD83E\uDD80 Добро пожаловать в бот для поиска тиммейтов в Rust!\n" +
               "\n" +
               "Здесь ты сможешь найти надежных напарников для вайпов, рейдов и строительства могучей базы.\n" +
               "\n" +
               "*Чтобы начать, тебе нужно создать свой игровой профиль.*\n" +
               "Это займет не более 2-х минут.\n" +
               "\n" +
               "Готов начать?";
    }


}
