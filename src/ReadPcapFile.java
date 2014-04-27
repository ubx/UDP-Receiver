import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.ByteArrays;

import java.io.File;
import java.net.InetAddress;

public class ReadPcapFile {

    /**
     * Read a pcap-file and convert the packages payload data to a gpx file.
     * <p/>
     * Pcap-file captured on the telephone with the Android App "Shark for Root" (https://play.google.com/store/apps/details?id=lv.n3o.shark)
     * Parameters: "-vv -s 0 udp port 5597"
     */

    private static final String PCAP_FILE_KEY = ReadPcapFile.class.getName() + ".pcapFile";

    public static void main(String[] args) throws Exception {
        // evaluate unique file name
        String fngpx;
        int c = 0;
        do {
        } while (new File(fngpx = "tracks2-" + c++ + ".gpx").isFile());
        GpxFileWriter gpxFileWriter = new GpxFileWriter(fngpx);

        String PCAP_FILE = System.getProperty(PCAP_FILE_KEY, args[0]);
        PcapHandle handle = Pcaps.openOffline(PCAP_FILE);

        int pcnt = 0;
        while (true) {
            try {
                Packet packet = handle.getNextPacket();
                if (packet == null) break;
                byte[] rd = packet.getRawData();
                byte[] buffer = ByteArrays.getSubArray(rd, rd.length - Convert.DATA_LENGTH, Convert.DATA_LENGTH);
                Convert.Fix fix = Convert.convert(buffer);
                if (fix != null) {
                    String hexKey = Convert.getHexKey(buffer);
                    String src = InetAddress.getByAddress(ByteArrays.getSubArray(rd, rd.length - 64, 4)).getHostAddress();
                    gpxFileWriter.writeFix(hexKey + "-dev", fix.time, fix, src);
                    pcnt++;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        handle.close();
        gpxFileWriter.close();
        System.out.println("fixes written=" + pcnt);
    }

}