package com.jobrec.memento;

import java.util.Stack;

// MEMENTO PATTERN — the Caretaker.
// Keeps a stack of saved profile drafts. Push a snapshot before each edit;
// pop it to undo the most recent change.
public class ProfileHistory {
    private final Stack<CandidateMemento> history = new Stack<>();

    public void save(CandidateMemento memento) {
        history.push(memento);
    }

    // Returns the most recent snapshot, or null if there is nothing to undo
    public CandidateMemento undo() {
        return history.isEmpty() ? null : history.pop();
    }

    public boolean isEmpty() { return history.isEmpty(); }
    public int size() { return history.size(); }
}
