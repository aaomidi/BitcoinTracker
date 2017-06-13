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


}
