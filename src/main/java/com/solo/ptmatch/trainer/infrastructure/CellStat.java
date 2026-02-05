package com.solo.ptmatch.trainer.infrastructure;

public record CellStat(
        long count,
        double sumLatitude,
        double sumLongitude
) {
    public static CellStat of(long count, double sumLatitude, double sumLongitude) {
        return new CellStat(count, sumLatitude, sumLongitude);
    }

    public static CellStat empty() {
        return new CellStat(0L, 0d, 0d);
    }

    public CellStat add(CellStat other) {
        return new CellStat(
                this.count + other.count,
                this.sumLatitude + other.sumLatitude,
                this.sumLongitude + other.sumLongitude
        );
    }

    public double centroidLatitude() {
        return count == 0 ? 0d : sumLatitude / count;
    }

    public double centroidLongitude() {
        return count == 0 ? 0d : sumLongitude / count;
    }
}
