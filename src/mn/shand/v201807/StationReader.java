/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class StationReader implements Runnable {

    private static final Map<String, String[]> STATION_IP = new HashMap<>();
    static {
        STATION_IP.put("de", new String[] {"192.168.255.141", "2020"});
        STATION_IP.put("zg", new String[] {"192.168.255.142", "2020"});
        STATION_IP.put("gb", new String[] {"192.168.255.143", "2020"});
    }

    private static final byte[] readBytes = HexUtil.hexToBytes("00 00 00 00 00 06 01 03 04 00 00 04".replace(" ", ""));

    private Map<String, StationReader.Value> stationValues;

    public StationReader(Map<String, StationReader.Value> stationValues) {
        this.stationValues = stationValues;
    }

    @Override
    public void run() {
        read("de");
        read("zg");
        read("gb");

        System.out.println("stationValues : " + stationValues);
    }

    private void read(String code) {
        String[] addr = STATION_IP.get(code);
        try(Socket socket = new Socket(addr[0], Integer.parseInt(addr[1]));
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();) {

            HexUtil.write(out, readBytes);
            byte[] bytes = HexUtil.read(in, 17);

            Value value = new Value();
            value.code = code;
            value.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
            value.p11 = Integer.parseInt(HexUtil.bytesToHex(bytes[ 9], bytes[10]), 16);
            value.p22 = Integer.parseInt(HexUtil.bytesToHex(bytes[11], bytes[12]), 16);
            value.d11 = Integer.parseInt(HexUtil.bytesToHex(bytes[13], bytes[14]), 16);
            value.d22 = Integer.parseInt(HexUtil.bytesToHex(bytes[15], bytes[16]), 16);

            stationValues.put(code, value);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // Туршилтын зорилгоор ашиглав.
    private void read1(String code) {
        Value value = new Value();
        value.code = code;
        value.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
        value.p11 = ThreadLocalRandom.current().nextInt(1000);
        value.p22 = ThreadLocalRandom.current().nextInt(1000);
        value.d11 = ThreadLocalRandom.current().nextInt(1000);
        value.d22 = ThreadLocalRandom.current().nextInt(1000);

        stationValues.put(code, value);
    }

    public static class Value {
        private String code;
        private int p11;
        private int p22;
        private int d11;
        private int d22;

        private Timestamp createdAt;

        public String toString() {
            return "p11=" + p11 + ", p22=" + p22 + ", d11=" + d11 + ", d22=" + d22 + ", time=" + createdAt;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getP11() {
            return p11;
        }

        public void setP11(int p11) {
            this.p11 = p11;
        }

        public int getP22() {
            return p22;
        }

        public void setP22(int p22) {
            this.p22 = p22;
        }

        public int getD11() {
            return d11;
        }

        public void setD11(int d11) {
            this.d11 = d11;
        }

        public int getD22() {
            return d22;
        }

        public void setD22(int d22) {
            this.d22 = d22;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }
}
