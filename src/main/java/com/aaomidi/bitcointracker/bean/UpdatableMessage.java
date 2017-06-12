package com.aaomidi.bitcointracker.bean;

import com.aaomidi.bitcointracker.BitcoinTracker;
import lombok.ToString;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;

import java.util.concurrent.TimeUnit;

@ToString
public class UpdatableMessage {
    private final BitcoinTracker instance;
    private final TelegramBot bot;
    private final Message message;
    private final Currency currency;
    private final boolean isChannel;

    private long lastUpdate;
    private LocalBitcoin lastCoin;


    public UpdatableMessage(BitcoinTracker instance, TelegramBot bot, Message message, Currency currency, long delay, boolean isChannel) {
        this.instance = instance;
        this.bot = bot;
        this.message = message;
        this.currency = currency;
        this.isChannel = isChannel;

        BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> {
            Bitcoin latestCoin = instance.getBitcoinHandler().getLatestCoin();
            if (latestCoin == null) return;
            LocalBitcoin coin = latestCoin.getCoin(currency);
            if (coin == null) return;

            if (lastCoin != null && !coin.hasChanged(lastCoin)) return;

            lastCoin = coin;
            bot.editMessageText(message, coin.getFormattedMessage(!isChannel), ParseMode.MARKDOWN, false, null);
        }, delay, delay, TimeUnit.SECONDS);
    }
}
