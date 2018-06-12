package com.salinco.papayoo.game;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Selection {
    public enum Validation { Automatic, Manual; }
    public enum Reason { SideCards, PlayCards, DiscardCards; }

    private TaskCompletionSource<List<Card>> myTaskSource;
    private List<Card> mySelectableCards;
    private List<Card> mySelectedCards;
    private Validation mySelectionValidation;
    private int mySelectionCount;
    private Reason mySelectionReason;
    private long mySelectionTimeStart;
    private long mySelectionTimeOut;

    public Selection (Reason reason, List<Card> cards, int count, Validation validation) {
        this.myTaskSource = new TaskCompletionSource<>();
        this.mySelectableCards = new ArrayList<>(cards);
        this.mySelectedCards = new ArrayList<>();
        this.mySelectionValidation = validation;
        this.mySelectionCount = count;
        this.mySelectionReason = reason;
        this.mySelectionTimeStart = 0;
        this.mySelectionTimeOut = 0;
    }

    public boolean deselectCard (Card card) {
        if (this.isSelectedCard(card)) {
            this.mySelectedCards.remove(card);
            return true;
        } else {
            return false;
        }
    }

    public boolean fillRandom () {
        if (!this.isSelectionComplete()) {
            List<Card> selectableCards = this.getSelectableCards();
            while (!this.isSelectionComplete() && !selectableCards.isEmpty()) {
                Card card = selectableCards.get(GameRules.Randomizer.nextInt(selectableCards.size()));
                this.mySelectedCards.add(card);
                selectableCards.remove(card);
            }
            return this.isSelectionComplete();
        } else {
            return false;
        }
    }

    public List<Card> getSelectableCards () {
        List<Card> selectableCards = new ArrayList<>();
        for (Card card : this.mySelectableCards) {
            if (!this.isSelectedCard(card)) {
                selectableCards.add(card);
            }
        }
        return selectableCards;
    }

    public List<Card> getSelectedCards () {
        return this.mySelectedCards;
    }

    public int getSelectionCount () {
        return this.mySelectionCount;
    }

    public int getSelectionCountRemaining () {
        return this.mySelectionCount - this.mySelectedCards.size();
    }

    public Reason getSelectionReason () {
        return this.mySelectionReason;
    }

    public long getSelectionDurationMax () {
        return Math.max(this.mySelectionTimeOut - this.mySelectionTimeStart, 0);
    }

    public long getSelectionDurationRemaining () {
        return Math.max(this.mySelectionTimeOut - System.currentTimeMillis(), 0);
    }

    public boolean isSelectableCard (Card card) {
        return this.mySelectableCards.contains(card) && !this.isSelectedCard(card);
    }

    public boolean isSelectedCard (Card card) {
        return this.mySelectedCards.contains(card);
    }

    public boolean isSelectionComplete () {
        return this.getSelectionCountRemaining() == 0;
    }

    public boolean selectCard (Card card) {
        if (this.isSelectableCard(card) && !this.isSelectionComplete()) {
            this.mySelectedCards.add(card);
            if (this.mySelectionValidation == Validation.Automatic && this.isSelectionComplete()) {
                this.validate();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setDuration (long timeStart, long timeOut) {
        this.mySelectionTimeStart = timeStart;
        this.mySelectionTimeOut = timeOut;
        return true;
    }

    public boolean validate () {
        return this.isSelectionComplete() && this.myTaskSource.trySetResult(this.mySelectedCards);
    }
/*
    public static Task<List<Card>> WaitForSelection (Selection selection) {
        return selection.myTaskSource.getTask();
    }
    public static Task<Void> WaitForAll (List<Selection> selectionList) {
        List<Task<List<Card>>> taskList = new ArrayList<>(selectionList.size());
        // Creating task list
        long duration = 0;
        for (Selection selection : selectionList) {
            taskList.add(selection.myTaskSource.getTask());
            duration = Math.max(duration, selection.getSelectionDurationRemaining());
        }
        return Tasks.whenAll(taskList);
    }*/

    public static boolean WaitForSelection (Selection selection) {
        try {
            long duration = selection.getSelectionDurationRemaining();
            if (duration > 0) {
                Tasks.await(selection.myTaskSource.getTask(), duration, TimeUnit.MILLISECONDS);
            } else {
                Tasks.await(selection.myTaskSource.getTask());
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static boolean WaitForAll (List<Selection> selectionList) {
        List<Task<List<Card>>> taskList = new ArrayList<>(selectionList.size());

        // Creating task list
        long duration = 0;
        for (Selection selection : selectionList) {
            taskList.add(selection.myTaskSource.getTask());
            duration = Math.max(duration, selection.getSelectionDurationRemaining());
        }

        // Waiting for all tasks to finish or timeout
        try {
            if (duration > 0) {
                Tasks.await(Tasks.whenAll(taskList), duration, TimeUnit.MILLISECONDS);
            } else {
                Tasks.await(Tasks.whenAll(taskList));
            }
            return true;
        } catch (Exception exception) {
            Log.d("Papayoo", exception.getMessage());
            return false;
        }
    }
}
