package com.example.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

/** Loads and validates immutable dialogue content from an asset file. */
final class DialogueLoader {

    private static final String START_NODE = "start";

    DialogueDefinition load(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Dialogue path cannot be empty");
        }

        DialogueData data = new Json().fromJson(DialogueData.class, Gdx.files.internal(path));
        if (data == null || data.nodes == null || data.nodes.size == 0) {
            throw new IllegalStateException("Dialogue contains no nodes: " + path);
        }

        DialogueDefinition definition = new DialogueDefinition();
        validateStats(data.player);
        if (data.player != null) {
            definition.initialStats.putAll(data.player);
        }
        loadNodes(data, definition.nodes);
        validateLinks(definition.nodes);
        return definition;
    }

    private void loadNodes(DialogueData data, ObjectMap<String, DialogueNode> nodes) {
        for (ObjectMap.Entry<String, DialogueData.NodeData> entry : data.nodes.entries()) {
            String id = entry.key;
            DialogueData.NodeData source = entry.value;

            if (id == null || id.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Dialogue node has empty id"
                );
            }

            if (source == null) {
                throw new IllegalStateException(
                        "Dialogue node is null: " + id
                );
            }

            if (source.text == null) {
                throw new IllegalStateException(
                        "Node has no text: " + id
                );
            }

            if (nodes.containsKey(id)) {
                throw new IllegalStateException(
                        "Duplicate dialogue node: " + id
                );
            }

            DialogueNode node = new DialogueNode(
                    id,
                    source.text,
                    normalize(source.background),
                    normalize(source.music)
            );

            if (source.choices != null) {
                for (DialogueData.ChoiceData choice : source.choices) {
                    validateChoice(id, choice);

                    node.choices.add(
                            new DialogueNode.Choice(
                                    choice.text,
                                    normalize(choice.next),
                                    normalize(choice.action),
                                    validateMap(
                                            choice.requires,
                                            "requires",
                                            id
                                    ),
                                    validateMap(
                                            choice.effects,
                                            "effects",
                                            id
                                    ),
                                    convertOutcomes(
                                            choice.outcomes,
                                            id
                                    )
                            )
                    );
                }
            }

            nodes.put(id, node);
        }
    }

    private Array<DialogueNode.Outcome> convertOutcomes(
            Array<DialogueData.OutcomeData> source,
            String nodeId
    ) {
        if (source == null || source.size == 0) {
            return null;
        }

        Array<DialogueNode.Outcome> result =
                new Array<>(source.size);

        for (DialogueData.OutcomeData outcome : source) {
            if (outcome == null) {
                throw new IllegalStateException(
                        "Null outcome in node: " + nodeId
                );
            }

            String next = normalize(outcome.next);

            if (next == null) {
                throw new IllegalStateException(
                        "Outcome must contain 'next' in node: " + nodeId
                );
            }

            if (outcome.weight <= 0) {
                throw new IllegalStateException(
                        "Outcome weight must be > 0 in node: "
                                + nodeId
                                + ", next: "
                                + next
                );
            }

            result.add(
                    new DialogueNode.Outcome(
                            outcome.weight,
                            next,
                            validateMap(
                                    outcome.requires,
                                    "outcome requires",
                                    nodeId
                            )
                    )
            );
        }

        return result;
    }

    private void validateChoice(
            String nodeId,
            DialogueData.ChoiceData choice
    ) {
        if (choice == null) {
            throw new IllegalStateException(
                    "Null choice in node: " + nodeId
            );
        }

        if (choice.text == null ||
                choice.text.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Choice has no text in node: " + nodeId
            );
        }

        String next = normalize(choice.next);
        String action = normalize(choice.action);

        boolean hasNext = next != null;
        boolean hasAction = action != null;
        boolean hasOutcomes =
                choice.outcomes != null &&
                choice.outcomes.size > 0;

        int variants =
                (hasNext ? 1 : 0) +
                (hasAction ? 1 : 0) +
                (hasOutcomes ? 1 : 0);

        if (variants != 1) {
            throw new IllegalStateException(
                    "Choice must contain exactly one of 'next', 'action', 'outcomes': "
                            + nodeId
            );
        }

        if (hasAction && !"exit".equals(action)) {
            throw new IllegalStateException(
                    "Unknown action '" +
                            action +
                            "' in node: " +
                            nodeId
            );
        }

        validateMap(
                choice.requires,
                "requires",
                nodeId
        );

        validateMap(
                choice.effects,
                "effects",
                nodeId
        );
    }

    private ObjectMap<String, Integer> validateMap(
            ObjectMap<String, Integer> map,
            String name,
            String nodeId
    ) {
        if (map == null) {
            return null;
        }

        for (ObjectMap.Entry<String, Integer> entry : map) {
            if (entry.key == null ||
                    entry.key.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Empty key in '" +
                                name +
                                "' of node: " +
                                nodeId
                );
            }

            if (entry.value == null) {
                throw new IllegalStateException(
                        "Null value for '" +
                                entry.key +
                                "' in '" +
                                name +
                                "' of node: " +
                                nodeId
                );
            }
        }

        return map;
    }

    private void validateStats(
            ObjectMap<String, Integer> stats
    ) {
        if (stats == null) {
            return;
        }

        validateMap(
                stats,
                "player",
                "root"
        );
    }

    private void validateLinks(ObjectMap<String, DialogueNode> nodes) {
        if (!nodes.containsKey(START_NODE)) {
            throw new IllegalStateException(
                    "Starting node 'start' not found"
            );
        }

        for (DialogueNode node : nodes.values()) {
            for (DialogueNode.Choice choice : node.choices) {
                if (choice.next != null) {
                    checkLink(
                            nodes,
                            node.id,
                            choice.next
                    );
                }

                if (choice.outcomes != null) {
                    for (DialogueNode.Outcome outcome :
                            choice.outcomes) {
                        checkLink(
                                nodes,
                                node.id,
                                outcome.next
                        );
                    }
                }
            }
        }
    }

    private void checkLink(
            ObjectMap<String, DialogueNode> nodes,
            String fromId,
            String targetId
    ) {
        if (!nodes.containsKey(targetId)) {
            throw new IllegalStateException(
                    "Node '" +
                            fromId +
                            "' links to missing node '" +
                            targetId +
                            "'"
            );
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
