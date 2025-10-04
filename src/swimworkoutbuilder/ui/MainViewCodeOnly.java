package swimworkoutbuilder.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * A pure-JavaFX version of the Main View (no FXML).
 *
 * GOAL:
 * - Demonstrate how to build the same UI structure you designed in Scene Builder,
 *   but entirely in code. This is useful both for learning and for quick prototyping.
 *
 * LAYOUT OVERVIEW (BorderPane regions):
 *   ┌───────────────────────────────────────────────────────────────┐
 *   │ TOP:    MenuBar                                               │
 *   ├───────────────────────────────────────────────────────────────┤
 *   │ LEFT:   VBox ("Workout" label + Tree/List of items)           │
 *   │                                                               │
 *   │ CENTER: TabPane ("Set Editor" + "Preview")                    │
 *   │                                                               │
 *   │ RIGHT:  VBox -> TitledPane ("Seeds & Units") -> GridPane      │
 *   ├───────────────────────────────────────────────────────────────┤
 *   │ BOTTOM: HBox (status bar)                                     │
 *   └───────────────────────────────────────────────────────────────┘
 */
public class MainViewCodeOnly extends Application {

    @Override
    public void start(Stage stage) {
        // Root container for the whole window. BorderPane gives us 5 regions:
        // top, left, center, right, bottom.
        BorderPane root = new BorderPane();

        // TOP: Menu bar
        MenuBar menuBar = new MenuBar();

        // "File" menu (with Close)
        Menu fileMenu = new Menu("File");
        MenuItem miClose = new MenuItem("Close");
        // simple behavior: close the window
        miClose.setOnAction(e -> stage.close());
        fileMenu.getItems().add(miClose);

        // "Edit" menu (placeholder)
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().add(new MenuItem("Delete"));

        // "Help" menu (About dialog)
        Menu helpMenu = new Menu("Help");
        MenuItem miAbout = new MenuItem("About");
        miAbout.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION,
                        "SwimWorkoutBuilder\nMVP UI + pacing engine.\n© 2025").showAndWait()
        );
        helpMenu.getItems().add(miAbout);

        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        root.setTop(menuBar);

        // -----------------------------
        // LEFT: Workout list/tree
        // -----------------------------
        // We use a VBox to stack a label above a list component. You can swap ListView
        // for a TreeView later when you’re ready for groups/sets nesting.
        VBox leftPane = new VBox(8);                  // 8px vertical gap between children
        leftPane.setPadding(new Insets(10));          // inner padding so content isn’t flush to edges
        leftPane.setPrefWidth(260);                   // give the left column a comfortable width

        Label workoutLabel = new Label("Workout");

        // Placeholder content: a list of strings.
        // Swap for a TreeView<SetGroup/SwimSet> later.
        ListView<String> workoutList = new ListView<>();
        workoutList.getItems().addAll(
                "Warmup",
                "Drills",
                "Main Set",
                "Cooldown"
        );
        // Let the list grow to fill available vertical space in the left VBox
        VBox.setVgrow(workoutList, Priority.ALWAYS);

        leftPane.getChildren().addAll(workoutLabel, workoutList);
        root.setLeft(leftPane);

        // -----------------------------
        // CENTER: Tab pane (Set Editor / Preview)
        // -----------------------------
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // no close buttons on tabs
        // Give the tab pane a chance to expand inside center area
        VBox centerBox = new VBox(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Editor tab (placeholder content for now)
        Tab editorTab = new Tab("Set Editor", new Label("Set Editor content"));

        // Preview tab (placeholder content for now)
        Tab previewTab = new Tab("Preview", new Label("Preview content"));

        tabPane.getTabs().addAll(editorTab, previewTab);
        root.setCenter(centerBox);

        // -----------------------------
        // RIGHT: Seeds & Units panel
        // -----------------------------
        // Wrap it in a VBox for future expansion (e.g., add more panes later).
        VBox rightPane = new VBox();
        rightPane.setPadding(new Insets(10));
        rightPane.setSpacing(8);
        rightPane.setPrefWidth(260);

        // TitledPane as a section container
        TitledPane seedsUnitsPane = new TitledPane();
        seedsUnitsPane.setText("Seeds & Units");
        seedsUnitsPane.setCollapsible(false); // keep it always open in MVP

        // Grid for labels + controls
        GridPane grid = new GridPane();
        grid.setHgap(10);   // horizontal gap between columns
        grid.setVgap(8);    // vertical gap between rows

        // Optional: make column 1 (index 1) stretch a bit
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.NEVER); // label column stays natural size

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS); // input column can grow

        grid.getColumnConstraints().addAll(c0, c1);

        // Row 0: Course choice
        grid.add(new Label("Course:"), 0, 0);
        ChoiceBox<String> courseChoice = new ChoiceBox<>();
        courseChoice.getItems().addAll("SCY (25y)", "SCM (25m)", "LCM (50m)");
        courseChoice.getSelectionModel().selectFirst();
        grid.add(courseChoice, 1, 0);

        // Row 1: Units (Yards/Meters)
        grid.add(new Label("Units:"), 0, 1);
        ToggleGroup unitsGroup = new ToggleGroup();
        RadioButton yards = new RadioButton("Yards");
        yards.setToggleGroup(unitsGroup);
        yards.setSelected(true); // default
        RadioButton meters = new RadioButton("Meters");
        meters.setToggleGroup(unitsGroup);

        // Put the radios side-by-side
        HBox unitBox = new HBox(10, yards, meters);
        // Make the unit box consume any extra width to avoid clipping text
        HBox.setHgrow(unitBox, Priority.ALWAYS);
        grid.add(unitBox, 1, 1);

        // (Optional) you can add seed entry fields here later, for each stroke, etc.

        seedsUnitsPane.setContent(grid);
        rightPane.getChildren().add(seedsUnitsPane);
        root.setRight(rightPane);

        // -----------------------------
        // BOTTOM: Status bar
        // -----------------------------
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(6, 10, 6, 10));
        statusBar.setStyle("-fx-background-color: -fx-control-inner-background; "
                + "-fx-border-color: -fx-box-border; -fx-border-width: 1 0 0 0;");
        statusBar.getChildren().add(new Label("Ready"));
        root.setBottom(statusBar);

        // -----------------------------
        // SCENE + STAGE
        // -----------------------------
        // Scene ties the node tree to the window. Set an initial size that feels roomy.
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Workout Builder (Code-Only Demo)");
        stage.show();

        // (Optional) If you want to react to units choice in MVP:
        unitsGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT instanceof RadioButton rb) {
                System.out.println("Units changed to: " + rb.getText());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}