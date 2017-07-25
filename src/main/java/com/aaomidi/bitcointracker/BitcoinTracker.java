package com.aaomidi.bitcointracker;

import com.aaomidi.bitcointracker.handler.BitcoinHandler;
import com.aaomidi.bitcointracker.handler.TelegramHandler;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BitcoinTracker {
    public final static ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(15);
    @Getter
    private final BitcoinHandler bitcoinHandler;
    @Getter
    private final TelegramHandler telegramHandler;

    public BitcoinTracker(String... args) {
        String key;
        if (args.length == 0) {
            key = System.getenv("TELEGRAM_KEY");
        } else {
            key = args[0];
        }
        bitcoinHandler = new BitcoinHandler(this);
        telegramHandler = new TelegramHandler(this, key);
    }

    public static void main(String... args) {
        new BitcoinTracker(args);
    }
}
