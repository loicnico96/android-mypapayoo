package com.salinco.papayoo.game;

import java.util.List;
import java.util.Observable;

public abstract class AbstractPlayerState extends Observable {
    public abstract AbstractGameState getGameState();
    public abstract AbstractPlayerController getPlayerController();
    public abstract List<Card> getPlayerHandCards();
    public abstract int getPlayerIndex();
    public abstract Selection getSelection();

    public boolean hasSelection () {
        return this.getSelection() != null;
    }
}
