package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swimworkoutbuilder.model.SetGroup;

public class SetGroupFormController {

    @FXML private TextField tfName;
    @FXML private Spinner<Integer> spnReps;
    @FXML private TextArea taNotes;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private SetGroup result;
    public SetGroup getResult() { return result; }

    /** Optional: preload for "Edit Group" use-case. */
    public void setInitial(SetGroup g) {
        if (g == null) return;
        tfName.setText(g.getName());
        spnReps.getValueFactory().setValue(Math.max(1, g.getReps()));
        taNotes.setText(g.getNotes() == null ? "" : g.getNotes());
    }

    @FXML
    private void initialize() {
        // reps 1..99 default 1
        spnReps.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        spnReps.setEditable(true);

        // disable Save until name provided
        btnSave.disableProperty().bind(
                tfName.textProperty().length().lessThanOrEqualTo(0)
        );
    }

    @FXML
    private void onSave() {
        String name = tfName.getText().trim();
        int reps = spnReps.getValue();
        String notes = taNotes.getText();

        // order is assigned by caller (insert position). Use 0 placeholder.
        result = new SetGroup(name, reps, 0);
        result.setNotes((notes == null || notes.isBlank()) ? "" : notes);

        close();
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