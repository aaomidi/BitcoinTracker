package com.aaomidi.bitcointracker;

import com.aaomidi.bitcointracker.handler.BitcoinHandler;
import com.aaomidi.bitcointracker.handler.TelegramHandler;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BitcoinTracker {
    public final static ExecutorService service = Executors.newFixedThreadPool(15);
    public final static ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(15);
    @Getter
    private final BitcoinHandler bitcoinHandler;
    @Getter
    private final TelegramHandler telegramHandler;

    public BitcoinTracker(String... args) {
        bitcoinHandler = new BitcoinHandler();
        telegramHandler = new TelegramHandler(this, args[0]);
    }

    public static void main(String... args) {
        new BitcoinTracker(args);
    }
}
