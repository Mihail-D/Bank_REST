package com.example.bankcards.entity;

public final class HistoryEventType {
    private HistoryEventType() {}
    public static final String TRANSFER = "TRANSFER";
    public static final String CARD_BLOCKED = "CARD_BLOCKED";
    public static final String CARD_UNBLOCKED = "CARD_UNBLOCKED";
    public static final String CARD_EXPIRED = "CARD_EXPIRED";
    public static final String CARD_ACTIVATED = "CARD_ACTIVATED";
}
