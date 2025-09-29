package swimworkoutbuilder.model.pacing;

import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;

/** Strategy interface for turning sets + seeds into concrete timing. */
public interface PacePolicy {

    /** Goal time (seconds) for a single rep (not the send-off). */
    double goalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /**
     * Interval/send-off (seconds) for a single rep.
     * MVP rule: interval = round(goal) + rest.
     */
    int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /** Rest after the rep (seconds). MVP rule: derived from Effort. */
    int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /** Optional short label for UI/printer (e.g., "rest :20"). */
    default String timingLabel(Workout workout, SwimSet set) {
        return "rest :" + String.format("%02d", restSeconds(workout, set, null, 1));
    }
}