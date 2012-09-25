package com.hoodwebmedia;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 *
 * @author dhoodlum
 */
public class FileHelper {
    public static ArrayList recursionListFiles = new ArrayList();
    final public static String APPTITLE = "Dynamite";
    
    public static String fixFileUri(String pathToFix) {
        return pathToFix.replace("\\", "/")
                        .replace("%", "%25")
                        .replace("[", "%5B")
                        .replace("]", "%5D")
                        .replace("`", "%60")
                        .replace(" ", "%20")
                        .replace("{", "%7B")
                        .replace("}", "%7D")
                        .replace("&", "%26")
                        .replace("^", "%5E")
                        .replace("#", "%23");
    }
    
    /*
     * Creates a new file in app directory with given 
     * name and extension. Will not overwrite 
     * existing file
     */
    public static File createFile(String f_name) {
        File f;
        f = new File(getLocalStorage() + f_name);
        if(!f.exists()){
            try {
                new File(getLocalStorage()).mkdirs();
                f.createNewFile();
            } catch (IOException ex) {
                //Logger.getLogger(AppProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return f;        
    }
    
    public static String readFile(String filePath){
        String data = "";
        String line;
        try {
            FileReader input = new FileReader(filePath);
            BufferedReader bufRead = new BufferedReader(input);
            line = bufRead.readLine();
            while (line != null){
                data += line;
                line = bufRead.readLine();
            }
            bufRead.close();
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Usage: java ReadFile filename\n");
	}catch (IOException e){
            e.printStackTrace();
        }
        
        return data;
    }
    
    public static void writeToFile(String filePath, String txt, Boolean append) {
        try{
            // Create file 
            FileWriter fstream = new FileWriter(filePath, append);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(txt);
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /* Local storage locater --------------------------------------------------
     *  -----------------------------------------------------------------------
     * Gets the local application storage directory.
     * This will be different on different os's, user accounts
     */
    public static String getLocalStorage() {
        URL url = null;
        String user_dir = "file:" + System.getProperty("user.home");
        try {
            if (isWindows()) {
                url = new URL(user_dir + "/AppData/Roaming/." + APPTITLE);
            } else if (isMac()) {
                url = new URL(user_dir + "/Library/." + APPTITLE);
            } else if (isUnix()) {
                url = new URL(user_dir + "/." + APPTITLE);
            } else if (isSolaris()) {
                url = new URL(user_dir + "/." + APPTITLE);
            } else {
                url = new URL(user_dir + "/." + APPTITLE);
            }
        } catch (MalformedURLException ex) {
            try {
                url = new URL(user_dir + "/." + APPTITLE);
            } catch (MalformedURLException ex1) {
                return null;
            }
        }
        return url.getPath() + "/";
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    public static boolean isSolaris() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("sunos") >= 0);
    }
}
