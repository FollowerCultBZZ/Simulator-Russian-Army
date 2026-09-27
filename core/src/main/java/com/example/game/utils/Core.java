package com.example.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public final class Core {

    public static Skin skin;

    private static final String SKIN_JSON = "ui.json";

    private Core() {
    }

    public static void load() {
        dispose();
        skin = new Skin(Gdx.files.internal(SKIN_JSON));
    }

    public static void dispose() {
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
    }
}
