import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.awt.image.ImageWatched;
import sun.plugin2.main.server.Plugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.List;

public class MainPanelController implements Initializable {
    private int localStorageFolderLevelCounter = 0;
    private int cloudStorageFolderLevelCounter = 0;
    private HashMap<Integer, LinkedList<File>> folderCloudStorageListViews;
    private LinkedList<File> pathsToCloudStorageFiles;
    private String watchableDirectory = "ClientSide" + File.separator + "LocalStorage";
    private String currentDirectoryName = "";
    AudioClip soundOfFolderOpening = new AudioClip(this.getClass().getResource("foldersound.mp3").toExternalForm());


    @FXML
    ListView<StorageItem> listOfLocalElements;
    @FXML
    Button localStorageUpdate;
    @FXML
    VBox firstBlockMainPanel;
    @FXML
    ListView<StorageItem> listOfCloudStorageElements;
    @FXML
    ChoiceBox menu;
    @FXML
    Button cloudStorageUpdate;
    @FXML
    Button goToPreviousFolderInLocalStorageButton;
    @FXML
    Button goToPreviousFolderInCloudStorageButton;
    @FXML
    Button localStorageDelete;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ConnectionWithServer.startConnection();
        initializeListOfLocalStorageItems();
        dragAndDropIntoLocalStorage();
        dragFilesFromLocalStorageToCloud();
        mainPanelServerListener.setDaemon(true);
        mainPanelServerListener.start();
        deleteChosenFilesOnKeyDeletePressed();
        updateCloudStoragePanel();
    }

    Thread mainPanelServerListener = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    Object object = null;
                    object = ConnectionWithServer.readIncomingObject();
                    if (object instanceof UpdateMessage) {
                        UpdateMessage message = (UpdateMessage) object;
                        folderCloudStorageListViews = new HashMap<>();
                        folderCloudStorageListViews.putAll(message.getCloudStorageContents());
                        Platform.runLater(() -> initializeListOfCloudStorageItems(folderCloudStorageListViews));
                    }else if (object.toString().equals("DeletionSuccess")){
                        Platform.runLater(() -> {
                            initializeListOfCloudStorageItems(folderCloudStorageListViews);
                        });
                    }else if (object instanceof FileMessage){
                        FileMessage fileMessage = (FileMessage)object;
                        if (fileMessage.isDirectory() && fileMessage.isEmpty()){
                            Path pathToNewEmptyDirectory = Paths.get("ClientSide"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName());
                            if (Files.exists(pathToNewEmptyDirectory)) {
                                System.out.println("Такая директория уже существует");
                            }else {
                                Platform.runLater(() -> {
                                    try {
                                        Files.createDirectory(pathToNewEmptyDirectory);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }else {
                            try {
                                Files.write(Paths.get("ClientSide"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName()), fileMessage.getData(),StandardOpenOption.CREATE);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                        Platform.runLater(() -> initializeListOfLocalStorageItems());
                    }else if (object.toString().equals("success")){
                        System.out.println("success");
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    });

    public void initializeListOfLocalStorageItems() {
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        File pathToLocalStorage = new File(watchableDirectory);
        File[] listOfLocalStorageFiles = pathToLocalStorage.listFiles();
        if (listOfLocalStorageFiles.length == 0 && localStorageFolderLevelCounter == 0) {
            Image image = new Image("/icons/dropfilesicon.png");
            BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            listOfLocalElements.setBackground(new Background(bi));
            listOfLocalElements.setOpacity(0.9);
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        } else if (listOfLocalStorageFiles.length > 0){
            listOfLocalElements.setBackground(null);
                for (int i = 0; i < listOfLocalStorageFiles.length; i++) {
                    long initialSizeOfLocalFileOrDirectory = 0;
                    String nameOfLocalFileOrDirectory = listOfLocalStorageFiles[i].getName();
                    if (listOfLocalStorageFiles[i].isDirectory()) {
                        try {
                            initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(listOfLocalStorageFiles[i]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        initialSizeOfLocalFileOrDirectory = listOfLocalStorageFiles[i].length();
                    }
                    String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfLocalStorageFiles[i].lastModified()));
                    File pathToFileInLocalStorage = new File(listOfLocalStorageFiles[i].getAbsolutePath());
                    listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInLocalStorage));
                }
                listOfLocalElements.setItems(listOfLocalItems);
                listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        }else {
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        }
    }

    public void initializeListOfCloudStorageItems(HashMap<Integer, LinkedList<File>> listOfCloudStorageFiles) {
        if (cloudStorageFolderLevelCounter > 0){
            cloudStorageFolderLevelCounter = 0;
            goToPreviousFolderInCloudStorageButton.setVisible(false);
        }
        try {
            ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
            if (!listOfCloudStorageFiles.isEmpty()) {
                for (int i = 0; i < listOfCloudStorageFiles.get(0).size(); i++) {
                    long initialSizeOfCloudFileOrDir = 0;
                    String nameOfCloudFileOrDir = listOfCloudStorageFiles.get(0).get(i).getName();
                    if (listOfCloudStorageFiles.get(0).get(i).isDirectory()) {
                        try {
                            initialSizeOfCloudFileOrDir = getActualSizeOfFolder(listOfCloudStorageFiles.get(0).get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        initialSizeOfCloudFileOrDir = listOfCloudStorageFiles.get(0).get(i).length();
                    }
                    String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfCloudStorageFiles.get(0)
                            .get(i).lastModified()));
                    File pathOfFileInCloudStorage = new File(listOfCloudStorageFiles.get(0).get(i).getAbsolutePath());
                    listOfCloudItems.addAll(new StorageItem(nameOfCloudFileOrDir, initialSizeOfCloudFileOrDir, false, dateOfLastModification, pathOfFileInCloudStorage));
                }
                listOfCloudStorageElements.setItems(listOfCloudItems);
                listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
            } else {
                listOfCloudStorageElements.setItems(listOfCloudItems);
                listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
    public void dragAndDropIntoLocalStorage() {
        listOfLocalElements.setOnDragOver(event -> {
            listOfLocalElements.setStyle("-fx-background-color: white; -fx-opacity: 0.5;");
            event.acceptTransferModes(TransferMode.MOVE);

        });
        listOfLocalElements.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            ObservableList<StorageItem> listOfFiles = FXCollections.observableArrayList();
            String pathToCorrectDirectory = watchableDirectory+File.separator;
            if (db.hasFiles()) {
                for (int i = 0; i < db.getFiles().size(); i++) {
                    long initialSize = 0;
                    String name = db.getFiles().get(i).getName();
                    if (db.getFiles().get(i).isDirectory()){
                        try {
                            initialSize = getActualSizeOfFolder(db.getFiles().get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        initialSize = db.getFiles().get(i).length();
                    }
                    String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                            .format(new Date(db.getFiles().get(i).lastModified()));
                    File pathOfDroppedFileInLocalStorage = db.getFiles().get(i).getAbsoluteFile();
                    Path sourcePath = Paths.get(db.getFiles().get(i).getAbsolutePath());
                    File destinationPath = new File(pathToCorrectDirectory + db.getFiles().get(i).getName());
                    try {
                        Files.move(sourcePath, Paths.get(destinationPath.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    listOfFiles.add(new StorageItem(name, initialSize, false, dateOfLastModification,pathOfDroppedFileInLocalStorage));
                    listOfLocalElements.setItems(listOfFiles);
                    listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
                }
                initializeListOfLocalStorageItems();
                listOfLocalElements.setStyle("-fx-background-color: white;");
            }
            success = true;
            event.setDropCompleted(success);
            event.consume();
        });
        listOfLocalElements.setOnDragExited(event -> {
            listOfLocalElements.setStyle("-fx-background-color: white; -fx-opacity: 1;");
            event.acceptTransferModes(TransferMode.NONE);
        });
    }
    public void dragFilesFromLocalStorageToCloud() {
        listOfLocalElements.setOnDragDetected(event -> {
            Dragboard db = listOfLocalElements.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            List<File> localFiles = new LinkedList<File>();
            localFiles.addAll(getPathsOfSelectedFilesInLocalStorage());
            content.putFiles(localFiles);
            listOfLocalElements.setStyle("-fx-opacity: 1;");
            db.setContent(content);
        });

        listOfLocalElements.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            listOfLocalElements.setStyle("-fx-opacity: 1;");
        });
        listOfCloudStorageElements.setOnDragEntered(event -> {
            if (event.getGestureSource() != listOfLocalElements){
                event.acceptTransferModes(TransferMode.NONE);
            }
            listOfCloudStorageElements.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
        });

        listOfCloudStorageElements.setOnDragOver(event -> {
            if (event.getGestureSource() != listOfLocalElements){
                event.acceptTransferModes(TransferMode.NONE);
            }else {
                event.acceptTransferModes(TransferMode.COPY);
                listOfCloudStorageElements.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
                listOfLocalElements.setStyle("-fx-opacity: 1;");
            }

        });

        listOfCloudStorageElements.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            listOfCloudStorageElements.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            listOfLocalElements.setStyle("-fx-opacity: 1;");
        });

        listOfCloudStorageElements.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            listOfCloudStorageElements.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            listOfLocalElements.setStyle("-fx-opacity: 1;");
            transferFilesToCloudStorage();
        });
    }


    public void goToNextDirectoryInLocalStorageOnDoubleClickOrOpenFile(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else if (mouseEvent.getClickCount() == 2) {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (listOfLocalElements.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile;
                pathToClickedFile = listOfLocalElements.getSelectionModel().getSelectedItem().getPathToFile();
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {

                    }else if (nextDirectory.length != 0) {
                        playSoundOfFolderOpening();
                        localStorageFolderLevelCounter++;
                        if (localStorageFolderLevelCounter > 0) {
                            goToPreviousFolderInLocalStorageButton.setVisible(true);
                        }
                        if (localStorageFolderLevelCounter > 0 && nextDirectory.length != 0) {
                            watchableDirectory += File.separator + pathToClickedFile.getName();
                            currentDirectoryName = pathToClickedFile.getName();
                        } else {
                            currentDirectoryName = "ClientSide/LocalStorage";
                        }
                        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfLocalFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalFileOrDirectory = 0;
                            try {
                                if (nextDirectory[i].isDirectory()){
                                    initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(nextDirectory[i]);
                                }else{
                                    initialSizeOfLocalFileOrDirectory = nextDirectory[i].length();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathOfFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification,pathOfFileInLocalStorage));
                            listOfLocalElements.setItems(listOfLocalItems);
                            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
                        }

                    }
                }else {
                    Desktop desktop = null;
                    if (desktop.isDesktopSupported()){
                        desktop = desktop.getDesktop();
                        try {
                            desktop.open(pathToClickedFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public void goToPreviousDirectoryInLocalStorage() {
        playSoundOfFolderOpening();
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        File file = new File(watchableDirectory);
        File previousDirectory = new File(file.getParent());
        File[] contentsOfPreviousDirectory = previousDirectory.listFiles();
        for (int i = 0; i < contentsOfPreviousDirectory.length; i++) {
            files.add((contentsOfPreviousDirectory[i]));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = 0;
            try {
                if (files.get(i).isDirectory()){
                    initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(files.get(i));
                }else {
                    initialSizeOfLocalFileOrDirectory = files.get(i).length();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathOfFileInLocalStorage = files.get(i).getAbsoluteFile();
            listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathOfFileInLocalStorage
            ));
        }
        listOfLocalElements.setItems(listOfLocalItems);
        listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        localStorageFolderLevelCounter--;
        if (localStorageFolderLevelCounter <= 0) {
            goToPreviousFolderInLocalStorageButton.setVisible(false);
            watchableDirectory = "ClientSide"+File.separator+"LocalStorage";
            currentDirectoryName = "LocalStorage";
        }else {
            watchableDirectory = previousDirectory.toString();
            currentDirectoryName = previousDirectory.getName();
        }
    }
    public void goToNextDirectoryInCloudStorageOnDoubleClick(MouseEvent mouseEvent) {
        pathsToCloudStorageFiles = new LinkedList<>();
        if (mouseEvent.getClickCount() == 1) {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else if (mouseEvent.getClickCount() == 2) {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (listOfCloudStorageElements.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile = new File("");
                for (int i = 0; i < folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).size(); i++) {
                    File file = folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).get(i);
                    if (listOfCloudStorageElements.getSelectionModel().getSelectedItem().getName().equals(file.getName())) {
                        pathToClickedFile = folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).get(i);
                    }
                }
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {
                        System.out.println("пустая директория");
                    }
                    if (nextDirectory.length != 0) {
                        for (int i = 0; i < nextDirectory.length; i++) {
                            try {
                                pathsToCloudStorageFiles.add(nextDirectory[i]);
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                        playSoundOfFolderOpening();
                        cloudStorageFolderLevelCounter++;
                        folderCloudStorageListViews.put(cloudStorageFolderLevelCounter, pathsToCloudStorageFiles);
                        ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfCloudStorageFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalStorageFileOrDirectory = nextDirectory[i].length();
                            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathToFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfCloudItems.addAll(new StorageItem(nameOfCloudStorageFileOrDirectory, initialSizeOfLocalStorageFileOrDirectory, false, dateOfLastModification, pathToFileInLocalStorage));
                            listOfCloudStorageElements.setItems(listOfCloudItems);
                            listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
                        }
                    }
                    if (cloudStorageFolderLevelCounter > 0) {
                        goToPreviousFolderInCloudStorageButton.setVisible(true);
                    }
                }else {
                    System.out.println("Это не директория");
                }
            }
        }
    }
    public void goToPreviousDirectoryInCloudStorage(ActionEvent event) {
        ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        for (int i = 0; i < folderCloudStorageListViews.get(cloudStorageFolderLevelCounter - 1).size(); i++) {
            files.add((folderCloudStorageListViews.get(cloudStorageFolderLevelCounter - 1).get(i)));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = files.get(i).length();
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathToFileInCloudStorage = new File(files.get(i).getAbsolutePath());
            listOfCloudItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInCloudStorage));
        }
        listOfCloudStorageElements.setItems(listOfCloudItems);
        listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
        folderCloudStorageListViews.remove(cloudStorageFolderLevelCounter);
        cloudStorageFolderLevelCounter--;
        if (cloudStorageFolderLevelCounter <= 0) {
            goToPreviousFolderInCloudStorageButton.setVisible(false);
        }
        playSoundOfFolderOpening();
    }

    public LinkedList getPathsOfSelectedFilesInLocalStorage() {
        try {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            LinkedList<File> listOfSelectedElementsInLocalStorage = new LinkedList<File>();
            if (listOfLocalElements.getSelectionModel().getSelectedItems().size() != 0) {
                System.out.println(listOfLocalElements.getSelectionModel().getSelectedItems().size());
                for (int i = 0; i < listOfLocalElements.getSelectionModel().getSelectedItems().size(); i++) {
                    listOfSelectedElementsInLocalStorage.add(listOfLocalElements.getSelectionModel().getSelectedItems().get(i).getPathToFile());
                }
                return listOfSelectedElementsInLocalStorage;
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }
    public LinkedList getPathsOfSelectedFilesInCloudStorage(){
        listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        LinkedList<File> listOfSelectedElementsInCloudStorage = new LinkedList<File>();
        if (listOfCloudStorageElements.getSelectionModel().getSelectedItems().size() != 0){
            for (int i = 0; i < listOfCloudStorageElements.getSelectionModel().getSelectedItems().size(); i++) {
                listOfSelectedElementsInCloudStorage.add(listOfCloudStorageElements.getSelectionModel().getSelectedItems().get(i).getPathToFile());
            }
        }
        return listOfSelectedElementsInCloudStorage;
    }
    public void sendDeletionMessageToServer(){
        ConnectionWithServer.sendDeletionMessage(CurrentLogin.getCurrentLogin(), getPathsOfSelectedFilesInCloudStorage());
    }

    public void deleteChosenFilesFromLocalStorage(){
        listOfCloudStorageElements.getSelectionModel().clearSelection();
        for (int i = 0; i < getPathsOfSelectedFilesInLocalStorage().size(); i++) {
            String absolutePath = getPathsOfSelectedFilesInLocalStorage().get(i).toString();
            Path path = Paths.get(absolutePath);
            File file = new File(getPathsOfSelectedFilesInLocalStorage().get(i).toString());
            try {
                if (file.isDirectory()) {
                    deleteContentsOfFolderRecursively(file);
                } else {
                    Files.delete(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initializeListOfLocalStorageItems();
    }

    public void deleteChosenFilesOnKeyDeletePressed() {
        listOfLocalElements.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
               deleteChosenFilesFromLocalStorage();
            }
        });
        listOfCloudStorageElements.setOnKeyPressed(event -> {
            listOfLocalElements.getSelectionModel().clearSelection();
            if (event.getCode() == KeyCode.DELETE){
               sendDeletionMessageToServer();
            }
        });
    }
    public void selectAllFilesFromCloudStorage() {
        if (listOfCloudStorageElements.getItems().size() == listOfCloudStorageElements.getSelectionModel().getSelectedItems().size()) {
            listOfCloudStorageElements.getSelectionModel().clearSelection();
        } else {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listOfCloudStorageElements.getSelectionModel().selectAll();
        }
    }
    public void selectAllFilesFromLocalStorage() {
        if (listOfLocalElements.getItems().size() == listOfLocalElements.getSelectionModel().getSelectedItems().size()) {
            listOfLocalElements.getSelectionModel().clearSelection();
        } else {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listOfLocalElements.getSelectionModel().selectAll();
        }
    }

    public void updateCloudStoragePanel() {
        ConnectionWithServer.sendUpdateMessageToServer(CurrentLogin.getCurrentLogin());
    }

    public static void deleteContentsOfFolderRecursively(File file) throws Exception {
        try {
            if (file.isDirectory()) {
                for (File c : file.listFiles()) {
                    deleteContentsOfFolderRecursively(c);
                }
            }
            if (!file.delete()) {
                throw new Exception("Delete command returned false for file: " + file);
            }
        } catch (Exception e) {
            throw new Exception("Failed to delete the folder: " + file, e);
        }
    }
    public static long getActualSizeOfFolder(File file) throws Exception {
        long actualSizeOfFolder = 0;
        if (file.isDirectory()){
            for (File f: file.listFiles()){
                if (f.isFile()){
                    actualSizeOfFolder += f.length();
                }else if (f.isDirectory()){
                    actualSizeOfFolder += getActualSizeOfFolder(f);
                }
            }
        }
        return actualSizeOfFolder;
    }

    public void downloadFilesIntoLocalStorage() {
        ConnectionWithServer.sendFileRequest(getPathsOfSelectedFilesInCloudStorage());
    }

    public void transferFilesToCloudStorage() {
        ConnectionWithServer.transferFilesToCloudStorage(CurrentLogin.getCurrentLogin(),getPathsOfSelectedFilesInLocalStorage());
    }

    public void goToOpeningPanelToChangeProfileOrLeaveApp(){
        if (menu.getSelectionModel().getSelectedItem().toString().equals("Change profile")) {
            try {
                Stage stage;
                Parent root;
                stage = (Stage)menu.getScene().getWindow();
                root = FXMLLoader.load(getClass().getResource("/OpeningPanel.fxml"));
                Scene scene = new Scene(root,800,600);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.setTitle("Cloud Storage");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (menu.getSelectionModel().getSelectedItem().toString().equals("Exit")){
            Stage stage;
            stage = (Stage)menu.getScene().getWindow();
            stage.close();
        }
        menu.setValue(null);
    }

    public void playSoundOfFolderOpening() {
        soundOfFolderOpening.setVolume(0.2);
        soundOfFolderOpening.play();
    }

}
