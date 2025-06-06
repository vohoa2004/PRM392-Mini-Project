package com.group6.miniproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Singleton class to manage audio playback across all activities
 */
public class AudioManager {
    private static AudioManager instance;
    private MediaPlayer mediaPlayer;
    private int currentMusic = -1;
    private int volume = 100;

    // Music resource IDs
    public static final int BACKGROUND_MUSIC = R.raw.s;
    public static final int ROUND_MUSIC = R.raw.round;
    public static final int ENDING_MUSIC = R.raw.ending;
    public static final int BETTING_MUSIC = R.raw.betting;

    // Private constructor to prevent direct instantiation
    private AudioManager() {
    }

    /**
     * Get the singleton instance
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Play music
     */
    public void playMusic(Context context, int musicRes, boolean looping, boolean forceRestart) {
        try {
            // If the same music is already playing and we're not forcing a restart, do nothing
            if (currentMusic == musicRes && mediaPlayer != null && mediaPlayer.isPlaying() && !forceRestart) {
                return;
            }

            // Release any existing MediaPlayer
            releaseMediaPlayer();

            // Create and set up the new MediaPlayer
            mediaPlayer = MediaPlayer.create(context, musicRes);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(looping);
                setVolume(volume);
                mediaPlayer.start();
                currentMusic = musicRes;
            }
        } catch (Exception e) {
            Log.e("AudioManager", "Error playing music", e);
        }
    }

    /**
     * Set the volume level
     */
    public void setVolume(int volumeLevel) {
        this.volume = volumeLevel;
        if (mediaPlayer != null) {
            float volumeFloat = volumeLevel / 100f;
            mediaPlayer.setVolume(volumeFloat, volumeFloat);
        }
    }

    /**
     * Pause the music playback
     */
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                Log.e("AudioManager", "Error pausing music", e);
            }
        }
    }

    /**
     * Resume the music playback
     */
    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                Log.e("AudioManager", "Error resuming music", e);
            }
        }
    }

    /**
     * Release MediaPlayer resources
     */
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("AudioManager", "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
    }
    
    /**
     * Check if a specific music is currently playing
     * @param musicResource the music resource ID to check
     * @return true if the specified music is playing, false otherwise
     */
    public boolean isPlayingMusic(int musicResource) {
        return currentMusic == musicResource && mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    /**
     * Get the current music resource ID
     * @return the resource ID of the current music, or -1 if no music
     */
    public int getCurrentMusic() {
        return currentMusic;
    }
    
    /**
     * Check if any music is currently playing
     * @return true if any music is playing, false otherwise
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
} 