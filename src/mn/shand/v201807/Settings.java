/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sarnai
 */
public class Settings {
    private int offHourStart;
    private int offHourEnd;

    public static Settings load() {
        Map<String, String> values = Util.read();

        Settings settings = new Settings();
        settings.offHourStart = Util.asInt(values, "off.hours.start", 17);
        settings.offHourEnd   = Util.asInt(values, "off.hours.end",   22);
        return settings;
    }

    public void save() {
        Map<String, String> values = new HashMap<>();
        values.put("off.hours.start", String.valueOf(offHourStart));
        values.put("off.hours.end",   String.valueOf(offHourEnd));
        Util.write(values);
    }

    public int getOffHourStart() {
        return offHourStart;
    }

    public void setOffHourStart(int offHourStart) {
        this.offHourStart = offHourStart;
    }

    public int getOffHourEnd() {
        return offHourEnd;
    }

    public void setOffHourEnd(int offHourEnd) {
        this.offHourEnd = offHourEnd;
    }

    public static class Util {
        private static final String filePath = ".";
        private static final String fileName = "settings.log";

        private static Map<String, String> read() {
            File file = makeExist();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                Map<String, String> values = new HashMap<>();
                String line = br.readLine();
                while (line != null) {
                    String[] part = line.split("=");
                    values.put(part[0], part[1]);
                    line = br.readLine();
                }

                return values;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static void write(Map<String, String> lines) {
            File file = makeExist();

            try(FileWriter fw = new FileWriter(file, false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                for (Map.Entry<String, String> entry : lines.entrySet()) {
                    out.println(entry.getKey() + "=" + entry.getValue());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static File makeExist() {
            File path = new File(filePath);
            if (!path.exists()) {
                path.mkdirs();
            }

            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }

                return file;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static int asInt(Map<String, String> map, String attr, int defaultValue) {
            String value = map.get(attr);
            if (value == null) {
                return defaultValue;
            }

            return Integer.parseInt(value);
        }
    }
}
