package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.Course;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * Modal form for creating or editing a Workout.
 *
 * Usage:
 *  - For NEW: call setSwimmerId(...), show window, then getResult() (null if cancelled).
 *  - For EDIT: call setSwimmerId(...), setInitial(existingWorkout), show window, then getResult().
 *              The MainViewController can then copy fields from result back into the existing model.
 */
public class WorkoutFormController {

    // FXML controls
    @FXML private TextField tfName;
    @FXML private ChoiceBox<Course> cbCourse;
    @FXML private Spinner<Integer> spnRest;   // used as "planned duration (minutes)" per your current constructor
    @FXML private TextArea taNotes;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Inputs / Outputs
    private UUID swimmerId;       // provided by caller
    private Workout initial;      // optional: if editing, we prefill from this
    private Workout result;       // null if user cancelled

    /** The caller reads this after the dialog closes. */
    public Workout getResult() { return result; }

    /** Must be provided by caller (used when constructing result). */
    public void setSwimmerId(UUID swimmerId) {
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
    }

    /**
     * Optional: provide the existing workout when editing.
     * This ONLY pre-fills the form; the caller should copy the returned result
     * back into the original object if they want to preserve object identity.
     */
    public void setInitial(Workout w) {
        this.initial = w;
        if (w == null) return;

        // Pre-fill fields from the existing workout
        tfName.setText(safe(w.getName()));
        cbCourse.setValue(w.getCourse() == null ? Course.SCY : w.getCourse());
        taNotes.setText(safe(w.getNotes()));

        // Duration minutes: your current Workout may or may not expose this.
        // We use reflection so this controller compiles either way.
        Integer minutes = reflectGetDurationMinutes(w);
        if (minutes == null) minutes = 60; // sensible default
        spnRest.getValueFactory().setValue(Math.max(0, minutes));
    }

    @FXML
    private void initialize() {
        // Populate course choices
        cbCourse.getItems().setAll(Course.SCY, Course.SCM, Course.LCM);
        cbCourse.setValue(Course.SCY);

        // Spinner setup (if not defined in FXML)
        if (spnRest.getValueFactory() == null) {
            spnRest.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 600, 60, 5)
            );
        }
        spnRest.setEditable(true);

        // Keyboard affordances
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        // Guard save until required fields present
        btnSave.disableProperty().bind(
                tfName.textProperty().isEmpty()
                        .or(cbCourse.valueProperty().isNull())
        );
    }

    // --------------------------
    // Event handlers
    // --------------------------

    @FXML
    private void onSave() {
        if (swimmerId == null) {
            new Alert(Alert.AlertType.ERROR,
                    "No swimmer selected. Set the swimmer before creating a workout.")
                    .showAndWait();
            return;
        }

        String name   = trimOrNull(tfName.getText());
        Course course = cbCourse.getValue();
        String notes  = trimOrNull(taNotes.getText());
        Integer mins  = spnRest.getValue();          // we keep your current constructor usage

        if (name == null || course == null) {
            new Alert(Alert.AlertType.WARNING, "Workout name and course are required.").showAndWait();
            return;
        }

        // Create a *new* Workout instance. MainViewController will copy fields back on edit.
        result = new Workout(
                swimmerId,
                name,
                course,
                (notes == null || notes.isBlank()) ? null : notes,
                Math.max(0, mins == null ? 0 : mins)  // your constructor's 5th arg
        );

        close();
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    // --------------------------
    // Helpers
    // --------------------------

    private String safe(String s) { return s == null ? "" : s; }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Reflection accessor for getDurationMinutes() so this controller works
     * whether or not your Workout model defines that method yet.
     * Returns null if the method is missing or failed.
     */
    private Integer reflectGetDurationMinutes(Workout w) {
        try {
            Method m = w.getClass().getMethod("getDurationMinutes");
            Object v = m.invoke(w);
            if (v instanceof Number n) return n.intValue();
        } catch (Throwable ignore) {
            // Method absent on your current model — that's fine; we default elsewhere.
        }
        return null;
    }

    private void close() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}