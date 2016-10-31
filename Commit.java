import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;


@SuppressWarnings("serial")
public class Commit implements Serializable {
    private final String message;
    private final Date date;
    private final String hashCode;
    private HashMap<String, String> files;
    private HashSet<String> newFiles;
    private HashSet<String> removedFiles;
    private Commit parent;
    
    private final int hashNum = 31;
    
    /**
     * Initiates all class variables.
     * Inherits all files from parent.
     * Adds a files in newFiles, then removes all files in removeFiles.
     */
    public Commit(String message, HashSet<String> newFiles, 
            HashSet<String> removeFiles, Commit parent) {
        this.message = message;
        this.parent = parent;
        this.date = new Date();
        this.files = new HashMap<String, String>();
        this.newFiles = new HashSet<String>();
        this.removedFiles = new HashSet<String>();
        
        if (parent != null) {
            this.files.putAll(parent.getFiles());
        }
        
        this.newFiles.addAll(newFiles);
        this.removedFiles.addAll(removeFiles);
        this.hashCode = genHash();
        
        for (String item : newFiles) {
            files.put(item, getHash());
        }
        for (String item : removeFiles) {
            files.remove(item);
        }
    }
    
    /**
     * Returns message for this commit.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns time formatted for use in log and global-log methods (yyyy-MM-dd hh:mm:ss)
     */
    public String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
    }
    
    /**
     * Returns parent of this commit.
     */
    public Commit getParent() {
        return parent;
    }
    
    /**
     * Returns a map of all files in this commit.
     * Key = fileName, Value = commit ID containing the file.
     */
    public HashMap<String, String> getFiles() {
        return files;
    }
    
    /**
     * Returns a list of all new files added to this commit.
     */
    public HashSet<String> getNewFiles() {
        return newFiles;
    }
    
    /**
     * Returns a list of all files that were removed from this commit from the previous commit.
     */
    public HashSet<String> getRemovedFiles() {
        return removedFiles;
    }
    
    /**
     * Adds a file to the commit, stored in the given commit ID (hash)
     */
    public void addNewFile(String fileName, String hash) {
        files.put(fileName, hash);
        newFiles.add(fileName);
    }
    
    /**
     * Generates a SHA-256 hash, based on parent's hashcode, time, and message.
     * Referenced in part from online for how to calculate SHA-256 hashcodes, 
     * but wrote code on my own, Eclipse helped me make try/catch blocks
     */
    private String genHash() { 
        long hCode = 1;
        hCode = hashNum * hCode + (parent == null ? 0 : parent.hashCode());
        hCode = hashNum * hCode + date.hashCode();
        hCode = hashNum * hCode + message.hashCode();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(hCode).getBytes("UTF-8"));
            Formatter formatter = new Formatter();
            for (int i = 0; i < hash.length; i++) {
                formatter.format("%02x", hash[i]);
            }
            String gennedHash = formatter.toString();
            formatter.close();
            return gennedHash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Failed to calculate hash.";
    }
    
    /**
     * Returns the saved hashCode.
     */
    public String getHash() {
        return hashCode;
    }
    
    /**
     * Changes the parent to the given parent. For use in rebase
     * when making new commits.
     */
    public void rebaseParent(Commit newParent) {
        this.parent = newParent;
    }
}
