package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.CryptoCoin;
import com.aaomidi.bitcointracker.registries.CoinRegistry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.json.JSONArray;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BitcoinHandler {
    private final Map<CoinType, CoinRegistry> coins = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    private String blockchainAddr = "https://blockchain.info/ticker";
    private URL blockchainURL;

    public BitcoinHandler() {
        try {
            blockchainURL = new URL(blockchainAddr);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        handleBitcoinWisdomAPI();
    }

    private void handleBitcoinWisdomAPI() {
        try {
            WebSocket socket = new WebSocketFactory()
                    .createSocket("wss://d2.bitcoinwisdom.com/overview")
                    .addHeader("Origin", "https://bitcoinwisdom.com")
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String message) throws Exception {
                            System.out.println(message);
                            lock.lock();
                            try {
                                long time = System.currentTimeMillis();
                                JSONArray data = new JSONArray(message);
                                if (data.getString(0).equals("info")) {

                                    CoinType coinType;
                                    String info = data.getString(1);
                                    if (info.contains("ltcusd")) {
                                        coinType = CoinType.LTC;
                                    } else if (info.contains("btcusd")) {
                                        coinType = CoinType.BTC;
                                    } else {
                                        return;
                                    }

                                    CryptoCoin cryptoCoin = new CryptoCoin(coinType, data.getString(1), data.getInt(2), data.getDouble(3), time);
                                    registerCoin(cryptoCoin);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }).connectAsynchronously();

            BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> socket.sendText("ping"),50,15, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void registerCoin(CryptoCoin cryptoCoin) {
        CoinRegistry registry = coins.getOrDefault(cryptoCoin.getType(), new CoinRegistry(cryptoCoin.getType()));
        registry.registerCoin(cryptoCoin);
        coins.put(cryptoCoin.getType(), registry);
    }

    public CoinRegistry getCoin(CoinType type) {
        return coins.get(type);
    }

}
