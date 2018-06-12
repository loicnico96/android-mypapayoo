package com.salinco.papayoo.game;

import java.util.List;
import java.util.Observable;

public abstract class AbstractGameState extends Observable {
    public class PlayerData {
        public CardColor playerColor;
        public String playerName;
        public int playerPointsGivenCurrent;
        public int playerPointsTakenCurrent;
        public int playerPointsGivenTotal;
        public int playerPointsTakenTotal;

        public PlayerData (String playerName, CardColor playerColor) {
            this.playerColor = playerColor;
            this.playerName = playerName;
            this.playerPointsGivenCurrent = 0;
            this.playerPointsTakenCurrent = 0;
            this.playerPointsGivenTotal = 0;
            this.playerPointsTakenTotal = 0;
        }
    }

    public abstract int getCurrentPlayerIndex ();
    public abstract List<Card> getFieldCards ();
    public abstract CardColor getGameColor ();
    public abstract int getGameCount ();
    public abstract Card getHighestFieldCard ();
    public abstract int getStartingPlayerIndex ();
    public abstract int getPlayerCount ();
    public abstract List<PlayerData> getPlayers ();
    public abstract int getTurnCount ();
    public abstract AbstractPlayerState registerPlayer (String playerName, AbstractPlayerController playerController);
}
