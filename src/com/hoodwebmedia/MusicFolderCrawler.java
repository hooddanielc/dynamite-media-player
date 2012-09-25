package com.hoodwebmedia;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;

public class MusicFolderCrawler {
    private File musicFolder;
    private int dirsInProgress = 0;
    private int dirsDone = 0;
    private int checkedFiles = 0;
    
    private ArrayList<OnProgressListener> onProgressListeners
            = new ArrayList<OnProgressListener>();
    private ArrayList<OnCompleteListener> onCompleteListeners
            = new ArrayList<OnCompleteListener>();
    
    public ArrayList<File> recursionListFiles = new ArrayList<File>();
    public ArrayList<File> supportedListFiles = new ArrayList<File>();
    
    final public static int FILESCRAWLED = 0;
    final public static int FILESCHECKED = 1;
    
    public MusicFolderCrawler(File folder) {
        musicFolder = folder;
    }
    
    public void start() {
        visitAllFiles(musicFolder);
    }

    // Process only files under dir
    private void visitAllFiles(File dir) {
        if (dir.isDirectory()) {
            dirsInProgress++;
            File[] children = dir.listFiles();
            for (int i=0; i<children.length; i++) {
                File f = children[i];
                try {
                    if(!f.getCanonicalFile().isDirectory()) {
                        recursionListFiles.add(f);
                        for(OnProgressListener l : onProgressListeners) {
                            l.onProgress(FILESCRAWLED, 0, 
                                    recursionListFiles.size());
                        }
                    } else {
                        visitAllFiles(f);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MusicFolderCrawler.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        } else {
            recursionListFiles.add(dir);
        }
        dirsDone++;
        if(dirsDone == dirsInProgress) {
            callOnProgressCheckSupportedFiles();
        }
    }
    // RECURSION end
    
    
    // Interfaces (Progress Listeners)
    
    public void callOnProgressCheckSupportedFiles() {
        for(File files : recursionListFiles) {
            try {
                Media media = new Media("file:///" 
                        + FileHelper.fixFileUri(files.getPath()));
                String name = files.getName();
                int pos = name.lastIndexOf(".");
                if(!name.substring(pos + 1).toLowerCase().equals("wav")) {
                    // We can't support wav files
                    // because jAudioTagger doesn't
                    supportedListFiles.add(files);
                }
            } catch (MediaException ex) {
                // eat that shit
            }
            checkedFiles++;
            for(OnProgressListener l : onProgressListeners) {
                l.onProgress(FILESCHECKED, supportedListFiles.size(), 
                        recursionListFiles.size());
            }
        }
        callOnComplete();
    }
    
    public void callOnComplete() {
        for(OnCompleteListener listener : onCompleteListeners) {
            listener.onComplete(supportedListFiles);
        }
    }
    
    public interface OnProgressListener {
        public abstract void onProgress(int type, int progress, int total);
    }
    
    public interface OnCompleteListener {
        public abstract void onComplete(ArrayList<File> files);
    }
    
    public void addProgressListener(OnProgressListener l) {
        onProgressListeners.add(l);
    }
    
    public void addCompleteListener(OnCompleteListener l) {
        onCompleteListeners.add(l);
    }
}