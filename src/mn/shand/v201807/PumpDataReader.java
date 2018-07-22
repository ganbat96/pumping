/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class PumpDataReader implements Runnable {
    public static final Logger pumpLogger1 = new Logger("pump-data-1.log", false);
    public static final Logger pumpLogger2 = new Logger("pump-data-2.log", false);

    private static final Map<String, String[]> CLIENT_IP = new HashMap<>();
    static {
        CLIENT_IP.put("1", new String[] {"192.168.255.126", "50001"});
        CLIENT_IP.put("2", new String[] {"192.168.255.127", "50001"});
    }


    private static final byte[] flowBytes     = new byte[] {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xc4, (byte) 0x0b};
    private static final byte[] wholeBytes    = new byte[] {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x02, (byte) 0x45, (byte) 0xc9};
    private static final byte[] fractionBytes = new byte[] {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x01, (byte) 0xa4, (byte) 0x08};

    private MainForm.UIUpdater updater;

    public PumpDataReader(MainForm.UIUpdater updater) {
        this.updater = updater;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void write(OutputStream out, byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }

    private static long read(InputStream in, int count, int start, int end) throws IOException {
        byte[] bytes = new byte[count];
        in.read(bytes);
        
        
        String hex = bytesToHex(Arrays.copyOfRange(bytes, start, end + 1));
        return Long.parseLong(hex, 16);
        
    }

    public static PumpData read(String code) {

        String[] addr = CLIENT_IP.get(code);

        try(Socket socket = new Socket(addr[0], Integer.parseInt(addr[1]));
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();) {

            write(out, flowBytes);
            long flow = read(in, 9, 3, 6);

            write(out, wholeBytes);
            long whole = read(in, 9, 3, 6);

            write(out, fractionBytes);
            long fraction = read(in, 7, 3, 4);

            PumpData data = new PumpData();
            data.setPumpCode(code);
            data.setFlowRate(BigDecimal.valueOf(flow).divide(new BigDecimal("1000")));
            data.setCounterValue(new BigDecimal(whole + "." + fraction));
            data.setReadAt(new Timestamp(System.currentTimeMillis()));
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Туршилтын зорилгоор ашиглав.
    private static PumpData read1(String code) {
        PumpData data = new PumpData();
        data.setPumpCode(code);
        data.setReadAt(new Timestamp(System.currentTimeMillis()));
        data.setCounterValue( BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(100d, 123456d)).setScale(3, RoundingMode.DOWN) );
        data.setFlowRate(     BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(100d,    500d)).setScale(3, RoundingMode.DOWN) );
        return data;
    }

    @Override
    public void run() {
        PumpData data1 = read("1");
        PumpData data2 = read("2");

        log(pumpLogger1, data1);
        log(pumpLogger2, data2);

        updater.queuePumpData(data1, data2);
    }

    private void log(Logger logger, PumpData data) {
        if (data == null) {
            return;
        }

        List<String> lines = logger.tail(1);
        if (lines != null && !lines.isEmpty()) {
            PumpData prev = PumpData.fromStr(lines.get(0));

            long t0 = prev.getReadAt().getTime();
            long t1 = data.getReadAt().getTime();

            long ms = t1 - t0;
            long sec = ms / 1000;
            long min = sec / 60;
            long hour = min / 60;

            // 6 цаг тутамд лог хадгалахын тулд ...
            if (hour < 6) {
                return;
            }
        }

        logger.log(PumpData.toStr(data));
    }
}
