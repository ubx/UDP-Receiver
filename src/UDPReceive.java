import java.io.File;
import java.net.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class UDPReceive {

    private static DatagramSocket socket;

    private static DecimalFormat dfLat = new DecimalFormat("##.00000");
    private static DecimalFormat dfLon = new DecimalFormat("###.00000");
    private static DecimalFormat dfAlt = new DecimalFormat("#####");
    private static DecimalFormat dfMis = new DecimalFormat("####");
    private static Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    private static final String DEF_IP_ADDRESS = "95.128.34.172";
    public static final int PORT = 5597;

    private static GpxFileWriter gpxFileWriter;

    static {
        Runtime.getRuntime().
                addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        if (socket != null) {
                            socket.close();
                            gpxFileWriter.close();
                        }
                    }
                }));
    }

    public static void main(String args[]) throws UnknownHostException {

        boolean relay = args.length > 0 && args[0] != null && args[0].equals("-relay");
        boolean br = args.length > 0 && args[0] != null && args[0].equals("-br");
        DatagramPacket datagram = null;
        SocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(DEF_IP_ADDRESS), PORT);
        DatagramSocket socketTx = null;

        String fngpx;
        int c = 0;
        do {
        } while (new File(fngpx = "tracks-" + c++ + ".gpx").isFile());
        gpxFileWriter = new GpxFileWriter(fngpx);

        try {

            // Create a socket to listen on the port.
            socket = new DatagramSocket(PORT);

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            byte[] buffer = new byte[2048];

            // Create a packet to receive data into the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Now loop forever, waiting to receive packets and printing them.
            while (true) {
                // Wait to receive a datagram
                socket.receive(packet);

                Convert.Fix fix = null;
                if (packet.getLength() == Convert.DATA_LENGTH) {
                    fix = Convert.convert(buffer);
                }
                if (fix != null) {
                    String hexKey = Convert.getHexKey(buffer);
                    if (br) {
                        System.out.println("Rcv: " +fix.time
                                + "," + hexKey
                                + "," + dfLon.format(fix.longitude)
                                + "," + dfLat.format(fix.latitude)
                                + "," + dfAlt.format(fix.altitude));
                    } else {
                        String src = packet.getAddress().getHostName();
                        int rcvtime = getCurrentTime();
                        System.out.println(src
                                + ": Fix, rcv time=" + rcvtime
                                + " time=" + fix.time
                                + "(" + (rcvtime - fix.time) + ")"
                                + " key=" + hexKey
                                + " lon=" + dfLon.format(fix.longitude)
                                + " lat=" + dfLat.format(fix.latitude)
                                + " trk=" + dfMis.format(fix.track)
                                + " gs=" + dfMis.format(fix.groundSpeed)
                                + " alt=" + dfAlt.format(fix.altitude)
                                + " vario=" + dfMis.format(fix.vario)
                                + " noise=" + dfMis.format(fix.noise));
                        gpxFileWriter.writeFix(hexKey + "-gps", fix.time, fix, src);
                        gpxFileWriter.writeFix(hexKey + "-rcv", rcvtime, fix, src);
                    }

                    // relay packet to real SkyLines server
                    if (relay) {
                        if (socketTx == null) {
                            socketTx = new DatagramSocket();
                        }
                        if (datagram == null) {
                            datagram = new DatagramPacket(buffer, Convert.DATA_LENGTH, serverAddress);
                        } else {
                            datagram.setData(buffer);
                            datagram.setLength(Convert.DATA_LENGTH);
                        }
                        socketTx.send(datagram);
                    }
                } else {
                    // Convert the contents to a string, and display them
                    String msg = new String(buffer, 0, packet.getLength());
                    System.out.println(packet.getAddress().getHostName() + ": " + msg);
                }
                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }

        } catch (SocketException e) {
            // ignore
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

           