import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;

public class FileWithContentsMessage {

    private LinkedList<File> listOfDirectories;
    private LinkedList<Path> pathsOfFiles;
    private HashMap<String,Byte> fileContainer;

    public FileWithContentsMessage(LinkedList<File> listOfDirectories) {
        this.listOfDirectories = listOfDirectories;
    }
    public FileWithContentsMessage(LinkedList<File> listOfDirectories, LinkedList<File> paths){
       this.listOfDirectories = listOfDirectories;
        for (int i = 0; i < paths.size(); i++) {

        }
    }

    public LinkedList<File> getListOfDirectories() {
        return listOfDirectories;
    }
}
