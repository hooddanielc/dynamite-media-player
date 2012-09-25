package com.hoodwebmedia;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.EventHandler;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.util.Pair;

public class DynamiteMediaPlayer {
    private ArrayList<OnTrackEndListener> onTrackEndListeners = new ArrayList<OnTrackEndListener>();
    private ArrayList<OnTrackStartListener> onTrackStartListeners = new ArrayList<OnTrackStartListener>();
    private ArrayList<OnTrackPauseListener> onTrackPauseListeners = new ArrayList<OnTrackPauseListener>();
    private ArrayList<OnTrackProgressListener> onTrackProgressListeners = new ArrayList<OnTrackProgressListener>();
    private ScheduledExecutorService ses;
    public MediaPlayer mediaPlayer;
    public Media currentMedia;
    public String currentFilePath;
    private boolean playing = false;
    private double volume = 1.0;
    
    public DynamiteMediaPlayer() {
        ses = Executors.newSingleThreadScheduledExecutor();
        Runnable periodicTask = new Runnable() {
            public void run() {
                if(playing) {
                    for(OnTrackProgressListener l : onTrackProgressListeners) {
                        l.onProgress(mediaPlayer.getCurrentTime().toSeconds(), 
                                mediaPlayer.getStopTime().toSeconds());
                    }
                }
            }
        };
        ses.scheduleAtFixedRate(periodicTask, 0, 500, TimeUnit.MILLISECONDS);
    }
    
    public void playFile(String file) {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(file);
        currentMedia = media;
        currentFilePath = file;
        mediaPlayer = new MediaPlayer(currentMedia);
        mediaPlayer.setVolume(volume);
        mediaPlayer.play();
        
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                playing = false;
                for(OnTrackEndListener l : onTrackEndListeners) {
                    l.onEnd(currentFilePath);
                }
            }
        });
        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                playing = true;
                for(OnTrackStartListener l : onTrackStartListeners) {
                    l.onStart(currentFilePath);
                }
            }
        });
        mediaPlayer.setOnPaused(new Runnable() {
            @Override
            public void run() {
                for(OnTrackPauseListener l : onTrackPauseListeners) {
                    l.onPause(currentFilePath);
                }
                playing = false;
            }
        });
    }
    
    public void seekByPercent(double percent) {
        double currentTime = mediaPlayer.getCurrentTime().toMillis();
        double stopTime = mediaPlayer.getStopTime().toMillis();
        percent *= 0.01;
        double requestSeek = stopTime * percent;
        mediaPlayer.seek(new Duration(requestSeek));
    }
    
    public void pause() {
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    
    public void play() {
        if(mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
    
    public boolean isPlaying() {
        return playing;
    }
    
    public void setVolume(double v) {
        volume = v;
        if(mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
    
    public void shutDown() {
        ses.shutdown();
    }
    
    public void addOnTrackEndListener(OnTrackEndListener otel) {
        onTrackEndListeners.add(otel);
    }
    
    public void addOnTrackStartListener(OnTrackStartListener otsl) {
        onTrackStartListeners.add(otsl);
    }
    
    public void addOnTrackPauseListener(OnTrackPauseListener otpl) {
        onTrackPauseListeners.add(otpl);
    }
    
    public void addOnTrackProgressListener(OnTrackProgressListener otpl) {
        onTrackProgressListeners.add(otpl);
    }
    
    public interface OnTrackEndListener {
        public abstract void onEnd(String track);
    }
    public interface OnTrackStartListener {
        public abstract void onStart(String track);
    }
    public interface OnTrackPauseListener {
        public abstract void onPause(String track);
    }
    public interface OnTrackProgressListener {
        public abstract void onProgress(double currentTime, double stopTime);
    }
}