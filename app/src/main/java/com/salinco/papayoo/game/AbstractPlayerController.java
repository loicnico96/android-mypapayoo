package com.salinco.papayoo.game;

import java.util.Observable;

public abstract class AbstractPlayerController extends Observable {
    public abstract void requestSelection (AbstractGameState gameState, Selection selection);
}
