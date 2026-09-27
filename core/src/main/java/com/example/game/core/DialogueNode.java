package com.example.game.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class DialogueNode {

    public final String id;
    public final String text;
    public final String background;
    public final String music;
    public final Array<Choice> choices = new Array<>();

    public DialogueNode(String id, String text, String background, String music) {
        this.id = id;
        this.text = text;
        this.background = background;
        this.music = music;
    }

    public static class Choice {

        public final String text;
        public final String next;         // может быть null, если есть outcomes
        public final String action;       // пока только "exit"
        public final ObjectMap<String, Integer> requires;   // условия видимости
        public final ObjectMap<String, Integer> effects;    // что меняется после выбора
        public final Array<Outcome> outcomes;               // взвешенный рандом

        public Choice(String text,
                      String next,
                      String action,
                      ObjectMap<String, Integer> requires,
                      ObjectMap<String, Integer> effects,
                      Array<Outcome> outcomes) {
            this.text = text;
            this.next = next;
            this.action = action;
            this.requires = requires;
            this.effects = effects;
            this.outcomes = outcomes;
        }
    }

    /** Вариант исхода при случайном выпадении. */
    public static class Outcome {
        public final int weight;
        public final String next;
        public final ObjectMap<String, Integer> requires; // ← новое

        public Outcome(int weight, String next, ObjectMap<String, Integer> requires) {
            this.weight = weight;
            this.next = next;
            this.requires = requires;
        }
    }
}