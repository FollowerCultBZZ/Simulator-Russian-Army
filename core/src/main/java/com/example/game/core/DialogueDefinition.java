package com.example.game.core;

import com.badlogic.gdx.utils.ObjectMap;

/** Internal validated representation of a dialogue script. */
final class DialogueDefinition {
    final ObjectMap<String, DialogueNode> nodes = new ObjectMap<>();
    final ObjectMap<String, Integer> initialStats = new ObjectMap<>();
}
