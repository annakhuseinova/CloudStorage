import java.io.File;

public class StorageItem {

    private String name;
    private long size;
    private boolean isChosen;
    private String lastModificationDate;
    private File pathToFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public File getPathToFile() {
        return pathToFile;
    }

    public StorageItem(String name, long size, boolean isChosen, String lastModificationDate, File pathToFile) {
        this.name = name;
        this.size = size;
        this.isChosen = isChosen;
        this.lastModificationDate = lastModificationDate;
        this.pathToFile = pathToFile;
    }
}