import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;

public class StorageListViewItem extends ListCell<StorageItem> {

    FXMLLoader localStorageItemLoader;
    public CheckBox isLocalItemChecked;
    public Label localItemName;
    public Label localItemSize;
    public Label localItemModified;
    public Label fileIcon;
    public VBox localStorageItemCell;



    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if (selected){
            isLocalItemChecked.setSelected(true);
        }else{
            isLocalItemChecked.setSelected(false);
        }
    }

    @Override
    protected void updateItem(StorageItem item, boolean empty) {
        super.updateItem(item, empty);
        try {
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (localStorageItemLoader == null) {
                    localStorageItemLoader = new FXMLLoader(getClass().getResource("/ItemCellView.fxml"));
                    localStorageItemLoader.setController(this);
                    try {
                        localStorageItemLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                localItemName.setText(item.getName());
                Tooltip name = new Tooltip(item.getName());
                localItemName.setTooltip(name);

                Tooltip size = new Tooltip();
                if (item.getSize() / 1073741824 > 0) {
                    localItemSize.setText(item.getSize() / 1073741824 + " GB");
                    size.setText(localItemSize.getText());
                    localItemSize.setTooltip(size);
                } else if (item.getSize() / 1048576 > 0) {
                    localItemSize.setText((item.getSize() / 1048576) + " MB");
                    size.setText(localItemSize.getText());
                    localItemSize.setTooltip(size);
                } else if (item.getSize() / 1024 > 0) {
                    localItemSize.setText(item.getSize() / 1024 + " KB");
                    size.setText(localItemSize.getText());
                    localItemSize.setTooltip(size);
                } else if (item.getSize() / 1024 <= 0) {
                    localItemSize.setText(item.getSize() + " bytes");
                    size.setText(localItemSize.getText());
                    localItemSize.setTooltip(size);
                }
                isLocalItemChecked.setSelected(item.isChosen());
                localItemModified.setText("" + item.getLastModificationDate());
                Tooltip timeOfLastModification = new Tooltip(localItemModified.getText());
                localItemModified.setTooltip(timeOfLastModification);
                // Определяем иконки для файлов разных форматов.
                String fileFormat = FilenameUtils.getExtension(localItemName.getText());
                InputStream input = getClass().getResourceAsStream("/icons/neutralicon.png");
                if (fileFormat.equalsIgnoreCase("avi")) {
                    input = getClass().getResourceAsStream("/icons/aviicon.png");
                } else if (fileFormat.equalsIgnoreCase("bin")) {
                    input = getClass().getResourceAsStream("/icons/binicon.png");
                } else if (fileFormat.equalsIgnoreCase("c")) {
                    input = getClass().getResourceAsStream("/icons/cicon.png");
                } else if (fileFormat.equalsIgnoreCase("cpp")) {
                    input = getClass().getResourceAsStream("/icons/cpluspluscon.png");
                } else if (fileFormat.equalsIgnoreCase("css")) {
                    input = getClass().getResourceAsStream("/icons/cssicon.png");
                } else if (fileFormat.equalsIgnoreCase("doc") || fileFormat.equals("docx")) {
                    input = getClass().getResourceAsStream("/icons/docicon.png");
                } else if (fileFormat.equalsIgnoreCase("exe")) {
                    input = getClass().getResourceAsStream("/icons/exeicon.png");
                } else if (fileFormat.equalsIgnoreCase("gif")) {
                    input = getClass().getResourceAsStream("/icons/gificon.png");
                } else if (fileFormat.equalsIgnoreCase("html") || fileFormat.equalsIgnoreCase("htm")) {
                    input = getClass().getResourceAsStream("/icons/htmlicon.png");
                } else if (fileFormat.equalsIgnoreCase("java") || fileFormat.equals("jar")) {
                    input = getClass().getResourceAsStream("/icons/javaicon.png");
                } else if (fileFormat.equalsIgnoreCase("jpeg") || fileFormat.equalsIgnoreCase("jpg")) {
                    input = getClass().getResourceAsStream("/icons/jpegicon.png");
                } else if (fileFormat.equalsIgnoreCase("js")) {
                    input = getClass().getResourceAsStream("/icons/jsicon.png");
                } else if (fileFormat.equalsIgnoreCase("midi")) {
                    input = getClass().getResourceAsStream("/icons/midiicon.png");
                } else if (fileFormat.equalsIgnoreCase("mov")) {
                    input = getClass().getResourceAsStream("/icons/movicon.png");
                } else if (fileFormat.equalsIgnoreCase("mp4")) {
                    input = getClass().getResourceAsStream("/icons/mp4icon.png");
                } else if (fileFormat.equalsIgnoreCase("mpeg")) {
                    input = getClass().getResourceAsStream("/icons/mpegicon.png");
                } else if (fileFormat.equals("oggi")) {
                    input = getClass().getResourceAsStream("/icons/oggiicon.png");
                } else if (fileFormat.equals("pdf")) {
                    input = getClass().getResourceAsStream("/icons/pdficon.png");
                } else if (fileFormat.equalsIgnoreCase("php")) {
                    input = getClass().getResourceAsStream("/icons/phpicon.png");
                } else if (fileFormat.equalsIgnoreCase("png")) {
                    input = getClass().getResourceAsStream("/icons/pngicon.png");
                } else if (fileFormat.equals("pub")) {
                    input = getClass().getResourceAsStream("/icons/pubicon.png");
                } else if (fileFormat.equalsIgnoreCase("py")) {
                    input = getClass().getResourceAsStream("/icons/pythonicon.png");
                } else if (fileFormat.equalsIgnoreCase("rar")) {
                    input = getClass().getResourceAsStream("/icons/raricon.png");
                } else if (fileFormat.equalsIgnoreCase("rtf")) {
                    input = getClass().getResourceAsStream("/icons/rtficon.png");
                } else if (fileFormat.equalsIgnoreCase("wav")) {
                    input = getClass().getResourceAsStream("/icons/wavicon.png");
                } else if (fileFormat.equalsIgnoreCase("wma")) {
                    input = getClass().getResourceAsStream("/icons/wmaicon.png");
                } else if (fileFormat.equalsIgnoreCase("wmv")) {
                    input = getClass().getResourceAsStream("/icons/wmvicon.png");
                } else if (fileFormat.equalsIgnoreCase("xlsx")) {
                    input = getClass().getResourceAsStream("/icons/xlsxicon.png");
                } else if (fileFormat.equalsIgnoreCase("txt")) {
                    input = getClass().getResourceAsStream("/icons/txticon.png");
                } else if (fileFormat.equalsIgnoreCase("torrent")) {
                    input = getClass().getResourceAsStream("/icons/torrenticon.png");
                } else if (fileFormat.equalsIgnoreCase("svg")) {
                    input = getClass().getResourceAsStream("/icons/svgicon.png");
                } else if (fileFormat.equalsIgnoreCase("psd")) {
                    input = getClass().getResourceAsStream("/icons/psdicon.png");
                } else if (fileFormat.equals("")) {
                    input = getClass().getResourceAsStream("/icons/folder.png");
                } else if (fileFormat.equalsIgnoreCase("zip")) {
                    input = getClass().getResourceAsStream("/icons/zipicon.png");
                } else if (fileFormat.equalsIgnoreCase("mkv")) {
                    input = getClass().getResourceAsStream("/icons/mkvicon.png");
                } else if (fileFormat.equalsIgnoreCase("iso")) {
                    input = getClass().getResourceAsStream("/icons/isoicon.png");
                } else if (fileFormat.equalsIgnoreCase("djvu")) {
                    input = getClass().getResourceAsStream("/icons/djvuicon.png");
                } else if (fileFormat.equalsIgnoreCase("mp3")) {
                    input = getClass().getResourceAsStream("/icons/mp3icon.png");
                }
                ImageView imageView = new ImageView(new Image(input));
                fileIcon.setGraphic(imageView);
            }
            setText(null);
            setGraphic(localStorageItemCell);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}

