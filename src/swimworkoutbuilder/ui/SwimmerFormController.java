package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import swimworkoutbuilder.model.Swimmer;

public class SwimmerFormController {

    // --- FXML fields ---
    @FXML private Label lblCreateSwimmer; // not required but fine to keep
    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfPreferredName;
    @FXML private TextField tfTeamName;
    @FXML private Button btSave;
    @FXML private Button btCancel;

    // --- Result object (null if cancelled) ---
    private Swimmer result;
    public Swimmer getResult() { return result; }

    @FXML
    private void initialize() {
        // Make keyboard-friendly:
        btSave.setDefaultButton(true);     // Enter triggers Save
        btCancel.setCancelButton(true);    // Esc triggers Cancel

        // Disable Save until required fields present:
        btSave.disableProperty().bind(
                tfFirstName.textProperty().isEmpty()
                        .or(tfLastName.textProperty().isEmpty())
        );
    }

    @FXML
    private void onSave() {
        String first = tfFirstName.getText().trim();
        String last  = tfLastName.getText().trim();

        // Double-check (belt & suspenders)
        if (first.isEmpty() || last.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "First and Last name are required.").showAndWait();
            return;
        }

        String pref = tfPreferredName.getText().trim();
        String team = tfTeamName.getText().trim();

        // Construct your domain object (null for optional empties is fine)
        result = new Swimmer(
                first,
                last,
                pref.isEmpty() ? null : pref,
                team.isEmpty() ? null : team
        );

        closeWindow();


    }

    @FXML
    private void onCancel() {
        result = null; // explicitly mark as cancelled
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btSave.getScene().getWindow();
        stage.close();
    }
}