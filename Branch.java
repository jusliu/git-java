import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


@SuppressWarnings("serial")
public class Branch implements Serializable {

    private String name;
    private Commit head;
    
    /**
     * Initializes all class variables. Sets head to null.
     * Used only in initial commit.
     */
    public Branch(String name) {
        this.name = name;
        this.head = null;
    }
    
    /**
     * Initializes all class variables. Sets head to head of parent.
     */
    public Branch(String name, Branch parent) {
        this.name = name;
        this.head = parent.getHead();
    }
    
    /**
     * Returns the name of this branch.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the head (most recent commit) of this branch.
     */
    public Commit getHead() {
        return head;
    }
    
    /**
     * Returns a list of the history of this branch from the head to the initial commit.
     */
    public ArrayList<String> getCommitHistory() {
        ArrayList<String> commits = new ArrayList<String>();
        Commit iterator = head;
        while (iterator != null) {
            commits.add(iterator.getHash());
            iterator = iterator.getParent();
        }
        return commits;
    }
    
    /**
     * Adds a commit to this branch using the given message into the given data.
     * Reads staged files, files marked for removal, and creates new commit accordingly.
     * Updates head to the new commit.
     */
    public void commit(String message, Data data) {
        HashSet<String> stagedFiles = data.getStagedFiles();
        HashSet<String> removedFiles = data.getRemoveFiles();
        
        if (stagedFiles.size() == 0 && removedFiles.size() == 0 && head != null) {
            System.out.println("No changes added to the commit."); return;
        }
        
        Commit commit = new Commit(message, stagedFiles, removedFiles, head);
        head = commit;
        data.addCommit(commit);
        data.clear();
        
        if (commit.getNewFiles().size() > 0) {
            new File(".gitlet" + File.separator + commit.getHash()).mkdir();
            for (String item : commit.getNewFiles()) {
                Tools.copyToCommit(item, commit.getHash());
            }
        }
    }
    
    /**
     * Prints a list of all commits in the current commit history of this branch.
     * Prints the commit ID, the times, and the messages of each commit.
     */
    public void log() {
        HashMap<String, Commit> commitMap = Gitlet.loadData().getCommits();
        for (String item : getCommitHistory()) {
            System.out.println("====");
            System.out.println("Commit " + item + ".");
            System.out.println(commitMap.get(item).getTime());
            System.out.println(commitMap.get(item).getMessage());
            System.out.println();
        }
    }

    /**
     * Changes the head (most recent commit) of this branch to the given commit.
     * Copies all files in new commit to working directory.
     */
    public void reset(Commit commit) {
        head = commit;
        for (String fileName : commit.getFiles().keySet()) {
            Tools.copyToDirectory(fileName, commit.getFiles().get(fileName));
        }
    }
    
    /**
     * Finds the splitting commit point of the current branch and the given branch.
     */
    private Commit findSplitPoint(Branch branch) {
        HashSet<String> findSplitPoint = new HashSet<String>();
        Commit iterator = head;
        while (iterator != null) {
            findSplitPoint.add(iterator.getHash());
            iterator = iterator.getParent();
        }
        iterator = branch.getHead();
        while (iterator != null) {
            if (findSplitPoint.contains(iterator.getHash())) {
                return iterator;
            }
            iterator = iterator.getParent();
        }
        return null;
    }
    
    /**
     * Updates working directory based on the following cases for each 
     * file in branch's most recent commit:
     * **For all following, "modified" refers to modification 
     * since the last split point of both branches
     * CASE 0: File doesn't exist in current branch's most recent 
     * commit, and has been modified in given branch
     * CASE 1: Modified in given branch, but not in current branch
     *  = CHANGE TO GIVEN BRANCH'S VERSION
     * CASE 2: Modified in current branch, but not in given branch
     *  = DO NOTHING
     * CASE 3: Modified in both = COPY BRANCH OVER AS .CONFLICTED
     * CASE 4: Neither is modified = DO NOTHING
     */
    public void merge(Branch branch) {
        Commit iterator = head;
        HashSet<String> thisNewFiles = new HashSet<String>();
        HashSet<String> branchNewFiles = new HashSet<String>();
        
        Commit splitPoint = findSplitPoint(branch);
        
        while (!iterator.getHash().equals(splitPoint.getHash())) {
            thisNewFiles.addAll(iterator.getNewFiles());
            iterator = iterator.getParent();
        }
        iterator = branch.getHead();
        while (!iterator.getHash().equals(splitPoint.getHash())) {
            branchNewFiles.addAll(iterator.getNewFiles());
            iterator = iterator.getParent();
        }
       
        for (String item : branch.getHead().getFiles().keySet()) {
            boolean thisContains = thisNewFiles.contains(item);
            boolean branchContains = branchNewFiles.contains(item);
            if ((!head.getFiles().containsKey(item) && branchContains)
                    || !thisContains && branchContains) {
                Tools.copyToDirectory(item, branch.getHead().getFiles().get(item));
            } else if (thisContains && branchContains) {
                Tools.copyToDirectoryConflicted(item, branch.getHead().getFiles().get(item));
            }
        }
    }  
    
