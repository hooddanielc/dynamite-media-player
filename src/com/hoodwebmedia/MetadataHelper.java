package com.hoodwebmedia;

import java.io.File;
import java.io.IOException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 * @author dhood
 */
public class MetadataHelper {
    public AudioFile audioFile;
    public Tag tag;
    public AudioHeader audioHeader;
    
    // audio header info
    String bitRate;
    String bitsPerSample;
    String trackLength;
    String encodingType;
    String format;
    String sampleRate;
    
    
    // tag info
    String title;
    String artist;
    String album;
    String album_artist;
    String track;
    String year;
    String composer;
    String diskNumber;
    String genre;
    
    public MetadataHelper(File f, Boolean debug) throws CannotReadException, 
                                              IOException, 
                                              TagException, 
                                              ReadOnlyFileException, 
                                              InvalidAudioFrameException {
        audioFile = AudioFileIO.read(f);

        tag = audioFile.getTag();
        audioHeader = audioFile.getAudioHeader();
        
        fillMetaInfo();
        fillHeaderInfo();
        
        if(debug) {
            printResults();
        }
    }
    
    private void fillMetaInfo() {
        if(tag == null) {
            title = audioFile.getFile().getName();
            artist = "";
            album = "";
            album_artist = "";
            track = "";
            composer = "";
            year = "";
            diskNumber = "";
            genre = "";
        } else {
            title = tag.getFirst(FieldKey.TITLE);
            if(title.equals("")) {
                title = audioFile.getFile().getName();
            }
            artist = tag.getFirst(FieldKey.ARTIST);
            album = tag.getFirst(FieldKey.ALBUM);
            album_artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            track = tag.getFirst(FieldKey.TRACK);
            composer = tag.getFirst(FieldKey.COMPOSER);
            year = tag.getFirst(FieldKey.YEAR);
            diskNumber = tag.getFirst(FieldKey.DISC_NO);
            genre = tag.getFirst(FieldKey.GENRE);
        }
    }
    
    private void fillHeaderInfo() {
        // audio header info
        bitRate = audioHeader.getBitRate();
        bitsPerSample = Integer.toString(audioHeader.getBitsPerSample());
        trackLength = Integer.toString(audioHeader.getTrackLength());
        encodingType = audioHeader.getEncodingType();
        format = audioHeader.getFormat();
        sampleRate = audioHeader.getSampleRate();
    }
    
    // TODO Write function for writing meta data. JAudioTagger includes this.
    
    public void printResults() {
        System.out.println("\n<-- ALL TAG INFO -->");
        System.out.println("ARTIST: " + artist);
        System.out.println("TITLE: " + title);
        System.out.println("ALBUM: " + album);
        System.out.println("ALBUM ARTIST: " + album_artist);
        System.out.println("TRACK: " + track);
        System.out.println("COMPOSER: " + composer);
        System.out.println("YEAR: " + year);
        System.out.println("DISK NO: " + diskNumber);
        
        System.out.println("\n<-- ALL AUDIO HEADERS -->");
        System.out.println("BIT RATE: " + audioHeader.getBitRate());
        System.out.println("BITS PER SAMPLE: " + audioHeader.getBitsPerSample());
        System.out.println("TRACK LENGTH: " + audioHeader.getTrackLength());
        System.out.println("ENCODING TYPE: " + audioHeader.getEncodingType());
        System.out.println("FORMAT: " + audioHeader.getFormat());
        System.out.println("SAMPLE RATE: " + audioHeader.getSampleRate());
    }
}
