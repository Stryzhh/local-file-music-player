package Main;

import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) throws Exception {
        Platform.setImplicitExit(false);
        Parent startWindow = FXMLLoader.load(Objects.requireNonNull(getClass()
                .getClassLoader().getResource("Main/MainUI/main.fxml")));

        window.initStyle(StageStyle.UNDECORATED);
        window.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(startWindow, 325, 100);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.setMinWidth(325);
        window.setMinHeight(100);
        window.getIcons().add(new javafx.scene.image.Image(System.getProperty("user.dir") + "\\src\\logo.png"));
        window.show();
    }

}
