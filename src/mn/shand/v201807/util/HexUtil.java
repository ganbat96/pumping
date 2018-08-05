/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class HexUtil {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte ... bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void write(OutputStream out, byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }

    public static long readLong(InputStream in, int count, int start, int end) throws IOException {
        byte[] bytes = new byte[count];
        in.read(bytes);


        String hex = bytesToHex(Arrays.copyOfRange(bytes, start, end + 1));
        return Long.parseLong(hex, 16);
    }

    public static byte[] read(InputStream in, int count) throws IOException {
        byte[] bytes = new byte[count];
        int bytesRead = in.read(bytes);
        return bytes;
    }

    private static final byte[] flowBytes     = new byte[] {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xc4, (byte) 0x0b};

    public static void main(String[] args) {
        String hex = bytesToHex(flowBytes);

        byte[] bytes = hexToBytes(hex);

        String _hex = bytesToHex(bytes);

        System.out.println(hex);
        System.out.println(_hex);

    }
}
