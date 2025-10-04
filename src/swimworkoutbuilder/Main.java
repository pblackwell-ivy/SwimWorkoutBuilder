package swimworkoutbuilder;

import javafx.application.Application;
import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.*;
import swimworkoutbuilder.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder.model.pacing.PacePolicy;
import swimworkoutbuilder.model.utils.WorkoutPrinter;
import swimworkoutbuilder.model.units.Distance;

import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        System.out.println("🏊 SwimWorkoutBuilder is running!");

        // 1) Swimmer
        Swimmer swimmer = new Swimmer("Parker", "Blackwell", "", "Indy Aquatic Masters");

        // 2) Seeds (per 100 yards, in seconds)
        swimmer.updateSeed100Y(StrokeType.FREESTYLE, 78.0);  // 1:18 / 100y
        swimmer.updateSeed100Y(StrokeType.KICK,      118.0); // 1:58 / 100y
        swimmer.updateSeed100Y(StrokeType.DRILL,     110.0); // 1:50 / 100y

        // 3) Workout (SCY)
        Workout w = new Workout(
                swimmer.getId(),
                "Test Workout",
                Course.SCY,
                "Demo workout to validate pacing and totals.",
                60 // default rest between groups (seconds)
        );

        Course course = w.getCourse();

        // 4) Groups
        SetGroup warmup   = new SetGroup("Warmup",   1, 1); warmup.setNotes("Focus on technique");
        SetGroup drills   = new SetGroup("Drills",   1, 2); drills.setNotes("Focus on alignment and rotation");
        SetGroup main     = new SetGroup("Main",     4, 3);
        SetGroup cooldown = new SetGroup("Cooldown", 1, 4);

        Collections.addAll(w.getGroups(), warmup, drills, main, cooldown);

        // 5) Sets
        warmup.addSet(new SwimSet(StrokeType.FREESTYLE, 1, Distance.ofYards(400), Effort.EASY,      course, "Perfect Freestyle"));
        warmup.addSet(new SwimSet(StrokeType.KICK,      4, Distance.ofYards(50),  Effort.EASY,      course, "Kick w/ streamline"));

        drills.addSet(new SwimSet(StrokeType.DRILL,     2, Distance.ofYards(50),  Effort.ENDURANCE, course, "3 strokes / 6 kicks drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, Distance.ofYards(50),  Effort.EASY,      course, "Apply the drill"));
        drills.addSet(new SwimSet(StrokeType.DRILL,     2, Distance.ofYards(50),  Effort.ENDURANCE, course, "Rhythm drill"));
        drills.addSet(new SwimSet(StrokeType.FREESTYLE, 1, Distance.ofYards(50),  Effort.EASY,      course, "Apply the drill"));

        main.addSet(new SwimSet(StrokeType.FREESTYLE,   4, Distance.ofYards(50),  Effort.RACE_PACE, course, "USRPT style"));
        main.addSet(new SwimSet(StrokeType.FREESTYLE,   1, Distance.ofYards(50),  Effort.EASY,      course, "Active recovery"));

        cooldown.addSet(new SwimSet(StrokeType.FREESTYLE, 4, Distance.ofYards(50), Effort.EASY, course, "Silent swimming"));

        // 6) Print to console (same as before)
        PacePolicy policy = new DefaultPacePolicy();
        WorkoutPrinter.printWorkout(w, swimmer, policy);

        // 7) Hand the workout to the JavaFX App and launch the UI
        App.setWorkout(w);
        Application.launch(App.class, args);
    }
}