package com.example.game.core;

import com.badlogic.gdx.math.MathUtils;
import com.example.game.world.Player;

/** Provides dialogue navigation and weighted outcome resolution. */
public final class DialogueManager {

    private static final String START_NODE = "start";

    private final DialogueDefinition definition;

    public DialogueManager(String path) {
        definition = new DialogueLoader().load(path);
    }

    public void applyInitialStats(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        player.init(definition.initialStats);
    }

    public boolean isAvailable(
            DialogueNode.Choice choice,
            Player player
    ) {
        if (choice == null || player == null) {
            throw new IllegalArgumentException(
                    "Choice and player cannot be null"
            );
        }

        if (!player.meets(choice.requires)) {
            return false;
        }

        if (choice.outcomes == null || choice.outcomes.size == 0) {
            return true;
        }

        for (DialogueNode.Outcome outcome : choice.outcomes) {
            if (player.meets(outcome.requires)) {
                return true;
            }
        }

        return false;
    }

    public String resolveNext(
            DialogueNode.Choice choice,
            Player player
    ) {
        if (choice == null || player == null) {
            throw new IllegalArgumentException(
                    "Choice and player cannot be null"
            );
        }

        if (!isAvailable(choice, player)) {
            throw new IllegalStateException(
                    "Choice requirements are not met"
            );
        }

        if (choice.outcomes != null &&
                choice.outcomes.size > 0) {

            long total = 0;

            for (DialogueNode.Outcome outcome :
                    choice.outcomes) {
                if (!player.meets(outcome.requires)) {
                    continue;
                }
                total += outcome.weight;

                if (total > Integer.MAX_VALUE) {
                    throw new IllegalStateException(
                            "Total outcome weight exceeds integer range"
                    );
                }
            }

            int roll = MathUtils.random(
                    1,
                    (int) total
            );

            int accumulated = 0;

            for (DialogueNode.Outcome outcome :
                    choice.outcomes) {
                if (!player.meets(outcome.requires)) {
                    continue;
                }
                accumulated += outcome.weight;

                if (roll <= accumulated) {
                    return outcome.next;
                }
            }

            throw new IllegalStateException(
                    "Failed to resolve dialogue outcome"
            );
        }

        return choice.next;
    }

    public DialogueNode get(String id) {
        if (id == null) {
            return null;
        }

        return definition.nodes.get(id);
    }

    public DialogueNode getStart() {
        return definition.nodes.get(START_NODE);
    }

    public boolean contains(String id) {
        return id != null && definition.nodes.containsKey(id);
    }

}
