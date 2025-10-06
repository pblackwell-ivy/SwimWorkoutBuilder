package swimworkoutbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * App entry point. Loads MainView.fxml and shows an empty UI
 * (no demo swimmer/workout seeded here).
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // FXML lives at src/resources/ui/MainView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1160, 780); // same pref size as your FXML
            stage.setTitle("SwimWorkoutBuilder");
            stage.setScene(scene);

            // Optional: minimum size to keep layout sane
            stage.setMinWidth(900);
            stage.setMinHeight(600);

            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            // Fail fast with a visible error if something is misconfigured
            throw new RuntimeException("Failed to load MainView.fxml", ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}