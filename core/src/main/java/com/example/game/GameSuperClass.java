package com.example.game;

import com.badlogic.gdx.Game;
import com.example.game.screens.TestScreen;
import com.example.game.utils.Core;

public class GameSuperClass extends Game {

    @Override
    public void create() {
        Core.load();
        setScreen(new TestScreen());
    }

    @Override
    public void dispose() {
        super.dispose();
        Core.dispose();
    }
}
