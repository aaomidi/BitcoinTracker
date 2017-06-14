package com.aaomidi.bitcointracker.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Getter
public class PriceAlert {
    private final CoinType coinType;
    private final AlertDirection alertDirection;
    private final double amount;

    private final String requestingUser;
    private final String requestingChat;

    public enum AlertDirection {
        ABOVE,
        BELOW
    }
}
