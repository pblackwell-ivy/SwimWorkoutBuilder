package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import swimworkoutbuilder.model.Swimmer;

/**
 * Controller for the <b>“New Swimmer”</b> dialog window.
 *
 * <p>This class handles all user interaction with the swimmer creation form,
 * which allows entry of basic identity information such as first name, last name,
 * preferred name, and team name.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Initialize and validate user input fields defined in {@code SwimmerForm.fxml}.</li>
 *   <li>Enable or disable the Save button depending on required field completion.</li>
 *   <li>Create a {@link Swimmer} object when the user clicks Save.</li>
 *   <li>Return the result object (or {@code null} if cancelled) to the caller.</li>
 *   <li>Manage dialog lifecycle (close on Save or Cancel).</li>
 * </ul>
 *
 * <h2>FXML Bindings</h2>
 * <ul>
 *   <li>{@code tfFirstName}, {@code tfLastName} — required text fields</li>
 *   <li>{@code tfPreferredName}, {@code tfTeamName} — optional fields</li>
 *   <li>{@code btSave}, {@code btCancel} — action buttons</li>
 *   <li>{@code lblCreateSwimmer} — optional header label (not functionally required)</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * FXMLLoader loader = new FXMLLoader(getClass().getResource("SwimmerForm.fxml"));
 * Parent root = loader.load();
 * SwimmerFormController controller = loader.getController();
 *
 * // Show dialog modally
 * Stage dialog = new Stage();
 * dialog.setScene(new Scene(root));
 * dialog.showAndWait();
 *
 * Swimmer swimmer = controller.getResult(); // null if cancelled
 * }</pre>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see swimworkoutbuilder.model.Swimmer
 */
public class SwimmerFormController {

    // --------------------------------------------------------------------
    // FXML-injected UI components
    // --------------------------------------------------------------------
    @FXML private Label lblCreateSwimmer;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfPreferredName;
    @FXML private TextField tfTeamName;
    @FXML private Button btSave;
    @FXML private Button btCancel;

    // --------------------------------------------------------------------
    // Result model (null if cancelled)
    // --------------------------------------------------------------------
    private Swimmer result;
    public Swimmer getResult() { return result; }

    // --------------------------------------------------------------------
    // Initialization
    // --------------------------------------------------------------------
    /**
     * Called automatically after FXML loading.
     * <p>Sets up control behaviors, including:</p>
     * <ul>
     *   <li>Marks the Save button as the default (triggered by Enter).</li>
     *   <li>Marks the Cancel button as the cancel action (triggered by Esc).</li>
     *   <li>Disables Save until both first and last name fields are populated.</li>
     * </ul>
     */
    @FXML
    private void initialize() {
        btSave.setDefaultButton(true);
        btCancel.setCancelButton(true);

        btSave.disableProperty().bind(
                tfFirstName.textProperty().isEmpty()
                        .or(tfLastName.textProperty().isEmpty())
        );
    }

    // --------------------------------------------------------------------
    // Event Handlers
    // --------------------------------------------------------------------

    /**
     * Handles the Save button click.
     * <p>Validates required fields, constructs a {@link Swimmer} object,
     * assigns it to {@code result}, and closes the dialog window.</p>
     * <p>If validation fails, shows a warning alert and keeps the dialog open.</p>
     */
    @FXML
    private void onSave() {
        String first = tfFirstName.getText().trim();
        String last  = tfLastName.getText().trim();

        if (first.isEmpty() || last.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "First and Last name are required.").showAndWait();
            return;
        }

        String pref = tfPreferredName.getText().trim();
        String team = tfTeamName.getText().trim();

        result = new Swimmer(
                first,
                last,
                pref.isEmpty() ? null : pref,
                team.isEmpty() ? null : team
        );

        closeWindow();
    }

    /**
     * Handles the Cancel button click.
     * <p>Sets {@code result} to {@code null} and closes the dialog window.</p>
     */
    @FXML
    private void onCancel() {
        result = null;
        closeWindow();
    }

    /**
     * Utility method that closes the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) btSave.getScene().getWindow();
        stage.close();
    }
}