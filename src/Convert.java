import java.nio.ByteBuffer;

public class Convert {


    /*  Header
    Int(MAGIC);
    Short(0); // CRC
    Short(type)
    Long(key);
    */

    /* Fix
    writeHeader(dos, TYPE_FIX);
    Int(FLAG_LOCATION | FLAG_TRACK | FLAG_GROUND_SPEED | FLAG_ALTITUDE);
    Int time);
    Int latitude;
    Int longitude;
    Int(0); // reserved
    Short(track);
    Short(groundSpeed);
    Short(0); // airspeed (unavailable)
    Short(altitude);
    Short(0); // vario (unavailable)
    Short(0); // engine noise level (unavailable)
    */

    private static int HEADER_SIZE = (Integer.SIZE + Short.SIZE + Short.SIZE + Long.SIZE) / 8;

    public static int DATA_LENGTH = 48;

    public static class Fix {

        public int time;
        public double latitude;
        public double longitude;
        public short track;
        public short groundSpeed;
        public short airspeed;
        public short altitude;
        public short vario;
        public short noise;

        public Fix(ByteBuffer bb) {
            bb.getInt(); //skip flags
            time = bb.getInt();
            latitude = (double) bb.getInt() / 1000000;
            longitude = (double) bb.getInt() / 1000000;
            bb.getInt(); //skip reserved
            track = bb.getShort();
            groundSpeed = bb.getShort();
            airspeed = bb.getShort();
            altitude = bb.getShort();
            vario = bb.getShort();
            noise = bb.getShort();
        }
    }

    private static Fix getFix(byte[] buffer) {
        ByteBuffer bb = ByteBuffer.wrap(buffer, HEADER_SIZE, DATA_LENGTH - HEADER_SIZE);
        return new Fix(bb);
    }

    private static short getType(byte[] buffer) {
        return ByteBuffer.wrap(buffer, (Integer.SIZE + Short.SIZE) / 8, Short.SIZE / 8).getShort();
    }

    public static String getHexKey(byte[] buffer) {
        return Long.toHexString(ByteBuffer.wrap(buffer, (Integer.SIZE + Short.SIZE + Short.SIZE) / 8, Long.SIZE / 8).getLong());
    }

    public static Fix convert(byte[] buffer) {
        switch (getType(buffer)) {
            case 3:
                return getFix(buffer);
            default:
                return null;
        }
    }

}
