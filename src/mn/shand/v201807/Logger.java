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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class Logger {
    private static final long rotateSize = 5L * 1024L * 1024L;  // 5 MB

    private static final String logPath = ".";

    private String fileName;
    private boolean shouldRotate;

    public Logger(String fileName, boolean shouldRotate) {
        this.fileName = fileName;
        this.shouldRotate = shouldRotate;
    }

    public void log(String line) {
        File file = makeExist(fileName, shouldRotate);
        if (file != null) {
            try (FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void log(List<String> lines) {
        File file = makeExist(fileName, shouldRotate);
        if (file != null) {
            try(FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                for (String line : lines) {
                    out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File makeExist(String fileName, boolean rotate) {
        File path = new File(logPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        try {
            String fullName = path.getPath() + File.separator + fileName;
            File file = new File(fullName);
            if (!file.exists()) {
                file.createNewFile();
            } else if (file.length() >= rotateSize && rotate) {
                rotateFile(path, file);
            }

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void rotateFile(File path, File file) throws IOException {
        String name = file.getName();
        int a = name.lastIndexOf('.');
        String ext = a != -1 ? name.substring(a) : "";

        name = a != -1 ? name.substring(0, a) : name;
        name += "-";

        int increment = 0;
        for (String _name : path.list()) {
            if (_name.startsWith(name) && _name.endsWith(ext)) {
                String part = _name.substring(name.length(), _name.length() - ext.length());
                try {
                    int i = Integer.parseInt(part);
                    if (i > increment) {
                        increment = i;
                    }
                } catch (Exception e) {}
            }
        }

        increment = increment + 1;

        String incrStr = String.format("%3d", increment).replace(' ', '0');
        String newName = path.getPath() + File.separator + name + incrStr + ext;
        File newFile = new File(newName);
        file.renameTo(newFile);

        file.createNewFile();
    }

    public List<String> tail(int lines) {
        File file = makeExist(fileName, false);

        List<String> lineList = new ArrayList<>();

        try (java.io.RandomAccessFile fileHandler = new java.io.RandomAccessFile( file, "r" )) {
            StringBuilder sb = new StringBuilder();
            int line = 0;

            long fileLength = fileHandler.length() - 1;
            for (long filePointer = fileLength; filePointer != -1; filePointer--) {

                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();
                if (readByte == 0xA) { // \n
                    if (filePointer < fileLength) {
                        line ++;
                        lineList.add(sb.reverse().toString());
                        sb.setLength(0);
                    }
                } else {
                    sb.append((char) readByte);
                }

                if (line >= lines) {
                    break;
                }
            }

            return lineList;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> lines() {
        File file = makeExist(fileName, false);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> lines = new ArrayList<>();

            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }

            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
