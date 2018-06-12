package com.salinco.papayoo.game;

import android.util.Log;

public class AutoPlayerController extends AbstractPlayerController {

    public int getSidedCardValue (AbstractGameState gameState, Card card) {
        if (card.getCardColor() == GameRules.CardColorSpecial) {
            return card.getCardRank();
        } else if (card.getCardColor() == gameState.getGameColor()) {
            if (card.getCardRank() == GameRules.CardRankSpecial) {
                return card.getCardRank() * 8;
            } else if (card.getCardRank() > GameRules.CardRankSpecial) {
                return card.getCardRank() * 4;
            } else {
                return card.getCardRank();
            }
        } else {
            return card.getCardRank() * 2;
        }
    }
    public int getPlayedCardValue (AbstractGameState gameState, Card card) {
        if (gameState.getFieldCards().size() > 0) {
            Card highestCard = gameState.getHighestFieldCard();
            if (gameState.getFieldCards().size() == gameState.getPlayerCount()-1
                    && GameRules.GetTurnTotalValue(gameState) == 0) {
                return card.getCardRank() * 2;
            } else if (GameRules.IsCardHigher(card, highestCard)) {
                return -card.getCardRank();
            } else {
                return card.getCardRank();
            }
        } else if (card.getCardColor() == GameRules.CardColorSpecial) {
            return -card.getCardRank();
        } else if (card.getCardColor() == gameState.getGameColor()) {
            if (card.getCardRank() == GameRules.CardRankSpecial) {
                return -card.getCardRank() * 8;
            } else if (card.getCardRank() > GameRules.CardRankSpecial) {
                return -card.getCardRank() * 8;
            } else {
                return -card.getCardRank() * 2;
            }
        } else {
            return -card.getCardRank() * 2;
        }
    }
    public int getDiscardedCardValue (AbstractGameState gameState, Card card) {
        if (card.getCardColor() == GameRules.CardColorSpecial) {
            return card.getCardRank() * 2;
        } else if (card.getCardColor() == gameState.getGameColor()) {
            if (card.getCardRank() == GameRules.CardRankSpecial) {
                return card.getCardRank() * 8;
            } else if (card.getCardRank() > GameRules.CardRankSpecial) {
                return card.getCardRank() * 4;
            } else {
                return card.getCardRank();
            }
        } else {
            return card.getCardRank() * 3;
        }
    }

    @Override
    public void requestSelection (AbstractGameState gameState, Selection selection) {
        while (!selection.isSelectionComplete()) {
            Card selectedCard = selection.getSelectableCards().get(0);
            if (selection.getSelectionReason() == Selection.Reason.SideCards) {
                int selectedCardValue = this.getSidedCardValue(gameState, selectedCard);
                for (Card card : selection.getSelectableCards()) {
                    int cardValue = this.getSidedCardValue(gameState, card);
                    if (cardValue > selectedCardValue) {
                        selectedCardValue = cardValue;
                        selectedCard = card;
                    }
                }
            } else if (selection.getSelectionReason() == Selection.Reason.PlayCards) {
                int selectedCardValue = this.getPlayedCardValue(gameState, selectedCard);
                for (Card card : selection.getSelectableCards()) {
                    int cardValue = this.getPlayedCardValue(gameState, card);
                    if (cardValue > selectedCardValue) {
                        selectedCardValue = cardValue;
                        selectedCard = card;
                    }
                }
            } else if (selection.getSelectionReason() == Selection.Reason.DiscardCards) {
                int selectedCardValue = this.getDiscardedCardValue(gameState, selectedCard);
                for (Card card : selection.getSelectableCards()) {
                    int cardValue = this.getDiscardedCardValue(gameState, card);
                    if (cardValue > selectedCardValue) {
                        selectedCardValue = cardValue;
                        selectedCard = card;
                    }
                }
            }
            selection.selectCard(selectedCard);
        }
        selection.validate();
    }
}
