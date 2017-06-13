package com.aaomidi.bitcointracker.bean;

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
public class LocalBitcoin {
    private transient static TimeZone tz = TimeZone.getTimeZone("UTC");
    private transient static DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss z");

    static {
        df.setTimeZone(tz);
    }

    private final Currency currency;
    private final double buy;
    private final double sell;
    private final long ts;

    public String getFormattedMessage(boolean isPrivate, long timeRemaining) {
        long time = ts * 1000;
        StringBuilder msg = new StringBuilder("💰 Latest Bitcoin Price 💰");
        msg
                .append(String.format("%nCurrency: *%s*%nSell: *%s%.2f*%nBuy: *%s%.2f*", currency.getName(), currency.getSymbol(), buy, currency.getSymbol(), sell))
                .append(String.format("%nLast Updated: *%s*", df.format(time)));

        if (timeRemaining > 0) {
            msg.append(String.format("%nTime remaining: *%d minutes %d seconds*", timeRemaining / 60, timeRemaining % 60));
        }

        if (isPrivate) {
            msg.append(String.format("%n%nJoin @BitCoinTracker for live tracking!"));
        }
        return msg.toString();
    }

    public InlineQueryResultArticle getInline() {
        return InlineQueryResultArticle.builder()
                .title(currency.getName())
                .description(String.format("Buy: %s%.2f Sell: %s%.2f", currency.getSymbol(), buy, currency.getSymbol(), sell))
                .inputMessageContent(
                        InputTextMessageContent.builder()
                                .messageText(getFormattedMessage(true, -1))
                                .parseMode(ParseMode.MARKDOWN)
                                .build())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .addRow(InlineKeyboardButton.builder()
                                .text("Start live updates!")
                                .callbackData(currency.toString())
                                .build()
                        )
                        .build())
                .build();
    }

    public boolean hasChanged(LocalBitcoin localBitcoin) {
        return true;
        // return localBitcoin.buy != buy || localBitcoin.sell != sell;
    }
}
