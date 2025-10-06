package swimworkoutbuilder.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.pacing.SeedPace;
import swimworkoutbuilder.model.units.Distance;

import java.util.EnumMap;
import java.util.Map;

/**
 * Controller for SeedForm.fxml (JavaFX 21).
 * - Shows swimmer name.
 * - Lets user enter per-100 seed times (yards or meters).
 * - Accepts mm:ss(.d) or plain seconds.
 * - Persists to Swimmer; reopens with values filled.
 */
public class SeedFormController {

    // header
    @FXML private Label lblTitle;
    @FXML private Label lblSwimmer;
    @FXML private RadioButton rbMeters;
    @FXML private RadioButton rbYards;

    // per-stroke fields
    @FXML private TextField tfFree;
    @FXML private TextField tfBack;
    @FXML private TextField tfBreast;
    @FXML private TextField tfFly;
    @FXML private TextField tfIM;
    @FXML private TextField tfKick;
    @FXML private TextField tfDrill;

    // buttons
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final ToggleGroup unitsGroup = new ToggleGroup();
    private final Map<StrokeType, TextField> fields = new EnumMap<>(StrokeType.class);

    private Swimmer swimmer;
    private Course initialCourse = Course.SCY;
    private boolean initialized;

    @FXML
    private void initialize() {
        // hook radios into a group and select a default
        rbMeters.setToggleGroup(unitsGroup);
        rbYards.setToggleGroup(unitsGroup);
        selectRadiosFromCourse(initialCourse);

        // map strokes -> fields
        fields.put(StrokeType.FREESTYLE,           tfFree);
        fields.put(StrokeType.BACKSTROKE,          tfBack);
        fields.put(StrokeType.BREASTSTROKE,        tfBreast);
        fields.put(StrokeType.BUTTERFLY,           tfFly);
        fields.put(StrokeType.INDIVIDUAL_MEDLEY,   tfIM);
        fields.put(StrokeType.KICK,                tfKick);
        fields.put(StrokeType.DRILL,               tfDrill);

        // update when units switch
        unitsGroup.selectedToggleProperty().addListener((o, a, b) -> prefillFromSeeds());

        // if the caller already set swimmer/course, reflect them now
        renderSwimmerName();
        prefillFromSeeds();

        initialized = true;
    }

    // called by MainViewController after loading the FXML
    public void setSwimmer(Swimmer s) {
        this.swimmer = s;
        // render immediately whether initialize() ran or not
        renderSwimmerName();
        prefillFromSeeds();
    }

    // called by MainViewController (uses workout course as hint)
    public void setInitialCourse(Course c) {
        if (c != null) initialCourse = c;
        selectRadiosFromCourse(initialCourse);
        prefillFromSeeds();
    }

    // --- actions ---

    @FXML
    private void handleCancel() { close(btnCancel); }

    @FXML
    private void handleSave() {
        if (swimmer != null) {
            boolean yards = isYardsSelected();
            saveIfPresent(StrokeType.FREESTYLE,         tfFree,  yards);
            saveIfPresent(StrokeType.BACKSTROKE,        tfBack,  yards);
            saveIfPresent(StrokeType.BREASTSTROKE,      tfBreast,yards);
            saveIfPresent(StrokeType.BUTTERFLY,         tfFly,   yards);
            saveIfPresent(StrokeType.INDIVIDUAL_MEDLEY, tfIM,    yards);
            saveIfPresent(StrokeType.KICK,              tfKick,  yards);
            saveIfPresent(StrokeType.DRILL,             tfDrill, yards);
        }
        close(btnSave);
    }

    // --- helpers ---

    private void renderSwimmerName() {
        if (lblSwimmer == null) return;
        if (swimmer == null) { lblSwimmer.setText("—"); return; }
        String base = safe(swimmer.getFirstName()) + " " + safe(swimmer.getLastName());
        String pref = swimmer.getPreferredName();
        lblSwimmer.setText((pref == null || pref.isBlank()) ? base : pref + " (" + base + ")");
    }

    private void prefillFromSeeds() {
        if (swimmer == null) return;
        boolean yards = isYardsSelected();
        for (var e : fields.entrySet()) {
            SeedPace sp = swimmer.getSeedTime(e.getKey());
            e.getValue().setText(formatPer100(sp, yards));
        }
    }

    private void saveIfPresent(StrokeType stroke, TextField tf, boolean yards) {
        if (tf == null) return;
        String txt = tf.getText();
        if (txt == null || txt.isBlank()) return;
        double seconds = parseTimeSeconds(txt.trim());
        if (Double.isNaN(seconds) || seconds <= 0) return;

        if (yards) swimmer.updateSeed100Y(stroke, seconds);
        else       swimmer.updateSeed100M(stroke, seconds);
    }

    private String formatPer100(SeedPace seed, boolean yards) {
        if (seed == null) return "";
        double metersPer100 = yards ? Distance.ofYards(100).toMeters()
                : Distance.ofMeters(100).toMeters();
        double v = seed.speedMps();
        if (v <= 0) return "";
        return formatSeconds(metersPer100 / v);
    }

    private void selectRadiosFromCourse(Course c) {
        if (c == null) c = Course.SCY;
        if (c == Course.SCY) { rbYards.setSelected(true); rbMeters.setSelected(false); }
        else                  { rbMeters.setSelected(true); rbYards.setSelected(false); }
    }

    private boolean isYardsSelected() {
        Toggle t = unitsGroup.getSelectedToggle();
        if (t == null) return initialCourse == Course.SCY;
        return t == rbYards;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private void close(Node n) {
        if (n == null || n.getScene() == null) return;
        Stage st = (Stage) n.getScene().getWindow();
        if (st != null) st.close();
    }

    // --- time parsing/formatting ---

    /** Accepts "mm:ss", "m:ss.d", "ss", "ss.dd". Returns NaN if invalid. */
    private static double parseTimeSeconds(String s) {
        if (s == null || s.isBlank()) return Double.NaN;
        s = s.trim();
        int i = s.indexOf(':');
        if (i >= 0) {
            String m = s.substring(0, i).trim();
            String sec = s.substring(i + 1).trim();
            try {
                int minutes = Integer.parseInt(m);
                double seconds = Double.parseDouble(sec);
                if (minutes < 0 || seconds < 0) return Double.NaN;
                return minutes * 60.0 + seconds;
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }
        try {
            double seconds = Double.parseDouble(s);
            return seconds >= 0 ? seconds : Double.NaN;
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    /** Nicely formats seconds into mm:ss(.d) or s(.d). */
    private static String formatSeconds(double secs) {
        if (!(secs > 0)) return "";
        int whole = (int)Math.floor(secs);
        int m = whole / 60;
        int s = whole % 60;
        double frac = secs - whole;
        if (m > 0) {
            if (frac >= 0.05) return String.format("%d:%02d.%1d", m, s, (int)Math.round(frac*10));
            return String.format("%d:%02d", m, s);
        }
        if (frac >= 0.05) return String.format("%.1f", secs);
        return Integer.toString(whole);
    }
}