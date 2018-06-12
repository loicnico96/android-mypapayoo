package com.salinco.papayoo.game;

import android.view.View;
import android.widget.TextView;

public class LocalPlayerController extends AbstractPlayerController implements View.OnClickListener {
    private Card myFocusedCard;
    private Selection mySelection;

    public LocalPlayerController () {
        this.myFocusedCard = null;
    }

    public void focusCard (Card card) {
        if (this.myFocusedCard != card) {
            this.myFocusedCard = card;
            this.setChanged();
            this.notifyObservers(NotifyReason.PlayerController_FocusChanged);
        }
    }

    public Card getFocusedCard () {
        return this.myFocusedCard;
    }

    public boolean isFocusedCard (Card card) {
        return this.myFocusedCard != null && this.myFocusedCard.equals(card);
    }

    @Override
    public void onClick (View view) {
        if (this.mySelection != null) {
            if (view instanceof CardView) { // Test for card view, better use cards
                CardView cardView = (CardView) view;
                Card card = cardView.getCard();
                if (card != null) {
                    if (this.isFocusedCard(card)) {
                        if (this.mySelection.isSelectedCard(card)) {
                            this.mySelection.deselectCard(card);
                            this.focusCard(null);
                        } else if (this.mySelection.isSelectableCard(card)) {
                            this.mySelection.selectCard(card);
                            this.focusCard(null);
                        }
                    } else {
                        this.focusCard(card);
                    }
                }
            } else if (view instanceof TextView) { // Test for text view, better use tags
                if (this.mySelection.isSelectionComplete()) {
                    this.mySelection.validate();
                }
            } // could add selection cancel, reset
        }
    }

    @Override
    public void requestSelection (AbstractGameState gameState, Selection selection) {
        this.mySelection = selection;
    }
}
