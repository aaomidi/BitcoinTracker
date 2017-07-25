package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.UpdatableMessage;
import com.aaomidi.bitcointracker.registries.CoinRegistry;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;

public class TelegramHandler implements Listener {
    private final static String channelID = System.getenv("TELEGRAM_CHAN");
    //private final static String channelID = "-244026053";
    //private final static String channelID = "-224385404";

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

        startChannel(false);
    }

    public void startChannel(boolean isInstant) {
        SendableTextMessage.SendableTextMessageBuilder builder = SendableTextMessage.builder()
                .message("Bot starting up...")
                .disableNotification(true)
                .parseMode(ParseMode.MARKDOWN);

        if (isInstant) {
            builder.disableNotification(false);
        }


        Message message = channel.sendMessage(builder.build());

        new UpdatableMessage(instance, bot, message, CoinType.BTC, 3L, true, isInstant);
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        System.out.println("works");
        if (!event.getCommand().equalsIgnoreCase("start")) {
            return;
        }
        String arg = event.getArgsString();
        if (arg == null || arg.isEmpty()) arg = "BTC";
        CoinType coinType = null;
        try {
            coinType = CoinType.valueOf(arg);
        } catch (IllegalArgumentException ex) {
            // currency not found
        }
        if (coinType == null) return;

        CoinRegistry coin = instance.getBitcoinHandler().getCoin(coinType);
        if (coin == null) return;

        event.getChat().sendMessage(SendableTextMessage.builder()
                .message(coin.getFormattedMessage(true, -1))
                .parseMode(ParseMode.MARKDOWN).build());
    }

}
