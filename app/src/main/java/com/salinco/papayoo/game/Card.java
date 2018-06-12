package com.salinco.papayoo.game;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Card implements Comparable {
    private int myCardID;

    public static List<Card> GetCardsFromID (int[] cardIDs) {
        List<Card> cards = new ArrayList<Card>();
        for (int cardID : cardIDs) {
            cards.add(new Card(cardID));
        }
        return cards;
    }

    public Card (int cardID) {
        this.myCardID = cardID;
    }

    public CardColor getCardColor () {
        return CardColor.GetColor(this.myCardID / 100);
    }

    public int getCardID () {
        return this.myCardID;
    }

    public int getCardRank () {
        return this.myCardID % 100;
    }

    @Override
    public boolean equals (Object other) {
        return (other instanceof Card) && (this.myCardID == ((Card)other).myCardID);
    }

    @Override
    public int compareTo (@NonNull Object other) {
        if (other instanceof Card) {
            return this.myCardID - ((Card)other).myCardID;
        } else {
            throw new RuntimeException("Trying to compare Card with " + other.getClass().getSimpleName() + ".");
        }
    }
}
