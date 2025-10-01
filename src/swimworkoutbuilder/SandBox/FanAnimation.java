package swimworkoutbuilder.SandBox;

import javafx.animation.*;
import javafx.application.*;
import javafx.beans.binding.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.*;

public class FanAnimation extends Application {
    public void start(Stage primaryStage) {

        // Create an buttonBox with buttons, centered
        HBox  buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);

        Button btPause = new Button("Pause");
        Button btResume = new Button("Resume");
        Button btReverse = new Button("Reverse");
        Button btHigh = new Button("High");
        Button btMedium = new Button("Medium");
        Button btLow = new Button("Low");

        // make all buttons equal size and share space if window grows
        for (Button b : new Button[]{btPause, btResume, btReverse, btHigh, btMedium, btLow}) {
            HBox.setHgrow(b, Priority.ALWAYS);
            b.setMaxWidth(Double.MAX_VALUE);
        }

        buttonBox.getChildren().addAll(btPause, btResume, btReverse, btHigh, btMedium, btLow);

        // Layout
        BorderPane pane = new BorderPane();
        BorderPane.setMargin(buttonBox, new Insets(10, 10, 15, 10));
        pane.setBottom(buttonBox);

        // Fan and animation
        FanPane fan = new FanPane();
        pane.setCenter(fan);
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(25), e -> fan.move()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.setRate(1);
        animation.play();

        // Button actions
        btPause.setOnAction(e -> animation.pause());
        btResume.setOnAction(e -> animation.play());
        btReverse.setOnAction(e -> fan.reverse());
        btHigh.setOnAction(e -> animation.setRate(4));
        btMedium.setOnAction(e -> animation.setRate(2));
        btLow.setOnAction(e -> animation.setRate(1));

        // Scene setup
        Scene scene = new Scene(pane);
        primaryStage.setTitle("Fan Animation");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
// FanPane: draws and animates the fan
class FanPane extends Pane {
    private final Circle circle = new Circle();
    private final Arc[] arc = new Arc[4];
    private final double BLADE_ANGLE = 35;

    private double startAngle = 30;
    private double increment = 5;

    public FanPane() {
        // Add circle: center bound to pane center; radius bound to min(w, h) * 0.45
        setPrefSize(220, 220); // or any reasonable starting size
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.WHITE);
        circle.centerXProperty().bind(widthProperty().divide(2));
        circle.centerYProperty().bind(heightProperty().divide(2));
        circle.radiusProperty().bind(Bindings.min(widthProperty(), heightProperty()).multiply(0.45));
        getChildren().add(circle);

        // draw the fan blades (arcs): same center; radii bound to circle radius * 0.9
        for (int i = 0; i < 4; i++) {
            Arc a = new Arc();
            a.setType(ArcType.ROUND);
            a.setFill(Color.RED);

            a.centerXProperty().bind(circle.centerXProperty());
            a.centerYProperty().bind(circle.centerYProperty());
            a.radiusXProperty().bind(circle.radiusProperty().multiply(0.9));
            a.radiusYProperty().bind(circle.radiusProperty().multiply(0.9));

            a.startAngleProperty().set(startAngle + i * 90);
            a.setLength(BLADE_ANGLE);

            arc[i] = a;
            getChildren().add(a);
        }
    }


    public void move() {
        // update the start angle
        setStartAngle(startAngle + increment);
    }

    public void reverse() {
        increment = -increment;
    }
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        for (int i = 0; i < 4; i++) {
            arc[i].setStartAngle(startAngle + i * 90);
        }
    }
}
