package swimworkoutbuilder.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import swimworkoutbuilder.model.SeedPace100;
import swimworkoutbuilder.model.SetGroup;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;


import java.util.Collections;
import java.util.List;

import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

/**
 * MainView.fxml controller: renders Workout tree, supports DnD reordering,
 * and opens the Create-Swimmer dialog.
 */
public class MainViewController {

    // FXML
    @FXML private TreeView<Object> workoutTree;

    // Model
    private Workout workout;

    // Drag-and-drop state
    private TreeItem<Object> draggedItem;

    /** Inject a workout and (re)build the tree. */
    public void setWorkout(Workout workout) {
        this.workout = workout;
        rebuildTree();
    }

    @FXML private javafx.scene.control.Label lblSwimmer;

    private static String nameOrPreferred(Swimmer s) {
        if (s == null) return "—";
        String base = s.getFirstName() + " " + s.getLastName();
        return (s.getPreferredName() == null || s.getPreferredName().isBlank())
                ? base
                : s.getPreferredName() + " (" + base + ")";
    }

    private void setCurrentSwimmer(Swimmer s) {
        if (lblSwimmer != null) {
            lblSwimmer.setText("Swimmer: " + nameOrPreferred(s));
        }
    }

    @FXML private javafx.scene.control.ChoiceBox<Course> courseChoice;

    private void initCourseChoice() {
        if (courseChoice == null) return;
        courseChoice.getItems().setAll(Course.SCY, Course.SCM, Course.LCM);
        courseChoice.setValue(Course.SCY); // default for now
    }
    @FXML private javafx.scene.control.TextArea previewArea;

    private void refreshPreview() {
        if (previewArea == null) return;
        if (workout == null) { previewArea.setText("(no workout)"); return; }

        StringBuilder sb = new StringBuilder();
        sb.append("Workout: ").append(workout.getName()).append("\n")
                .append("Course: ").append(workout.getCourse()).append("\n\n");

        int gi = 1;
        for (SetGroup g : workout.getGroups()) {
            sb.append(gi++).append(") ").append(g.getName());
            if (g.getReps() > 1) sb.append(" x").append(g.getReps());
            if (g.getNotes() != null && !g.getNotes().isBlank()) sb.append(" — ").append(g.getNotes());
            sb.append("\n");

            int si = 1;
            for (SwimSet s : g.getSets()) {
                sb.append("   ").append(si++).append(". ")
                        .append(s.getReps()).append("x")
                        .append(displayDistance(s)).append(" ")
                        .append(s.getStroke());
                if (s.getEffort() != null) sb.append("  ").append(s.getEffort().getLabel());
                if (s.getNotes() != null && !s.getNotes().isBlank()) sb.append(" — ").append(s.getNotes());
                sb.append("\n");
            }
            sb.append("\n");
        }
        previewArea.setText(sb.toString());
    }

