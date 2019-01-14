import java.io.File;
import java.util.LinkedList;

public class FileRequest extends AbstractMessage {
    private LinkedList<File> filesToRequest;


    public FileRequest(LinkedList<File> files) {
        this.filesToRequest = files;
    }

    public LinkedList<File> getFilesToRequest() {
        return filesToRequest;
    }



}