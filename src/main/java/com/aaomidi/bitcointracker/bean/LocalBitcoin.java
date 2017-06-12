package com.aaomidi.bitcointracker.bean;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pro.zackpollard.telegrambot.api.chat.inline.send.content.InputTextMessageContent;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResultArticle;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;

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

    public String getFormattedMessage(boolean isPrivate) {
        long time = ts * 1000;
        StringBuilder msg = new StringBuilder("💰 Latest Bitcoin Price 💰");
        msg
                .append(String.format("%nCurrency: *%s*%nSell: *%s%.2f*%nBuy: *%s%.2f*", currency.getName(), currency.getSymbol(), buy, currency.getSymbol(), sell))
                .append(String.format("%nLast Updated: *%s*", df.format(time)));

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
                                .messageText(getFormattedMessage(true)).parseMode(ParseMode.MARKDOWN).build()
                )
                .build();
    }

    public boolean hasChanged(LocalBitcoin localBitcoin) {
        return localBitcoin.buy != buy || localBitcoin.sell != sell;
    }
}
