import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Git {
    
    /**
     * Takes user input and executes corresponding command.
     * Prints appropriate message if invalid command.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command."); return;
        }
        switch (args[0]) {
            case "init":
                initialize(); break;
            case "add":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                }
                add(args[1]); break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Please enter a commit message."); break;
                } 
                commit(args[1]); break;
            case "log":
                log(); break;
            case "global-log":
                globalLog(); break;
            case "status":
                status(); break;
            case "checkout":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                checkout(args); break;
            case "reset":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                }
                reset(args[1]); break;
            case "branch":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                newBranch(args[1]); break;
            case "rm":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                remove(args[1]); break;
            case "find":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                find(args[1]); break;
            case "rm-branch":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                removeBranch(args[1]); break;
            case "merge":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                merge(args[1]); break;
            case "rebase":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                rebase(args[1], false); break;
            case "i-rebase":
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments."); break;
                } 
                rebase(args[1], true); break;
            default:
                System.out.println("Unrecognized command.");
        }
    }
    
    /**
     * Initializes gitlet.
     * Creates .gitlet folder and master branch, commits empty initial commit.
     */
    private static void initialize() {
        if (new File(".gitlet").exists()) {
            System.out.println("A gitlet version control system already exists "
                    + "in the current directory."); 
            return;
        }
        new File(".gitlet").mkdir();
        Data data = new Data();        
        Branch branch = new Branch("master");
        data.addBranch(branch);
        data.setCurrBranch("master");
        saveData(data);
        
        commit("initial commit");
    }
    
    /**
     * Returns the current branch.
     */
    private static Branch getCurrBranch() {
        return loadData().getCurrBranch();
    }
    
    /**
     * Adds a file to the stage if file exists and has been modified.
     * Removes file from removed file list.
     */
    private static void add(String fileName) {
        if (!new File(fileName).exists()) {
            System.out.println("File does not exist."); return;
        }
        if (!Tools.checkFileChanged(fileName, getCurrBranch().getHead()
                .getFiles().get(fileName))) {
            System.out.println("File has not been modified since the last commit."); return;
        } 
        Data data = loadData();
        data.add(fileName);
        saveData(data);
    }
    
    /**
     * Adds a file to the removed file list and removes if staged.
     * Checks to make sure there is a reason to remove the file:
     * Most recent commit doesn't contain it, and it is not staged.
     */
    private static void remove(String fileName) {
        Data data = loadData();
        if (!getCurrBranch().getHead().getFiles().keySet().contains(fileName) 
                && !data.getStagedFiles().contains(fileName)) {
            System.out.println("No reason to remove the file."); return;
        }
        data.remove(fileName);
        saveData(data);
    }
    
    /**
     * Creates a new commit using staged files, inherited files, and message.
     */
    private static void commit(String message) {
        Data data = loadData();
        data.getCurrBranch().commit(message, data);
        saveData(data);
    }
    
    /**
     * Prints log of all previous commits in current branch history.
     */
    private static void log() {
        getCurrBranch().log();
    }
    
    /**
     * Prints log of all commits in the commit map.
     */
    private static void globalLog() {
        HashMap<String, Commit> commits = loadData().getCommits();
        for (String item : commits.keySet()) {
            System.out.println("====");
            System.out.println("Commit " + commits.get(item).getHash() + ".");
            System.out.println(commits.get(item).getTime());
            System.out.println(commits.get(item).getMessage());
            System.out.println();
        }
    }
    
    /**
     * Prints status of all current branches, list of staged files, 
     * and list of files marked for removal
     */
    private static void status() {
        System.out.println("=== Branches ===");
        for (String item : loadData().getBranches().keySet()) {
            if (item.equals(getCurrBranch().getName())) {
                System.out.print("*");
            }
            System.out.println(item);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String item : loadData().getStagedFiles()) {
            System.out.println(item);
        }
        System.out.println();
        System.out.println("=== Files Marked for Removal ===");
        for (String item : loadData().getRemoveFiles()) {
            System.out.println(item);
        }
    }
    
    /**
     * Prints all commit IDs with the given message.
     */
    private static void find(String message) {
        HashSet<String> commits = loadData().getCommitMessages().get(message);
        if (commits == null) {
            System.out.println("Found no commit with that message."); return;
        }
        for (String item : commits) {
            System.out.println(item);
        }
    }
    
    /**
     * Either checks out to a given fileName, a given branch, or
     * a given fileName in a specific commit ID.
     * Returns errors if checking out to current branch, 
     * or a commit, file, or branch doesn't exist.
     */
    private static void checkout(String[] args) {
        if (!checkDangerous()) {
            return;
        }
        if (args.length == 2 && loadData().getBranches().keySet().contains(args[1])) {
            if (args[1].equals(getCurrBranch().getName())) {
                System.out.println("No need to checkout the current branch.");
            } else {
                Data data = loadData();
                data.setCurrBranch(args[1]);
                data.getCurrBranch().reset(data.getCurrBranch().getHead());
                saveData(data);
            }
        } else {
            String fileName;
            String commitID;
            if (args.length > 2) {
                fileName = args[2];
                commitID = args[1];
                if (!loadData().getCommits().containsKey(commitID)) {
                    System.out.println("No commit with that id exists."); return;
                }
            } else {
                fileName = args[1];
                commitID = getCurrBranch().getHead().getHash();
            }
            
            if (!loadData().getCommits().get(commitID).getFiles().containsKey(fileName)) {
                if (args.length == 2) {
                    System.out.println("File does not exist in the most recent commit, "
                            + "or no such branch exists.");
                } else {
                    System.out.println("File does not exist in that commit."); 
                }
                return;
            }
            Tools.copyToDirectory(fileName, commitID);
        }
        
    }
    
    /**
     * Resets working directory and branch to a given commit ID.
     * Returns an error if commit ID doesn't exist.
     */
    private static void reset(String commitID) {
        if (!checkDangerous()) {
            return;
        }
        Data data = loadData();
        if (!data.getCommits().containsKey(commitID)) {
            System.out.println("No commit with that id exists."); return;
        }
        data.getCurrBranch().reset(data.getCommits().get(commitID));
        saveData(data);
    }
    
    /**
     * Creates a new branch with the given name.
     * Returns an error if a branch already exists with the given name.
     */
    private static void newBranch(String branchName) {
        Data data = loadData();
        if (data.getBranches().keySet().contains(branchName)) {
            System.out.println("A branch with that name already exists."); return;
        }
        data.addBranch(new Branch(branchName, getCurrBranch()));
        saveData(data);
    }
    
    /**
     * Removes the branch with the given name.
     * Returns errors if branch doesn't exist, or given branch is current branch.
     */
    private static void removeBranch(String branchName) {
        if (!loadData().getBranches().keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist."); return;
        }
        if (branchName.equals(getCurrBranch())) {
            System.out.println("Cannot remove the current branch."); return;
        }
        Data data = loadData();
        data.removeBranch(branchName);
        saveData(data);
    }
    
    /**
     * Merges current branch with given branch.
     * Changes working directory to changes in most recent commit of given branch.
     * Specific cases are detailed in Branch's merge method documentation.
     */
    private static void merge(String branchName) {
        if (!checkDangerous()) {
            return;
        }
        Data data = loadData();
        if (!data.getBranches().keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist."); return;
        }
        if (getCurrBranch().getName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself."); return;
        }
        data.getCurrBranch().merge(data.getBranches().get(branchName));
        saveData(data);
    }
    
    /**
     * Rebases current branch with given branch.
     * Copies all current commits up to split point of given branch and current branch and
     * attaches new commits to head of given branch.
     * Specific cases are detailed in Branch's rebase method documentation.
     */
    private static void rebase(String branchName, boolean isInteractive) {
        if (!checkDangerous()) {
            return;
        }
        Data data = loadData();
        if (!data.getBranches().keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist."); return;
        }
        if (getCurrBranch().getName().equals(branchName)) {
            System.out.println("Cannot rebase a branch onto itself."); return;
        }
        data.getCurrBranch().rebase(data.getBranches().get(branchName), data, isInteractive);
        saveData(data);
    }
    
    /**
     * Prompts user to ask if s/he wants to continue with potentially dangerous action.
     */
    private static boolean checkDangerous() {
        System.out.println("Warning: The command you entered may alter the files in your working "
                + "directory. Uncommitted changes may be lost. "
                + "Are you sure you want to continue? (yes/no)");
        @SuppressWarnings("resource")
        Scanner reader = new Scanner(System.in);
        String input = reader.next();
        if (input.equals("yes")) {
            return true;
        } else {
            System.out.println("Did not type 'yes', so aborting"); 
            return false;
        }
    }
    
    /**
     * Load's serialized saved data.
     */
    public static Data loadData() {
        Data data = null;
        File dataFile = new File(".gitlet" + File.separator + "Data.ser");
        if (dataFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(dataFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                data = (Data) objectIn.readObject();
                objectIn.close();
            } catch (IOException e) {
                System.out.println("IOException while loading Data.");
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFoundException while loading Data.");
            }
        }
        return data;
    }
    
    /**
     * Serializes and saves given data.
     */
    public static void saveData(Data data) {
        if (data == null) {
            return;
        }
        try {
            File dataFile = new File(".gitlet" + File.separator + "Data.ser");
            FileOutputStream fileOut = new FileOutputStream(dataFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(data);
            objectOut.close();
        } catch (IOException e) {
            System.out.println("IOException while saving Data.");
        }
    }
}
