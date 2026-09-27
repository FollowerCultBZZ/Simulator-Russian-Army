package com.example.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.example.game.core.DialogueManager;
import com.example.game.core.DialogueNode;
import com.example.game.core.GameScene;
import com.example.game.core.MusicManager;
import com.example.game.world.Player;
import com.example.game.utils.Core;

public final class TestScreen extends ScreenAdapter {

    private static final String DIALOGUE_PATH = "dialogue.json";
    private static final float MUSIC_VOLUME = 0.7f;

    private Skin skin;
    private GameScene scene;
    private DialogueManager dialogue;
    private Player player;
    private MusicManager musicManager;

    @Override
    public void show() {
        dispose();

        try {
            if (Core.skin == null) {
                Core.load();
            }
            skin = Core.skin;
            dialogue = new DialogueManager(DIALOGUE_PATH);

            player = new Player();
            dialogue.applyInitialStats(player);

            scene = new GameScene(skin, player, dialogue);

            musicManager = new MusicManager();
            musicManager.setVolume(MUSIC_VOLUME);

            scene.setNodeListener(new GameScene.NodeListener() {
                @Override
                public void onNodeShown(DialogueNode node) {
                    applyMusic(node);
                }
            });

            Gdx.input.setInputProcessor(scene);

            DialogueNode start = dialogue.getStart();
            if (start == null) {
                throw new IllegalStateException("Start node is missing");
            }

            scene.showNode(start);
        } catch (RuntimeException exception) {
            Gdx.app.error("TestScreen", "Failed to initialize screen", exception);
            dispose();
            throw exception;
        }
    }

    private void applyMusic(DialogueNode node) {
        if (node == null || musicManager == null) {
            return;
        }

        String path = node.music;
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        musicManager.play(path);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.WHITE);

        if (scene != null) {
            scene.act(delta);
            scene.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (scene != null) {
            scene.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == scene) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (Gdx.input.getInputProcessor() == scene) {
            Gdx.input.setInputProcessor(null);
        }

        if (scene != null) {
            scene.dispose();
            scene = null;
        }

        if (musicManager != null) {
            musicManager.dispose();
            musicManager = null;
        }

        skin = null;

        dialogue = null;
        player = null;
    }
}
