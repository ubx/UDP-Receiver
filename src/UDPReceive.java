import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class UDPReceive {


    private static DecimalFormat dfLat = new DecimalFormat("##.######");
    private static DecimalFormat dfLon = new DecimalFormat("###.######");
    private static DecimalFormat dfAlt = new DecimalFormat("#####");
    private static DecimalFormat dfMis = new DecimalFormat("####");

    private static Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    public static void main(String args[]) {
        try {
            int port = 5597;

            // Create a socket to listen on the port.
            DatagramSocket dsocket = new DatagramSocket(port);

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            byte[] buffer = new byte[2048];

            // Create a packet to receive data into the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Now loop forever, waiting to receive packets and printing them.
            while (true) {
                // Wait to receive a datagram
                dsocket.receive(packet);

                Convert.Fix fix = null;
                if (packet.getLength() == Convert.DATA_LENGTH) {
                    fix = Convert.convert(buffer);
                }
                if (fix != null) {
                    int rcvtime = getCurrentTime();
                    System.out.println(packet.getAddress().getHostName()
                            + ": Fix, rcv time=" + rcvtime
                            + " time=" + fix.time
                            + "(" + (rcvtime - fix.time) + ")"
                            + " key=" + Convert.getHexKey(buffer)
                            + " lat=" + dfLat.format(fix.latitude)
                            + " lon=" + dfLon.format(fix.longitude)
                            + " trk=" + dfMis.format(fix.track)
                            + " gs=" + dfMis.format(fix.groundSpeed)
                            + " alt=" + dfAlt.format(fix.altitude)
                            + " vario=" + dfMis.format(fix.vario)
                            + " noise=" + dfMis.format(fix.noise)
                    );


                } else {
                    // Convert the contents to a string, and display them
                    String msg = new String(buffer, 0, packet.getLength());
                    System.out.println(packet.getAddress().getHostName() + ": " + msg);
                }

                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }


    private static int getCurrentTime() {

        calendar.setTimeInMillis(System.currentTimeMillis());
        return (calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
                + calendar.get(Calendar.SECOND)) * 1000 + calendar.get(Calendar.MILLISECOND);
    }
}

           