package swimworkoutbuilder.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import swimworkoutbuilder.model.Swimmer;

import java.net.URL;

public class SwimmerFormLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1) Locate FXML
        URL fxml = getClass().getResource("/ui/SwimmerForm.fxml");
        if (fxml == null) {
            throw new IllegalStateException("FXML not found at /ui/SwimmerForm.fxml");
        }

        // 2) Load FXML + controller
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load(); // must load before getController()
        SwimmerFormController controller = loader.getController(); // <-- get the controller

        // 3) Build modal dialog
        Stage dialog = new Stage();
        dialog.setTitle("Swimmer Setup");
        dialog.initOwner(primaryStage);                 // owner can be invisible
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(root));
        dialog.setResizable(false);

        // 4) Show dialog and wait for Save/Cancel to close it
        dialog.showAndWait();

        // 5) Read the result from the controller
        Swimmer created = controller.getResult();       // <-- singular method name
        if (created != null) {
            System.out.println("User created swimmer: " + created);
        } else {
            System.out.println("User cancelled swimmer creation");
        }

        // 6) Exit app (since we only launched a dialog)
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}