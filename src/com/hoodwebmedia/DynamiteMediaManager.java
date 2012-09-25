package com.hoodwebmedia;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

/**
 *
 * @author dhood
 */
public class DynamiteMediaManager {
    public Connection dbConnection;
    Statement s;
    PreparedStatement audioFileInsert;
    PreparedStatement playlistInsert;
    PreparedStatement playlistFileInsert;
    
    public void initiateStorage() {
        try {
            // We load the driver once and only once.
            // We set up connection for badass jazz
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            String dbConnect = "jdbc:derby:dynamite_db;create=true";
            dbConnection = DriverManager.getConnection(dbConnect);
            dbConnection.setAutoCommit(false);
            ResultSet rs = dbConnection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            s = dbConnection.createStatement();
            if(!rs.next()) {
                s.execute("CREATE TABLE audio_files("
                        + "file_location VARCHAR(400),"
                        + "title VARCHAR(100),"
                        + "artist VARCHAR(100),"
                        + "album VARCHAR(100),"
                        + "album_artist VARCHAR(100),"
                        + "track INT,"
                        + "track_year INT,"
                        + "composer VARCHAR(100),"
                        + "disk_number INT,"
                        
                        + "bit_rate INT,"
                        + "bits_per_sample INT,"
                        + "track_length INT,"
                        + "encoding_type VARCHAR(100),"
                        + "format VARCHAR(100),"
                        + "sample_rate INT,"
                        + "genre VARCHAR(100)"
                        + ")");
                s.execute("CREATE TABLE audio_playlist("
                        + "playlist_name VARCHAR(100),"
                        + "create_date VARCHAR(100)"
                        + ")");
                s.execute("CREATE TABLE audio_playlist_files("
                        + "playlist_name VARCHAR(100),"
                        + "file_location VARCHAR(400)"
                        + ")");
                dbConnection.commit();
            }
            audioFileInsert = dbConnection.prepareStatement(
                        "insert into audio_files values("
                        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    );
            playlistInsert = dbConnection.prepareStatement("insert into "
                    + "audio_playlist values(?, ?)");
            playlistFileInsert = dbConnection.prepareStatement("insert into "
                    + "audio_playlist_files values(?, ?)");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // AUDIO PLAYLIST //
    /////////////////////////////////////////////////////////////
    public String addPlaylist(String name, Boolean doCommit) {
        String result = null;
        try {
            ResultSet playlistExists = s.executeQuery("SELECT playlist_name FROM "
                    + "audio_playlist WHERE playlist_name = '" + name + "'");
            if(!playlistExists.next()) {
                playlistInsert.setString(1, name);
                playlistInsert.setString(2, getDateNow());
                playlistInsert.executeUpdate();
                dbConnection.commit();
            } else {
                result = "Playlist already added";
            }
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public ResultSet getPlaylist(String playlist_name) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_playlist WHERE "
          + "playlist_name='" + playlist_name + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public String removePlaylist(String playlistName) {
        String result = null;
        try {
            s.execute("DELETE FROM audio_playlist_files WHERE playlist_name='"
                    + playlistName + "'");
            s.execute("DELETE FROM audio_playlist WHERE playlist_name='"
                    + playlistName + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public String addToPlaylist(String playlistName, String fileLocation) {
        String result = null;
        try {
            ResultSet rs_playlist = s.executeQuery("SELECT playlist_name FROM audio_playlist "
                    + "WHERE playlist_name = '" + playlistName + "'");
            if(rs_playlist.next()) {
                ResultSet rs_file = s.executeQuery("SELECT file_location FROM "
                        + "audio_files WHERE file_location = '" + fileLocation + "'");
                if(rs_file.next()) {
                    ResultSet rs_audio_playlist = s.executeQuery("SELECT * "
                            + "FROM audio_playlist_files WHERE file_location"
                            + "='" + fileLocation + "' AND playlist_name='"
                            + playlistName + "'");
                    if(!rs_audio_playlist.next()) {
                        playlistFileInsert.setString(1, playlistName);
                        playlistFileInsert.setString(2, fileLocation);
                        playlistFileInsert.executeUpdate();
                        dbConnection.commit();
                    } else {
                        return "File already exists in playlist";
                    }
                } else {
                    return "File doesn't exist";
                }
            } else {
                return "Playlist doesn't exist";
            }
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public String removeFileFromPlaylist(String playlist_name, String file_location) {
        String result = null;
        try {
            s.execute("DELETE FROM audio_playlist_files WHERE "
                    + "file_location='" + file_location + "' AND "
                    + "playlist_name='" + playlist_name + "'");
            dbConnection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public ResultSet getFilesFromPlaylist(String playlist_name) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files "
                    + "INNER JOIN audio_playlist_files "
                    + "ON audio_files.file_location="
                    + "audio_playlist_files.file_location "
                    + "WHERE audio_playlist_files.playlist_name='" + playlist_name + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    // AUDIO FILES //
    /////////////////////////////////////////////////////////////
    public String addAudioFile(File f, Boolean doCommit) {
        String result = null;
        try {
            ResultSet isAlreadyAdded = s.executeQuery("SELECT * FROM audio_files "
                    + "WHERE file_location = '" + f.getAbsolutePath() + "'");
            if(!isAlreadyAdded.next()) {
                MetadataHelper meta = new MetadataHelper(f, false);
                audioFileInsert.setString(1, f.getAbsolutePath());
                audioFileInsert.setString(2, meta.title);
                audioFileInsert.setString(3, meta.artist);
                audioFileInsert.setString(4, meta.album);
                audioFileInsert.setString(5, meta.album_artist);
                try {
                    audioFileInsert.setInt(6, Integer.parseInt(meta.track));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(6, 0);
                }
                try {
                    audioFileInsert.setInt(7, Integer.parseInt(meta.year));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(7, 0);
                }
                audioFileInsert.setString(8, meta.composer);
                try {
                    audioFileInsert.setInt(9, Integer.parseInt(meta.diskNumber));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(9, 0);
                }
                try {
                    audioFileInsert.setInt(10, Integer.parseInt(meta.bitRate));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(10, 0);
                }
                try {
                    audioFileInsert.setInt(11, Integer.parseInt(meta.bitsPerSample));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(11, 0);
                }
                try {
                    audioFileInsert.setInt(12, Integer.parseInt(meta.trackLength));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(12, 0);
                }
                audioFileInsert.setString(13, meta.encodingType);
                audioFileInsert.setString(14, meta.format);
                try {
                    audioFileInsert.setInt(15, Integer.parseInt(meta.sampleRate));
                } catch(NumberFormatException ex) {
                    audioFileInsert.setInt(15, 0);
                }
                audioFileInsert.setString(16, meta.genre);
                audioFileInsert.executeUpdate();
                if(doCommit) {
                    dbConnection.commit();
                }
            } else {
                result = "File Already Added";
            }
        } catch (SQLException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        } catch (CannotReadException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        } catch (IOException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        } catch (TagException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        } catch (ReadOnlyFileException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        } catch (InvalidAudioFrameException ex) {
            //Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        }
        return result;
    }
    
    public void removeAudioFile(String f_location) {
        try {
            s.execute("DELETE FROM audio_playlist_files WHERE file_location"
                    + "='" + f_location + "'");
            s.execute("DELETE FROM audio_files "
                    + "WHERE file_location='" + f_location + "'");
            dbConnection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ResultSet getAllAudioFiles() {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files");    
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getAllAudioFilesSearch(String searchString) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files WHERE "
                    + "artist LIKE '%" + searchString + "%' OR "
                    + "title LIKE '%" + searchString + "%' OR "
                    + "album LIKE '%" + searchString + "%' "
                    + "ORDER BY artist");    
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getAudioFile(String f_location) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files WHERE file_location = '" + f_location + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getDistinctArtist() {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT DISTINCT artist FROM audio_files ORDER BY artist ASC");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getFilesFromArtist(String artist) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files WHERE artist='"+
                    escapeString(artist) + "' ORDER BY artist ASC");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getFilesFromAlbum(String artist, String album) {
        ResultSet rs = null;
        try {
            rs = s.executeQuery("SELECT * FROM audio_files WHERE artist='"+
                    escapeString(artist) + "' AND "
                    + "album='"+ escapeString(album) +"' ORDER BY artist ASC");
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public ResultSet getDistinctAlbumsByArtist(String artist) {
        ResultSet rs = null;
        try {
            String query = "SELECT DISTINCT album FROM audio_files WHERE "
                    + "artist='" + escapeString(artist) + "' ORDER BY album ASC";
            rs = s.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public String escapeString(String string) {
        return string.replace("'", "''")
                .replace("\"", "\"\"");
    }
    
     
    
    
    
    
    
    
    
    
    
    
    
    
    
    // Miscellaneous help 
    /////////////////////////////////////////
    public static String  getDateNow() {
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter= 
        new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        String dateNow = formatter.format(currentDate.getTime());
        return dateNow;
    }
    
    /* ==================================================
     * ==================================================
     * ==================================================
     * For testing... ================================ */
    public void printDatabase() {
        try {
            ResultSet rs = s.executeQuery("SELECT * FROM audio_files");
            while(rs.next()) {
                System.out.println("FILE LOCATION: " + rs.getString(1));
                System.out.println("TITLE: " + rs.getString(2));
                System.out.println("ARTIST: " + rs.getString(3));
                System.out.println("ALBUM: " + rs.getString(4));
                System.out.println("ALBUM ARTIST: " + rs.getString(5));
                System.out.println("TRACK: " + rs.getString(6));
                System.out.println("YEAR: " + rs.getString(7));
                System.out.println("COMPOSER: " + rs.getString(8));
                System.out.println("DISK NUMBER: " + rs.getString(9));
                System.out.println("BIT RATE: " + rs.getString(10));
                System.out.println("BITS PER SAMPLE: " + rs.getString(11));
                System.out.println("TRACK LENGTH: " + rs.getString(12));
                System.out.println("ENCODING TYPE: " + rs.getString(13));
                System.out.println("FORMAT: " + rs.getString(14));
                System.out.println("SAMPLE RATE: " + rs.getString(15));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void resetDatabase() {
        try {
            s.execute("drop table audio_files");
            s.execute("drop table audio_playlist");
            s.execute("drop table audio_playlist_files");
            dbConnection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(DynamiteMediaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
