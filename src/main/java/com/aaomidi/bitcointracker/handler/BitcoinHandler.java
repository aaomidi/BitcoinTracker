package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.Bitcoin;
import com.aaomidi.bitcointracker.bean.Currency;
import com.aaomidi.bitcointracker.bean.LocalBitcoin;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BitcoinHandler {
    private final TreeMap<Long, Bitcoin> coins = new TreeMap<>();
    private final Gson gson = new Gson();
    private final ReentrantLock lock = new ReentrantLock(true);

    private URL url;
    private String addr = "https://blockchain.info/ticker";

    public BitcoinHandler() {
        try {
            url = new URL(addr);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        BitcoinTracker.scheduledService.scheduleAtFixedRate(() -> {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
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
        coins.put(bitcoin.getTimestamp(), bitcoin);
    }

    public Bitcoin getLatestCoin() {
        lock.lock();
        Bitcoin bitcoin = null;
        try {
            Map.Entry<Long, Bitcoin> entry = coins.lastEntry();
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
    }
}
