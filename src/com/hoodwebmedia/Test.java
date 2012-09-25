package com.hoodwebmedia;

import com.hoodwebmedia.MusicFolderCrawler.OnCompleteListener;
import com.hoodwebmedia.MusicFolderCrawler.OnProgressListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import junit.framework.TestCase;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

/**
 *
 * @author dhood
 */
public class Test extends TestCase {
    
    final private String testDir = "/Users/dhoodlum/Documents/git/dynamite/"
            + "lib/testdata";
    final private String testFile1 = testDir + "/test.m4a";
    
    public void testDynamiteMediaManager() {
        DynamiteMediaManager dmm = new DynamiteMediaManager();
        // Initialize and de-initialize
        dmm.initiateStorage();
        dmm.resetDatabase();
        dmm.initiateStorage();
        
        // Adding a file to library
        
        File file = new File(testFile1);
        if(!file.exists()) {
            fail("The test can't run if the test file doesn't exist.");
        }
        System.out.println("Well I guess it's working right?");
        dmm.addAudioFile(file, true);
        ResultSet rs = dmm.getAudioFile(file.getAbsolutePath());
        try {
            if(rs.next()) {
                assertEquals(file.getAbsolutePath(), rs.getString(1));
            } else {
                fail("TEST FAILED!!!! We couldn't access file in database"
                        + " after adding it!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Removing audio file
        dmm.removeAudioFile(file.getAbsolutePath());
        
        ResultSet rs2 = dmm.getAllAudioFiles();
        try {
            if(rs2.next()) {
                fail("FAILURE!!!! We just deleted the only file in the database."
                        + " Why is it still there...");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Add the file back into the database
        dmm.addAudioFile(file, true);
        
        // Add a playlist and see if you can select it.
        dmm.addPlaylist("Playlist 1", true);
        
        ResultSet rs3 = dmm.getPlaylist("Playlist 1");
        try {
            if(!rs3.next()) {
                fail("Could not find playlist after adding it...");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // add a file to a playlist
        dmm.addToPlaylist("Playlist 1", file.getAbsolutePath());
        
        // Try to retrieve the playlist files
        ResultSet rs4 = dmm.getFilesFromPlaylist("Playlist 1");
        try {
            if(!rs4.next()) {
                fail("Couldn't get file from playlist after adding it"
                        + " to the playlist.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Remove file from the playlist
        dmm.removeFileFromPlaylist("Playlist 1", file.getAbsolutePath());
        
        ResultSet rs5 = dmm.getFilesFromPlaylist("Playlist 1");
        try {
            if(rs5.next()) {
                fail("The playlist shouldn't have any files in the playlist");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        dmm.addToPlaylist("Playlist 1", file.getAbsolutePath());
        dmm.addToPlaylist("Playlist 1", file.getAbsolutePath());
        
        
        ResultSet rs6 = dmm.getFilesFromPlaylist("Playlist 1");
        try {
            int size = 0;
            while(rs6.next()) {
                size++;
            }
            if(size != 1) {
                fail("There should only be one row");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Testing removal of 
        dmm.removeFileFromPlaylist("Playlist 1", file.getAbsolutePath());
        
        ResultSet rs7 = dmm.getFilesFromPlaylist("Playlist 1");
        int size = 0;
        try {
            while(rs7.next()) {
                size++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(size != 0) {
            fail("There shouldn't be any files in this playlist anymore");
        }
        
        // Testing proper removal from playlist when removing file completely
        dmm.addPlaylist("Playlist 2", true);
        dmm.addToPlaylist("Playlist 1", file.getAbsolutePath());
        dmm.addToPlaylist("Playlist 2", file.getAbsolutePath());
        
        
        dmm.removeAudioFile(file.getAbsolutePath());
        
        
        
        try {
            int size1 = 0;
            ResultSet rs8 = dmm.getFilesFromPlaylist("Playlist 1");
            while(rs8.next()) {
                size1++;
            }
            ResultSet rs9 = dmm.getFilesFromPlaylist("Playlist 2");
            while(rs9.next()) {
                size1++;
            }
            if(size1 != 0) {
                fail("FAILURE, Removing file from library should remove it "
                        + "from playlist as well.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Lets test removal of playlist and see if it removed playlist files..
        dmm.addToPlaylist("Playlist 1", file.getAbsolutePath());
        dmm.addToPlaylist("Playlist 2", file.getAbsolutePath());
        
        dmm.removePlaylist("Playlist 1");
        dmm.removePlaylist("Playlist 2");
        
        ResultSet rs10 = dmm.getFilesFromPlaylist("Playlist 1");
        try {
            if(rs10.next()) {
                fail("There shouldn't be any rows from this result set...");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("If you didn't get an error, you passed all test.");
    }
    
    public void testMusicFolderCrawler() {
        // These test are meant for observation only.
        // There are no unit test for MusicFolderCrawler
        File file = new File(testDir);
        MusicFolderCrawler mfc = new MusicFolderCrawler(file);
        
        mfc.addProgressListener(new OnProgressListener() {
            @Override
            public void onProgress(int type, int progress, int total) {
                if(type == MusicFolderCrawler.FILESCRAWLED) {
                    System.out.println("Progress : " + progress);
                    System.out.println("Total : " + total);
                } else if(type == MusicFolderCrawler.FILESCHECKED) {
                    System.out.println("Progress : " + progress);
                    System.out.println("Total : " + total);
                }
            }
        });
        
        mfc.addCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(ArrayList<File> files) {
                System.out.println("We are starting...");
                for(File file : files) {
                    System.out.println(file.getAbsolutePath());
                }
                
                Media media = new Media("file://" + files.get(4).getAbsolutePath());
                MediaPlayer player = new MediaPlayer(media);
                player.play();
                
                try {
                    MetadataHelper mh = new MetadataHelper(files.get(4), true);
                } catch (CannotReadException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TagException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ReadOnlyFileException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidAudioFrameException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        
        mfc.start();
    }
}