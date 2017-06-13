package com.aaomidi.bitcointracker.bean;

import lombok.Getter;
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

@ToString
@RequiredArgsConstructor
public class CryptoCoin {
    private transient static TimeZone tz = TimeZone.getTimeZone("UTC");
    private transient static DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss z");

    static {
        df.setTimeZone(tz);
    }

    @Getter
    private final CoinType type;
    @Getter
    private final String exchange;
    @Getter
    private final int day;
    @Getter
    private final double price;
    @Getter
    private final long timestamp;

    public String getFormattedMessage(boolean isPrivate, long timeRemaining) {
        StringBuilder msg = new StringBuilder(String.format("💰 Latest %s Price 💰", type.getHumanizedName()));
        msg
                .append(String.format("Price: *$%.2f*", price))
                .append(String.format("%nLast Updated: *%s*", df.format(timestamp)));

        if (timeRemaining > 0) {
            msg.append(String.format("%nTime remaining: *%d minutes %d seconds*", timeRemaining / 60, timeRemaining % 60));
        }

        if (isPrivate) {
            msg.append(String.format("%n%nJoin @BitCoinTracker for live tracking and price alerts!"));
        }
        return msg.toString();
    }

    public InlineQueryResultArticle getInline() {
        return InlineQueryResultArticle.builder()
                .title(type.getHumanizedName())
                .description(String.format("Price: $%.2f", price))
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
