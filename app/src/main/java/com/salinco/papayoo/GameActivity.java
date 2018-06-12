package com.salinco.papayoo;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.salinco.papayoo.game.AbstractGameState;
import com.salinco.papayoo.game.AbstractPlayerState;
import com.salinco.papayoo.game.AutoPlayerController;
import com.salinco.papayoo.game.Card;
import com.salinco.papayoo.game.CardColor;
import com.salinco.papayoo.game.CardView;
import com.salinco.papayoo.game.GameView;
import com.salinco.papayoo.game.LocalGameState;
import com.salinco.papayoo.game.LocalPlayerController;
import com.salinco.papayoo.game.NotifyReason;
import com.salinco.papayoo.game.Selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class GameActivity extends AppCompatActivity implements Observer {
    //private GameView myGameView;

    private CountDownTimer myActionTimer;
    private ImageView myImageGameColor;
    private ViewGroup myLayoutActionTimer;
    private ViewGroup myLayoutFieldCards;
    private ViewGroup myLayoutHandCards;
    private ViewGroup myLayoutPlayers;
    private TextView myTextActionSelect;
    private TextView myTextActionValidate;

    private AbstractGameState myGameState;
    private AbstractPlayerState myPlayer;
    private LocalPlayerController myPlayerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this.myPlayerController = new LocalPlayerController();
        // this.myGameView = new GameView(this, this.myPlayerController);
        //
        // this.myGameRules = new DefaultGameRules();
        // this.myGameState = new LocalGameState(this.myGameRules);
        // this.myGameState.addListener(this.myGameView);

        setContentView(R.layout.activity_game);
        this.myImageGameColor = (ImageView)this.findViewById(R.id.Image_GameColor);
        this.myLayoutActionTimer = (ViewGroup)this.findViewById(R.id.Layout_ActionTimer);
        this.myLayoutFieldCards = (ViewGroup)this.findViewById(R.id.Layout_FieldCards);
        this.myLayoutHandCards = (ViewGroup)this.findViewById(R.id.Layout_HandCards);
        this.myLayoutPlayers = (ViewGroup)this.findViewById(R.id.Layout_HeaderPlayers);
        this.myTextActionSelect = (TextView)this.findViewById(R.id.Text_ActionDescription);
        this.myTextActionValidate = (TextView)this.findViewById(R.id.Text_ActionValidation);

        // Creating game instance
        Log.d("Papayoo", "Creating game instance...");
        this.myGameState = new LocalGameState ();
        this.myGameState.addObserver(this);

        // Creating player controller
        Log.d("Papayoo", "Creating player controller...");
        this.myPlayerController = new LocalPlayerController ();
        this.myPlayerController.addObserver(this);

        // Registering local player
        Log.d("Papayoo", "Registering local player...");
        this.myPlayer = this.myGameState.registerPlayer("Salinco", this.myPlayerController);
        this.myPlayer.addObserver(this);

        // Registering AI players
        Log.d("Papayoo", "Registering AI players...");
        int playerCount = 5;
        for (int playerIndex = 1 ; playerIndex < playerCount ; playerIndex++) {
            this.myGameState.registerPlayer("NPC"+playerIndex, new AutoPlayerController());
        }

        // Registering input listeners
        Log.d("Papayoo", "Registering input listeners...");
        for (int viewIndex = 0 ; viewIndex < this.myLayoutHandCards.getChildCount() ; viewIndex++) {
            this.myLayoutHandCards.getChildAt(viewIndex).setOnClickListener(this.myPlayerController);
        }
        for (int viewIndex = 0 ; viewIndex < this.myLayoutFieldCards.getChildCount() ; viewIndex++) {
            this.myLayoutFieldCards.getChildAt(viewIndex).setOnClickListener(this.myPlayerController);
        }
        this.myTextActionValidate.setOnClickListener(this.myPlayerController);

        //this.myGameView = new GameView(this, this.myPlayerController);

        // Starting local game
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Papayoo", "Starting local game...");
                    ((LocalGameState) GameActivity.this.myGameState).startLocalGame();
                } catch (Exception exception) {
                    Log.e("Papayoo", "Local game exception : " + exception.getMessage());
                }
            }
        }).start();
    }

    /**
     * Updates game graphics.
     * @param target Object requesting the update
     * @param params Additional information about the update (unused)
     */
    @Override
    public void update (Observable target, final Object params) {
        //this.myGameView.sendMessage(target, (GameView.Reason)params);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.i("Papayoo", "Update : " + (NotifyReason)params);

                // Updating game color
                Log.d("Papayoo", "Updating game color...");
                GameActivity.this.updateGameColor(
                        GameActivity.this.myGameState.getGameColor()
                );

                // Updating player header
                Log.d("Papayoo", "Updating player header...");
                GameActivity.this.updatePlayerHeader(
                        GameActivity.this.myGameState.getPlayers(),
                        GameActivity.this.myGameState.getStartingPlayerIndex(),
                        GameActivity.this.myGameState.getCurrentPlayerIndex()
                );

                // Updating action timer
                Log.d("Papayoo", "Updating action timer...");
                GameActivity.this.updateActionTimer(
                        GameActivity.this.myPlayer.hasSelection()? GameActivity.this.myPlayer.getSelection().getSelectionDurationMax() : 0,
                        GameActivity.this.myPlayer.hasSelection()? GameActivity.this.myPlayer.getSelection().getSelectionDurationRemaining() : 0
                );

                // Updating field cards
                if (GameActivity.this.myPlayer.hasSelection() && GameActivity.this.myPlayer.getSelection().getSelectionReason() == Selection.Reason.SideCards) {
                    Log.d("Papayoo", "Updating selected cards...");
                    GameActivity.this.updateFieldCards(
                            GameActivity.this.myPlayer.getSelection().getSelectionCount(),
                            GameActivity.this.myPlayer.getSelection().getSelectedCards(),
                            GameActivity.this.myPlayer.getSelection().getSelectedCards(),
                            GameActivity.this.myPlayerController.getFocusedCard(),
                            null
                    );
                } else {
                    Log.d("Papayoo", "Updating field cards...");
                    GameActivity.this.updateFieldCards(
                            GameActivity.this.myGameState.getPlayerCount(),
                            GameActivity.this.myGameState.getFieldCards(),
                            null,
                            null,
                            GameActivity.this.myGameState.getHighestFieldCard()
                    );
                }

                // Updating hand cards
                Log.d("Papayoo", "Updating hand cards...");
                if (GameActivity.this.myPlayer.hasSelection()) {
                    List<Card> handCards = new ArrayList<>(GameActivity.this.myPlayer.getPlayerHandCards());
                    handCards.removeAll(GameActivity.this.myPlayer.getSelection().getSelectedCards());
                    GameActivity.this.updateHandCards(
                            handCards,
                            GameActivity.this.myPlayer.getSelection().getSelectableCards(),
                            GameActivity.this.myPlayerController.getFocusedCard()
                    );
                } else {
                    GameActivity.this.updateHandCards(
                            GameActivity.this.myPlayer.getPlayerHandCards(),
                            null,
                            GameActivity.this.myPlayerController.getFocusedCard()
                    );
                }


                // Updating selection description
                Log.d("Papayoo", "Updating selection description...");
                if (GameActivity.this.myPlayer.hasSelection()) {
                    GameActivity.this.myTextActionSelect.setVisibility(View.VISIBLE);
                    switch (GameActivity.this.myPlayer.getSelection().getSelectionReason()) {
                        case DiscardCards:
                            GameActivity.this.myTextActionSelect.setText(R.string.Action_Select_DiscardCards);
                            break;
                        case PlayCards:
                            GameActivity.this.myTextActionSelect.setText(R.string.Action_Select_PlayCards);
                            break;
                        case SideCards:
                            GameActivity.this.myTextActionSelect.setText(R.string.Action_Select_SideCards);
                            break;
                    }
                } else {
                    GameActivity.this.myTextActionSelect.setVisibility(View.INVISIBLE);
                }

                // Updating selection validation button
                Log.d("Papayoo", "Updating selection validation button...");
                if (GameActivity.this.myPlayer.hasSelection() && GameActivity.this.myPlayer.getSelection().isSelectionComplete()) {
                    GameActivity.this.myTextActionValidate.setVisibility(View.VISIBLE);
                } else {
                    GameActivity.this.myTextActionValidate.setVisibility(View.INVISIBLE);
                }

                Log.i("Papayoo", "Update : " + (NotifyReason)params + " (successful)");
            }
        });
    }

    /**
     * Updates the action timer on top-right of the screen.
     * @param maxDuration Maximum timer duration (in milliseconds, 0 if no timer)
     * @param remainingDuration Remaining timer duration (in milliseconds)
     */
    public void updateActionTimer (long maxDuration, long remainingDuration) {
        if (maxDuration > 0) {
            final ProgressBar timerProgressBar = (ProgressBar)this.myLayoutActionTimer.getChildAt(0);
            final TextView timerCounter = (TextView)this.myLayoutActionTimer.getChildAt(1);
            this.myLayoutActionTimer.setVisibility(View.VISIBLE);
            if (this.myActionTimer != null) {
                this.myActionTimer.cancel();
            }

            // Setting progress bar parameters
            timerProgressBar.setMax((int)maxDuration);
            timerProgressBar.setProgress((int)remainingDuration);

            // Animating the progress bar
            ObjectAnimator animation = ObjectAnimator.ofInt(timerProgressBar, "progress", (int)remainingDuration, 0);
            animation.setDuration(remainingDuration);
            animation.setInterpolator(new LinearInterpolator());
            animation.start();

            // Animating the counter
            if (remainingDuration > 0) {
                this.myActionTimer = new CountDownTimer(remainingDuration, 100) {
                    public void onTick(final long millisUntilFinished) {
                        timerCounter.setText(String.format("%d", (999 + millisUntilFinished) / 1000));
                    }
                    public void onFinish() {
                        GameActivity.this.myLayoutActionTimer.setVisibility(View.INVISIBLE);
                    }
                };
                this.myActionTimer.start();
            }

        } else { // Hiding the timer
            this.myLayoutActionTimer.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Updates field cards.
     * @param fieldCount Number of card slots on the field (empty card slots are just an outline)
     * @param fieldCards Cards on the field
     * @param highlightCards Highlighted cards on the field (null if none)
     * @param focusedCard Focused card on the field (null if none)
     * @param highestCard Highest card on the field (null if none)
     */
    public void updateFieldCards (int fieldCount, List<Card> fieldCards, List<Card> highlightCards, Card focusedCard, Card highestCard) {
        if (fieldCount > 0) {

            // Calculating dimensions
            int fieldCardSpacing = (int)getResources().getDimension(R.dimen.FieldCard_Spacing);

            // Making cards visible
            this.myLayoutFieldCards.setVisibility(View.VISIBLE);
            for (int viewIndex = 0 ; viewIndex < this.myLayoutFieldCards.getChildCount() ; viewIndex++) {
                CardView cardView = (CardView)this.myLayoutFieldCards.getChildAt(viewIndex);
                if (viewIndex < fieldCount) {
                    Card card = (fieldCards != null && viewIndex < fieldCards.size())? fieldCards.get(viewIndex) : null;
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setCard(card);

                    // Setting background
                    if (highestCard != null && highestCard.equals(card)) {
                        cardView.setBackgroundResource(R.drawable.border_red);
                        cardView.getBackground().setAlpha(170);
                    } else if (focusedCard != null && focusedCard.equals(card)) {
                        cardView.setBackgroundResource(R.drawable.border_yellow);
                        cardView.getBackground().setAlpha(170);
                    } else if (highlightCards != null && highlightCards.contains(card)) {
                        cardView.setBackgroundResource(R.drawable.border_green);
                        cardView.getBackground().setAlpha(170);
                    } else {
                        cardView.setBackgroundColor(Color.TRANSPARENT);
                    }

                    // Setting padding
                    cardView.setMargins(
                            (viewIndex > 0)? fieldCardSpacing : 0,                  // Left
                            0,                                                      // Top
                            0,                                                      // Right
                            0                                                       // Bottom
                    );

                } else { // Hiding the card
                    cardView.setVisibility(View.GONE);
                }
            }

        } else { // Hiding the layout
            this.myLayoutFieldCards.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the game color icon on top-left of the screen.
     * @param gameColor Current game color (null for no color)
     */
    public void updateGameColor (CardColor gameColor) {
        if (gameColor != null) {
            this.myImageGameColor.setVisibility(View.VISIBLE);
            switch (gameColor) {
                case Spades:
                    this.myImageGameColor.setImageResource(R.drawable.icon_spades);
                    break;
                case Hearts:
                    this.myImageGameColor.setImageResource(R.drawable.icon_hearts);
                    break;
                case Clubs:
                    this.myImageGameColor.setImageResource(R.drawable.icon_clubs);
                    break;
                case Diamonds:
                    this.myImageGameColor.setImageResource(R.drawable.icon_diamonds);
                    break;
            }

        } else { // Hiding the icon
            this.myImageGameColor.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Updates hand cards.
     * @param handCards Cards in the hand.
     * @param highlightCards Highlighted cards in the hand (null if none)
     * @param focusedCard Focused card in the hand (null if none)
     */
    public void updateHandCards (List<Card> handCards, List<Card> highlightCards, Card focusedCard) {
        if (handCards != null && handCards.size() > 0) {

            // Calculating dimensions
            int handCardFocusOffset = (int)getResources().getDimension(R.dimen.HandCard_FocusOffset);
            int handCardSpacing = (int)getResources().getDimension(R.dimen.HandCard_TotalSpacing) / handCards.size();
            handCardSpacing = Math.min(handCardSpacing, (int)getResources().getDimension(R.dimen.HandCard_MaxSpacing));
            handCardSpacing = Math.max(handCardSpacing, (int)getResources().getDimension(R.dimen.HandCard_MinSpacing));

            // Making cards visible
            this.myLayoutHandCards.setVisibility(View.VISIBLE);
            int focusedCardIndex = handCards.indexOf(focusedCard);
            for (int viewIndex = 0 ; viewIndex < this.myLayoutHandCards.getChildCount() ; viewIndex++) {
                CardView cardView = (CardView)this.myLayoutHandCards.getChildAt(viewIndex);
                if (viewIndex < handCards.size()) {
                    Card card = handCards.get(viewIndex);
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setCard(card);

                    // Setting priority
                    cardView.setZ(-Math.abs(viewIndex - focusedCardIndex));

                    // Setting background
                    if (highlightCards != null && highlightCards.contains(card)) {
                        if (card.equals(focusedCard)) {
                            cardView.setBackgroundResource(R.drawable.border_yellow);
                            cardView.getBackground().setAlpha(170);
                        } else {
                            cardView.setBackgroundResource(R.drawable.border_green);
                            cardView.getBackground().setAlpha(170);
                        }
                    } else {
                        cardView.setBackgroundColor(Color.TRANSPARENT);
                    }

                    // Setting padding
                    cardView.setMargins(
                            (viewIndex != 0)? handCardSpacing : 0,                  // Left
                            (card.equals(focusedCard))? handCardFocusOffset : 0,    // Top
                            0,                                                      // Right
                            0                                                       // Bottom
                    );

                } else { // Hiding the card
                    cardView.setVisibility(View.GONE);
                }
            }

        } else { // Hiding the layout
            this.myLayoutHandCards.setVisibility(View.GONE);
        }
    }

    /**
     * Updates player header (player name, points...)
     * @param playerList List of data for all players
     * @param startingPlayerIndex Index of the starting player (absolute)
     * @param currentPlayerIndex Index of the current player (absolute, -1 for no current player)
     */
    public void updatePlayerHeader (List<AbstractGameState.PlayerData> playerList, int startingPlayerIndex, int currentPlayerIndex) {
        int playerCount = playerList.size();
        for (int playerIndex = 0 ; playerIndex < this.myLayoutPlayers.getChildCount() ; playerIndex++) {
            ViewGroup playerLayout = (ViewGroup)this.myLayoutPlayers.getChildAt(playerIndex);
            if (playerIndex < playerCount) {
                int realPlayerIndex = (startingPlayerIndex + playerIndex) % playerCount;
                AbstractGameState.PlayerData playerData = playerList.get(realPlayerIndex);
                ImageView viewPlayerIcon = (ImageView)playerLayout.getChildAt(0);
                TextView viewPlayerName = (TextView)playerLayout.getChildAt(1);
                TextView viewPlayerPts = (TextView)playerLayout.getChildAt(2);
                playerLayout.setVisibility(View.VISIBLE);

                // Setting player name
                viewPlayerName.setText(playerData.playerName);

                // Setting player points
                if (playerData.playerPointsTakenCurrent > 0) {
                    viewPlayerPts.setText(String.format(
                            "%d (+%d)",
                            playerData.playerPointsTakenTotal,
                            playerData.playerPointsTakenCurrent
                    ));
                } else {
                    viewPlayerPts.setText(String.format(
                            "%d",
                            playerData.playerPointsTakenTotal
                    ));
                }

                // Setting player color
                switch (playerData.playerColor) {
                    case Spades:
                        viewPlayerIcon.setImageResource(R.drawable.icon_spades);
                        break;
                    case Hearts:
                        viewPlayerIcon.setImageResource(R.drawable.icon_hearts);
                        break;
                    case Clubs:
                        viewPlayerIcon.setImageResource(R.drawable.icon_clubs);
                        break;
                    case Diamonds:
                        viewPlayerIcon.setImageResource(R.drawable.icon_diamonds);
                        break;
                }

                // Setting padding
                ViewGroup.MarginLayoutParams playerLayoutParams = (ViewGroup.MarginLayoutParams)playerLayout.getLayoutParams();
                playerLayoutParams.setMargins(
                    (playerIndex > 0)? ((int)this.getResources().getDimension(R.dimen.Header_TotalWidth)) / playerCount : 0,
                    0,
                    0,
                    0
                );
                playerLayout.setLayoutParams(playerLayoutParams);

                // Formatting player name and icon for current player
                if (realPlayerIndex == currentPlayerIndex) {
                    // Increasing player icon size
                    ViewGroup.MarginLayoutParams playerIconLayoutParams = (ViewGroup.MarginLayoutParams)viewPlayerIcon.getLayoutParams();
                    playerIconLayoutParams.width = (int)this.getResources().getDimension(R.dimen.Header_PlayerIconSize_Special);
                    playerIconLayoutParams.height = (int)this.getResources().getDimension(R.dimen.Header_PlayerIconSize_Special);
                    //playerIconLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    viewPlayerIcon.setLayoutParams(playerIconLayoutParams);
                    // Increasing player text size
                    viewPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.getResources().getDimension(R.dimen.Header_TextSize_Special));
                    // Adding player name underline
                    viewPlayerName.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    // Adding player name margin
                    ViewGroup.MarginLayoutParams playerNameLayoutParams = (ViewGroup.MarginLayoutParams)viewPlayerName.getLayoutParams();
                    playerNameLayoutParams.setMargins(0, (int)this.getResources().getDimension(R.dimen.Header_PlayerNameOffset_Special), 0, 0);
                    viewPlayerName.setLayoutParams(playerNameLayoutParams);
                } else {
                    // Reducing player icon size
                    ViewGroup.LayoutParams playerIconLayoutParams = (ViewGroup.MarginLayoutParams)viewPlayerIcon.getLayoutParams();
                    playerIconLayoutParams.width = (int)this.getResources().getDimension(R.dimen.Header_PlayerIconSize_Normal);
                    playerIconLayoutParams.height = (int)this.getResources().getDimension(R.dimen.Header_PlayerIconSize_Normal);
                    //playerIconLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    viewPlayerIcon.setLayoutParams(playerIconLayoutParams);
                    // Reducing player name size
                    viewPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.getResources().getDimension(R.dimen.Header_TextSize_Normal));
                    // Removing player name underline
                    viewPlayerName.setPaintFlags(0);
                    // Removing player name margin
                    ViewGroup.MarginLayoutParams playerNameLayoutParams = (RelativeLayout.LayoutParams)viewPlayerName.getLayoutParams();
                    playerNameLayoutParams.setMargins(0, 0, 0, 0);
                    viewPlayerName.setLayoutParams(playerNameLayoutParams);
                }

            } else { // Hiding player data
                playerLayout.setVisibility(View.GONE);
            }
        }
    }
}
