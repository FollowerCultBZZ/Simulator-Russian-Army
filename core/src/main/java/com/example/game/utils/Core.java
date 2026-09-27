package com.example.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tools.font.FreeTypeFontGenerator;

public final class Core {

    public static Skin skin;

    private static final String SKIN_ATLAS = "ui.atlas";
    private static final String SKIN_JSON = "ui.json";
    private static final String FONT_FILE = "fonts/Roboto-Regular.ttf";
    private static final String DEFAULT_FONT = "default-font";
    private static final int FONT_SIZE = 44;
    private static final String CYRILLIC_CHARACTERS =
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
            "«»—…№";

    private Core() {
    }

    public static void load() {
        dispose();

        Skin loadedSkin = new Skin(
                new TextureAtlas(Gdx.files.internal(SKIN_ATLAS))
        );

        try {
            loadedSkin.add(DEFAULT_FONT, createFont());
            loadedSkin.load(Gdx.files.internal(SKIN_JSON));
            skin = loadedSkin;
        } catch (RuntimeException exception) {
            loadedSkin.dispose();
            throw exception;
        }
    }

    public static void dispose() {
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
    }

    private static BitmapFont createFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal(FONT_FILE)
        );

        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameters =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameters.size = FONT_SIZE;
            parameters.characters =
                    FreeTypeFontGenerator.DEFAULT_CHARS +
                            CYRILLIC_CHARACTERS;
            return generator.generateFont(parameters);
        } finally {
            generator.dispose();
        }
    }
}
