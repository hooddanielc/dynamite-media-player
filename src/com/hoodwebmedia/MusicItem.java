package com.hoodwebmedia;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class MusicItem {

    private final SimpleStringProperty name;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final SimpleStringProperty genre;
    private final SimpleStringProperty track;
    private final SimpleStringProperty time;
    private final SimpleStringProperty location;
    private final SimpleBooleanProperty checked;

    public MusicItem(String sName, String sArtist, String sAlbum,
                String sGenre, String sTrack, String sTime, String sLocation, Boolean bChecked) {
        this.name = new SimpleStringProperty(sName);
        this.artist = new SimpleStringProperty(sArtist);
        this.album = new SimpleStringProperty(sAlbum);
        this.genre = new SimpleStringProperty(sGenre);
        this.track = new SimpleStringProperty(sTrack);
        this.time = new SimpleStringProperty(sTime);
        this.location = new SimpleStringProperty(sLocation);
        this.checked = new SimpleBooleanProperty(bChecked);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String fName) {
        name.set(fName);
    }

    public String getArtist() {
        return artist.get();
    }

    public void setArtist(String fName) {
        artist.set(fName);
    }

    public String getAlbum() {
        return album.get();
    }

    public void setAlbum(String fName) {
        album.set(fName);
    }
    
    public String getGenre() {
        return genre.get();
    }

    public void setGenre(String fName) {
        genre.set(fName);
    }
    
    public String getTrack() {
        return track.get();
    }

    public void setTrack(String fName) {
        track.set(fName);
    }
    
    public String getTime() {
        return time.get();
    }

    public void setTime(String fName) {
        time.set(fName);
    }
    
    public String getLocation() {
        return location.get();
    }

    public void setLocation(String fName) {
        time.set(fName);
    }
    
    public boolean getChecked() {
        return checked.get();
    }

    public void setChecked(boolean bChecked) {
        checked.set(bChecked);
    }
}