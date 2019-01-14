import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class ConnectionWithServer {

    private static Socket socket;
    private static ObjectEncoderOutputStream outcomingStream;
    private static ObjectDecoderInputStream incomingStream;

    public static void startConnection() {
        try {
            socket = new Socket("localhost", 8800);
            outcomingStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            incomingStream = new ObjectDecoderInputStream(socket.getInputStream(), 20971520);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopConnection() {
        try {
            outcomingStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            incomingStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendDeletionMessage(String login, LinkedList<File> filesToDelete) {
        try {
            if (!filesToDelete.isEmpty()) {
                outcomingStream.writeObject(new DeletionMessage(login, filesToDelete));
                outcomingStream.flush();
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean transferFilesToCloudStorage(String login, LinkedList<File> filesToSendToCloud) {
        try {
            if (!filesToSendToCloud.isEmpty()) {
                for (int i = 0; i < filesToSendToCloud.size(); i++) {
                    Path path = Paths.get(filesToSendToCloud.get(i).getAbsolutePath());
                    outcomingStream.writeObject(new FileMessage(login, path));
                    outcomingStream.flush();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendUpdateMessageToServer(String login){
        try {
            outcomingStream.writeObject(new UpdateMessage(login));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean sendFileRequest(LinkedList<File> filesToRequest){
        try {
            if (!filesToRequest.isEmpty()){
                outcomingStream.writeObject(new FileRequest(filesToRequest));
                outcomingStream.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static boolean sendAuthMessageToServer(String login, String password) {
        try {
            outcomingStream.writeObject(new AuthMessage(login,password));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendRegMessageToServer(String login, String password) {
        try {
            outcomingStream.writeObject(new RegistrationMessage(login,password));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static Object readIncomingObject() throws IOException, ClassNotFoundException {
        Object object = incomingStream.readObject();
        return  object;
    }
}