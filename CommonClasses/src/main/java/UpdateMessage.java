import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class UpdateMessage extends AbstractMessage {
    private HashMap<Integer, LinkedList<File>> cloudStorageContents;
    private String login;

    public UpdateMessage(HashMap<Integer, LinkedList<File>> cloudStorageContents) {
        this.cloudStorageContents = cloudStorageContents;
    }
    public UpdateMessage(String login){
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public HashMap<Integer, LinkedList<File>> getCloudStorageContents() {
        return cloudStorageContents;
    }
}
