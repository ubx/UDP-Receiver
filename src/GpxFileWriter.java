import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.util.*;

public class GpxFileWriter {

    private static FileOutputStream out;
    private static HashMap<String, ArrayList<Waypoint>> key2Waypoints = new HashMap<String, ArrayList<Waypoint>>(10);

    public GpxFileWriter(String fileName) {
        try {
            out = new FileOutputStream(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writeFix(String key, Convert.Fix fix) {
        ArrayList<Waypoint> wps = key2Waypoints.get(key);
        if (wps == null) {
            wps = new ArrayList<Waypoint>();
            key2Waypoints.put(key, wps);
        }
        Waypoint wp = new Waypoint();
        wp.setLongitude(fix.longitude);
        wp.setLatitude(fix.latitude);
        wp.setElevation(Double.valueOf(fix.altitude));
        wp.setTime(toDate(fix.time));
        wps.add(wp);
    }

    public void close() {
        GPX gpx = new GPX();
        for (String key : key2Waypoints.keySet()) {
            ArrayList<Waypoint> wps = key2Waypoints.get(key);
            Track track = new Track();
            track.setTrackPoints(wps);
            track.setName(key);
            gpx.addTrack(track);
        }
        GPXParser gpxParser = new GPXParser();
        try {
            gpxParser.writeGPX(gpx, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Date toDate(int millisOfDay) {
        Calendar calendar = GregorianCalendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, millisOfDay / 3600000);
//        calendar.set(Calendar.MINUTE, millisOfDay % 3600000);
//        calendar.set(Calendar.SECOND, millisOfDay % 60000);
//        calendar.set(Calendar.MILLISECOND, millisOfDay % 1000);
        return calendar.getTime();
    }


}


