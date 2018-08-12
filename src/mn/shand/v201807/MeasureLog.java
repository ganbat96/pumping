/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import mn.shand.v2018.msg.ClientMsg;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class MeasureLog implements Runnable {
    public static final Logger hlogger = new Logger("hmeasurement.log", false);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void run() {
        StringJoiner joiner = new StringJoiner(",");

        joiner.add(String.valueOf(System.currentTimeMillis()));

        for (String value : new String[] {"zg0", "gb0", "de0", "myng1", "myng2"}) {
            int l = value.length();
            int idx = Integer.parseInt(value.substring(l - 1));
            String code = value.substring(0, l - 1);

            ClientMsg msg = Client.statusMap.get(code);
            if (msg != null) {
                int h = msg.getHPercentage(idx);
                joiner.add(value + ":" + h);
            }
        }

        hlogger.log(joiner.toString());
    }

    public static List<Map<String, String>> tail(int lines) {
        List<String> _lines = hlogger.tail(lines);

        List<Map<String, String>> result = new ArrayList<>(_lines.size());
        for (String l : _lines) {
            if (l != null && !l.isEmpty()) {
                result.add(convert(l));
            }
        }

        return result;
    }

    public static List<Map<String, String>> search(String date) {
        List<String> _lines = hlogger.lines();

        List<Map<String, String>> result = new ArrayList<>(_lines.size());
        for (String l : _lines) {
            Map<String, String> map = convert(l);

            String _date = map.get("date");
            if (_date.startsWith(date)) {
                result.add(map);
            }
        }

        Collections.reverse(result);

        return result;
    }

    public static Map<String, String> convert(String line) {
        String[] parts = line.split("(\\s|,)+");
        Date date = new Date(Long.parseLong(parts[0]));

        Map<String, String> map = new HashMap<>();
        map.put("date", sdf.format(date));

        for (int i = 1; i < parts.length; i ++) {
            String value = parts[i];
            String[] _value = value.split(":");

            map.put(_value[0], _value[1]);
        }

        return map;
    }
}
