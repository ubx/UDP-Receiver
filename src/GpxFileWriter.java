import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
        // ToDo -- set time
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


}


