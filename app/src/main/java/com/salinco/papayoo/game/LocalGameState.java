package com.salinco.papayoo.game;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalGameState extends AbstractGameState {
    private int myCurrentPlayerIndex;
    private List<Card> myFieldCards;
    private CardColor myGameColor;
    private int myGameCount;
    private int myStartingPlayerIndex;
    private List<PlayerData> myPlayers;
    private List<LocalPlayerState> myPlayerStates;
    private int myTurnCount;

    public LocalGameState () {
        this.myCurrentPlayerIndex = -1;
        this.myFieldCards = new ArrayList<> ();
        this.myGameColor = null;
        this.myGameCount = 0;
        this.myStartingPlayerIndex = 0;
        this.myPlayers = new ArrayList<> ();
        this.myPlayerStates = new ArrayList<> ();
        this.myTurnCount = 0;
    }

    @Override
    public int getCurrentPlayerIndex () {
        return this.myCurrentPlayerIndex;
    }

    @Override
    public List<Card> getFieldCards () {
        return this.myFieldCards;
    }

    @Override
    public CardColor getGameColor () {
        return this.myGameColor;
    }

    @Override
    public int getGameCount () {
        return this.myGameCount;
    }

    @Override
    public Card getHighestFieldCard () {
        return GameRules.GetHighestFieldCard(this);
    }

    @Override
    public int getStartingPlayerIndex () {
        return this.myStartingPlayerIndex;
    }

    @Override
    public int getPlayerCount () {
        return this.myPlayers.size();
    }

    @Override
    public List<PlayerData> getPlayers () {
        return this.myPlayers;
    }

    public LocalPlayerState getPlayerState (int index) {
        return this.myPlayerStates.get(index);
    }

    @Override
    public int getTurnCount () {
        return this.myTurnCount;
    }

    @Override
    public AbstractPlayerState registerPlayer (String playerName, AbstractPlayerController playerController) {
        int playerIndex = this.myPlayers.size();
        LocalPlayerState playerState = new LocalPlayerState (this, playerIndex, playerController);
        this.myPlayerStates.add(playerState);
        this.myPlayers.add(new PlayerData (playerName, GameRules.GetRandomCardColor()));
        return playerState;
    }

    public void startLocalGame () {
        // Checking for correct player count
        if (!GameRules.IsValidPlayerCount(this)) {
            throw new RuntimeException (String.format(
                "Invalid number of players (%d instead of %d-%d).",
                this.getPlayerCount(),
                GameRules.MinPlayerCount,
                GameRules.MaxPlayerCount
            ));
        }

        // Starting game
        this.myGameCount++;
        this.myTurnCount = 0;
        this.myFieldCards.clear();
        this.myCurrentPlayerIndex = -1;
        this.myStartingPlayerIndex = this.myGameCount % this.myPlayerStates.size();
        for (LocalPlayerState player : this.myPlayerStates) {
            player.getPlayerHandCards().clear();
        }
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_GameStarted);

        // Rolling game color
        this.myGameColor = GameRules.GetRandomCardColor();
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_GameColorRolled);

        // Dealing cards
        List<List<Card>> handCards = GameRules.DealCards(this);
        for (LocalPlayerState player : this.myPlayerStates) {
            player.getPlayerHandCards().addAll(handCards.get(player.getPlayerIndex()));
            Collections.sort(player.getPlayerHandCards());
        }
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_PlayerHandsDealt);

        // Selecting cards to side
        List<Selection> selectionList = new ArrayList<>(this.getPlayerCount());
        for (LocalPlayerState player : this.myPlayerStates) {
            selectionList.add(player.requestSelection(
                    this,
                    Selection.Reason.SideCards,
                    player.getPlayerHandCards(),
                    GameRules.GetSidedCardCount(this),
                    Selection.Validation.Manual
            ));
        }
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_SideCardsSelecting);

        // Waiting for player selection
        Selection.WaitForAll(selectionList);
        for (Selection selection : selectionList) {
            selection.fillRandom();
        }
        try{
            Thread.sleep(500);
        } catch (Exception e) {}

        // Siding cards
        for (LocalPlayerState player : this.myPlayerStates) {
            List<Card> selectedCards = player.getSelection().getSelectedCards();
            int nextPlayerIndex = (player.getPlayerIndex() + this.myPlayerStates.size() - 1) % this.myPlayerStates.size();
            LocalPlayerState nextPlayer = this.myPlayerStates.get(nextPlayerIndex);
            player.getPlayerHandCards().removeAll(selectedCards);
            Collections.sort(player.getPlayerHandCards());
            nextPlayer.getPlayerHandCards().addAll(selectedCards);
            Collections.sort(nextPlayer.getPlayerHandCards());
            player.cancelSelection();
        }
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_SideCardsDone);
        try{
            Thread.sleep(500);
        } catch (Exception e) {}

        // Playing
        while (!GameRules.IsGameOver(this)) {

            // Starting turn
            this.myCurrentPlayerIndex = this.myStartingPlayerIndex;
            this.myFieldCards.clear();
            this.myTurnCount++;
            this.setChanged();
            this.notifyObservers(NotifyReason.GameState_NewTurnStarted);

            // Taking turns
            for (int turnPlayerIndex = 0 ; turnPlayerIndex < this.myPlayerStates.size() ; turnPlayerIndex++) {
                this.myCurrentPlayerIndex = (this.myStartingPlayerIndex + turnPlayerIndex) % this.myPlayerStates.size();

                LocalPlayerState turnPlayer = this.myPlayerStates.get(this.myCurrentPlayerIndex);
                this.setChanged();
                this.notifyObservers(NotifyReason.GameState_PlayerTurnStarted);

                if (GameRules.IsPlayerCanPlayCard(this, turnPlayer)) {
                    turnPlayer.requestSelection(
                            this,
                            Selection.Reason.PlayCards,
                            GameRules.GetPlayableCards(this, turnPlayer),
                            1,
                            Selection.Validation.Automatic
                    );
                } else { // Discarding if not able to play
                    turnPlayer.requestSelection(
                            this,
                            Selection.Reason.DiscardCards,
                            GameRules.GetDiscardableCards(this, turnPlayer),
                            1,
                            Selection.Validation.Automatic
                    );
                }

                // Waiting for player selection
                Selection.WaitForSelection(turnPlayer.getSelection());
                turnPlayer.getSelection().fillRandom();

                // Playing card
                Card selectedCard = turnPlayer.getSelection().getSelectedCards().get(0);
                turnPlayer.getPlayerHandCards().remove(selectedCard);
                turnPlayer.cancelSelection();
                this.myFieldCards.add(selectedCard);
                this.setChanged();
                this.notifyObservers(NotifyReason.GameState_PlayerCardPlayed);
                try{
                    Thread.sleep(500);
                } catch (Exception e) {}
            }

            // ending turn
            Card highestCard = GameRules.GetHighestFieldCard(this);
            int highestCardIndex = this.myFieldCards.indexOf(highestCard);
            int highestPlayerIndex = (this.myStartingPlayerIndex + highestCardIndex) % this.myPlayerStates.size();

            // Calculating points
            int totalPoints = GameRules.GetTurnTotalValue(this);
            for (int turnPlayerIndex = 0 ; turnPlayerIndex < this.getPlayerCount() ; turnPlayerIndex++) {
                int points = GameRules.GetCardValue(this, this.myFieldCards.get(turnPlayerIndex));
                int playerIndex = (this.myStartingPlayerIndex + turnPlayerIndex) % this.myPlayerStates.size();
                if (playerIndex == highestPlayerIndex) {
                    this.myPlayers.get(playerIndex).playerPointsTakenCurrent += totalPoints;
                } else {
                    this.myPlayers.get(playerIndex).playerPointsGivenCurrent += points;
                }
            }
            this.setChanged();
            this.notifyObservers(NotifyReason.GameState_TurnEnded);
            try{
                Thread.sleep(500);
            } catch (Exception e) {}

            // Preparing for next turn
            this.myStartingPlayerIndex = highestPlayerIndex;
            for (PlayerData playerData : this.myPlayers) {
                playerData.playerPointsGivenTotal += playerData.playerPointsGivenCurrent;
                playerData.playerPointsTakenTotal += playerData.playerPointsTakenCurrent;
                playerData.playerPointsGivenCurrent = 0;
                playerData.playerPointsTakenCurrent = 0;
            }
        }

        // ending game
        this.setChanged();
        this.notifyObservers(NotifyReason.GameState_GameEnded);

        try{
            Thread.sleep(1000);
        } catch (Exception e) {}
        this.startLocalGame();
    }
}
