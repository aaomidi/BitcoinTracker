package com.aaomidi.bitcointracker.registries;

import com.aaomidi.bitcointracker.BitcoinTracker;
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
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

@ToString
@RequiredArgsConstructor
public class CoinRegistry {
    private transient static TimeZone tz = TimeZone.getTimeZone("UTC");
    private transient static DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss z");

    static {
        df.setTimeZone(tz);
    }

    private final BitcoinTracker instance;
    private final CoinType type;

    private transient long lastUpdate = 0;
    private transient ConcurrentHashMap<Integer, ConcurrentHashMap<String, CryptoCoin>> coins = new ConcurrentHashMap<>();

    public static double getPercent(double now, double other) {
        return now * 100 / other;
    }

    public void registerCoin(CryptoCoin coin) {
        try {
            lastUpdate = coin.getTimestamp();
            ConcurrentHashMap<String, CryptoCoin> map = coins.getOrDefault(coin.getDay(), new ConcurrentHashMap<>());
            map.put(coin.getExchange(), coin);
            coins.put(coin.getDay(), map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public double getAverage(int day) {
        try {
            ConcurrentHashMap<String, CryptoCoin> map = coins.getOrDefault(day, new ConcurrentHashMap<>());
            double average = 0;
            for (CryptoCoin coin : map.values()) {
                average += coin.getPrice();
            }
            if (map.size() != 0) {
                average /= map.size();
            }
            // Handle other types of coins.
            if (type != CoinType.BTC && type != CoinType.LTC) {
                double btc = instance.getBitcoinHandler().getCoin(CoinType.BTC).getAverage(99);

                return btc * average;
            }


            return average;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private String getPercentString(double now, double other) {
        double percent = getPercent(now, other);
        double val = percent - 100;

        if (val > 0) {
            return String.format("\t⬆ +%.2f%%", Math.abs(val));
        } else {
            return String.format("\t⬇ -%.2f%%", Math.abs(val));
        }
    }

    public String getFormattedMessage(boolean isPrivate, long timeRemaining) {
        try {

            StringBuilder msg = new StringBuilder(String.format("💰 %s Tracker 💰", type.getHumanizedName()));
            double now = getAverage(99);
            double other;

            msg.append(String.format("%n%n\uD83D\uDD37 Right now: *$%.2f*", now));

            other = getAverage(1);
            msg.append(String.format("%n%n☀ 24 Hours: *$%.2f* %s", other, getPercentString(now, other)));

            other = getAverage(7);
            msg.append(String.format("%n\uD83D\uDCC6 Week: *$%.2f* %s", other, getPercentString(now, other)));

            other = getAverage(30);
            msg.append(String.format("%n\uD83D\uDDD3 Month: *$%.2f* %s", other, getPercentString(now, other)));

            msg.append(String.format("%n%n\uD83D\uDD38 Last Updated: *%s*", df.format(lastUpdate)));
            if (timeRemaining > 0) {
                msg.append(String.format("%n%n⏰ Time remaining: *%d minutes %d seconds*", timeRemaining / 60, timeRemaining % 60));
            }

            if (isPrivate) {
                msg.append(String.format("%n%nJoin @BitcoinTracker for live tracking and price alerts!"));
            }
            return msg.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public InlineQueryResultArticle getInline() {
        try {
            return InlineQueryResultArticle.builder()
                    .title(type.getHumanizedName())
                    .description(String.format("Price: $%.2f", getAverage(99)))
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
