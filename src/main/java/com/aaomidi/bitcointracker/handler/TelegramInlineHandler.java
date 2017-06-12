package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.Bitcoin;
import com.aaomidi.bitcointracker.bean.Currency;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.inline.send.InlineQueryResponse;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResult;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineQueryReceivedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TelegramInlineHandler implements Listener {
    private final BitcoinTracker instance;
    private final TelegramBot bot;
    private final transient Cache<UUID, Currency> currencyCache;


    public TelegramInlineHandler(BitcoinTracker instance, TelegramBot bot) {
        this.instance = instance;
        this.bot = bot;
        this.currencyCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
    }

    @Override
    public void onInlineQueryReceived(InlineQueryReceivedEvent event) {
        String query = event.getQuery().getQuery().toUpperCase();
        Bitcoin bitcoin = instance.getBitcoinHandler().getLatestCoin();
        if (bitcoin == null) {
            return;
        }
        List<InlineQueryResult> list = new LinkedList<>();
        for (Currency currency : Currency.values()) {
            if (currency.getName().startsWith(query)) {
                list.add(bitcoin.getCoin(currency).getInline());
            }
        }

        event.getQuery().answer(bot, InlineQueryResponse.builder().results(list).cacheTime(10).build());
    }
}
