import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class Tools {
    
    /**
     * Copies a file in the working directory to given commit ID (hash) folder.
     */
    public static void copyToCommit(String fileName, String hash) {
        try {
            String[] folders = fileName.split("/");
            String folderToMake = "";
            for (int u = 0; u < folders.length - 1; u++) {
                folderToMake += ("/" + folders[u]);
                File newFolder = new File(".gitlet" + File.separator + hash + File.separator
                        + folderToMake);
                if (!newFolder.exists()) {
                    newFolder.mkdir();
                }
            }
            Files.copy(new File(fileName).toPath(), new File(".gitlet" + File.separator + hash
                    + File.separator + fileName).toPath(), 
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Copies a file in given commit ID (hash) to working directory.
     */
    public static void copyToDirectory(String fileName, String hash) {
        try {
            String[] folders = fileName.split("/");
            String folderToMake = folders[0];
            for (int u = 1; u < folders.length - 1; u++) {
                folderToMake += ("/" + folders[u]);
                File newFolder = new File(".gitlet" + File.separator + hash + File.separator
                        + folderToMake);
                if (!newFolder.exists()) {
                    newFolder.mkdir();
                }
            }
            Files.copy(new File(".gitlet" + File.separator + hash + File.separator
                    + fileName).toPath(), new File(fileName).toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Copies file in given commit ID (hash) to working directory as .conflicted. For use in merge.
     */
    public static void copyToDirectoryConflicted(String fileName, String hash) {
        try {
            String[] folders = fileName.split("/");
            String folderToMake = folders[0];
            for (int u = 1; u < folders.length - 1; u++) {
                folderToMake += ("/" + folders[u]);
                File newFolder = new File(".gitlet" + File.separator + hash + File.separator
                        + folderToMake);
                if (!newFolder.exists()) {
                    newFolder.mkdir();
                }
            }
            Files.copy(new File(".gitlet" + File.separator + hash + File.separator
                    + fileName).toPath(), new File(fileName + ".conflicted").toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();    
        }
    }
    
    /**
     * Checks to see if a file in the given commit ID (hash) has been changed from the
     * same file in the working directory.
     * Returns true if file exists in commit, or if file has been changed.
     */
    public static boolean checkFileChanged(String fileName, String hash) {
        File committedFile = new File(".gitlet" + File.separator + hash + File.separator
                + new File(fileName));
        File newFile = new File(fileName);
        if (!(committedFile.exists())) {
            return true;
        }
        boolean fileChanged = false;
        try {
            FileReader fileReader1 = new FileReader(committedFile);
            FileReader fileReader2 = new FileReader(newFile); 
            
            BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
            BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
            
            while (!fileChanged) {
                String committedFileLine = bufferedReader1.readLine();
                String newFileLine = bufferedReader2.readLine();
                if (committedFileLine == null && newFileLine == null) {
                    break;
                } else if (committedFileLine == null && newFileLine != null) {
                    fileChanged = true;
                } else if (committedFileLine != null && newFileLine == null) {
                    fileChanged = true;
                } else if (!(committedFileLine).equals(newFileLine)) {
                    fileChanged = true;
                }
            }
            bufferedReader1.close();
            bufferedReader2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileChanged;
    }
}
