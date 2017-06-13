package com.aaomidi.bitcointracker.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Currency {
    USD("USD", "$");
    /*RUB("RUB", "\u20BD"),
    CNY("CNY", "¥"),
    BRL("BRL", "R$"); */

    @Getter
    private final String name;
    @Getter
    private final String symbol;
}
