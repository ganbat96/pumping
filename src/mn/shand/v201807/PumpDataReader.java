/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import mn.shand.v201807.util.HexUtil;
import mn.shand.v201807.util.Logger;

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
    private Settings settings;

    public PumpDataReader(MainForm.UIUpdater updater) {
        this.updater = updater;

        this.settings = Settings.load();
    }

    public static PumpData read(String code) {

        String[] addr = CLIENT_IP.get(code);

        try(Socket socket = new Socket(addr[0], Integer.parseInt(addr[1]));
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();) {

            HexUtil.write(out, flowBytes);
            long flow = HexUtil.readLong(in, 9, 3, 6);

            HexUtil.write(out, wholeBytes);
            long whole = HexUtil.readLong(in, 9, 3, 6);

            HexUtil.write(out, fractionBytes);
            long fraction = HexUtil.readLong(in, 7, 3, 4);

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

            int t0 = getLogHour(prev.getReadAt());
            int t1 = getLogHour(data.getReadAt());

            if (t0 == t1) {
                return;
            }
        }

        logger.log(PumpData.toStr(data));
    }

    private int getLogHour(java.sql.Timestamp time) {
        LocalDateTime dt = time.toLocalDateTime();
        int hour = getLogHour(settings.getCounterLogHours(), dt.getHour());

        return dt.getDayOfMonth() * 100 + hour;
    }

    private static int getLogHour(int[] hours, int hour) {
        for (int i = hours.length - 1; i >= 0; i --) {
            if (hour >= hours[i]) {
                return hours[i];
            }
        }

        return hours[hours.length - 1];
    }
}
