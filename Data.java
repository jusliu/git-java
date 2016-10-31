import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;


@SuppressWarnings("serial")
public class Data implements Serializable {
    private HashMap<String, Commit> commits;
    private HashMap<String, Branch> branches;
    private Branch currBranch;
    private HashSet<String> remove;
    private HashSet<String> stage;
    private HashMap<String, HashSet<String>> commitMessages;
    
    /**
     * Initiates all class variables.
     */
    public Data() {
        commits = new HashMap<String, Commit>();
        branches = new HashMap<String, Branch>();
        currBranch = null;
        remove = new HashSet<String>();
        stage = new HashSet<String>();
        commitMessages = new HashMap<String, HashSet<String>>();
    }
    
    /**
     * Returns all saved commits.
     */
    public HashMap<String, Commit> getCommits() {
        return commits;
    }
    
    /**
     * Returns all saved branches.
     */
    public HashMap<String, Branch> getBranches() {
        return branches;
    }
    
    /**
     * Returns current branch.
     */
    public Branch getCurrBranch() {
        return currBranch;
    }
    
    /**
     * Returns names of all files currently marked for removal.
     */
    public HashSet<String> getRemoveFiles() {
        return remove;
    }
    
    /**
     * Returns all currently staged files.
     */
    public HashSet<String> getStagedFiles() {
        return stage;
    }
    
    /**
     * Returns map of commit messages mapped to a set of commit IDs with that message.
     * Used for use in find command.
     */
    public HashMap<String, HashSet<String>> getCommitMessages() {
        return commitMessages;
    }
    
    /**
     * Sets the current branch to the given branch name.
     * Assumes given branch exists.
     */
    public void setCurrBranch(String branchName) {
        currBranch = branches.get(branchName);
    }
    
    /**
     * Adds the fileName to stage.
     * If marked for removal, unmark.
     */
    public void add(String fileName) {
        stage.add(fileName);
        if (remove.contains(fileName)) {
            remove.remove(fileName);
        }
    }
    
    /**
     * Marks the file for removal.
     * If file was staged, unstage.
     */
    public void remove(String fileName) {
        remove.add(fileName);
        if (stage.contains(fileName)) {
            stage.remove(fileName);
        }
    }
    
    /**
     * Clears the stage and all files marked for removal.
     */
    public void clear() {
        stage.clear();
        remove.clear();
    }
    
    /**
     * Adds a new branch to the branch map.
     * Assumes branch does not previously exist.
     */
    public void addBranch(Branch branch) {
        branches.put(branch.getName(), branch);
    }
    
    /**
     * Removes a branch from current branches.
     * Assumes branch exists.
     */
    public void removeBranch(String branchName) {
        branches.remove(branchName);
    }
    
    /**
     * Adds a new commit to the commit map.
     * Adds commit ID to map of commit messages for more efficient runtime use in find command.
     */
    public void addCommit(Commit commit) {
        commits.put(commit.getHash(), commit);
        if (commitMessages.containsKey(commit.getMessage())) {
            commitMessages.get(commit.getMessage()).add(commit.getHash());
        } else {
            HashSet<String> temp = new HashSet<String>();
            temp.add(commit.getHash());
            commitMessages.put(commit.getMessage(), temp);
        }
    }
}
