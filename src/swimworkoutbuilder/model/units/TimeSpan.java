package swimworkoutbuilder.model.units;

import java.util.Objects;

/** Immutable time span stored as milliseconds (long). */
public final class TimeSpan implements Comparable<TimeSpan> {
    private final long millis;

    private TimeSpan(long millis) { this.millis = millis; }

    public static TimeSpan ofMillis(long ms) { return new TimeSpan(ms); }

    public static TimeSpan ofSeconds(double seconds) {
        return new TimeSpan(Math.round(seconds * 1000.0));
    }

    public static TimeSpan ofMinutesSecondsMillis(int minutes, int seconds, int millis) {
        long total = Math.addExact(Math.addExact(minutes * 60_000L, seconds * 1_000L), millis);
        return new TimeSpan(total);
    }

    public long toMillis() { return millis; }
    public double toSeconds() { return millis / 1000.0; }

    public TimeSpan plus(TimeSpan other) { return new TimeSpan(Math.addExact(millis, other.millis)); }
    public TimeSpan minus(TimeSpan other) { return new TimeSpan(Math.subtractExact(millis, other.millis)); }
    public TimeSpan times(double factor) { return new TimeSpan(Math.round(millis * factor)); }

    @Override public int compareTo(TimeSpan o) { return Long.compare(this.millis, o.millis); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSpan)) return false;
        return millis == ((TimeSpan) o).millis;
    }

    @Override public int hashCode() { return Long.hashCode(millis); }

    /** e.g., 1:23.45 (mm:ss.SS) */
    @Override public String toString() {
        long total = millis;
        long minutes = total / 60_000; total %= 60_000;
        long seconds = total / 1_000;  total %= 1_000;
        long hundredths = Math.round(total / 10.0);
        if (hundredths == 100) { hundredths = 0; seconds++; }
        if (seconds == 60) { seconds = 0; minutes++; }
        return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    }
}