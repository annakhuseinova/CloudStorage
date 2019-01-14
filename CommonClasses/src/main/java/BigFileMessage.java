import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BigFileMessage extends AbstractMessage{
    private byte[] data;
    private int portions;
    private int currentPortion;
    private String fileName;
    private String login;

    public BigFileMessage(String login,String fileName, byte[] bytes, int currentPortion, int portions){
           this.login = login;
           this.fileName = fileName;
           this.data = bytes;
           this.currentPortion = currentPortion;
           this.portions = portions;

    }

    public String getLogin() {
        return login;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPortions() {
        return portions;
    }

    public int getCurrentPortion() {
        return currentPortion;
    }

}
