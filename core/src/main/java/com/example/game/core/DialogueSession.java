package com.example.game.core;

import com.badlogic.gdx.utils.Array;
import com.example.game.world.Player;

/** Owns the mutable state of one play-through of a dialogue. */
public final class DialogueSession {

    private final DialogueManager dialogue;
    private final Player player;
    private DialogueNode currentNode;

    public DialogueSession(DialogueManager dialogue) {
        if (dialogue == null) {
            throw new IllegalArgumentException("DialogueManager cannot be null");
        }
        this.dialogue = dialogue;
        player = new Player();
        dialogue.applyInitialStats(player);
    }

    public DialogueNode start() {
        currentNode = dialogue.getStart();
        if (currentNode == null) {
            throw new IllegalStateException("Start node is missing");
        }
        return currentNode;
    }

    public DialogueNode getCurrentNode() {
        return currentNode;
    }

    public Array<DialogueNode.Choice> getAvailableChoices() {
        if (currentNode == null) {
            throw new IllegalStateException("Dialogue session has not been started");
        }

        Array<DialogueNode.Choice> choices = new Array<>();
        for (DialogueNode.Choice choice : currentNode.choices) {
            if (dialogue.isAvailable(choice, player)) {
                choices.add(choice);
            }
        }
        return choices;
    }

    /** Applies a choice and returns the next node, or {@code null} for an exit action. */
    public DialogueNode select(DialogueNode.Choice choice) {
        if (choice == null || currentNode == null || !currentNode.choices.contains(choice, true)) {
            throw new IllegalArgumentException("Choice does not belong to the current dialogue node");
        }
        if (!dialogue.isAvailable(choice, player)) {
            throw new IllegalStateException("Choice requirements are not met");
        }

        player.apply(choice.effects);
        String nextId = dialogue.resolveNext(choice, player);
        if (nextId == null) {
            if ("exit".equals(choice.action)) {
                return null;
            }
            throw new IllegalStateException("Choice has no next node or supported action");
        }

        currentNode = dialogue.get(nextId);
        if (currentNode == null) {
            throw new IllegalStateException("Missing dialogue node: " + nextId);
        }
        return currentNode;
    }
}
