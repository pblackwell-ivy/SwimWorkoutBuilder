package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.units.Distance;

import java.util.Objects;

/**
 * Renders and edits a Workout using inline, action-first controls.
 * - Each SetGroup appears as a "card" with Edit / Add Set / Add Group actions.
 * - Sets are listed under their group; formatting matches coach-style notation.
 * - Bottom bar exposes Save/Cancel; header shows live totals.
 *
 * NOTE: Handlers that open dialogs (SetGroupForm, SetForm, etc.) are stubbed
 * with simple examples/alerts so you can wire your forms when ready.
 */
public class WorkoutEditorController {

    // Header labels
    @FXML private Label lblWorkoutName;
    @FXML private Label lblCourse;
    @FXML private Label lblTotals;

    // Container for group "cards"
    @FXML private VBox groupsBox;

    private Workout workout; // injected by MainViewController

    // ----- Public API -----

    public void setWorkout(Workout workout) {
        this.workout = Objects.requireNonNull(workout, "workout");
        refreshHeader();
        rebuildGroups();
    }

    // ----- UI build -----

    private void refreshHeader() {
        if (workout == null) {
            lblWorkoutName.setText("—");
            lblCourse.setText("—");
            lblTotals.setText("0 • 0:00");
            return;
        }
        lblWorkoutName.setText(workout.getName());
        lblCourse.setText(workout.getCourse().toString());

        // Display totals in the workout’s display unit (yards for SCY, meters otherwise)
        boolean yards = (workout.getCourse() == Course.SCY);
        long dist = 0L;
        for (SetGroup g : workout.getGroups()) {
            dist += (yards ? Math.round(g.totalDistance().toYards())
                    : Math.round(g.totalDistance().toMeters()));
        }
        // Time totals are printed in WorkoutPrinter; here we keep it simple.
        lblTotals.setText(dist + (yards ? " yd" : " m"));
    }

    private void rebuildGroups() {
        groupsBox.getChildren().clear();
        if (workout == null) return;

        for (SetGroup g : workout.getGroups()) {
            groupsBox.getChildren().add(makeGroupCard(g));
        }
    }

    private Node makeGroupCard(SetGroup group) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(8));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 6;
            -fx-border-color: #2e7d32;
            -fx-border-width: 2;
            -fx-border-radius: 6;
        """);

        // Header row
        HBox header = new HBox(8);
        Label title = new Label(group.getName() + " (x" + group.getReps() + ")");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btEdit   = new Button("Edit");
        Button btAddSet = new Button("Add Set");
        Button btAddGrp = new Button("Add Group");

        btEdit.setOnAction(e -> handleEditGroup(group));
        btAddSet.setOnAction(e -> handleAddSet(group));
        btAddGrp.setOnAction(e -> handleAddGroupAfter(group));

        header.getChildren().addAll(title, spacer, btEdit, btAddSet, btAddGrp);

        // Sets list
        VBox setsBox = new VBox(4);
        setsBox.setPadding(new Insets(4, 0, 0, 12));
        for (SwimSet s : group.getSets()) {
            setsBox.getChildren().add(makeSetRow(s));
        }

        // Optional notes under header
        if (group.getNotes() != null && !group.getNotes().isBlank()) {
            Label notes = new Label(group.getNotes());
            notes.setStyle("-fx-font-style: italic;");
            card.getChildren().addAll(header, notes, setsBox);
        } else {
            card.getChildren().addAll(header, setsBox);
        }

        return card;
    }

    private Node makeSetRow(SwimSet s) {
        String distance = formatDistanceForWorkoutDisplay(s.getDistancePerRep(), workout.getCourse());
        String stroke   = (s.getStroke() == null) ? "" : s.getStroke().getShortLabel();
        String line = s.getReps() + " x " + distance + " " + stroke
                + (s.getEffort() != null ? ", " + s.getEffort().getLabel() : "");

        VBox row = new VBox(2);
        Label main = new Label(line);
        row.getChildren().add(main);
        if (s.getNotes() != null && !s.getNotes().isBlank()) {
            Label notes = new Label(s.getNotes());
            notes.setStyle("-fx-font-style: italic;");
            row.getChildren().add(notes);
        }
        return row;
    }

    private static String formatDistanceForWorkoutDisplay(Distance d, Course c) {
        if (c == Course.SCY) {
            int yards = (int) Math.round(d.toYards());
            int snapped = Math.round(yards / 25f) * 25;
            return snapped + " yd";
        }
        int meters = (int) Math.round(d.toMeters());
        return meters + " m";
    }

    // ----- Actions (wire these to your dialogs later) -----

    @FXML
    private void handleAddGroupTop() {
        ensureWorkout();
        // TODO: open SetGroupForm; for now add a sample group
        SetGroup g = new SetGroup("New Group", 1, workout.getGroups().size() + 1);
        workout.addSetGroup(g);
        refreshHeader();
        rebuildGroups();
    }

    private void handleAddGroupAfter(SetGroup after) {
        ensureWorkout();
        int idx = workout.getGroups().indexOf(after);
        SetGroup g = new SetGroup("New Group", 1, after.getOrder() + 1);
        workout.insertSetGroup(idx + 1, g);
        refreshHeader();
        rebuildGroups();
    }

    private void handleEditGroup(SetGroup group) {
        // TODO: open SetGroupForm pre-filled; stub change
        group.setName(group.getName() + " *");
        refreshHeader();
        rebuildGroups();
    }

    private void handleAddSet(SetGroup group) {
        // TODO: open SetForm; stub example set so you see it add inline
        SwimSet s = new SwimSet(
                StrokeType.FREESTYLE,
                4,
                Distance.ofYards(50),
                Effort.EASY,
                workout.getCourse(),
                "New set"
        );
        group.addSet(s);
        refreshHeader();
        rebuildGroups();
    }

    @FXML
    private void handleSaveWorkout() {
        new Alert(Alert.AlertType.INFORMATION, "Workout saved (stub).").showAndWait();
    }

    @FXML
    private void handleCancelWorkout() {
        new Alert(Alert.AlertType.INFORMATION, "Canceled (stub).").showAndWait();
    }

    private void ensureWorkout() {
        if (workout == null) throw new IllegalStateException("Workout not set in WorkoutEditorController.");
    }
}