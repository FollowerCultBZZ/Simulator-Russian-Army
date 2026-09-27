package com.example.game.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class DialogueData {

    public ObjectMap<String, Integer> player;
    public ObjectMap<String, NodeData> nodes;

    public static class NodeData {
        public String text;
        public String background;
        public String music;
        public Array<ChoiceData> choices;
    }

    public static class ChoiceData {
        public String text;
        public String next;
        public String action;
        public ObjectMap<String, Integer> requires;
        public ObjectMap<String, Integer> effects;
        public Array<OutcomeData> outcomes;
    }

    public static class OutcomeData {
        public int weight = 1;
        public String next;
        public ObjectMap<String, Integer> requires;
    }
}
