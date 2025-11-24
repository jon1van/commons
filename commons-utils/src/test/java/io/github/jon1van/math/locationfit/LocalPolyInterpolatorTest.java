package io.github.jon1van.math.locationfit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static io.github.jon1van.math.locationfit.LatLongFitter.unMod;
import static io.github.jon1van.units.HasLatLong.avgLatLong;
import static io.github.jon1van.units.Time.compareByTime;
import static io.github.jon1van.utils.FileUtils.getResourceAsFile;
import static io.github.jon1van.utils.NeighborIterator.newNeighborIterator;
import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;
import static java.time.Instant.EPOCH;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.StatsAccumulator;
import io.github.jon1van.units.*;
import io.github.jon1van.units.LatLong;
import io.github.jon1van.utils.FileUtils;
import io.github.jon1van.utils.IterPair;
import io.github.jon1van.utils.NeighborIterator;
import org.junit.jupiter.api.Test;

public class LocalPolyInterpolatorTest {

    /*
     * @todo -- Weave in a plotting tool so that these tests automatically generate images that
     *     show the altitude, LatLong, Speed, and Course data.
     */
    @Test
    public void basicUsage() {

        LocalPolyInterpolator qi = new LocalPolyInterpolator(Duration.ofMinutes(1));

        List<TestLocationDatum> testData = testPoints();
        Collections.sort(testData, (a, b) -> a.time().compareTo(b.time()));

        List<Position> wrappedTestData = asRecords(testData);

        TimeWindow range = TimeWindow.of(
                testData.get(0).time(), testData.get(testData.size() - 1).time());

        int NUM_SAMPLES = 300;

        List<KineticPosition> interpolatedPoints = newArrayList();

        for (int i = 0; i < NUM_SAMPLES; i++) {

            Instant sampleTime = range.instantWithin(i * 1.0 / (double) NUM_SAMPLES);
            KineticPosition approximation =
                    qi.interpolate(wrappedTestData, sampleTime).get();

            interpolatedPoints.add(approximation);
        }

        Collections.sort(interpolatedPoints, compareByTime());

        validateLatLongs(testData, interpolatedPoints);
        validateAltitudes(testData, interpolatedPoints);
        validateSpeeds(testData, interpolatedPoints);
        validateCourses(testData, interpolatedPoints);
        validateTurnRates(interpolatedPoints);
        validateTurnRateAndCurvatureAreConsistent(interpolatedPoints);
    }

    @Test
    public void basicUsage_trackCrossesInternationalDateLine() {
        // same test as above, but with a track that goes over the international date line

        LocalPolyInterpolator qi = new LocalPolyInterpolator(Duration.ofMinutes(1));

        List<TestLocationDatum> testData = testPoints_crossInternationalDateLine();
        Collections.sort(testData, (a, b) -> a.time().compareTo(b.time()));

        List<Position> wrappedTestData = asRecords(testData);

        TimeWindow range = TimeWindow.of(
                testData.getFirst().time(), testData.get(testData.size() - 1).time());

        int NUM_SAMPLES = 300;

        List<KineticPosition> interpolatedPoints = newArrayList();

        for (int i = 0; i < NUM_SAMPLES; i++) {

            Instant sampleTime = range.instantWithin(i * 1.0 / (double) NUM_SAMPLES);
            KineticPosition approximation =
                    qi.interpolate(wrappedTestData, sampleTime).get();

            interpolatedPoints.add(approximation);
        }

        Collections.sort(interpolatedPoints, compareByTime());

        validateLatLongs(testData, interpolatedPoints);
        validateAltitudes(testData, interpolatedPoints);
        validateSpeeds(testData, interpolatedPoints);
        validateCourses(testData, interpolatedPoints);
        validateTurnRates(interpolatedPoints);
        validateTurnRateAndCurvatureAreConsistent(interpolatedPoints);
    }

    private void validateTurnRateAndCurvatureAreConsistent(List<KineticPosition> interpolatedPoints) {
        /*
         * Positive Turn rates should yield positive turn radii (i.e. clock wise turning)
         * Negative Turn rates should yield negative turn radii (i.e. counter clock wise turning)
         */
        int count = 0;
        for (KineticPosition kp : interpolatedPoints) {
            if (kp.turnRate() > 0) {
                assertThat(kp.turnRadius().isPositive()).isTrue();
                count++;
            }

            if (kp.turnRate() < 0) {
                assertThat(kp.turnRadius().isNegative()).isTrue();
                count++;
            }
        }
        assertThat(count > 100).isTrue();
    }

