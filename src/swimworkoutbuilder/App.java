package swimworkoutbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.ui.MainViewController;

public class App extends Application {

    private static Workout workoutFromMain; // stash the workout built in Main

    public static void setWorkout(Workout w) {
        workoutFromMain = w;
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
        Parent root = loader.load();   // ✅ cast to Parent

        // hand workout to controller
        MainViewController controller = loader.getController();
        if (workoutFromMain != null) {
            controller.setWorkout(workoutFromMain);
        }

        stage.setTitle("SwimWorkoutBuilder");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}