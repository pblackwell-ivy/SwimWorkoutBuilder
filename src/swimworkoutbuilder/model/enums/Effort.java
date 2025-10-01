package swimworkoutbuilder.model.enums;

/**
 * Effort levels are essential for creating goal-oriented structured workouts.
 * v1: enum name + long description
 * v2: added label and short description for UI
 * v3: added paceMultiplier (scales seed /100) and restAllowanceSec (seconds added to goal for interval)
 */
public enum Effort {
    EASY(
            "Easy",
            "Warmup/cooldown, active recovery",
            "Active recovery, technique focus, light pace, minimal exertion. Used between challenging sets or for warm-up/cool-down.",
            1.70, 10
    ),
    ENDURANCE(
            "Endurance",
            "Aerobic, steady cruise pace",
            "Aerobic steady, cruise pace, able to sustain for long durations (~10+ minutes) with little to moderate rest. Develops aerobic capacity.",
            1.35, 15
    ),
    THRESHOLD(
            "Threshold",
            "Strong, controlled pace",
            "Lactate threshold effort. Strong but controlled pace, just below race intensity. Can sustain repeats of 3–5 minutes with short rest.",
            1.22, 20
    ),
    RACE_PACE(
            "Race Pace",
            "Target competition pace",
            "Target competition pace. Swim at the exact speed of your goal event to develop pacing and race endurance. Effort is high, but repeatable.",
            1.05, 30
    ),
    VO2_MAX(
            "VO2 Max",
            "Very intense, near max",
            "High aerobic power effort. Very intense pace, near maximum oxygen uptake, sustainable for ~1–3 minutes. Builds maximum aerobic capacity.",
            1.00, 40
    ),
    SPRINT(
            "Sprint",
            "All-out, maximal speed",
            "All-out, maximal effort. Short bursts (≤25–50m) at top speed, long recovery required. Focus on power, explosiveness, and pure speed.",
            0.95, 60
    );
    // v1: label and long description
    private final String label;
    private final String longDescription;

    // v2: short description for UI
    private final String shortDescription;

    // v3: multipliers and rest allowance
    private final double paceMultiplier;   // scales seed /100 for this effort
    private final int restAllowanceSec;    // seconds added to goal for interval

    Effort(String label, String shortDescription, String longDescription,
           double paceMultiplier, int restAllowanceSec) {
        this.label = label;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.paceMultiplier = paceMultiplier;
        this.restAllowanceSec = restAllowanceSec;
    }

    public String getLabel() { return label; }
    public String getShortDescription() { return shortDescription; }
    public String getLongDescription() { return longDescription; }

    /** Multiplier applied to seed pace per 100 for this effort level. */
    public double paceMultiplier() { return paceMultiplier; }

    /** Rest allowance (seconds) added to goal when computing interval. */
    public int restAllowanceSec() { return restAllowanceSec; }

    @Override
    public String toString() { return label; }
}