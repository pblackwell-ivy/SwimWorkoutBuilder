package swimworkoutbuilder.model.enums;

/**
 * Effort levels are essential for creating goal-oriented structured workouts.  SwimWorkoutBuilder defines these
 * effort levels, consistent with how top-tier coaches think when putting together a practice.
 * v1: just the enum name and long description.
 * v2: added label and short description to be used in the UI.
 */
public enum Effort {
    EASY(
            "Easy",
            "Warmup/cooldown, active recovery",
            "Active recovery, technique focus, light pace, minimal exertion. Used between challenging sets or for warm-up/cool-down."
    ),
    ENDURANCE(
            "Endurance",
            "Aerobic, steady cruise pace",
            "Aerobic steady, cruise pace, able to sustain for long durations (~10+ minutes) with little to moderate rest. Develops aerobic capacity."
    ),
    THRESHOLD(
            "Threshold",
            "Strong, controlled pace",
            "Lactate threshold effort. Strong but controlled pace, just below race intensity. Can sustain repeats of 3-5 minutes with short rest."
    ),
    RACE_PACE(
            "Race Pace",
            "Target compeetition pace",
            "Target competition pace. Swim at the exact speed of your goal event to develop pacing and race endurance. Effort is high, but repeatable."
    ),
    VO2_MAX(
            "VO2 Max",
            "Very intense, near max",
            "High aerobic power effort. Very intense pace, near maximum oxygen uptake, sustainable for ~1-3 minutes. Builds maximum aerobic capacity."
    ),
    SPRINT(
            "Sprint",
            "All-out, maximal speed",
            "All-out, maximal effort. Short bursts (≤25-50m) at top speed, long recovery required. Focus on power, explosiveness, and pure speed."
    );

    private final String label;
    private final String shortDescription;
    private final String longDescription;

    Effort(String label, String shortDescription, String longDescription) {
        this.label = label;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    public String getLabel() {
        return label;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    @Override
    public String toString() {
        return label;
    }
}
