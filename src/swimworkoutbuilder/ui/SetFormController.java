package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.Equipment;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.units.Distance;

import java.util.EnumSet;
import java.util.Set;

/**
 * Modal controller for creating/editing a {@link SwimSet}.
 * Course is injected (inherited from the workout) so distance snaps correctly.
 */
public class SetFormController {

    // Injected context + optional original
    private Course course;            // required (from workout)
    private SwimSet initial;          // null when creating

    // Result (null if cancelled)
    private SwimSet result;
    public SwimSet getResult() { return result; }

    // FXML
    @FXML private ChoiceBox<StrokeType> cbStroke;
    @FXML private ChoiceBox<Effort>     cbEffort;
    @FXML private Spinner<Integer>      spReps;
    @FXML private Spinner<Integer>      spDistance;
    @FXML private Label                 lblDistance;
    @FXML private CheckBox chkPullBuoy, chkPaddles, chkSnorkel, chkFins;
    @FXML private TextArea taNotes;
    @FXML private Button btnSave, btnCancel;

    // Track when controls are ready
    private boolean controlsReady = false;

    /** Parent must set the course (usually workout.getCourse()). */
    public void setCourse(Course course) {
        this.course = course;
        // If controls already exist, update label immediately
        if (lblDistance != null) {
            lblDistance.setText(course == Course.SCY
                    ? "Distance per rep (yards):"
                    : "Distance per rep (meters):");
        }
        prefillIfReady();
    }

    /** Optional: prefill for editing an existing set. */
    public void setInitial(SwimSet s) {
        this.initial = s;
        // If course not set yet, prefer the set's own course so units match
        if (this.course == null && s != null) this.course = s.getCourse();
        prefillIfReady();
    }

    @FXML
    private void initialize() {
        // Populate choices
        cbStroke.getItems().setAll(StrokeType.values());
        cbEffort.getItems().setAll(Effort.values());
        cbStroke.setValue(StrokeType.FREESTYLE);
        cbEffort.setValue(Effort.ENDURANCE);

        // Spinners
        spReps.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        spReps.setEditable(true);

        // Default distance step of 25 keeps UI snappy for pool lengths
        spDistance.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(25, 5000, 50, 25));
        spDistance.setEditable(true);

        // Buttons keyboard-friendly
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        controlsReady = true;
        // If parent already called setCourse/setInitial, this will now apply
        prefillIfReady();
    }

    /** Apply initial+course into controls if both the controls exist. */
    private void prefillIfReady() {
        if (!controlsReady) return;

        // Ensure distance label matches current course
        if (course != null && lblDistance != null) {
            lblDistance.setText(course == Course.SCY
                    ? "Distance per rep (yards):"
                    : "Distance per rep (meters):");
        }

        if (initial == null) return; // creating a new set → keep defaults

        // Stroke / Effort / Reps
        if (initial.getStroke() != null) cbStroke.setValue(initial.getStroke());
        if (initial.getEffort() != null) cbEffort.setValue(initial.getEffort());
        spReps.getValueFactory().setValue(initial.getReps());

        // Distance shown in the current course units
        int dist = 0;
        if (course == Course.SCY) {
            dist = (int)Math.round(initial.getDistancePerRep().toYards());
        } else {
            dist = (int)Math.round(initial.getDistancePerRep().toMeters());
        }
        spDistance.getValueFactory().setValue(Math.max(1, dist));

        // Notes
        taNotes.setText(initial.getNotes() == null ? "" : initial.getNotes());

        // Equipment
        Set<Equipment> eq = initial.getEquipment();
        chkPullBuoy.setSelected(eq != null && eq.contains(Equipment.PULL_BUOY));
        chkPaddles.setSelected(eq != null && eq.contains(Equipment.PADDLES));
        chkSnorkel.setSelected(eq != null && eq.contains(Equipment.SNORKEL));
        chkFins.setSelected(eq != null && eq.contains(Equipment.FINS));
    }

    @FXML
    private void onSave() {
        if (course == null) {
            new Alert(Alert.AlertType.ERROR, "Internal error: course not set.").showAndWait();
            return;
        }

        int reps   = spReps.getValue();
        int amount = spDistance.getValue();
        if (reps < 1 || amount <= 0) {
            new Alert(Alert.AlertType.WARNING, "Reps and distance must be positive.").showAndWait();
            return;
        }

        Distance perRep = (course == Course.SCY)
                ? Distance.ofYards(amount)
                : Distance.ofMeters(amount);

        SwimSet s = (initial == null)
                ? new SwimSet(cbStroke.getValue(), reps, perRep, cbEffort.getValue(), course, taNotes.getText().trim())
                : updateClone(initial, cbStroke.getValue(), reps, perRep, cbEffort.getValue(), course, taNotes.getText().trim());

        // Equipment
        Set<Equipment> eq = EnumSet.noneOf(Equipment.class);
        if (chkPullBuoy.isSelected()) eq.add(Equipment.PULL_BUOY);
        if (chkPaddles.isSelected())  eq.add(Equipment.PADDLES);
        if (chkSnorkel.isSelected())  eq.add(Equipment.SNORKEL);
        if (chkFins.isSelected())     eq.add(Equipment.FINS);
        s.setEquipment(eq);

        result = s;
        close();
    }

    private SwimSet updateClone(SwimSet orig, StrokeType stroke, int reps, Distance perRep,
                                Effort effort, Course course, String notes) {
        SwimSet s = new SwimSet(stroke, reps, perRep, effort, course, notes);
        s.setEquipment(orig.getEquipment());
        return s;
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}