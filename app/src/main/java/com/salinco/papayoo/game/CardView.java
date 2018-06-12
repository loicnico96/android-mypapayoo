package com.salinco.papayoo.game;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class CardView extends AppCompatImageView {
    private Card myCard;

    public CardView (Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public Card getCard () {
        return this.myCard;
    }

    public void setCard (Card card) {
        if (this.myCard != card) {
            this.myCard = card;
            String resourceName = (card != null)? "card" + card.getCardID() : "card_empty";
            this.setImageResource(this.getResources().getIdentifier(resourceName, "drawable", "com.salinco.papayoo"));
        }
    }

    public void setMargins (int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)this.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        this.setLayoutParams(layoutParams);
    }
}
