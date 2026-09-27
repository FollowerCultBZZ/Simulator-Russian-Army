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

public final class GameScene extends Stage {

    public interface NodeListener {
        void onNodeShown(DialogueNode node);
    }

    public interface ChoiceListener {
        void onChoiceSelected(DialogueNode.Choice choice);
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

    private final Image backgroundImage;
    private final Label dialogLabel;
    private final Table dialogBox;
    private final Table choicesTable;

    private Texture backgroundTexture;
    private String currentBackgroundPath;
    private NodeListener nodeListener;
    private ChoiceListener choiceListener;

    public GameScene(Skin skin) {
        super(new ExtendViewport(VW, VH));

        if (skin == null) {
            throw new IllegalArgumentException("Skin cannot be null");
        }
        this.skin = skin;
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

    public void setChoiceListener(ChoiceListener listener) {
        this.choiceListener = listener;
    }

    public void showNode(DialogueNode node, Iterable<DialogueNode.Choice> choices) {

        if (node == null || choices == null) {
            throw new IllegalArgumentException("Node and choices cannot be null");
        }

        if (node.background != null) {
            setBackground(node.background);
        }

        dialogLabel.setText(node.text == null ? "" : node.text);
        choicesTable.clearChildren();

        boolean anyVisible = false;

        for (final DialogueNode.Choice choice : choices) {
            anyVisible = true;

            TextButton button = new TextButton(choice.text, skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    notifyChoiceSelected(choice);
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

    private void notifyChoiceSelected(DialogueNode.Choice choice) {
        if (choiceListener != null) {
            choiceListener.onChoiceSelected(choice);
        }
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
