package swimworkoutbuilder;

import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.*;
import swimworkoutbuilder.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder.model.pacing.PacePolicy;
import swimworkoutbuilder.model.utils.WorkoutPrinter;

import java.util.Collections;

import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

/**
 * Main class for running the SwimWorkoutBuilder application to test functionality behind the UI.
 * 1) Create a Swimmer
 * 2) Define seed times for the swimmer
 * 3) Create a Workout
 * 4) Create SetGroups and SwimSets
 * 5) Print the workout using the default policy to calcualte pace and print to console
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🏊 SwimWorkoutBuilder is running!");
        // 1) Swimmer
        Swimmer swimmer = new Swimmer("Parker", "Blackwell", "", "Indy Aquatic Masters");

        // 2) Seeds (per 100, seconds)
        swimmer.updateSeedTime(StrokeType.FREESTYLE,  new SeedPace100(78.0, CourseUnit.YARDS));  // 1:18 / 100y
        swimmer.updateSeedTime(StrokeType.FREE_KICK,  new SeedPace100(118.0, CourseUnit.YARDS)); // 1:58 / 100y
        swimmer.updateSeedTime(StrokeType.DRILL,      new SeedPace100(110.0, CourseUnit.YARDS)); // 1:50 / 100y

        // 3) Workout
        Workout w = new Workout(
                swimmer.getId(),
                "Test Workout",
                Course.SCY,
                "Demo workout to validate pacing and totals.",
                60 // default rest between groups (seconds)
        );

        Course course = w.getCourse(); // convenience

        // 4) Groups
        SetGroup warmup   = new SetGroup("Warmup", 1, 1);
        warmup.setNotes("Focus on technique");

        SetGroup drills   = new SetGroup("Drills", 1, 2);
        drills.setNotes("Focus on alignment and rotation");

        SetGroup main     = new SetGroup("Main", 4, 3);
        SetGroup cooldown = new SetGroup("Cooldown", 1, 4);

        Collections.addAll(w.getGroups(), warmup, drills, main, cooldown);

        // 5) Sets (now pass course to SwimSet)
        warmup.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(400), Effort.EASY,      course, "Perfect Freestyle"));
        warmup.addSet(new SwimSet(StrokeType.FREE_KICK, 4, yardsToMeters(50),  Effort.EASY,      course, "Kick w/ streamline"));

        drills.addSet(new SwimSet(StrokeType.DRILL,     2, yardsToMeters(50),  Effort.ENDURANCE, course, "3 strokes / 6 kicks drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(50),  Effort.EASY,      course, "Apply the drill"));
        drills.addSet(new SwimSet(StrokeType.DRILL,     2, yardsToMeters(50),  Effort.ENDURANCE, course, "Rhythm drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(50),  Effort.EASY,      course, "Apply the drill"));

        main.addSet(new SwimSet(StrokeType.FREESTYLE,   4, yardsToMeters(50),  Effort.RACE_PACE, course, "USRPT style"));
        main.addSet(new SwimSet(StrokeType.FREESTYLE,   1, yardsToMeters(50),  Effort.EASY,      course, "Active recovery"));

        cooldown.addSet(new SwimSet(StrokeType.FREESTYLE,4, yardsToMeters(50),  Effort.EASY,      course, "Silent swimming"));

        // (Optional) Example of adding equipment to a set
        // main.getSets().get(0).addEquipment(Equipment.FINS);

        // 6) Print full workout using policy
        PacePolicy policy = new DefaultPacePolicy();
        WorkoutPrinter.printWorkout(w, swimmer, policy);
    }
}