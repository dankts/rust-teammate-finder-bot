package dan.kts.rustfinderplayer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String BOT_TOKEN;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(BOT_TOKEN);
    }

    public String getBOT_TOKEN() {
        return BOT_TOKEN;
    }
}
