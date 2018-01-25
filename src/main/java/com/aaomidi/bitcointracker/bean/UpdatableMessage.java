package com.aaomidi.bitcointracker.bean;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.registries.CoinRegistry;
import lombok.ToString;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ToString
public class UpdatableMessage {
    public final static long MAX_TIME = 30 * 60;

    public final static double CHANGE_PERCENT = 3;

    private final BitcoinTracker instance;
    private final TelegramBot bot;
    private final String inlineMessageID;
    private final CoinType coinType;
    private final boolean isChannel;
    private final long start;

    private Message message;

    private ScheduledFuture future = null;

    private boolean isFirst = true;
    private CoinRegistry coinRegistry;
    private double lastAlertPrice = 0;


    public UpdatableMessage(BitcoinTracker instance, TelegramBot bot, Message message, CoinType coinType, long delay, boolean isChannel, boolean isInstant) {
        this(instance, bot, message, null, coinType, delay, isChannel, isInstant);
    }

    public UpdatableMessage(BitcoinTracker instance, TelegramBot bot, String inlineMessageID, CoinType coinType, long delay, boolean isChannel) {
        this(instance, bot, null, inlineMessageID, coinType, delay, isChannel, false);
    }

    private UpdatableMessage(BitcoinTracker instance, TelegramBot bot, Message message, String inlineMessageID, CoinType coinType, long delay, boolean isChannel, boolean isInstant) {
        long startDelay = 5L;
        if (isInstant) {
            startDelay = 0L;
        }
        this.instance = instance;
        this.bot = bot;
        this.inlineMessageID = inlineMessageID;
        this.message = message;
        this.coinType = coinType;
        this.isChannel = isChannel;


        start = System.currentTimeMillis() / 1000;

        if (!isChannel) {
            editMessage("The bot will update every " + delay + " seconds for " + MAX_TIME / 60 + " minutes.\nTo get live data join @BitcoinTracker");
        }
        future = BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis() / 1000;
                if (!isChannel && currentTime - start >= MAX_TIME) {
                    if (future == null) {
                        return;
                    }
                    editMessage("Live update period ended.\n\nTo get constant live bitcoin tracking join @BitcoinTracker");
                    future.cancel(false);
                    return;
                }

                if (isFirst) {
                    coinRegistry = instance.getBitcoinHandler().getCoin(coinType);
                    if (coinRegistry == null) return;
                    double price = coinRegistry.getAverage(99);
                    if (price == 0) return;

                    isFirst = false;
                    lastAlertPrice = price;
                }
                if (isChannel) {
                    if (handleChannelAlert()) {
                        future.cancel(false);
                        return;
                    }
                }

                if (message == null) {
                    editMessage(coinRegistry.getFormattedMessage(!isChannel, (MAX_TIME - (currentTime - start))));
                } else {
                    editMessage(coinRegistry.getFormattedMessage(isChannel, -1));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, startDelay, delay, TimeUnit.SECONDS);
    }

    private void editMessage(String msg) {
        if (message == null) {
            bot.editInlineMessageText(inlineMessageID, msg, ParseMode.MARKDOWN, false, null);
        } else {
            Message newMsg = bot.editMessageText(message, msg, ParseMode.MARKDOWN, false, null);
            if (newMsg != null) {
                message = newMsg;
            }
        }
    }

    private boolean handleChannelAlert() {
        double now = coinRegistry.getAverage(99);
        double percent = CoinRegistry.getPercent(now, lastAlertPrice);

        double percentChange = 100 - percent;

        if (Math.abs(percentChange) < CHANGE_PERCENT) {
            return false;
        }

        String action;
        if (percentChange < 0) {
            action = "increased";
        } else {
            action = "decreased";
        }

        editMessage(String.format("\uD83D\uDD14\uD83D\uDD14\uD83D\uDD14\n\n*%s Price Alert!*\n\nThe price has *%s* by *%.2f%%*", coinType.getHumanizedName(), action, Math.abs(percentChange)));

        instance.getTelegramHandler().startChannel(true);
        return true;
    }
}
