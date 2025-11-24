package io.github.jon1van.math.locationfit;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.github.jon1van.units.KineticPosition;
import io.github.jon1van.units.Position;

/// A PositionInterpolator derives a KineticPosition from a chronological sequence of Position
/// measurements. The KineticPosition is built to "fit" the trends of the provided position data
/// (i.e. if the raw position data reflects a moving object then the KineticPosition will deduce a
/// good estimate for latitude, longitude, speed, and direction of the object at sampleTime T).
public interface PositionInterpolator {

    /// @param positions  A chronological list of position data tracking a single object.
    /// @param sampleTime The time at which a "KineticPosition" will be deduced.
    ///
    /// @return A KineticPosition that "numerically fits" the provided Position data. An Optional is
    /// returned because a PositionInterpolator may not support deriving KineticPositions when
    /// certain input constraints are not met (i.e., Do you have enough sample data?  Does the
    /// sampleTime occur between the first and last Position sample?)
    Optional<KineticPosition> interpolate(List<Position> positions, Instant sampleTime);

    // THIS SHOULD BE REMOVED -- THE PROBLEM IS "looking up"

    //    /**
    //     * Use the interpolate method to deduce a KineticPosition at the sampleTime. THEN combine
    //     * the resulting KineticPosition with the "floor datum" from one of the PositionRecord inputs.
    //     *
    //     * @param positionData A time sorted list of position+datum data tracking a single object.
    //     * @param sampleTime   The time at which a "KineticPosition" will be deduced.
    //     * @param <T>          The type of data packaged with each Position measurement
    //     *
    //     * @return A KineticRecord that "numerically fits" the provided Position data. An Optional is
    //     * returned because a PositionInterpolator may not support deriving KineticPositions when
    //     * certain input constraints are not met (i.e., Do you have enough sample data?  Does the
    //     * sampleTime occur between the first and last Position sample?
    //     */
    //    default <T> Optional<KineticRecord<T>> floorInterpolate(List<PositionRecord<T>> positionData, Instant
    // sampleTime) {
    //
    //        List<Position> positions =
    //                positionData.stream().map(PositionRecord::position).collect(toList());
    //
    //        Optional<KineticPosition> kinetics = interpolate(positions, sampleTime);
    //
    //        if (!kinetics.isPresent()) {
    //            // interpolate did not return a KineticPosition ... return nothing
    //            return Optional.empty();
    //        } else {
    //            // a KineticPosition was made -- find out which datum to match with it
    //            PositionRecord<T> floor = HasTime.floor(positionData, sampleTime);
    //            KineticRecord<T> kr = new KineticRecord<>(floor.datum(), kinetics.get());
    //            return Optional.of(kr);
    //        }
    //    }
}
