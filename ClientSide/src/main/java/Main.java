import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/OpeningPanel.fxml"));
        primaryStage.setTitle("Cloud Storage");
        Image appIcon = new Image(getClass().getResourceAsStream("/icons/flaticon.png"));
        primaryStage.getIcons().add(appIcon);
        primaryStage.setScene(new Scene(root, 795, 595));
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}