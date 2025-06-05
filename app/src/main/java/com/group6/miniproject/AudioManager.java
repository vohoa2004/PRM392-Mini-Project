package com.group6.miniproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

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

    // Private constructor to prevent direct instantiation
    private AudioManager() {
    }

    /**
     * Get the singleton instance
     * @return AudioManager instance
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Initialize or change the music
     * @param context Context to create MediaPlayer
     * @param musicRes Resource ID of the music
     * @param looping Whether the music should loop
     * @param forceRestart Whether to restart even if same music is playing
     */
    public void playMusic(Context context, int musicRes, boolean looping, boolean forceRestart) {
        try {
            // If the same music is already playing and we're not forcing a restart, do nothing
            if (currentMusic == musicRes && mediaPlayer != null && mediaPlayer.isPlaying() && !forceRestart) {
                return;
            }

            // Check if music is already loaded but not playing (might be paused)
            if (currentMusic == musicRes && mediaPlayer != null && !mediaPlayer.isPlaying() && !forceRestart) {
                mediaPlayer.start();
                return;
            }

            // Release any existing MediaPlayer
            releaseMediaPlayer();

            // Create and set up the new MediaPlayer
            mediaPlayer = MediaPlayer.create(context, musicRes);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(looping);
                setVolume(volume);

                // Set completion listener for non-looping tracks
                if (!looping) {
                    mediaPlayer.setOnCompletionListener(mp -> {
                        // When non-looping music ends, start background music again
                        if (musicRes != BACKGROUND_MUSIC) {
                            playMusic(context, BACKGROUND_MUSIC, true, true);
                        }
                    });
                }

                mediaPlayer.start();
                currentMusic = musicRes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set the volume level
     * @param volumeLevel Volume level (0-100)
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
            mediaPlayer.pause();
        }
    }

    /**
     * Resume the music playback
     */
    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * Release the MediaPlayer resources
     */
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
} 