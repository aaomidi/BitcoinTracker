package com.aaomidi.bitcointracker.bean;

import com.aaomidi.bitcointracker.BitcoinTracker;
import lombok.ToString;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ToString
public class UpdatableMessage {
    public final static long MAX_TIME = 30 * 60;

    private final BitcoinTracker instance;
    private final TelegramBot bot;
    private final String inlineMessageID;
    private final Currency currency;
    private final boolean isChannel;
    private final long start;

    private Message message;

    private ScheduledFuture future = null;
    private long lastUpdate;
    private LocalBitcoin lastCoin;


    public UpdatableMessage(BitcoinTracker instance, TelegramBot bot, Message message, Currency currency, long delay, boolean isChannel) {
        this(instance, bot, message, null, currency, delay, isChannel);
    }

    public UpdatableMessage(BitcoinTracker instance, TelegramBot bot, String inlineMessageID, Currency currency, long delay, boolean isChannel) {
        this(instance, bot, null, inlineMessageID, currency, delay, isChannel);
    }

    private UpdatableMessage(BitcoinTracker instance, TelegramBot bot, Message message, String inlineMessageID, Currency currency, long delay, boolean isChannel) {

        this.instance = instance;
        this.bot = bot;
        this.inlineMessageID = inlineMessageID;
        this.message = message;
        this.currency = currency;
        this.isChannel = isChannel;


        start = System.currentTimeMillis() / 1000;

        if (!isChannel) {
            editMessage("The bot will update every " + delay + " seconds for " + MAX_TIME / 60 + " minutes.\nTo get live data join @BitcoinTracker");
        }
        future = BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis() / 1000;
            if (!isChannel && currentTime - start >= MAX_TIME) {
                if (future == null) {
                    return;
                }
                future.cancel(false);
            }

            Bitcoin latestCoin = instance.getBitcoinHandler().getLatestCoin();
            if (latestCoin == null) return;
            LocalBitcoin coin = latestCoin.getCoin(currency);
            if (coin == null) return;

            if (lastCoin != null && !coin.hasChanged(lastCoin)) return;

            lastCoin = coin;
            if (message == null) {
                editMessage(coin.getFormattedMessage(!isChannel, (MAX_TIME - (currentTime - start))));
            } else {
                editMessage(coin.getFormattedMessage(isChannel, -1));
            }
        }, 5, delay, TimeUnit.SECONDS);
    }

    private void editMessage(String msg) {
        if (message == null) {
            bot.editInlineMessageText(inlineMessageID, msg, ParseMode.MARKDOWN, false, null);
        } else {
            message = bot.editMessageText(message, msg, ParseMode.MARKDOWN, false, null);
        }
    }
}