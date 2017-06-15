package com.aaomidi.bitcointracker.handler;

import com.aaomidi.bitcointracker.BitcoinTracker;
import com.aaomidi.bitcointracker.bean.CoinType;
import com.aaomidi.bitcointracker.bean.CryptoCoin;
import com.aaomidi.bitcointracker.registries.CoinRegistry;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import lombok.Getter;
import org.json.JSONArray;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BitcoinHandler {
    private final ConcurrentHashMap<CoinType, CoinRegistry> coins = new ConcurrentHashMap<>();
    private final BitcoinTracker instance;

    private String blockchainAddr = "https://blockchain.info/ticker";
    private URL blockchainURL;
    private WebSocket socket;

    public BitcoinHandler(BitcoinTracker instance) {
        this.instance = instance;
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
            socket = new WebSocketFactory()
                    .createSocket("wss://d2.bitcoinwisdom.com/overview")
                    .addHeader("Origin", "https://bitcoinwisdom.com")
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                            socket = websocket.recreate().connectAsynchronously();
                        }

                        @Override
                        public void onTextMessage(WebSocket websocket, String message) throws Exception {
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
                                    } else if (info.contains("xmrbtc")) {
                                        coinType = CoinType.XMR;
                                    } else {
                                        return;
                                    }

                                    CryptoCoin cryptoCoin = new CryptoCoin(coinType, data.getString(1), data.getInt(2), data.getDouble(3), time);
                                    registerCoin(cryptoCoin);

                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).connectAsynchronously();

            socket.setPingInterval(15 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void registerCoin(CryptoCoin cryptoCoin) {
        CoinRegistry registry = coins.getOrDefault(cryptoCoin.getType(), new CoinRegistry(instance, cryptoCoin.getType()));
        registry.registerCoin(cryptoCoin);
        coins.put(cryptoCoin.getType(), registry);
    }

    public CoinRegistry getCoin(CoinType type) {
        return coins.get(type);
    }

}