    private void validateLatLongs(List<TestLocationDatum> testData, List<KineticPosition> fitData) {

        StatsAccumulator stats = new StatsAccumulator();

        for (KineticPosition pt : fitData) {
            FloorAndCeiling floorAndCeiling = floorAndCeiling(testData, pt.time());

            double distToFloor =
                    pt.latLong().distanceTo(latLongFor(floorAndCeiling.first())).inNauticalMiles();
            double distToCeiling = pt.latLong()
                    .distanceTo(latLongFor(floorAndCeiling.second()))
                    .inNauticalMiles();

            stats.add(distToFloor);
            stats.add(distToCeiling);
        }

        // perform unit tests on the aggregate statistics.
        assertThat(stats.count() > 100).isTrue(); // There are at least 100 samples
        assertThat(stats.mean() < .075).isTrue(); // The average distance is small
        assertThat(stats.max() < 2).isTrue(); // No single sample has a large distance error
    }

    private void validateAltitudes(List<TestLocationDatum> testData, List<KineticPosition> fitData) {
        // Aggregate statistics on the difference between the raw data and the fit data
        StatsAccumulator stats = new StatsAccumulator();

        for (KineticPosition kp : fitData) {
            FloorAndCeiling floorAndCeiling = floorAndCeiling(testData, kp.time());

            double deltaToFirst = abs(kp.altitude().inFeet() - altitudeOf(floorAndCeiling.first()));
            double deltaToSecond = abs(kp.altitude().inFeet() - altitudeOf(floorAndCeiling.second()));

            stats.add(deltaToFirst);
            stats.add(deltaToSecond);
        }

        // perform unit tests on the aggregate statistics.
        assertThat(stats.count() > 100).isTrue(); // There are at least 100 samples
        assertThat(stats.mean() < 50).isTrue(); // The average delta is small
        assertThat(stats.max() < 250).isTrue(); // No single sample has a large difference in altitude
    }

    private void validateSpeeds(List<TestLocationDatum> testData, List<KineticPosition> fitData) {

        // Aggregate statistics on the difference between the raw data and the fit data
        StatsAccumulator stats = new StatsAccumulator();

        for (KineticPosition kp : fitData) {
            FloorAndCeiling floorAndCeiling = floorAndCeiling(testData, kp.time());

            double deltaToFirst =
                    abs(kp.speed().inKnots() - floorAndCeiling.first().speed().inKnots());
            double deltaToSecond =
                    abs(kp.speed().inKnots() - floorAndCeiling.second().speed().inKnots());

            stats.add(deltaToFirst);
            stats.add(deltaToSecond);
        }

        assertThat(stats.count() > 100).isTrue(); // There are at least 100 samples
        assertThat(stats.mean() < 4).isTrue(); // The average delta is small
        assertThat(stats.max() < 35).isTrue(); // No single sample has a large difference in speed
    }

    private void validateCourses(List<TestLocationDatum> testData, List<KineticPosition> samples) {

        // Aggregate statistics on the difference between the raw data and the fit data
        StatsAccumulator stats = new StatsAccumulator();
        int badCount = 0;
        Course THRESHOLD = Course.ofDegrees(30);

        for (KineticPosition kp : samples) {
            FloorAndCeiling floorAndCeiling = floorAndCeiling(testData, kp.time());

            Course deltaToFirst = Course.angleBetween(floorAndCeiling.first().course(), kp.course());

            Course deltaToSecond =
                    Course.angleBetween(kp.course(), floorAndCeiling.second().course());

            // This is more a measurement of how poorly the raw course data matches the raw LatLong data
            if (deltaToFirst.isGreaterThan(THRESHOLD) || deltaToSecond.isGreaterThan(THRESHOLD)) {
                badCount++;
            }

            stats.add(deltaToFirst.inDegrees());
            stats.add(deltaToSecond.inDegrees());
        }

        assertThat(stats.count() > 100).isTrue(); // There are at least 100 samples
        assertThat(stats.mean() < 2).isTrue(); // The average delta is small
        assertThat(badCount < 3).isTrue(); // Few samples are bad
    }

