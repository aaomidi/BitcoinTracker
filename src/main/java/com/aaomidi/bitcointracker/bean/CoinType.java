package com.aaomidi.bitcointracker.bean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CoinType {
    BTC("Bitcoin"),
    LTC("Litecoin"),
    XMR("Monero");
    @Getter
    private final String humanizedName;
}
