package com.salinco.papayoo.game;

public enum NotifyReason {
    GameState_GameStarted,
    GameState_GameColorRolled,
    GameState_PlayerHandsDealt,
    GameState_SideCardsSelecting,
    GameState_SideCardsDone,
    GameState_NewTurnStarted,
    GameState_PlayerTurnStarted,
    GameState_PlayerCardPlayed,
    GameState_TurnEnded,
    GameState_GameEnded,
    GameState_PlayerHandChanged,
    PlayerController_SelectionChanged,
    PlayerController_FocusChanged;
}