    private void validateTurnRates(List<KineticPosition> interpolatedPoints) {

        NeighborIterator<KineticPosition> pairIterator = newNeighborIterator(interpolatedPoints);

        PairedStatsAccumulator stats = new PairedStatsAccumulator();

        while (pairIterator.hasNext()) {
            IterPair<KineticPosition> pair = pairIterator.next();

            double newCourse = pair.current().course().inDegrees();
            double oldCourse = pair.prior().course().inDegrees();

            // ignore big changes (like from 1 degree to 359 degree or vice versa)
            if (abs(newCourse - oldCourse) > 200) {
                continue;
            }

            double courseDelta = newCourse - oldCourse; // estimated over ~5 second time horizon
            double turnRate = pair.prior().turnRate(); // instantaneous approximation

            stats.add(courseDelta, turnRate);
        }

        double correlation = stats.pearsonsCorrelationCoefficient();
        double slope = stats.leastSquaresFit().slope();

        assertThat(correlation > .95).isTrue(); // The correlation is VERY strong
        assertThat(slope > 0.05).isTrue(); // The slope is positive
        // i.e. positive courseDeltas are associated with positive turn rates (and vice versa)
    }

    record FloorAndCeiling(TestLocationDatum first, TestLocationDatum second) {}

    static FloorAndCeiling floorAndCeiling(List<TestLocationDatum> points, Instant time) {

        TestLocationDatum ceiling = null;
        TestLocationDatum floor = null;

        for (TestLocationDatum pt : points) {

            if (pt.time().isBefore(time) || pt.time().equals(time)) {
                floor = pt;
            }
            if (pt.time().isAfter(time) && ceiling == null) {
                ceiling = pt;
            }
        }

        return new FloorAndCeiling(floor, ceiling);
    }

    public List<TestLocationDatum> testPoints() {
        File file =
                getResourceAsFile(LocalPolyInterpolator.class, "oneTrack.txt").get();

        return FileUtils.fileLines(file).stream()
                .map(datum -> TestLocationDatum.parse(datum))
                .sorted((a, b) -> a.time().compareTo(b.time()))
                .collect(toList());
    }

    public List<TestLocationDatum> testPoints_crossInternationalDateLine() {

        List<TestLocationDatum> regularData = testPoints();

        List<LatLong> locations = regularData.stream()
                .map(x -> LatLong.of(x.latitude(), x.longitude()))
                .collect(toList());

        // we want the altered track to have its average longitude at exactly 180.0
        // this means some data will be on each side of international date-line
        double avgLongitude = avgLatLong(locations).longitude();
        double delta = 180 - avgLongitude;

        // cannot just add a delta, that leads to illegal longitude values
        // hence unMod(x.longitude() + delta) instead of just "x.longitude() + delta"
        List<TestLocationDatum> altered = regularData.stream()
                .map(x -> new TestLocationDatum(
                        LatLong.of(x.latitude(), unMod(x.longitude() + delta)),
                        x.time(),
                        x.altitude(),
                        x.speed(),
                        x.course()))
                .collect(toList());

        return altered;
    }

    public List<Position> asRecords(List<TestLocationDatum> points) {

        return points.stream()
                .map(pt -> new Position(
                        pt.time().toEpochMilli(),
                        pt.latitude(),
                        pt.longitude(),
                        pt.altitude().inFeet()))
                .collect(toList());
    }

    @Test
    public void cannotExtrapolateBeyondSourceData() {

        LocalPolyInterpolator qi = new LocalPolyInterpolator(Duration.ofMinutes(1));

        List<TestLocationDatum> testData = testPoints();

        TimeWindow range = TimeWindow.of(
                testData.get(0).time(), testData.get(testData.size() - 1).time());

        Instant beforeStart = range.start().minusMillis(1L);
        Instant start = range.start();
        Instant end = range.end();
        Instant afterEnd = range.end().plusMillis(1L);

        List<Position> wrappedTestData = asRecords(testData);
        Optional<KineticPosition> before = qi.interpolate(wrappedTestData, beforeStart);
        Optional<KineticPosition> atStart = qi.interpolate(wrappedTestData, start);
        Optional<KineticPosition> atEnd = qi.interpolate(wrappedTestData, end);
        Optional<KineticPosition> after = qi.interpolate(wrappedTestData, afterEnd);

        assertThat(before.isPresent()).isFalse(); // no result provided outside TimeWindow
        assertThat(atStart.isPresent()).isTrue();
        assertThat(atEnd.isPresent()).isTrue();
        assertThat(after.isPresent()).isFalse(); // no result provided outside TimeWindow
    }

