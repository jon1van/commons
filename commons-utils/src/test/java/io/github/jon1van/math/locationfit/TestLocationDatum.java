package io.github.jon1van.math.locationfit;

import static io.github.jon1van.uncheck.DemotedException.demote;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import io.github.jon1van.units.Course;
import io.github.jon1van.units.Distance;
import io.github.jon1van.units.LatLong;
import io.github.jon1van.units.Speed;

/// This Simple class exists to test/verify our MapBuilder and PositionInterpolator.  These
/// capabilities need position data to demonstrate
public class TestLocationDatum {

    private final LatLong location;

    private final Instant time;

    private final Distance altitude;

    private final Speed speed;

    private final Course course;

    public TestLocationDatum(LatLong location, Instant time, Distance altitude, Speed speed, Course course) {
        this.location = location;
        this.time = time;
        this.altitude = altitude;
        this.speed = speed;
        this.course = course;
    }

    public double latitude() {
        return location.latitude();
    }

    public double longitude() {
        return location.longitude();
    }

    public Distance altitude() {
        return altitude;
    }

    public Instant time() {
        return time;
    }

    public Speed speed() {
        return speed;
    }

    public Course course() {
        return course;
    }

    public static Iterable<TestLocationDatum> parseFile(File f) {

        try {
            return Files.readAllLines(f.toPath()).stream()
                    .map(str -> parse(str))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            throw demote(ex);
        }
    }

    public static TestLocationDatum parse(String s) {
        // sample row:
        // 03/29/2018 18:03:13.482 032.82810 -097.36291 010 070
        // Date, Time, Lat, Long, Speed, Heading
        String[] tokens = s.split(" ");

        Instant time = extractInstant(tokens[0], tokens[1]);
        double latitude = Double.parseDouble(tokens[2]);
        double longitude = Double.parseDouble(tokens[3]);
        int altitudeIn100Ft = Integer.parseInt(tokens[4]);
        int speedInKnots = Integer.parseInt(tokens[5]);
        int headingInDegrees = Integer.parseInt(tokens[6]);

        return new TestLocationDatum(
                LatLong.of(latitude, longitude),
                time,
                Distance.ofFeet(altitudeIn100Ft * 100),
                Speed.ofKnots(speedInKnots),
                Course.ofDegrees(headingInDegrees));
    }

    static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS X").withZone(ZoneOffset.UTC);

    public static Instant extractInstant(String dateString, String timeString) {

        ZonedDateTime zdt = ZonedDateTime.parse(dateString.replace("-", "/") + " " + timeString + " Z", FORMATTER);
        return Instant.from(zdt);
    }
}