    @FXML
    private void initialize() {
        if (workoutTree == null) {
            throw new IllegalStateException("workoutTree not injected; check fx:id in MainView.fxml.");
        }

        // Demo data to make the screen useful before wiring AppState.
        Swimmer swimmer = new Swimmer("Parker", "Blackwell", "", "Indy Aquatic Masters");
        swimmer.updateSeedTime(StrokeType.FREESTYLE,  new SeedPace100(78.0,  CourseUnit.YARDS));
        swimmer.updateSeedTime(StrokeType.FREE_KICK,  new SeedPace100(118.0, CourseUnit.YARDS));
        swimmer.updateSeedTime(StrokeType.DRILL,      new SeedPace100(110.0, CourseUnit.YARDS));


        Workout w = new Workout(
                swimmer.getId(),
                "Test Workout",
                Course.SCY,
                "Demo workout to validate pacing and totals.",
                60
        );

        Course course = w.getCourse();
        SetGroup warmup   = new SetGroup("Warmup",   1, 1); warmup.setNotes("Focus on technique");
        SetGroup drills   = new SetGroup("Drills",   1, 2); drills.setNotes("Focus on alignment and rotation");
        SetGroup main     = new SetGroup("Main",     4, 3);
        SetGroup cooldown = new SetGroup("Cooldown", 1, 4);

        Collections.addAll(w.getGroups(), warmup, drills, main, cooldown);

        warmup.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(400), Effort.EASY,      course, "Perfect Freestyle"));
        warmup.addSet(new SwimSet(StrokeType.FREE_KICK, 4, yardsToMeters(50),  Effort.EASY,      course, "Kick w/ streamline"));

        drills.addSet(new SwimSet(StrokeType.DRILL,     2, yardsToMeters(50),  Effort.ENDURANCE, course, "3 strokes / 6 kicks drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(50),  Effort.EASY,      course, "Apply the drill"));
        drills.addSet(new SwimSet(StrokeType.DRILL,     2, yardsToMeters(50),  Effort.ENDURANCE, course, "Rhythm drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(50),  Effort.EASY,      course, "Apply the drill"));

        main.addSet(new SwimSet(StrokeType.FREESTYLE,   4, yardsToMeters(50),  Effort.RACE_PACE, course, "USRPT style"));
        main.addSet(new SwimSet(StrokeType.FREESTYLE,   1, yardsToMeters(50),  Effort.EASY,      course, "Active recovery"));

        cooldown.addSet(new SwimSet(StrokeType.FREESTYLE,4, yardsToMeters(50),  Effort.EASY,      course, "Silent swimming"));

        setCurrentSwimmer(swimmer);          // show the current swimmer in the right sidebar
        initCourseChoice();                  // populate the choicebox
        if (courseChoice == null) {
            courseChoice.setValue(w.getCourse()); // keep UI in sync with demo workout (SCY/SCM/LCM)
        }
        setWorkout(w);

        workoutTree.setShowRoot(false);
        workoutTree.setCellFactory(makeCellFactory());
    }

    /** Build tree items from the workout model. */
    private void rebuildTree() {
        TreeItem<Object> root = new TreeItem<>("ROOT"); // hidden
        if (workout != null) {
            for (SetGroup g : workout.getGroups()) {
                TreeItem<Object> gItem = new TreeItem<>(g);
                for (SwimSet s : g.getSets()) {
                    gItem.getChildren().add(new TreeItem<>(s));
                }
                gItem.setExpanded(true);
                root.getChildren().add(gItem);
            }
        }
        workoutTree.setRoot(root);
        refreshPreview();
    }

    /** Custom cells: render text + wire DnD handlers. */
    private Callback<TreeView<Object>, TreeCell<Object>> makeCellFactory() {
        return tv -> new TreeCell<>() {

            {
                // Drag start
                setOnDragDetected(e -> {
                    if (getItem() == null) return;
                    draggedItem = getTreeItem();
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString("move");
                    db.setContent(cc);
                    e.consume();
                });

                // Drag over
                setOnDragOver(e -> {
                    if (isValidDragTarget(getTreeItem())) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                // Drop
                setOnDragDropped(this::onDrop);
            }

            private boolean isValidDragTarget(TreeItem<Object> target) {
                return draggedItem != null && target != null && draggedItem != target;
            }

            private void onDrop(DragEvent e) {
                TreeItem<Object> target = getTreeItem();
                boolean success = false;
                if (isValidDragTarget(target)) {
                    success = handleDrop(draggedItem, target);
                }
                draggedItem = null;
                e.setDropCompleted(success);
                e.consume();
            }

            @Override
            protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                if (value instanceof SetGroup g) {
                    String reps = (g.getReps() > 1) ? "  x" + g.getReps() : "";
                    setText("▪ " + g.getName() + reps);
                } else if (value instanceof SwimSet s) {
                    String effort = (s.getEffort() != null) ? s.getEffort().getLabel() : "";
                    setText(s.getReps() + "x" + displayDistance(s) + " " + s.getStroke() + "  " + effort);
                } else {
                    setText(value.toString());
                }
            }
        };
    }

    /** Move the dragged item in the UI and mirror that order into the model. */
    private boolean handleDrop(TreeItem<Object> dragged, TreeItem<Object> target) {
        TreeItem<Object> oldParent = dragged.getParent();
        if (oldParent == null) return false;
        oldParent.getChildren().remove(dragged);

        if (target.getValue() instanceof SetGroup) {
            target.getChildren().add(dragged); // drop onto group => append
        } else if (target.getParent() != null) {
            int idx = target.getParent().getChildren().indexOf(target);
            target.getParent().getChildren().add(idx, dragged); // drop on set => insert before
        } else {
            return false;
        }

        syncModelFromTree();
        return true;
    }

    /** Rewrite workout groups/sets lists to match current tree order. */
    private void syncModelFromTree() {
        if (workout == null || workoutTree.getRoot() == null) return;

        List<SetGroup> groups = workout.getGroups();
        groups.clear();

        for (TreeItem<Object> gItem : workoutTree.getRoot().getChildren()) {
            if (!(gItem.getValue() instanceof SetGroup g)) continue;
            groups.add(g);

            g.getSets().clear();
            for (TreeItem<Object> sItem : gItem.getChildren()) {
                if (sItem.getValue() instanceof SwimSet s) {
                    g.getSets().add(s);
                }
            }
        }
        refreshPreview();
    }

    /** Format distance using workout course unit. */
    private String displayDistance(SwimSet s) {
        int meters = s.getDistancePerRepMeters();
        if (workout != null && workout.getCourse().getUnit() == CourseUnit.YARDS) {
            return (int) Math.round(meters / 0.9144) + "yd";
        }
        return meters + "m";
    }

    // --- Menu actions ---

    @FXML
    private void handleAbout(ActionEvent e) {
        new Alert(Alert.AlertType.INFORMATION,
                "SwimWorkoutBuilder\nMVP UI + pacing engine.\n© 2025").showAndWait();
    }

    @FXML // allow MenuItem/Button onAction to call this
    private void handleNewSwimmer(ActionEvent e) {
        try {
            // Load the dialog UI
            var fxml = getClass().getResource("/ui/SwimmerForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/SwimmerForm.fxml");

            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            // Controller gives us the result after the dialog closes
            SwimmerFormController controller = loader.getController();

            // Build a modal dialog owned by this window
            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle("Swimmer Setup");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            // Block here until Save/Cancel
            dialog.showAndWait();

            // Read back the result (null if user cancelled)
            var created = controller.getResult();
            if (created != null) {
                try {
                    swimworkoutbuilder.model.io.SwimmerRepository.append(created);
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save swimmer:\n" + ex.getMessage()).showAndWait();
                    return;
                }
                setCurrentSwimmer(created); // updates “Swimmer: …” label
                new Alert(Alert.AlertType.INFORMATION,
                        "Created swimmer:\n" +
                                created.getFirstName() + " " + created.getLastName() +
                                (created.getPreferredName() != null ? " (“" + created.getPreferredName() + "”)" : "") +
                                (created.getTeamName() != null ? "\nTeam: " + created.getTeamName() : "")
                ).showAndWait();

                // TODO: AppState.setCurrentSwimmer(created); update UI as needed.
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not open Create Swimmer form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }
}