    /**
     * Rebases the current branch with given branch.
     * Copies all current commits up to split point of given branch and current branch and
     * attaches new commits to head of given branch.
     * 
     * Updates new commits with the following cases:
     * **For all following, "modified" refers to modification 
     * since the last split point of both branches
     * CASE 1: Modified in given branch, but not in current branch
     *  = CHANGE TO GIVEN BRANCH'S VERSION
     * CASE 2: Modified in current branch, but not in given branch = DO NOTHING
     * CASE 3: Modified in both = DO NOTHING
     * CASE 4: Neither is modified = DO NOTHING
     * 
     * If any changes are made, changes will be propagated all the way through from the first
     * new commit all the way to the most recent commit.
     */
    public void rebase(Branch branch, Data data, boolean isInteractive) {
        Commit splitPoint = findSplitPoint(branch);
        
        if (splitPoint.getHash().equals(branch.getHead().getHash())) {
            System.out.println("Already up-to-date."); return;
        }
        if (splitPoint.getHash().equals(head.getHash())) {
            head = branch.getHead(); return;
        }
        Commit iterator = head;
        HashSet<String> thisNewFiles = new HashSet<String>();
        HashSet<String> branchNewFiles = new HashSet<String>();
        ArrayList<Commit> newCommits = new ArrayList<Commit>();

        while (!iterator.getHash().equals(splitPoint.getHash())) {
            thisNewFiles.addAll(iterator.getNewFiles());
            newCommits.add(iterator);
            iterator = iterator.getParent();
        }
        iterator = branch.getHead();
        while (!iterator.getHash().equals(splitPoint.getHash())) {
            branchNewFiles.addAll(iterator.getNewFiles());
            iterator = iterator.getParent();
        }
        HashMap<String, String> propogateFiles = new HashMap<String, String>();
        for (String item : branch.getHead().getFiles().keySet()) {
            boolean thisContains = thisNewFiles.contains(item);
            boolean branchContains = branchNewFiles.contains(item);
            if (!thisContains && branchContains) {
                propogateFiles.put(item, branch.getHead().getFiles().get(item));
            }
        }
        
        Commit previous = branch.getHead();
        @SuppressWarnings("resource")
        Scanner reader = new Scanner(System.in);
        for (int i = newCommits.size() - 1; i >= 0; i--) {
            String input = "";
            Commit oldCommit = newCommits.get(i);
            if (isInteractive) {
                System.out.println("Currently replaying:");
                System.out.println("Commit " + oldCommit.getHash() + ".");
                System.out.println(oldCommit.getTime());
                System.out.println(oldCommit.getMessage());
            }
            while (isInteractive && !input.equals("c")
                    && !input.equals("s") && !input.equals("m")) {
                System.out.println("Would you like to (c)ontinue, "
                        + "(s)kip this commit, or change this commit's (m)essage?");
                input = reader.next();
            }
            if (!input.equals("s")) {
                String message;
                if (!isInteractive || input.equals("c")) {
                    message = oldCommit.getMessage();
                } else {
                    System.out.println("Please enter a new message for this commit.");
                    
                    input = reader.next();
                    message = input;
                }
                Commit newCommit = new Commit(message, oldCommit.getNewFiles(), 
                        oldCommit.getRemovedFiles(), oldCommit.getParent());
                newCommit.rebaseParent(previous);
                for (String item : propogateFiles.keySet()) {
                    newCommit.addNewFile(item, propogateFiles.get(item));
                }
                
                for (String item : oldCommit.getNewFiles()) {
                    newCommit.addNewFile(item, oldCommit.getFiles().get(item));
                }

                previous = newCommit;
                data.addCommit(newCommit);
                head = newCommit;
            }
        }
        reset(head);
    }
}
