package com.aaomidi.bitcointracker.registries;

import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.CryptoCoin;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pro.zackpollard.telegrambot.api.chat.inline.send.content.InputTextMessageContent;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResultArticle;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.keyboards.InlineKeyboardButton;
import pro.zackpollard.telegrambot.api.keyboards.InlineKeyboardMarkup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

@ToString
@RequiredArgsConstructor
public class CoinRegistry {
    private transient static TimeZone tz = TimeZone.getTimeZone("UTC");
    private transient static DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss z");

    static {
        df.setTimeZone(tz);
    }

    private final CoinType type;

    private transient long lastUpdate = 0;
    private transient HashMap<Integer, HashMap<String, CryptoCoin>> coins = new HashMap<>();

    public void registerCoin(CryptoCoin coin) {
        lastUpdate = coin.getTimestamp();
        HashMap<String, CryptoCoin> map = coins.getOrDefault(coin.getDay(), new HashMap<>());
        map.put(coin.getExchange(), coin);
        coins.put(coin.getDay(), map);
    }

    public double getAverage(int day) {
        HashMap<String, CryptoCoin> map = coins.getOrDefault(day, new HashMap<>());
        double average = 0;
        for (CryptoCoin coin : map.values()) {
            average += coin.getPrice();
        }
        if (map.size() != 0) {
            average /= map.size();
        }

        return average;
    }

    private double getPercent(double now, double other) {
        return other * 100 / now;
    }

    private String getPercentString(double now, double other) {
        double percent = getPercent(now, other);
        double val = percent - 100;

        if (val > 0) {
            return String.format("\t⬆ +%.2f%%", Math.abs(val));
        }else {
            return String.format("\t⬇ -%.2f%%", Math.abs(val));
        }
    }

    public String getFormattedMessage(boolean isPrivate, long timeRemaining) {
        StringBuilder msg = new StringBuilder(String.format("💰 %s Tracker 💰", type.getHumanizedName()));
        double now = getAverage(0);
        double other;

        msg.append(String.format("%n%n\uD83D\uDD37 Right now: *$%.2f*", now));

        other = getAverage(1);
        msg.append(String.format("%n%n☀ 24 Hours: *$%.2f* %s", other, getPercentString(now, other)));

        other = getAverage(7);
        msg.append(String.format("%n\uD83D\uDCC6 Week: *$%.2f* %s", getAverage(7), getPercentString(now, other)));

        other = getAverage(30);
        msg.append(String.format("%n\uD83D\uDDD3 Month: *$%.2f* %s", getAverage(30), getPercentString(now, other)));

        msg.append(String.format("%n%n\uD83D\uDD38 Last Updated: *%s*", df.format(lastUpdate)));
        if (timeRemaining > 0) {
            msg.append(String.format("%n%n⏰ Time remaining: *%d minutes %d seconds*", timeRemaining / 60, timeRemaining % 60));
        }

        if (isPrivate) {
            msg.append(String.format("%n%nJoin @BitCoinTracker for live tracking and price alerts!"));
        }
        return msg.toString();
    }

    public InlineQueryResultArticle getInline() {
        return InlineQueryResultArticle.builder()
                .title(type.getHumanizedName())
                .description(String.format("Price: $%.2f", getAverage(0)))
                .inputMessageContent(
                        InputTextMessageContent.builder()
                                .messageText(getFormattedMessage(true, -1))
                                .parseMode(ParseMode.MARKDOWN)
                                .build())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .addRow(InlineKeyboardButton.builder()
                                .text("Start live updates!")
                                .callbackData(type.toString())
                                .build()
                        )
                        .build())
                .build();
    }
}
