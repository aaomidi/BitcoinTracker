package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.UpdatableMessage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.inline.send.InlineQueryResponse;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResult;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineCallbackQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineQueryReceivedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TelegramInlineHandler implements Listener {
    private final static transient Cache<String, Boolean> messageCache;

    static {
        messageCache = CacheBuilder.newBuilder().expireAfterWrite(40, TimeUnit.MINUTES).build();
    }

    private final BitcoinTracker instance;
    private final TelegramBot bot;

    public TelegramInlineHandler(BitcoinTracker instance, TelegramBot bot) {
        this.instance = instance;
        this.bot = bot;
    }

    @Override
    public void onInlineQueryReceived(InlineQueryReceivedEvent event) {
        String query = event.getQuery().getQuery().toUpperCase();
        //Bitcoin bitcoin = instance.getBitcoinHandler().getLatestCoin();
        /*if (bitcoin == null) {
            return;
        }*/
        List<InlineQueryResult> list = new LinkedList<>();
        for (CoinType coinType : CoinType.values()) {
            if (query.isEmpty() || coinType.toString().toLowerCase().startsWith(query.toLowerCase())) {
                list.add(instance.getBitcoinHandler().getCoin(coinType).getInline());
            }
        }

        event.getQuery().answer(bot, InlineQueryResponse.builder().results(list).cacheTime(10).build());
    }

    @Override
    public void onInlineCallbackQueryReceivedEvent(InlineCallbackQueryReceivedEvent event) {
        if (messageCache.asMap().containsKey(event.getCallbackQuery().getInlineMessageId())) {
            return;
        }

        messageCache.put(event.getCallbackQuery().getInlineMessageId(), true);

        new UpdatableMessage(instance, bot, event.getCallbackQuery().getInlineMessageId(), CoinType.valueOf(event.getCallbackQuery().getData()), 5, false);
    }
}
