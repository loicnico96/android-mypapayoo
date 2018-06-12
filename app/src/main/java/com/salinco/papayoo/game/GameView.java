package com.salinco.papayoo.game;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.salinco.papayoo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameView extends Handler {
    public enum Reason {
        UpdateFieldCards(0),
        UpdateGameColor(1),
        UpdatePlayerData(2),
        UpdatePlayerHandCards(3),
        UpdatePlayerSelection(4);

        // Enum -> Int conversion
        private int value;
        Reason(int value) { this.value = value; }
        public int getValue() { return this.value; }
        // Int -> Enum conversion
        private static SparseArray<Reason> values;
        static { for (Reason reason : Reason.values()) values.put(reason.getValue(), reason); }
        public static Reason getReason(int value) { return values.get(value); }
    }

    private CountDownTimer myActionTimer;
    private ImageView myImageGameColor;
    private ViewGroup myLayoutActionTimer;
    private ViewGroup myLayoutFieldCards;
    private ViewGroup myLayoutHandCards;
    private ViewGroup myLayoutPlayerHeader;
    private Resources myResources;
    private TextView myTextSelectionDescription;
    private TextView myTextSelectionValidation;

    public GameView(@NonNull Activity activity, @NonNull LocalPlayerController controller) {
        super(Looper.getMainLooper());
        this.myResources = activity.getResources();
        this.myImageGameColor = activity.findViewById(R.id.Image_GameColor);
        this.myLayoutActionTimer = activity.findViewById(R.id.Layout_ActionTimer);
        this.myLayoutFieldCards = activity.findViewById(R.id.Layout_FieldCards);
        this.myLayoutHandCards = activity.findViewById(R.id.Layout_HandCards);
        this.myLayoutPlayerHeader = activity.findViewById(R.id.Layout_HeaderPlayers);
        this.myTextSelectionDescription = activity.findViewById(R.id.Text_ActionDescription);
        this.myTextSelectionValidation = activity.findViewById(R.id.Text_ActionValidation);
        this.myTextSelectionValidation.setOnClickListener(controller);
        for (int viewIndex = 0 ; viewIndex < this.myLayoutHandCards.getChildCount() ; viewIndex++) {
            this.myLayoutHandCards.getChildAt(viewIndex).setOnClickListener(controller);
        }
        for (int viewIndex = 0 ; viewIndex < this.myLayoutFieldCards.getChildCount() ; viewIndex++) {
            this.myLayoutFieldCards.getChildAt(viewIndex).setOnClickListener(controller);
        }
    }

    @Override
    public void handleMessage(Message message) {
        try {
            Reason reason = Reason.getReason(message.what);
            Log.i("Papayoo", String.format("GameView Update (%d) - %s", message.what, reason.toString()));
            switch (reason) {

                case UpdateFieldCards:
                    this.handleMessageFieldCards(message);
                    break;

                case UpdateGameColor:;
                    this.handleMessageGameColor(message);
                    break;

                case UpdatePlayerData:
                    this.handleMessagePlayerData(message);
                    break;

                case UpdatePlayerHandCards:
                    this.handleMessagePlayerHandCards(message);
                    break;

                case UpdatePlayerSelection:
                    this.handleMessagePlayerSelection(message);
                    break;

                default: // Unhandled update reason
                    throw new RuntimeException(String.format(Locale.ENGLISH,
                            "Unhandled message code (%s)",
                            reason.toString()
                    ));
            }
        } catch (Exception exception) {
            Log.e("Papayoo", String.format("GameView Update (%d) - %s", message.what, exception.getMessage()));
        }
    }

    /**
     * Sends an update request to the view, updating the view in the UI thread as soon as possible.
     * @param source Source of the message
     * @param reason Reason of the message
     */
    public boolean sendMessage(Object source, @NonNull GameView.Reason reason) {
        Message message = this.obtainMessage(reason.getValue(), source);
        return this.sendMessage(message);
    }

    private void handleMessageFieldCards(@NonNull Message message) {
        AbstractGameState gameState = (AbstractGameState)message.obj;
        this.updateFieldCards(
                gameState.getPlayerCount(),
                gameState.getFieldCards(),
                null,
                null,
                gameState.getHighestFieldCard()
        );
    }

    private void handleMessageGameColor(@NonNull Message message) {
        AbstractGameState gameState = (AbstractGameState)message.obj;
        this.updateGameColor(gameState.getGameColor());
    }

    private void handleMessagePlayerData(@NonNull Message message) {
        AbstractGameState gameState = (AbstractGameState)message.obj;
        this.updatePlayerHeader(
                gameState.getPlayers(),
                gameState.getStartingPlayerIndex(),
                gameState.getCurrentPlayerIndex()
        );
    }

    private void handleMessagePlayerHandCards(@NonNull Message message) {
        AbstractPlayerState player = (AbstractPlayerState)message.obj;
        List<Card> handCardList = new ArrayList<>(player.getPlayerHandCards());
        handCardList.removeAll(player.getSelection().getSelectedCards());
        this.updatePlayerHandCards(
                handCardList,
                player.getSelection().getSelectableCards(),
                null //player.getPlayerController().getFocusedCard()
        );
    }

    private void handleMessagePlayerSelection(@NonNull Message message) {
        AbstractPlayerState player = (AbstractPlayerState)message.obj;
        Selection playerSelection = player.getSelection();
        this.updateSelectionDescription(playerSelection);
        this.updateSelectionTimer(playerSelection);
        this.updateSelectionValidation(playerSelection);
        this.updateFieldCards(
                player.getSelection().getSelectionCount(),
                player.getSelection().getSelectedCards(),
                player.getSelection().getSelectedCards(),
                null, //player.getPlayerController().getFocusedCard()
                null
        );
        this.handleMessagePlayerHandCards(message);
    }

    /**
     * Updates field cards.
     * @param fieldCardCount Number of card slots
     * @param fieldCardList All cards on the field (null if none)
     * @param fieldCardSelectableList Selectable cards on the field (null if none)
     * @param fieldCardFocused Focused card on the field (null if none)
     * @param fieldCardHighest Highest card on the field (null if none)
     */
    private void updateFieldCards(
            int fieldCardCount,
            List<Card> fieldCardList,
            List<Card> fieldCardSelectableList,
            Card fieldCardFocused,
            Card fieldCardHighest) {
        Log.d("Papayoo", "Updating field cards...");
        if (fieldCardCount > 0) {

            // Making cards visible
            this.myLayoutFieldCards.setVisibility(View.VISIBLE);
            for (int viewIndex = 0 ; viewIndex < this.myLayoutFieldCards.getChildCount() ; viewIndex++) {
                CardView cardView = (CardView)this.myLayoutFieldCards.getChildAt(viewIndex);
                if (viewIndex < fieldCardCount) {
                    Card card = (fieldCardList != null)? fieldCardList.get(viewIndex) : null;
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setCard(card);

                    // Setting background
                    cardView.getBackground().setAlpha(170);
                    if (fieldCardSelectableList != null && fieldCardSelectableList.contains(card)) {
                        if (fieldCardFocused != null && fieldCardFocused.equals(card)) {
                            cardView.setBackgroundResource(R.drawable.border_yellow);
                        } else {
                            cardView.setBackgroundResource(R.drawable.border_green);
                        }
                    } else if (fieldCardHighest != null && fieldCardHighest.equals(card)) {
                        cardView.setBackgroundResource(R.drawable.border_red);
                    } else {
                        cardView.setBackgroundColor(Color.TRANSPARENT);
                    }

                } else { // Hiding the card
                    cardView.setVisibility(View.GONE);
                }
            }

        } else { // Hiding the layout
            this.myLayoutFieldCards.setVisibility(View.GONE);
        }
        Log.d("Papayoo", "Updating field cards - Done.");
    }

    /**
     * Updates the current game color.
     * @param gameColor Current game color (null if none)
     */
    private void updateGameColor(CardColor gameColor) {
        Log.d("Papayoo", "Updating game color...");
        if (gameColor != null) {
            switch (gameColor) {
                case Spades:
                    this.myImageGameColor.setImageResource(R.drawable.icon_spades);
                    this.myImageGameColor.setVisibility(View.VISIBLE);
                    break;
                case Hearts:
                    this.myImageGameColor.setImageResource(R.drawable.icon_hearts);
                    this.myImageGameColor.setVisibility(View.VISIBLE);
                    break;
                case Clubs:
                    this.myImageGameColor.setImageResource(R.drawable.icon_clubs);
                    this.myImageGameColor.setVisibility(View.VISIBLE);
                    break;
                case Diamonds:
                    this.myImageGameColor.setImageResource(R.drawable.icon_diamonds);
                    this.myImageGameColor.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.d("Papayoo", "Updating game color - Unhandled color (" + gameColor + ").");
                    this.myImageGameColor.setVisibility(View.INVISIBLE);
            }
        } else { // Hiding the game color icon
            this.myImageGameColor.setVisibility(View.INVISIBLE);
        }
        Log.d("Papayoo", "Updating game color - Done.");
    }

    /**
     * Updates player hand cards.
     * @param handCardList All cards in hand (null if none)
     * @param handCardSelectableList Highlighted cards in hand (null if none)
     * @param handCardFocused Focused card in hand (null if none)
     */
    private void updatePlayerHandCards(
            List<Card> handCardList,
            List<Card> handCardSelectableList,
            Card handCardFocused) {
        Log.d("Papayoo", "Updating player hand cards...");
        if (handCardList != null && handCardList.size() > 0) {
            // Calculating dimensions
            int handCardFocusIndex = handCardList.indexOf(handCardFocused);
            int handCardFocusOffset = (int)this.myResources.getDimension(R.dimen.HandCard_FocusOffset);
            int handCardSpacing = (int)this.myResources.getDimension(R.dimen.HandCard_TotalSpacing) / handCardList.size();
            handCardSpacing = Math.min(handCardSpacing, (int)this.myResources.getDimension(R.dimen.HandCard_MaxSpacing));
            handCardSpacing = Math.max(handCardSpacing, (int)this.myResources.getDimension(R.dimen.HandCard_MinSpacing));

            // Making cards visible
            this.myLayoutHandCards.setVisibility(View.VISIBLE);
            for (int viewIndex = 0 ; viewIndex < this.myLayoutHandCards.getChildCount() ; viewIndex++) {
                CardView cardView = (CardView)this.myLayoutHandCards.getChildAt(viewIndex);
                if (viewIndex < handCardList.size()) {
                    Card card = handCardList.get(viewIndex);
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setCard(card);

                    // Setting Z priority
                    cardView.setZ(-Math.abs(viewIndex - handCardFocusIndex));

                    // Setting border
                    cardView.getBackground().setAlpha(170);
                    if (handCardSelectableList != null && handCardSelectableList.contains(card)) {
                        if (handCardFocused != null && handCardFocused.equals(card)) {
                            cardView.setBackgroundResource(R.drawable.border_yellow);
                        } else {
                            cardView.setBackgroundResource(R.drawable.border_green);
                        }
                    } else {
                        cardView.setBackgroundColor(Color.TRANSPARENT);
                    }

                    // Setting padding
                    cardView.setMargins(
                            (viewIndex != 0)? handCardSpacing : 0,
                            (viewIndex == handCardFocusIndex)? handCardFocusOffset : 0,
                            0,
                            0
                    );

                } else { // Hiding the card
                    cardView.setVisibility(View.GONE);
                }
            }

        } else { // Hiding the hand card layout
            this.myLayoutHandCards.setVisibility(View.GONE);
        }
        Log.d("Papayoo", "Updating player hand cards - Done.");
    }

    /**
     * Updates player header.
     * @param playerList List of players (null if none)
     * @param startingPlayerIndex Absolute index of the starting player
     * @param currentPlayerIndex Absolute index of the current turn player
     */
    private void updatePlayerHeader(
            List<AbstractGameState.PlayerData> playerList,
            int startingPlayerIndex,
            int currentPlayerIndex) {
        Log.d("Papayoo", "Updating player header...");
        for (int viewIndex = 0 ; viewIndex < this.myLayoutPlayerHeader.getChildCount() ; viewIndex++) {
            ViewGroup playerLayout = (ViewGroup)this.myLayoutPlayerHeader.getChildAt(viewIndex);
            if (playerList != null && viewIndex < playerList.size()) {
                int playerIndex = (viewIndex + startingPlayerIndex) % playerList.size();
                AbstractGameState.PlayerData playerData = playerList.get(playerIndex);
                playerLayout.setVisibility(View.VISIBLE);

                // Setting player icon
                ImageView playerIconView = (ImageView)playerLayout.getChildAt(0);
                switch (playerData.playerColor) {
                    case Spades:
                        playerIconView.setImageResource(R.drawable.icon_spades);
                        break;
                    case Hearts:
                        playerIconView.setImageResource(R.drawable.icon_hearts);
                        break;
                    case Clubs:
                        playerIconView.setImageResource(R.drawable.icon_clubs);
                        break;
                    case Diamonds:
                        playerIconView.setImageResource(R.drawable.icon_diamonds);
                        break;
                }
                if (playerIndex == currentPlayerIndex) {
                    ViewGroup.LayoutParams playerIconLayoutParams = playerIconView.getLayoutParams();
                    playerIconLayoutParams.width = (int)this.myResources.getDimension(R.dimen.Header_PlayerIconSize_Special);
                    playerIconLayoutParams.height = (int)this.myResources.getDimension(R.dimen.Header_PlayerIconSize_Special);
                    playerIconView.setLayoutParams(playerIconLayoutParams);
                } else {
                    ViewGroup.LayoutParams playerIconLayoutParams = playerIconView.getLayoutParams();
                    playerIconLayoutParams.width = (int)this.myResources.getDimension(R.dimen.Header_PlayerIconSize_Normal);
                    playerIconLayoutParams.height = (int)this.myResources.getDimension(R.dimen.Header_PlayerIconSize_Normal);
                    playerIconView.setLayoutParams(playerIconLayoutParams);
                }

                // Setting player name
                TextView playerNameView = (TextView)playerLayout.getChildAt(1);
                playerNameView.setText(playerData.playerName);
                if (playerIndex == currentPlayerIndex) {
                    playerNameView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    playerNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.myResources.getDimension(R.dimen.Header_TextSize_Special));
                    ViewGroup.MarginLayoutParams playerNameLayoutParams = (ViewGroup.MarginLayoutParams)playerNameView.getLayoutParams();
                    playerNameLayoutParams.setMargins(0, (int)this.myResources.getDimension(R.dimen.Header_PlayerNameOffset_Special), 0, 0);
                    playerNameView.setLayoutParams(playerNameLayoutParams);
                } else {
                    playerNameView.setPaintFlags(0);
                    playerNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.myResources.getDimension(R.dimen.Header_TextSize_Normal));
                    ViewGroup.MarginLayoutParams playerNameLayoutParams = (ViewGroup.MarginLayoutParams)playerNameView.getLayoutParams();
                    playerNameLayoutParams.setMargins(0, 0, 0, 0);
                    playerNameView.setLayoutParams(playerNameLayoutParams);
                }

                // Setting player score
                TextView playerScoreView = (TextView)playerLayout.getChildAt(2);
                if (playerData.playerPointsTakenCurrent > 0) {
                    playerScoreView.setText(String.format(Locale.ENGLISH,
                            "%d (+%d)",
                            playerData.playerPointsTakenTotal,
                            playerData.playerPointsTakenCurrent
                    ));
                } else {
                    playerScoreView.setText(String.format(Locale.ENGLISH,
                            "%d",
                            playerData.playerPointsTakenTotal
                    ));
                }

            } else { // Hiding the player data
                playerLayout.setVisibility(View.GONE);
            }
        }
        Log.d("Papayoo", "Updating player header - Done.");
    }

    /**
     * Updates the selection description.
     * @param selection Current selection (null if none)
     */
    private void updateSelectionDescription(Selection selection) {
        Log.d("Papayoo", "Updating selection description...");
        if (selection != null) {
            Selection.Reason reason = selection.getSelectionReason();
            switch (reason) {
                case DiscardCards:
                    this.myTextSelectionDescription.setText(R.string.Action_Select_DiscardCards);
                    this.myTextSelectionDescription.setVisibility(View.VISIBLE);
                    break;
                case PlayCards:
                    this.myTextSelectionDescription.setText(R.string.Action_Select_PlayCards);
                    this.myTextSelectionDescription.setVisibility(View.VISIBLE);
                    break;
                case SideCards:
                    this.myTextSelectionDescription.setText(R.string.Action_Select_SideCards);
                    this.myTextSelectionDescription.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.d("Papayoo", "Updating selection description - Unhandled reason (" + reason + ").");
                    this.myTextSelectionDescription.setVisibility(View.INVISIBLE);
            }
        } else {
            this.myTextSelectionDescription.setVisibility(View.INVISIBLE);
        }
        Log.d("Papayoo", "Updating selection description - Done.");
    }

    /**
     * Updates the selection timer.
     * @param selection Current selection (null if none)
     */
    private void updateSelectionTimer(Selection selection) {
        Log.d("Papayoo", "Updating selection timer...");
        if (selection != null && selection.getSelectionDurationMax() > 0) {
            int maxDuration = (int)selection.getSelectionDurationMax();
            int remainingDuration = (int)selection.getSelectionDurationRemaining();
            ProgressBar timerProgress = this.myLayoutActionTimer.findViewById(R.id.ProgressBar_ActionTimer);
            this.myLayoutActionTimer.setVisibility(View.VISIBLE);
            if (this.myActionTimer != null) {
                this.myActionTimer.cancel();
            }

            // Setting progress bar parameters
            timerProgress.setMax(maxDuration);
            timerProgress.setProgress(remainingDuration);

            // Animating the progress bar
            ObjectAnimator animator = ObjectAnimator.ofInt(timerProgress, "progress", remainingDuration, 0);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(selection.getSelectionDurationRemaining());
            animator.start();

            // Animating the counter
            if (remainingDuration > 0) {
                this.myActionTimer = new CountDownTimer(remainingDuration, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        TextView timerCounter = myLayoutActionTimer.findViewById(R.id.Text_ActionTimer);
                        timerCounter.setText(String.format("%d", (millisUntilFinished + 1000 - 1) / 1000));
                    }
                    @Override
                    public void onFinish() {
                        myLayoutActionTimer.setVisibility(View.INVISIBLE);
                    }
                };
                this.myActionTimer.start();
            }
        } else {
            this.myLayoutActionTimer.setVisibility(View.INVISIBLE);
        }
        Log.d("Papayoo", "Updating selection timer -  Done.");
    }

    /**
     * Updates the selection validation.
     * @param selection Current selection (null if none)
     */
    private void updateSelectionValidation(Selection selection) {
        Log.d("Papayoo", "Updating selection validation...");
        if (selection != null && selection.isSelectionComplete()) {
            this.myTextSelectionValidation.setVisibility(View.VISIBLE);
        } else {
            this.myTextSelectionValidation.setVisibility(View.INVISIBLE);
        }
        Log.d("Papayoo", "Updating selection validation - Done.");
    }
}
