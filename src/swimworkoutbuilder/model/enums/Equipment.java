package swimworkoutbuilder.model.enums;

/**
 * Equipment factors adjust target pace calculations to account for training aids.
 * Multipliers are applied as part of the pace policy, and multiple pieces of
 * equipment can be combined (multipliers multiplied together).
 *
 * Typical defaults (tunable):
 *  - FINS:      0.88 (faster, strong kick propulsion)
 *  - PADDLES:   0.96 (slightly faster, more pull power)
 *  - PULL_BUOY: 1.05 (slower overall pace, less kick drive)
 *  - SNORKEL:   0.99 (neutral to slightly slower)
 *  - DRAG_SOCKS:1.15 (slower due to resistance)
 *  - PARACHUTE: 1.20 (significantly slower due to resistance)
 */
public enum Equipment {
    FINS("Fins", 0.88),
    PADDLES("Paddles", 0.96),
    PULL_BUOY("Pull Buoy", 1.05),
    SNORKEL("Snorkel", 0.99),
    DRAG_SOCKS("Drag Socks", 1.15),
    PARACHUTE("Parachute", 1.20);

    private final String label;
    private final double multiplier;

    Equipment(String label, double multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    /** User-friendly label for UI display. */
    public String getLabel() {
        return label;
    }

    /** Performance multiplier for this equipment. */
    public double multiplier() {
        return multiplier;
    }

    @Override
    public String toString() {
        return label;
    }
}