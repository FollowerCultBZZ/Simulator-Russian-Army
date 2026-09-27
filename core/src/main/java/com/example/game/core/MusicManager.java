package com.example.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public final class MusicManager {

    private static final float DEFAULT_VOLUME = 0.7f;

    private Music music;
    private String currentPath;
    private float volume = DEFAULT_VOLUME;

    public void play(String path) {
        String normalizedPath = normalize(path);

        if (normalizedPath == null) {
            stop();
            return;
        }

        if (normalizedPath.equals(currentPath) && music != null) {
            if (!music.isPlaying()) {
                music.play();
            }
            return;
        }

        stop();

        FileHandle file = Gdx.files.internal(normalizedPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Music file not found: " + normalizedPath);
        }

        Music newMusic = Gdx.audio.newMusic(file);
        newMusic.setLooping(true);
        newMusic.setVolume(volume);

        music = newMusic;
        currentPath = normalizedPath;
        music.play();
    }

    public void stop() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }

        currentPath = null;
    }

    public void setVolume(float volume) {
        if (Float.isNaN(volume) || Float.isInfinite(volume)) {
            throw new IllegalArgumentException("Volume must be a finite number");
        }

        this.volume = Math.max(0f, Math.min(1f, volume));

        if (music != null) {
            music.setVolume(this.volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return music != null && music.isPlaying();
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void dispose() {
        stop();
    }

    private String normalize(String path) {
        if (path == null) {
            return null;
        }

        String normalized = path.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
