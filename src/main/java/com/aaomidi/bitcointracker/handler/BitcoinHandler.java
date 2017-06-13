package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.bean.Bitcoin;
import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.CryptoCoin;
import com.aaomidi.bitcointracker.registries.CoinRegistry;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.json.JSONArray;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

public class BitcoinHandler {
    private final Map<CoinType, CoinRegistry> coins = new HashMap<>();

    private final TreeMap<Long, Bitcoin> map = new TreeMap<>();
    private final Gson gson = new Gson();
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

        //handleBlockchainAPI();
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
                            lock.lock();
                            try {
                                long time = System.currentTimeMillis();
                                JSONArray data = new JSONArray(message);
                                if (data.getString(0).equals("info")) {
                                    CoinType coinType = CoinType.BTC;
                                    if (data.getString(1).contains("ltcusd")) {
                                        coinType = CoinType.LTC;
                                    }
                                    CryptoCoin cryptoCoin = new CryptoCoin(coinType, data.getString(1), data.getInt(2), data.getDouble(3), time);
                                    registerCoin(cryptoCoin);
                                }
                            } catch (Exception ex) {

                            } finally {
                                lock.unlock();
                            }
                        }
                    }).connectAsynchronously();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerCoin(CryptoCoin cryptoCoin) {
        CoinRegistry registry = coins.getOrDefault(cryptoCoin.getType(), new CoinRegistry(cryptoCoin.getType()));
        registry.registerCoin(cryptoCoin);
        coins.put(cryptoCoin.getType(), registry);
    }

  /*  private void handleBlockchainAPI() {
        BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) blockchainURL.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                StringBuilder sb = new StringBuilder();

                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                // Locking
                lock.lock();
                try {
                    handleData(sb.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                // ignore;
            }
        }, 0L, 5L, TimeUnit.SECONDS);
    }
  private void handleData(String data) {
        long time = System.currentTimeMillis() / 1000;
        Map<String, Map<?, ?>> value = gson.fromJson(data, Map.class);
        HashMap<Currency, LocalBitcoin> coins = new HashMap<>();

        for (Map.Entry<String, Map<?, ?>> entry : value.entrySet()) {
            Map<?, ?> val = entry.getValue();
            Currency currency;
            try {
                currency = Currency.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException ex) {
                continue;
            }

            double buy = (Double) val.get("buy");
            double sell = (Double) val.get("sell");
            coins.put(currency, new LocalBitcoin(currency, buy, sell, time));
        }

        Bitcoin bitcoin = new Bitcoin(time, coins);
        registerBitcoin(bitcoin);
    }



    private void registerBitcoin(Bitcoin bitcoin) {
        map.put(bitcoin.getTimestamp(), bitcoin);
    }

    public Bitcoin getLatestCoin() {
        lock.lock();
        Bitcoin bitcoin = null;
        try {
            Map.Entry<Long, Bitcoin> entry = map.lastEntry();
            if (entry == null) {
                bitcoin = null;
            } else {
                bitcoin = entry.getValue();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return bitcoin;
    } */
}
