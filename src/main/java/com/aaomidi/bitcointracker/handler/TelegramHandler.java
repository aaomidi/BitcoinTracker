package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.Bitcoin;
import com.aaomidi.bitcointracker.bean.Currency;
import com.aaomidi.bitcointracker.bean.UpdatableMessage;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;

public class TelegramHandler implements Listener {
    private final static String channelID = "@BitcoinTracker";

    private final BitcoinTracker instance;
    private final Chat channel;
    private final TelegramBot bot;

    public TelegramHandler(BitcoinTracker instance, String apiKey) {
        bot = TelegramBot.login(apiKey);
        this.instance = instance;
        bot.startUpdates(false);
        bot.getEventsManager().register(this);

        bot.getEventsManager().register(new TelegramInlineHandler(instance, bot));
        channel = bot.getChat(channelID);

        //startChannel();
    }

    private void startChannel() {
        SendableTextMessage msg = SendableTextMessage.builder()
                .message("Bot starting up...")
                .disableNotification(true)
                .parseMode(ParseMode.MARKDOWN)
                .build();

        Message message = channel.sendMessage(msg);
        new UpdatableMessage(instance, bot, message, Currency.USD, 10L, true);
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        System.out.println("works");
        if (!event.getCommand().equalsIgnoreCase("start")) {
            return;
        }
        String arg = event.getArgsString();
        if (arg == null || arg.isEmpty()) arg = "USD";
        Currency currency = null;
        try {
            currency = Currency.valueOf(arg);
        } catch (IllegalArgumentException ex) {
            // currency not found
        }
        if (currency == null) return;

        Bitcoin coin = instance.getBitcoinHandler().getLatestCoin();
        if (coin == null) return;

        event.getChat().sendMessage(SendableTextMessage.builder()
                .message(coin.getCoin(currency).getFormattedMessage(true, -1))
                .parseMode(ParseMode.MARKDOWN).build());
    }

}
