package swimworkoutbuilder.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import swimworkoutbuilder.model.SetGroup;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.units.Distance;

import swimworkoutbuilder.model.pacing.PacePolicy;
import swimworkoutbuilder.model.pacing.DefaultPacePolicy;

import java.util.List;
import java.util.UUID;

/** Controller for MainView.fxml. */
public class MainViewController {

    private final PacePolicy pace = new DefaultPacePolicy();

    @FXML private TreeView<Object> workoutTree;
    @FXML private ChoiceBox<Course> courseChoice;
    @FXML private Label lblSwimmer;        // right-pane helper label (we'll use for seed summary)
    @FXML private TextArea previewArea;

    // header panel
    @FXML private VBox  workoutHeader;
    @FXML private Label hdrSwimmer;
    @FXML private Label hdrName;
    @FXML private Label hdrNotes;
    @FXML private Label hdrSummaryDistance;
    @FXML private Label hdrSummaryDuration;
    @FXML private Label hdrSummarySwim;
    @FXML private Label hdrSummaryRest;
    @FXML private Label hdrSummaryTotal;
    @FXML private Label hdrPool;

    private Swimmer currentSwimmer;
    private Workout workout;

    @FXML
    private void initialize() {
        if (workoutTree == null) throw new IllegalStateException("workoutTree not injected.");

        workoutTree.setShowRoot(false);
        workoutTree.setCellFactory(makeCellFactory());
        workoutTree.setRoot(new TreeItem<>("ROOT"));

        workoutTree.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.DELETE || ke.getCode() == KeyCode.BACK_SPACE) {
                var sel = workoutTree.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    Object v = sel.getValue();
                    if (v instanceof SwimSet s) deleteSet(s);
                    else if (v instanceof SetGroup g) deleteGroup(g);
                }
            }
        });

        workoutTree.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/ui/mainview.css").toExternalForm());
            }
        });

        initCourseChoice();
        installHeaderContextMenu();

        refreshHeader();
        refreshPreview();
    }

    // public API
    public void setWorkout(Workout w) {
        this.workout = w;
        rebuildTree();
        refreshHeader();
    }

    public void setCurrentSwimmer(Swimmer s) {
        this.currentSwimmer = s;
        updateRightPaneSwimmerSummary();
        refreshHeader();
        rebuildTree(); // so timing appears on rows
    }

    // build/refresh tree + preview
    private void rebuildTree() {
        TreeItem<Object> root = new TreeItem<>("ROOT");
        if (workout != null) {
            for (SetGroup g : workout.getGroups()) {
                TreeItem<Object> gItem = new TreeItem<>(g);
                for (SwimSet s : g.getSets()) gItem.getChildren().add(new TreeItem<>(s));
                gItem.setExpanded(true);
                root.getChildren().add(gItem);
            }
        }
        workoutTree.setRoot(root);
        refreshPreview();
    }

    private void refreshPreview() {
        if (previewArea == null) return;
        if (workout == null) { previewArea.setText("(no workout)"); return; }

        StringBuilder sb = new StringBuilder();
        sb.append("Workout: ").append(nullToDash(workout.getName())).append("\n")
                .append("Course: ").append(workout.getCourse()).append("\n\n");

        int gi = 1;
        for (SetGroup g : workout.getGroups()) {
            sb.append(gi++).append(") ").append(g.getName());
            if (g.getReps() > 1) sb.append(" x").append(g.getReps());
            if (!isBlank(g.getNotes())) sb.append(" — ").append(g.getNotes());
            sb.append("\n");

            int si = 1;
            for (SwimSet s : g.getSets()) {
                String strokeShort = (s.getStroke() == null) ? "" : s.getStroke().getShortLabel();
                String effort      = (s.getEffort() == null) ? "" : s.getEffort().getLabel();

                String timing = computeTimingSnippet(workout, s); // "@ 1:05 (goal 0:51)"
                sb.append("   ").append(si++).append(". ")
                        .append(s.getReps()).append("x").append(displayDistance(s)).append(" ")
                        .append(strokeShort).append(timing.isEmpty() ? "" : " " + timing);
                if (!effort.isEmpty()) sb.append(", ").append(effort);
                if (!isBlank(s.getNotes())) sb.append(" — ").append(s.getNotes());
                sb.append("\n");
            }
            sb.append("\n");
        }
        previewArea.setText(sb.toString());
    }

    // cell factory
    private Callback<TreeView<Object>, TreeCell<Object>> makeCellFactory() {
        return tv -> new TreeCell<>() {
            private final VBox  card    = new VBox(2);
            private final HBox  header  = new HBox(6);
            private final Label title   = new Label();
            private final Label sub     = new Label();
            private final Region spacer = new Region();
            private final Button btnEdit     = new Button("Edit");
            private final Button btnAddSet   = new Button("Add Set");
            private final Button btnAddGroup = new Button("Add Group");
            private final ContextMenu cmenu  = new ContextMenu();
            private final MenuItem miEdit         = new MenuItem("Edit…");
            private final MenuItem miAddSet       = new MenuItem("Add Set…");
            private final MenuItem miAddGroupAfter= new MenuItem("Add Group After…");
            private final MenuItem miDelete       = new MenuItem("Delete");

            {
                card.getStyleClass().add("work-card");
                header.getStyleClass().add("work-card-header");
                title.getStyleClass().add("work-card-title");
                sub.getStyleClass().add("work-card-sub");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                header.getChildren().addAll(title, spacer, btnEdit);
                card.getChildren().setAll(header);
                btnEdit.getStyleClass().add("compact-btn");
                btnAddSet.getStyleClass().add("compact-btn");
                btnAddGroup.getStyleClass().add("compact-btn");

                btnEdit.setOnAction(e -> onEditClicked());
                btnAddSet.setOnAction(e -> onAddSetClicked());
                btnAddGroup.setOnAction(e -> onAddGroupClicked());

                miEdit.setOnAction(e -> onEditClicked());
                miAddSet.setOnAction(e -> onAddSetClicked());
                miAddGroupAfter.setOnAction(e -> onAddGroupClicked());
                miDelete.setOnAction(e -> onDeleteClicked());
            }

            private void onEditClicked() {
                Object v = getItem();
                if (v instanceof SetGroup g) {
                    SetGroup edited = showSetGroupDialog(g);
                    if (edited != null) {
                        g.setName(edited.getName());
                        g.setReps(edited.getReps());
                        g.setNotes(edited.getNotes());
                        g.setRestAfterGroupSec(edited.getRestAfterGroupSec());
                        rebuildTree(); refreshHeader();
                    }
                } else if (v instanceof SwimSet s) {
                    SwimSet edited = showSetFormDialog(null, s);
                    if (edited != null) {
                        s.setStroke(edited.getStroke());
                        s.setEffort(edited.getEffort());
                        s.setReps(edited.getReps());
                        s.setDistancePerRep(edited.getDistancePerRep());
                        s.setNotes(edited.getNotes());
                        s.setEquipment(edited.getEquipment());
                        rebuildTree(); refreshHeader();
                    }
                }
            }
            private void onAddSetClicked() {
                Object v = getItem();
                if (!(v instanceof SetGroup g)) return;
                SwimSet created = showSetFormDialog(g, null);
                if (created == null) return;
                g.addSet(created); rebuildTree(); refreshHeader();
            }
            private void onAddGroupClicked() {
                Object v = getItem();
                if (!(v instanceof SetGroup anchor)) return;
                SetGroup created = showSetGroupDialog(null);
                if (created == null) return;
                List<SetGroup> groups = workout.getGroups();
                int anchorIdx = groups.indexOf(anchor);
                groups.add(anchorIdx >= 0 ? anchorIdx + 1 : groups.size(), created);
                normalizeGroupOrders(); rebuildTree(); refreshHeader();
            }
            private void onDeleteClicked() {
                Object v = getItem();
                if (v instanceof SwimSet s) deleteSet(s);
                else if (v instanceof SetGroup g) deleteGroup(g);
            }

            @Override
            protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                setText(null); setGraphic(null); setContextMenu(null);
                if (empty || value == null) return;

                header.getChildren().setAll(title, spacer, btnEdit);
                card.getChildren().setAll(header);
                sub.setText("");

                if (value instanceof SetGroup g) {
                    String reps = (g.getReps() > 1) ? " (x" + g.getReps() + ")" : "";
                    title.setText(g.getName() + reps);
                    if (!isBlank(g.getNotes())) { sub.setText(g.getNotes()); card.getChildren().add(sub); }
                    header.getChildren().addAll(btnAddSet, btnAddGroup);
                    cmenu.getItems().setAll(miEdit, miAddSet, miAddGroupAfter, new SeparatorMenuItem(), miDelete);
                    setContextMenu(cmenu);
                    setGraphic(card);

                } else if (value instanceof SwimSet s) {
                    String strokeShort = (s.getStroke() == null) ? "" : s.getStroke().getShortLabel();
                    String effort      = (s.getEffort() != null) ? s.getEffort().getLabel() : "";
                    String eqText      = formatEquipment(s.getEquipment());

                    String timing = computeTimingSnippet(workout, s); // "@ 1:05 (goal 0:51)"
                    StringBuilder tl = new StringBuilder()
                            .append(s.getReps()).append("×")
                            .append(displayDistance(s)).append(" ")
                            .append(strokeShort);
                    if (!timing.isEmpty()) tl.append(" ").append(timing);
                    if (!effort.isEmpty()) tl.append(", ").append(effort);
                    if (!eqText.isEmpty()) tl.append("  [").append(eqText).append("]");
                    title.setText(tl.toString());

                    if (!isBlank(s.getNotes())) { sub.setText(s.getNotes()); card.getChildren().add(sub); }
                    cmenu.getItems().setAll(miEdit, new SeparatorMenuItem(), miDelete);
                    setContextMenu(cmenu);
                    setGraphic(card);
                } else {
                    setText(value.toString());
                }
            }
        };
    }

    private void installHeaderContextMenu() {
        if (workoutHeader == null) return;
        var cm = new ContextMenu();
        var miNew   = new MenuItem("New Workout…");
        var miEdit  = new MenuItem("Edit Workout…");
        var miDel   = new MenuItem("Delete Workout…");
        var miSeeds = new MenuItem("Seed Times…");
        miNew.setOnAction(this::handleNewWorkout);
        miEdit.setOnAction(this::handleEditWorkout);
        miDel.setOnAction(this::handleDeleteWorkout);
        miSeeds.setOnAction(this::handleEditSeeds);
        cm.getItems().addAll(miNew, miEdit, miDel, new SeparatorMenuItem(), miSeeds);
        workoutHeader.setOnContextMenuRequested(e -> cm.show(workoutHeader, e.getScreenX(), e.getScreenY()));
    }

    private void refreshHeader() {
        if (hdrSwimmer != null) {
            hdrSwimmer.setText("Swimmer: " + (currentSwimmer == null ? "—" : displayName(currentSwimmer)));
        }

        if (workout == null) {
            if (hdrName != null) hdrName.setText("—");
            if (hdrNotes != null) { hdrNotes.setText(""); hdrNotes.setVisible(false); hdrNotes.setManaged(false); }
            if (hdrSummaryDistance != null) hdrSummaryDistance.setText("— yds");
            if (hdrSummaryDuration != null) hdrSummaryDuration.setText("Duration:");
            if (hdrSummarySwim != null) hdrSummarySwim.setText("— swimming");
            if (hdrSummaryRest != null) hdrSummaryRest.setText("— rest");
            if (hdrSummaryTotal != null) hdrSummaryTotal.setText("— total");
            if (hdrPool != null) hdrPool.setText("Pool Length: —");
            updateRightPaneSwimmerSummary();
            return;
        }

        if (hdrName != null) hdrName.setText(nullToDash(workout.getName()));
        if (hdrNotes != null) {
            String notes = workout.getNotes();
            boolean has = !isBlank(notes);
            hdrNotes.setText(has ? notes : "");
            hdrNotes.setVisible(has); hdrNotes.setManaged(has);
        }

        Distance total = computeTotalDistance(workout);
        if (hdrSummaryDistance != null) hdrSummaryDistance.setText(fmtDistanceForCourse(total, workout.getCourse()));
        if (hdrSummaryDuration != null) hdrSummaryDuration.setText("Duration:");
        if (hdrSummarySwim != null)     hdrSummarySwim.setText("— swimming");
        if (hdrSummaryRest != null)     hdrSummaryRest.setText("— rest");
        if (hdrSummaryTotal != null)    hdrSummaryTotal.setText("— total");
        if (hdrPool != null) {
            String pool = switch (workout.getCourse()) {
                case SCY -> "25 yds";
                case SCM -> "25 m";
                case LCM -> "50 m";
            };
            hdrPool.setText("Pool Length: " + pool);
        }

        if (courseChoice != null) {
            if (!courseChoice.getItems().contains(workout.getCourse())) initCourseChoice();
            courseChoice.setValue(workout.getCourse());
        }

        updateRightPaneSwimmerSummary();
    }

    // --- Menu handlers & dialogs (unchanged except for refreshes) ---
    @FXML
    private void handleEditSeeds(ActionEvent e) {
        handleEditSeeds(); // delegate to the no-arg version
    }
    @FXML
    private void handleNewSwimmer(ActionEvent e) {
        try {
            var fxml = getClass().getResource("/ui/SwimmerForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/SwimmerForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            SwimmerFormController c = loader.getController();

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle("Swimmer Setup");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Swimmer created = c.getResult();
            if (created != null) {
                setCurrentSwimmer(created);
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Swimmer form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleEditSeeds() {
        if (currentSwimmer == null) {
            new Alert(Alert.AlertType.WARNING, "Create/select a swimmer first.").showAndWait();
            return;
        }
        try {
            var fxml = getClass().getResource("/ui/SeedForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            SeedFormController c = loader.getController();
            c.setSwimmer(currentSwimmer);
            c.setInitialCourse(workout != null ? workout.getCourse() : Course.SCY);

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle("Seed Times");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            // After seeds change: refresh timing everywhere
            updateRightPaneSwimmerSummary();
            refreshHeader();
            rebuildTree();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Seed Times form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleNewWorkout(ActionEvent e) {
        if (currentSwimmer == null) {
            new Alert(Alert.AlertType.WARNING, "Create a swimmer first.").showAndWait();
            return;
        }
        try {
            var fxml = getClass().getResource("/ui/WorkoutForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/WorkoutForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            WorkoutFormController c = loader.getController();
            UUID swimmerId = currentSwimmer.getId();
            c.setSwimmerId(swimmerId);

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle("New Workout");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Workout created = c.getResult();
            if (created != null) {
                setWorkout(created);
                if (courseChoice != null) courseChoice.setValue(created.getCourse());
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Workout form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleEditWorkout(ActionEvent e) {
        if (workout == null) {
            new Alert(Alert.AlertType.INFORMATION, "No workout to edit.").showAndWait();
            return;
        }
        try {
            var fxml = getClass().getResource("/ui/WorkoutForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/WorkoutForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            WorkoutFormController c = loader.getController();
            try {
                var m = c.getClass().getMethod("setInitial", Workout.class);
                m.invoke(c, workout);
            } catch (ReflectiveOperationException ignore) { }

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle("Edit Workout");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Workout edited = c.getResult();
            if (edited != null) {
                workout.setName(edited.getName());
                workout.setCourse(edited.getCourse());
                workout.setNotes(edited.getNotes());
                refreshHeader(); rebuildTree();
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Workout form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteWorkout(ActionEvent e) {
        if (workout == null) {
            new Alert(Alert.AlertType.INFORMATION, "No workout to delete.").showAndWait();
            return;
        }
        boolean ok = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete the current workout \"" + workout.getName() + "\"?",
                ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        if (!ok) return;

        workout = null;
        workoutTree.setRoot(new TreeItem<>("ROOT"));
        refreshHeader(); refreshPreview();
    }

    @FXML private void handleAddGroup() {
        if (workout == null) { new Alert(Alert.AlertType.WARNING, "Create a workout first.").showAndWait(); return; }
        SetGroup created = showSetGroupDialog(null);
        if (created == null) return;
        workout.getGroups().add(created);
        normalizeGroupOrders(); rebuildTree(); refreshHeader();
    }

    @FXML private void handleAddSet() {
        if (workout == null) { new Alert(Alert.AlertType.WARNING, "Create a workout first.").showAndWait(); return; }
        var sel = workoutTree.getSelectionModel().getSelectedItem();
        if (sel == null || !(sel.getValue() instanceof SetGroup g)) {
            new Alert(Alert.AlertType.INFORMATION, "Select a group to add a set.").showAndWait(); return;
        }
        SwimSet created = showSetFormDialog(g, null);
        if (created == null) return;
        g.addSet(created);
        rebuildTree(); refreshHeader();
    }

    @FXML
    private void handleAbout(ActionEvent e) {
        new Alert(Alert.AlertType.INFORMATION, "SwimWorkoutBuilder\nMVP UI + pacing engine.\n© 2025").showAndWait();
    }

    // dialogs
    private SwimSet showSetFormDialog(SetGroup parent, SwimSet initial) {
        try {
            var fxml = getClass().getResource("/ui/SetForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/SetForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            SetFormController c = loader.getController();
            c.setCourse(workout != null ? workout.getCourse() : Course.SCY);
            if (initial != null) c.setInitial(initial);

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle(initial == null ? "New Set" : "Edit Set");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return c.getResult();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Set form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
            return null;
        }
    }

    private SetGroup showSetGroupDialog(SetGroup initial) {
        try {
            var fxml = getClass().getResource("/ui/SetGroupForm.fxml");
            if (fxml == null) throw new IllegalStateException("Missing /ui/SetGroupForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            SetGroupFormController c = loader.getController();
            if (initial != null) c.setInitial(initial);

            Stage owner = (Stage) workoutTree.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle(initial == null ? "New Set Group" : "Edit Set Group");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return c.getResult();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not open Set Group form:\n" + ex.getMessage()).showAndWait();
            ex.printStackTrace();
            return null;
        }
    }

    // delete helpers
    private void deleteSet(SwimSet target) {
        if (workout == null || target == null) return;
        SetGroup parent = workout.getGroups().stream()
                .filter(g -> g.getSets().contains(target)).findFirst().orElse(null);
        if (parent == null) return;
        if (!confirm("Delete Set", "Delete this set?\n\n" + target)) return;
        parent.getSets().remove(target);
        rebuildTree(); refreshHeader();
    }

    private void deleteGroup(SetGroup g) {
        if (workout == null || g == null) return;
        if (!confirm("Delete Group", "Delete the group \"" + g.getName() + "\" and all its sets?")) return;
        workout.getGroups().remove(g);
        normalizeGroupOrders(); rebuildTree(); refreshHeader();
    }

    private boolean confirm(String title, String message) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title); alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // utilities
    private void normalizeGroupOrders() {
        if (workout == null) return;
        int order = 1;
        for (SetGroup g : workout.getGroups()) g.setOrder(order++);
    }

    private static Distance computeTotalDistance(Workout w) {
        double meters = 0.0;
        for (SetGroup g : w.getGroups()) {
            double perGroupMeters = 0.0;
            for (SwimSet s : g.getSets()) {
                perGroupMeters += s.getDistancePerRep().toMeters() * Math.max(1, s.getReps());
            }
            meters += perGroupMeters * Math.max(1, g.getReps());
        }
        return Distance.ofMeters(meters);
    }

    private static String fmtDistanceForCourse(Distance d, Course c) {
        int yards  = (int)Math.round(d.toYards());
        int meters = (int)Math.round(d.toMeters());
        return switch (c) {
            case SCY -> yards + " yds";
            case SCM, LCM -> meters + " m";
        };
    }

    private String displayDistance(SwimSet s) {
        if (workout != null && workout.getCourse() == Course.SCY) {
            int yards = (int)Math.round(s.getDistancePerRep().toYards());
            int snapped = (int)Math.round(yards / 25.0) * 25;
            return snapped + "yd";
        }
        int meters = (int)Math.round(s.getDistancePerRep().toMeters());
        return meters + "m";
    }

    private static String formatEquipment(java.util.Set<swimworkoutbuilder.model.enums.Equipment> eq) {
        if (eq == null || eq.isEmpty()) return "";
        return eq.stream()
                .map(e -> e.name().toLowerCase().replace('_', ' '))
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private void initCourseChoice() {
        if (courseChoice == null) return;
        courseChoice.getItems().setAll(Course.SCY, Course.SCM, Course.LCM);
        courseChoice.setValue(Course.SCY);
        courseChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldC, newC) -> {
            if (workout != null && newC != null) {
                workout.setCourse(newC);
                rebuildTree(); refreshHeader();
            }
        });
    }

    private static String nullToDash(String s) { return (s == null || s.isBlank()) ? "—" : s; }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    // ====== NEW: timing + seed summary ======

    /** Computes " @ 1:05 (goal 0:51)" for a set using the active swimmer; blank if not computable. */
    private String computeTimingSnippet(Workout w, SwimSet s) {
        if (w == null || currentSwimmer == null || s == null || s.getStroke() == null) return "";
        if (currentSwimmer.getSeedTime(s.getStroke()) == null) return "";
        try {
            double goalSecs = pace.goalSeconds(w, s, currentSwimmer, 0);
            int interval = pace.intervalSeconds(w, s, currentSwimmer, 0);
            int goalRounded = (int)Math.round(goalSecs);
            return " @ " + fmtSecs(interval) + " (goal " + fmtSecs(goalRounded) + ")";
        } catch (RuntimeException ex) {
            // missing data/etc — just hide timing
            return "";
        }
    }

    private static String fmtSecs(int secs) {
        if (secs < 0) secs = 0;
        int m = secs / 60, s = secs % 60;
        return (m > 0) ? (m + ":" + String.format("%02d", s)) : (s + "s");
    }

    /** Shows a compact seed summary in the right pane label. */
    private void updateRightPaneSwimmerSummary() {
        if (lblSwimmer == null) return;
        if (currentSwimmer == null) { lblSwimmer.setText("Swimmer: —"); return; }

        String name = displayName(currentSwimmer);
        Course c = (workout != null ? workout.getCourse() : Course.SCY);
        boolean yards = (c == Course.SCY);

        String free   = seedPer100(currentSwimmer, StrokeType.FREESTYLE, yards);
        String back   = seedPer100(currentSwimmer, StrokeType.BACKSTROKE, yards);
        String breast = seedPer100(currentSwimmer, StrokeType.BREASTSTROKE, yards);
        String fly    = seedPer100(currentSwimmer, StrokeType.BUTTERFLY, yards);
        String im     = seedPer100(currentSwimmer, StrokeType.INDIVIDUAL_MEDLEY, yards);

        String summary = String.format("Swimmer: %s   •  Seeds: Free %s, Back %s, Breast %s, Fly %s, IM %s",
                name,
                (free.isEmpty() ? "—" : free),
                (back.isEmpty() ? "—" : back),
                (breast.isEmpty() ? "—" : breast),
                (fly.isEmpty() ? "—" : fly),
                (im.isEmpty() ? "—" : im));

        lblSwimmer.setText(summary);
    }

    private static String seedPer100(Swimmer s, StrokeType st, boolean yards) {
        var sp = s.getSeedTime(st);
        if (sp == null) return "";
        double metersFor100 = yards ? Distance.ofYards(100).toMeters() : Distance.ofMeters(100).toMeters();
        double v = sp.speedMps();
        if (v <= 0) return "";
        int secs = (int)Math.round(metersFor100 / v);
        int m = secs / 60, sec = secs % 60;
        return (m > 0) ? (m + ":" + String.format("%02d", sec)) : (sec + "s");
    }

    private static String displayName(Swimmer s) {
        if (s == null) return "—";
        String base = s.getFirstName() + " " + s.getLastName();
        if (isBlank(s.getPreferredName())) return base;
        return s.getPreferredName() + " (" + base + ")";
    }
}