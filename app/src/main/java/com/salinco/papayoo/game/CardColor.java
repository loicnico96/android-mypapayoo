package com.salinco.papayoo.game;

public enum CardColor {
    Spades(1), Hearts(2), Clubs(3), Diamonds(4), Special(5);

    private int value;
    private CardColor (int value) { this.value = value; }
    public int getValue() { return this.value; }

    private static CardColor[] colors = CardColor.values();
    public static CardColor GetColor(int value) { return colors[value-1]; }
}
