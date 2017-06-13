package com.aaomidi.bitcointracker.registries;

import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.CryptoCoin;
import lombok.RequiredArgsConstructor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
@RequiredArgsConstructor
public class CoinRegistry {
    private transient static TimeZone tz = TimeZone.getTimeZone("UTC");
    private transient static DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss z");

    static {
        df.setTimeZone(tz);
    }

    private final CoinType type;
    private transient HashMap<Integer, HashMap<String, CryptoCoin>> coins;

    public void registerCoin(CryptoCoin coin) {
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

    public String getFormattedMessage(boolean isPrivate, long timeRemaining) {
        StringBuilder msg = new StringBuilder(String.format("💰 %s Tracker 💰", type.getHumanizedName()));
        msg
                .append(String.format("Right now: *$%.2f*", getAverage(0)))
                .append(String.format("24 Hours: *$%.2f*", getAverage(0)))
                .append(String.format("24 Hours: *$%.2f*", getAverage(0)))
                .append(String.format("%nLast Updated: *%s*", df.format(timestamp)));

        if (timeRemaining > 0) {
            msg.append(String.format("%nTime remaining: *%d minutes %d seconds*", timeRemaining / 60, timeRemaining % 60));
        }

        if (isPrivate) {
            msg.append(String.format("%n%nJoin @BitCoinTracker for live tracking and price alerts!"));
        }
        return msg.toString();
    }
}
