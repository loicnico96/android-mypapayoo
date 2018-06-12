package com.salinco.papayoo.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class GameRules {
    public static Random Randomizer = new Random ();
    public static int MinPlayerCount = 3;
    public static int MaxPlayerCount = 6;
    public static CardColor CardColorSpecial = CardColor.Special;
    public static int CardRankSpecial = 7;
    public static int CardValueSpecial = 40;
    public static List<Card> AllCardsList = Card.GetCardsFromID(new int[] {
        101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
        201, 202, 203, 204, 205, 206, 207, 208, 209, 210,
        301, 302, 303, 304, 305, 306, 307, 308, 309, 310,
        401, 402, 403, 404, 405, 406, 407, 408, 409, 410,
        501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516, 517, 518, 519, 520
    });

    public static List<List<Card>> DealCards (AbstractGameState gameState) {
        List<Card> allCards = new ArrayList<>(AllCardsList);
        List<List<Card>> playerHandCards = new ArrayList<>(gameState.getPlayerCount());
        int playerCards = GetPlayerCardCount(gameState);
        Collections.shuffle(allCards);
        for (int playerIndex = 0 ; playerIndex < gameState.getPlayerCount() ; playerIndex++) {
            playerHandCards.add(allCards.subList(playerIndex * playerCards, playerIndex * playerCards + playerCards));
        }
        return playerHandCards;
    }

    public static int GetCardValue (AbstractGameState gameState, Card card) {
        if (card.getCardColor() == CardColorSpecial) {
            return card.getCardRank();
        } else if (card.getCardColor() == gameState.getGameColor() && card.getCardRank() == CardRankSpecial) {
            return CardValueSpecial;
        } else {
            return 0;
        }
    }

    public static List<Card> GetDiscardableCards (AbstractGameState gameState, AbstractPlayerState player) {
        if (IsPlayerCanPlayCard(gameState, player)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(player.getPlayerHandCards());
        }
    }

    public static Card GetHighestFieldCard (AbstractGameState gameState) {
        List<Card> fieldCards = gameState.getFieldCards();
        if (fieldCards.size() > 0) {
            Card highestCard = fieldCards.get(0);
            for (Card card : fieldCards) {
                if (card != highestCard && IsCardHigher(card, highestCard)) {
                    highestCard = card;
                }
            }
            return highestCard;
        } else {
            return null;
        }
    }

    public static List<Card> GetPlayableCards (AbstractGameState gameState, AbstractPlayerState player) {
        Log.w("Papayoo","GetPlayableCards");
        CardColor turnColor = GameRules.GetTurnColor(gameState);
        List<Card> handCards = player.getPlayerHandCards();
        List<Card> playableCards = new ArrayList<> ();
        for (Card card : handCards) {
            if (turnColor == null || card.getCardColor() == turnColor) {
                playableCards.add(card);
            }
        }
        return playableCards;
    }

    public static int GetPlayerCardCount (AbstractGameState gameState) {
        return AllCardsList.size() / gameState.getPlayerCount();
    }

    public static CardColor GetRandomCardColor () {
        CardColor[] colors = { CardColor.Spades, CardColor.Hearts, CardColor.Clubs, CardColor.Diamonds };
        return colors[Randomizer.nextInt(colors.length)];
    }

    public static int GetSidedCardCount (AbstractGameState gameState) {
        switch (gameState.getPlayerCount()) {
            case 3: case 4:
                return 5;
            case 5:
                return 4;
            case 6:
                return 3;
            default:
                return 0;
        }
    }

    public static int GetTurnTotalValue (AbstractGameState gameState) {
        int value = 0;
        for (Card card : gameState.getFieldCards()) {
            value += GetCardValue(gameState, card);
        }
        return value;
    }

    public static CardColor GetTurnColor (AbstractGameState gameState) {
        Log.w("Papayoo","GetTurnColor");
        Log.w("Papayoo","GetTurnColor " + gameState.getFieldCards().size());
        if (gameState.getFieldCards().size() > 0) {
            Log.w("Papayoo","GetTurnColor");
            Log.w("Papayoo","GetTurnColor " + gameState.getFieldCards().get(0));
            return gameState.getFieldCards().get(0).getCardColor();
        } else {
            return null;
        }
    }

    public static boolean IsCardHigher (Card cardA, Card cardB) {
        return cardA.getCardColor() == cardB.getCardColor() && cardA.getCardRank() > cardB.getCardRank();
    }

    public static boolean IsGameOver (AbstractGameState gameState) {
        return gameState.getTurnCount() == GetPlayerCardCount(gameState);
    }

    public static boolean IsPlayerCanPlayCard (AbstractGameState gameState, AbstractPlayerState player) {
        Log.w("Papayoo","IsPlayerCanPlayCard");
        return GetPlayableCards(gameState, player).size() > 0;
    }

    public static boolean IsValidPlayerCount (AbstractGameState gameState) {
        return gameState.getPlayerCount() >= MinPlayerCount && gameState.getPlayerCount() <= MaxPlayerCount;
    }
}
