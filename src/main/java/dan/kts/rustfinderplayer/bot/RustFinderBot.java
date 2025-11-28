package dan.kts.rustfinderplayer.bot;

import dan.kts.rustfinderplayer.configuration.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;


@Slf4j
@Component
public class RustFinderBot implements SpringLongPollingBot {

    private final BotConfig botConfig;
    private final UpdateCustomer updateCustomer;

    public RustFinderBot(BotConfig botConfig, UpdateCustomer updateCustomer) {
        this.botConfig = botConfig;
        this.updateCustomer = updateCustomer;
    }


    @Override
    public String getBotToken() {
        return botConfig.getBOT_TOKEN();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateCustomer;
    }
}
