package com.example.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.example.game.world.Player;

public final class GameScene extends Stage {

    public interface NodeListener {
        void onNodeShown(DialogueNode node);
    }

    public static final float VW = 1080f;
    public static final float VH = 1920f;

    private static final float BUTTON_W_RATIO = 0.80f;
    private static final float BUTTON_H_RATIO = 0.08f;
    private static final float BUTTON_PAD = 16f;
    private static final float DIALOG_PAD = 32f;
    private static final float DIALOG_PAD_BOTTOM = 32f;
    private static final float ROOT_PAD = 40f;
    private static final String DEFAULT_BACKGROUND = "background.png";

    private final Skin skin;
    private final Player player;
    private final DialogueManager dialogue;

    private final Image backgroundImage;
    private final Label dialogLabel;
    private final Table dialogBox;
    private final Table choicesTable;

    private Texture backgroundTexture;
    private String currentBackgroundPath;
    private NodeListener nodeListener;

    public GameScene(Skin skin, Player player, DialogueManager dialogue) {
        super(new ExtendViewport(VW, VH));

        if (skin == null) {
            throw new IllegalArgumentException("Skin cannot be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (dialogue == null) {
            throw new IllegalArgumentException("DialogueManager cannot be null");
        }

        this.skin = skin;
        this.player = player;
        this.dialogue = dialogue;

        backgroundImage = new Image();
        backgroundImage.setFillParent(true);
        backgroundImage.setScaling(Scaling.fill);
        addActor(backgroundImage);

        dialogBox = new Table(skin);
        dialogBox.setBackground("dialog");
        dialogBox.pad(DIALOG_PAD);

        dialogLabel = new Label("", skin);
        dialogLabel.setWrap(true);
        dialogLabel.setAlignment(Align.center);
        dialogBox.add(dialogLabel).growX();

        choicesTable = new Table();
        choicesTable.defaults()
                .width(VW * BUTTON_W_RATIO)
                .height(VH * BUTTON_H_RATIO)
                .pad(BUTTON_PAD / 2f);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(ROOT_PAD);
        addActor(root);

        root.add().expand().row();
        root.add(dialogBox).growX().padBottom(DIALOG_PAD_BOTTOM).row();
        root.add(choicesTable).growX().row();

        setBackground(DEFAULT_BACKGROUND);
    }

    public void setNodeListener(NodeListener listener) {
        this.nodeListener = listener;
    }

    public void showNode(DialogueNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }

        if (node.background != null) {
            setBackground(node.background);
        }

        dialogLabel.setText(node.text == null ? "" : node.text);
        choicesTable.clearChildren();

        boolean anyVisible = false;

        for (final DialogueNode.Choice choice : node.choices) {
            if (!player.meets(choice.requires)) {
                continue;
            }

            anyVisible = true;

            TextButton button = new TextButton(choice.text, skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    onChoiceSelected(choice);
                }
            });

            choicesTable.add(button).row();
        }

        if (!anyVisible) {
            TextButton fallback = new TextButton("Завершить", skin);
            fallback.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.exit();
                }
            });
            choicesTable.add(fallback).row();
        }

        if (nodeListener != null) {
            nodeListener.onNodeShown(node);
        }
    }

    private void onChoiceSelected(DialogueNode.Choice choice) {
        if (choice == null) {
            return;
        }

        player.apply(choice.effects);

        String nextId = dialogue.resolveNext(choice);

        if (nextId != null) {
            DialogueNode next = dialogue.get(nextId);
            if (next == null) {
                throw new IllegalStateException("Missing dialogue node: " + nextId);
            }
            showNode(next);
            return;
        }

        if ("exit".equals(choice.action)) {
            Gdx.app.exit();
            return;
        }

        throw new IllegalStateException("Choice has no next node or supported action");
    }

    public void setBackground(String path) {
        String normalizedPath = normalize(path);

        if (normalizedPath == null) {
            throw new IllegalArgumentException("Background path cannot be empty");
        }

        if (normalizedPath.equals(currentBackgroundPath) && backgroundTexture != null) {
            return;
        }

        Texture newTexture = new Texture(Gdx.files.internal(normalizedPath));
        newTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );

        Texture oldTexture = backgroundTexture;
        backgroundTexture = newTexture;
        currentBackgroundPath = normalizedPath;
        backgroundImage.setDrawable(new TextureRegionDrawable(newTexture));

        if (oldTexture != null) {
            oldTexture.dispose();
        }
    }

    @Override
    public void dispose() {
        Texture texture = backgroundTexture;
        backgroundTexture = null;
        currentBackgroundPath = null;

        super.dispose();

        if (texture != null) {
            texture.dispose();
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
