package com.aaomidi.bitcointracker.bean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@ToString
@RequiredArgsConstructor
public class Bitcoin {
    @Getter
    private final long timestamp;
    private final HashMap<Currency, LocalBitcoin> coins;

    public LocalBitcoin getCoin(Currency currency) {
        return coins.get(currency);
    }
}
