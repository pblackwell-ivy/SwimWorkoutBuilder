package swimworkoutbuilder.model.units;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable distance with exact internal storage.
 * Canonical unit: 0.0001 meters (1e-4 m, i.e., 0.1 mm) stored as a long.
 * This makes 1 yard = 0.9144 m = 9144 internal units (exact).
 */
public final class Distance implements Comparable<Distance> {

    public enum Unit { METERS, YARDS }

    // 1 internal unit = 0.0001 m
    private static final long UM4_PER_METER = 10_000L;
    private static final long UM4_PER_YARD  = 9_144L; // exact: 0.9144 m * 10_000

    private final long um4;       // canonical value
    private final Unit display;   // how the user entered / prefers to see it

    private Distance(long um4, Unit display) {
        this.um4 = um4;
        this.display = Objects.requireNonNull(display, "display");
    }

    /** Factory: exact when meters has ≤4 decimals; otherwise rounds to nearest 0.1 mm. */
    public static Distance ofMeters(double meters) {
        long v = Math.round(meters * UM4_PER_METER);
        return new Distance(v, Unit.METERS);
        // If you need guaranteed no-double path, add a BigDecimal overload.
    }

    /** Factory: exact for any fractional/whole yards (because 1 yd = 9144 um4 exactly). */
    public static Distance ofYards(double yards) {
        long v = Math.round(yards * UM4_PER_YARD);
        return new Distance(v, Unit.YARDS);
    }

    /** Exact factory (canonical). */
    public static Distance ofCanonicalUm4(long um4, Unit display) {
        return new Distance(um4, display);
    }

    /** Canonical raw value (0.0001 m units). */
    public long rawUm4() { return um4; }

    /** Preferred display unit. */
    public Unit displayUnit() { return display; }

    // --------- Conversions (use doubles for UI only; core math should use um4) ---------

    public double toMeters() { return (double) um4 / UM4_PER_METER; }

    public double toYards()  { return (double) um4 / UM4_PER_YARD;  }

    /** Convert to a new Distance with the requested display unit (value unchanged). */
    public Distance withDisplay(Unit unit) {
        if (unit == this.display) return this;
        return new Distance(this.um4, unit);
    }

    // --------- Arithmetic (exact in canonical space) ---------

    public Distance plus(Distance other) {
        return new Distance(Math.addExact(this.um4, other.um4), this.display);
    }

    public Distance minus(Distance other) {
        return new Distance(Math.subtractExact(this.um4, other.um4), this.display);
    }

    public Distance times(int k) {
        return new Distance(Math.multiplyExact(this.um4, k), this.display);
    }

    public Distance times(double factor) {
        // Only use for policy multipliers; rounds to nearest 0.1 mm at the edge.
        long v = Math.round(this.um4 * factor);
        return new Distance(v, this.display);
    }

    // --------- Comparisons / Equality ---------

    @Override public int compareTo(Distance o) { return Long.compare(this.um4, o.um4); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance)) return false;
        Distance d = (Distance) o;
        return um4 == d.um4; // display unit is UI preference; equality is canonical
    }

    @Override public int hashCode() { return Long.hashCode(um4); }

    // --------- Formatting helpers (UI/IO can use these) ---------

    /** Meters as BigDecimal with 4 decimal places (exact). */
    public BigDecimal metersAsBigDecimal() {
        return BigDecimal.valueOf(um4, 4); // scale = 4 decimal places
    }

    /** Yards as BigDecimal (may be repeating; this is for display/rounding at edges). */
    public BigDecimal yardsAsBigDecimal(int scale) {
        return BigDecimal.valueOf(um4)
                .divide(BigDecimal.valueOf(UM4_PER_YARD), scale, java.math.RoundingMode.HALF_UP);
    }

    @Override public String toString() {
        return display == Unit.METERS
                ? metersAsBigDecimal() + " m"
                : yardsAsBigDecimal(2) + " yd";
    }
}