    private static LatLong latLongFor(TestLocationDatum hit) {
        return LatLong.of(hit.latitude(), hit.longitude());
    }

    private static double altitudeOf(TestLocationDatum hit) {
        return hit.altitude().inFeet();
    }

    @Test
    public void basicUsage_withoutAltitudeData() {

        LocalPolyInterpolator qi = new LocalPolyInterpolator(Duration.ofMinutes(1), 3, true);

        List<TestLocationDatum> testData = testPoints();
        Collections.sort(testData, Comparator.comparing(TestLocationDatum::time));

        List<Position> wrappedTestData = asRecords(testData);

        TimeWindow range = TimeWindow.of(
                testData.get(0).time(), testData.get(testData.size() - 1).time());

        int NUM_SAMPLES = 300;

        List<KineticPosition> interpolatedPoints = newArrayList();

        for (int i = 0; i < NUM_SAMPLES; i++) {

            Instant sampleTime = range.instantWithin(i * 1.0 / (double) NUM_SAMPLES);
            KineticPosition approximation =
                    qi.interpolate(wrappedTestData, sampleTime).get();

            interpolatedPoints.add(approximation);
        }

        // Verify all altitude data is ignored..
        for (KineticPosition kr : interpolatedPoints) {
            assertThat(kr.altitude()).isEqualTo(Distance.ZERO_FEET);
            assertThat(kr.climbRate()).isEqualTo(Speed.ZERO_FEET_PER_MIN);
        }
    }

    private static class Dummy {}

    @Test
    public void verifyAccelerationDeduction() {

        // Numerically produce some LatLong data that shows an accelerating object..
        // Then verify the acceleration is correct

        Acceleration ACCEL = Acceleration.of(Speed.ofKnots(2.0)); // 1 knot per sec

        List<Position> records = createDataShowingConstantAcceleration(ACCEL);

        LocalPolyInterpolator interpolator = new LocalPolyInterpolator(Duration.ofMinutes(1), 7, true);

        KineticPosition kinetics =
                interpolator.interpolate(records, EPOCH.plusMillis(2500L)).get();

        Acceleration deducedAccel = kinetics.acceleration();

        // Approximation error can be as large a 1% of the input ACCEL value
        Speed threshold = ACCEL.speedDeltaPerSecond().times(.01);

        // Difference between input ACCEL and deduced value
        Speed error = ACCEL.speedDeltaPerSecond()
                .minus(deducedAccel.speedDeltaPerSecond())
                .abs();

        assertThat(error.isLessThan(threshold)).isTrue();
    }

    private List<Position> createDataShowingConstantAcceleration(Acceleration rate) {

        Duration TIME_STEP = Duration.ofMillis(250L);
        double scalar = ((double) TIME_STEP.toMillis()) / 1000.0;

        LinkedList<Position> locationData = newLinkedList();
        locationData.add(new Position(EPOCH, LatLong.of(38.9223, -77.2016)));
        Speed currentSpeed = Speed.ofKnots(0.0);

        for (int i = 0; i < 1_000; i++) {
            Position last = locationData.getLast();
            currentSpeed = currentSpeed.plus(rate.speedDeltaPerSecond().times(scalar));
            Distance dist = currentSpeed.times(TIME_STEP);
            LatLong nextLatLong = last.latLong().move(Course.EAST, dist);

            Position next = new Position(last.time().plus(TIME_STEP), nextLatLong);
            locationData.addLast(next);
        }

        return locationData;
    }

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS X").withZone(ZoneOffset.UTC);

    /// @param dateString The "date portion". For example the "10/18/2016" in
    ///                   "10/18/2016,00:57:12.962"
    /// @param timeString The "time portion" of a Nop Message. For example the "00:57:12.962" in
    ///                   "10/18/2016,00:57:12.962"
    ///
    /// @return The Instant corresponding to the date and time (Z time is assume)
    public static Instant parseTime(String dateString, String timeString) {

        ZonedDateTime zdt = ZonedDateTime.parse(dateString.replace("-", "/") + " " + timeString + " Z", DATE_FORMATTER);
        return Instant.from(zdt);
    }

