package com.salinco.papayoo.game;

import java.util.ArrayList;
import java.util.List;

public class LocalPlayerState extends AbstractPlayerState {
    private LocalGameState myGameState;
    private AbstractPlayerController myPlayerController;
    private List<Card> myPlayerHandCards;
    private int myPlayerIndex;
    private Selection mySelection;

    public LocalPlayerState (LocalGameState gameState, int playerIndex, AbstractPlayerController playerController) {
        this.myGameState = gameState;
        this.myPlayerController = playerController;
        this.myPlayerHandCards = new ArrayList<>();
        this.myPlayerIndex = playerIndex;
        this.mySelection = null;
    }

    public void cancelSelection () {
        this.mySelection = null;
    }

    @Override
    public AbstractGameState getGameState () {
        return this.myGameState;
    }

    @Override
    public AbstractPlayerController getPlayerController () {
        return this.myPlayerController;
    }

    @Override
    public List<Card> getPlayerHandCards () {
        return this.myPlayerHandCards;
    }

    @Override
    public int getPlayerIndex () {
        return this.myPlayerIndex;
    }

    @Override
    public Selection getSelection () {
        return this.mySelection;
    }

    public Selection requestSelection (AbstractGameState gameState, Selection.Reason reason, List<Card> cards, int count, Selection.Validation validation) {
        this.mySelection = new Selection(reason, cards, count, validation);
        this.myPlayerController.requestSelection(gameState, this.mySelection);
        return this.mySelection;
    }
}
