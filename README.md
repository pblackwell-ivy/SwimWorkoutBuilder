# SwimWorkoutBuilder 🏊

A Java project built for **Ivy Tech SWDEV-200 (Software Development in Java)** as the **Final Project**.  
The goal of this stage of the project is to design a **console-based MVP (Minimum Viable Product)** for planning and summarizing swim workouts before JavaFX UI is added.

---

## 📂 Project Structure
```text
SwimWorkoutBuilder/
├── src/swimworkoutbuilder/
│   ├── Main.java                 # Entry point – builds a sample workout and prints summary
│   ├── model/                    # Core domain classes
│   │   ├── Swimmer.java          # Represents an individual swimmer (names, seed paces)
│   │   ├── Workout.java          # A full workout (metadata + ordered SetGroups)
│   │   ├── SetGroup.java         # A logical group of SwimSets (Warmup, Main, etc.)
│   │   ├── SwimSet.java          # A single training set (reps × distance × effort)
│   │   ├── SeedPace100.java      # Baseline seed time for a stroke (per 100 yards/meters)
│   │   ├── enums/                # Enumerations (Course, CourseUnit, Effort, StrokeType)
│   │   ├── pacing/               # Interfaces & policies for timing logic
│   │   │   ├── PacePolicy.java
│   │   │   └── DefaultPacePolicy.java
│   │   └── utils/                # Helpers (unit conversions, printing, etc.)
│   │       ├── Distance.java
│   │       └── WorkoutPrinter.java
│   └── …
└── Test/swimworkoutbuilder/      # JUnit 5 tests
├── EnumsTest.java
├── DefaultPacePolicyTest.java
├── SeedPace100Test.java
├── SetGroupTest.java
├── SwimmerTest.java
├── SwimSetTest.java
└── WorkoutAggregationTest.java

---
```text

## 🚀 Features (MVP)
- **Workout hierarchy**:  
  `Swimmer → Workout → SetGroup → SwimSet`
- **Seed paces**: store baseline times for strokes (e.g., 100 Free in 1:18).
- **Effort levels**: Easy, Endurance, Threshold, Race Pace, VO2 Max, Sprint.
- **Timing policy**: Goal times and intervals calculated from seed pace + effort.
- **Console printer**: Nicely formatted summary of workouts.
- **Unit conversion**: Distances stored canonically in meters, but displayed in workout’s course unit (SCY, SCM, LCM).

---

## 📊 Example Output
/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home/bin/java -javaagent:/Applications/Utilities/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=53155 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/parkerblackwell/Documents/IntelliJProjects/SwimWorkoutBuilder/out/production/SwimWorkoutBuilder swimworkoutbuilder.Main
🏊 SwimWorkoutBuilder is running!
==================================================
Swimmer: Parker Blackwell  (id=dcaaf4fd)
Workout: Test Workout  [Short Course Yards (25 YARDS)]
Notes:   Demo workout to validate pacing and totals.

Groups (4):
  1) Warmup
     - Focus on technique
     1. 1x400yd FREESTYLE      Easy        
         #1  goal 5:28 | on 5:43 | rest 0:15
         note: Perfect Freestyle
     2. 4x50yd FREE_KICK      Easy        
         #1  goal 1:02 | on 1:17 | rest 0:15
         #2  goal 1:02 | on 1:17 | rest 0:15
         #3  goal 1:02 | on 1:17 | rest 0:15
         #4  goal 1:02 | on 1:17 | rest 0:15
         note: Kick w/ streamline
     Group totals: distance=600yd  swim=9:36  rest=1:15  total=10:51
     (+1:00 rest after group)

  2) Drills
     - Focus on alignment and rotation
     1. 2x50yd DRILL          Endurance   
         #1  goal 0:56 | on 1:16 | rest 0:20
         #2  goal 0:56 | on 1:16 | rest 0:20
         note: 3 strokes / 6 kicks drill
     2. 1x50yd FREESTYLE      Easy        
         #1  goal 0:41 | on 0:56 | rest 0:15
         note: Apply the drill
     3. 2x50yd DRILL          Endurance   
         #1  goal 0:56 | on 1:16 | rest 0:20
         #2  goal 0:56 | on 1:16 | rest 0:20
         note: Rhythm drill
     4. 1x50yd FREESTYLE      Easy        
         #1  goal 0:41 | on 0:56 | rest 0:15
         note: Apply the drill
     Group totals: distance=300yd  swim=5:06  rest=1:50  total=6:56
     (+1:00 rest after group)

  3) Main  x4
     1. 4x50yd FREESTYLE      Race Pace   
         #1  goal 0:38 | on 1:08 | rest 0:30
         #2  goal 0:38 | on 1:08 | rest 0:30
         #3  goal 0:38 | on 1:08 | rest 0:30
         #4  goal 0:38 | on 1:08 | rest 0:30
         note: USRPT style
     2. 1x50yd FREESTYLE      Easy        
         #1  goal 0:41 | on 0:56 | rest 0:15
         note: Active recovery
     Group totals: distance=1000yd  swim=12:52  rest=9:00  total=21:52
     (+1:00 rest after group)

  4) Cooldown
     1. 4x50yd FREESTYLE      Easy        
         #1  goal 0:41 | on 0:56 | rest 0:15
         #2  goal 0:41 | on 0:56 | rest 0:15
         #3  goal 0:41 | on 0:56 | rest 0:15
         #4  goal 0:41 | on 0:56 | rest 0:15
         note: Silent swimming
     Group totals: distance=200yd  swim=2:44  rest=1:00  total=3:44

Totals:
  swim time:          30:18
  intra-set rest:     13:05
  between-group rest: 3:00
  ------------------------------------
  workout total:      46:23
  total distance:     2100 yd
==================================================


Process finished with exit code 0
📌 Notes
	•	This is an MVP console app; no JavaFX GUI yet.
	•	Distances are stored canonically in meters internally, but converted to yards/meters for display based on course.
	•	Timing rules are defined by DefaultPacePolicy; future work may add more advanced pacing/rest models.

⸻

👤 Author

Parker Blackwell – Final Project for SWDEV-200 (Software Development in Java), Fall 2025.


---

## 📄 License & Copyright
© 2025 Parker Blackwell.  
This project was created as part of the **Ivy Tech SWDEV-200 (Software Development in Java)** course.  
All rights reserved. Educational use only.