    static Position parseTestInput(String input) {
        // Expected input = "07/17/2022,18:44:33.930,036.89253,-112.35783"
        // Expected input = "date,time,lat,long"
        String[] tokens = input.split(",");

        return new Position(
                parseTime(tokens[0], tokens[1]).toEpochMilli(), parseDouble(tokens[2]), parseDouble(tokens[3]));
    }

    @Test
    public void brokenExample_duplicate_time_and_position_1() {

        // FAILURE LOGS =
        //
        // Longitude is out of range: -222.96536332927727
        // Could not interpolate input data.
        //        sampleTime= 2022-07-17T18:44:40.600Z
        // 07/17/2022,18:44:33.930,036.89253,-112.35783
        // 07/17/2022,18:44:47.270,036.89253,-112.35783
        // 07/17/2022,18:44:47.270,036.89253,-112.35783

        Position p1 = parseTestInput("07/17/2022,18:44:33.930,036.89253,-112.35783");
        Position p2 = parseTestInput("07/17/2022,18:44:47.270,036.89253,-112.35783");
        Position p3 = parseTestInput("07/17/2022,18:44:47.270,036.89253,-112.35783");
        // notice, p2 and p3 are the same! -- Our raw data contains a duplicate, how should this be handled
        LocalPolyInterpolator interpolator = new LocalPolyInterpolator(Duration.ofSeconds(60), 3, true);

        Optional<KineticPosition> result =
                interpolator.interpolate(newArrayList(p1, p2, p3), Instant.parse("2022-07-17T18:44:40.600Z"));

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void brokenExample_nearInternationalDateLine() {
        // FAILURE LOGS =
        //
        // Longitude is out of range: -180.00084537630087
        // Could not interpolate input data.
        //        sampleTime= 2024-11-10T20:13:16.705Z
        // 11/10/2024,20:13:03.584,52.3755,-179.9606
        // 11/10/2024,20:13:07.745,52.3764,-179.9725
        // 11/10/2024,20:13:11.805,52.3774,-179.984
        // 11/10/2024,20:13:16.705,52.3786,-179.998
        Position p1 = parseTestInput("11/10/2024,20:13:03.584,52.3755,-179.9606");
        Position p2 = parseTestInput("11/10/2024,20:13:07.745,52.3764,-179.9725");
        Position p3 = parseTestInput("11/10/2024,20:13:11.805,52.3774,-179.984");
        Position p4 = parseTestInput("11/10/2024,20:13:16.705,52.3786,-179.998");
        LocalPolyInterpolator interpolator = new LocalPolyInterpolator(Duration.ofSeconds(60), 3, true);
        Optional<KineticPosition> result =
                interpolator.interpolate(newArrayList(p1, p2, p3, p4), Instant.parse("2024-11-10T20:13:16.705Z"));

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    public void brokenExample_duplicate_time_and_position_2() {

        // FAILURE LOGS =
        //
        //    Latitude is out of range: 96.0
        //    Could not interpolate input data.
        //        sampleTime= 2022-07-17T18:55:52Z
        // 07/17/2022,18:55:38.713,037.59117,-119.19317
        // 07/17/2022,18:55:38.713,037.59117,-119.19317
        // 07/17/2022,18:56:14.713,037.57589,-119.09622

        Position p1 = parseTestInput("07/17/2022,18:55:38.713,037.59117,-119.19317");
        Position p2 = parseTestInput("07/17/2022,18:55:38.713,037.59117,-119.19317");
        Position p3 = parseTestInput("07/17/2022,18:56:14.713,037.57589,-119.09622");
        // notice, p2 and p3 are the same! -- Our raw data contains a duplicate, how should this be handled
        LocalPolyInterpolator interpolator = new LocalPolyInterpolator(Duration.ofSeconds(60), 3, true);

        Optional<KineticPosition> result =
                interpolator.interpolate(newArrayList(p1, p2, p3), Instant.parse("2022-07-17T18:55:52Z"));

        assertThat(result.isPresent()).isFalse();
    }
}
