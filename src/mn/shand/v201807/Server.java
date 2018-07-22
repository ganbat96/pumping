/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class Server implements Runnable {
    private static final Map<String, String> CLIENT_IP_MAP = new HashMap<>();
    static {
        CLIENT_IP_MAP.put("/192.168.255.102", "bb");
        CLIENT_IP_MAP.put("/192.168.255.101", "de");
        CLIENT_IP_MAP.put("/192.168.255.125", "gb");
        CLIENT_IP_MAP.put("/192.168.255.100", "zg");
        CLIENT_IP_MAP.put("/192.168.255.103", "cc");
        CLIENT_IP_MAP.put("/192.168.255.107", "myng");
    }

    private MainForm.UIUpdater updater;
    private Map<String, Client> clients;

    public Server(MainForm mainForm, Map<String, Client> clients) {
        super();
        this.updater = new MainForm.UIUpdater(mainForm);
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSock = new ServerSocket(50000);

            while (true) {
                Socket clientSock = serverSock.accept();
                String ip = clientSock.getInetAddress().toString();

                String code = CLIENT_IP_MAP.get(ip);
                clientSock.setSoTimeout(80000);
                Client reader = new Client(code, clientSock);
                reader.setUpdater(updater);
                clients.put(code, reader);

                Thread listener = new Thread(reader);
                listener.start();

                updater.queueLog("new cliend connected: ip=" + ip + ", code=" + code);
            }
        } catch (Exception ex) {
            updater.queueLog("Server error : " + ex.getMessage());
//            ta_chat.append("анхаар холболт саллаа. \n");
        }
    }
}
