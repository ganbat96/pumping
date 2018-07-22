/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mn.shand.v2018.msg.ClientMsg;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class Client implements Runnable {
    public static final Logger logger = new Logger("client.log", true);

    private String code;
    private Socket socket;
    private PrintWriter writer;

    private MainForm.UIUpdater updater;

    public static final Map<String, ClientMsg> statusMap = new ConcurrentHashMap<>();

    public Client(String code, Socket socket){
        this.code = code;
        this.socket = socket;
    }

    public void send(String message) {
        try {
            writer.print(message);
            writer.flush();

            updater.queueLog("client.send : code = " + code + ", message = " + message);

            log(new Log(code, "send", message));
        } catch (Exception ex) {
            updater.queueLog("client.send eror : code = " + code + ", error = " + ex.getMessage());
            log(new Log(code, "send.Error", ex.getMessage()));
        }
    }

    public static void log(Log log) {
        if (log != null) {
            logger.log(log.toStr());
        }
    }

    @Override
    public void run() {
        ClientMsg msg0 = ClientMsg.create(code);
        msg0.setCode(code);
        msg0.setType(ClientMsg.TYPE_START);
        statusMap.put(code, msg0);

        updater.queue(msg0);
        log(new Log(code, "connected"));

        try (InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
             BufferedReader reader = new BufferedReader(isReader);
             PrintWriter writer = new PrintWriter(socket.getOutputStream())) {

            this.writer = writer;
            String message = null;

            while ((message = reader.readLine()) != null) {
                ClientMsg msg = ClientMsg.process(message);
                if (msg != null) {
                    msg.setType(ClientMsg.TYPE_STATUS);

                    updater.queueLog("Client processed a msg: " + message);
                    ClientMsg status = statusMap.get(code);
                    if (status == null) {
                        Log log = new Log();
                        log.setClientCode(code);
                        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        log.setAction("received");
                        log.setDescription(message);
                        log(log);
                    } else {
                        log(msg.changeLog(status));
                    }
                    statusMap.put(code, msg);

                    // Өөрчлөлтөө хийчихээд, цонхоо шинэчлэхийн тулд
                    updater.queue(msg);
                } else {
                    updater.queueLog("Client could not proccess this msg: " + message);
                }
            }
        } catch (IOException ex) {
            updater.queueLog("Client error: code=" + code + ", error=" + ex.getMessage());

            ClientMsg msg = ClientMsg.create(code);
            msg.setCode(code);
            msg.setType(ClientMsg.TYPE_STOP);
            statusMap.put(code, msg);

            updater.queue(msg);

            log(new Log(code, "conn.Error", ex.getMessage()));
        }
    }

    public MainForm.UIUpdater getUpdater() {
        return updater;
    }

    public void setUpdater(MainForm.UIUpdater updater) {
        this.updater = updater;
    }
}
