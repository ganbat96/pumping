/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mn.shand.v2018.msg.ClientMsg;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class AutoPilot implements Runnable {
    private static final Map<String, String[][]> stopPumpCodes    = new ConcurrentHashMap<>();
    private static final Map<String, String[][]> startPump1Codes = new ConcurrentHashMap<>();
    private static final Map<String, String[][]> startPump2Codes = new ConcurrentHashMap<>();
    static {
        stopPumpCodes.put("de", new String[][] { {"II", "3500"},
                                                {"FF", "1"   } });
        stopPumpCodes.put("cc", new String[][] { {"BB", "1"   } });
        stopPumpCodes.put("bb", new String[][] { {"DD", "0"   } });
        stopPumpCodes.put("gb", new String[][] { {"NN", "3500"},
                                                {"KK", "1"   } });
        stopPumpCodes.put("zg", new String[][] { {"SS", "3500"},
                                                {"PP", "1"   } });

        startPump1Codes.put("de", new String[][] { {"II", "3500"},
                                                   {"FF", "800" },
                                                   {"EE", "1000"},
                                                   {"HH", "1"   } });
        startPump1Codes.put("gb", new String[][] { {"NN", "3500"},
                                                   {"KK", "1000"},
                                                   {"JJ", "1000"},
                                                   {"MM", "1"   } });
        startPump1Codes.put("zg", new String[][] { {"SS", "3500"},
                                                   {"PP", "1000"},
                                                   {"OO", "800" },
                                                   {"RR", "1"   } });
        startPump1Codes.put("bb", new String[][] { {"CC", "1"   } });
        startPump1Codes.put("cc", new String[][] { {"AA", "1"   } });

        startPump2Codes.put("de", new String[][] { {"II", "3500"},
                                                   {"FF", "800" },
                                                   {"GG", "800" },
                                                   {"HH", "1"   } });
        startPump2Codes.put("gb", new String[][] { {"NN", "3500"},
                                                   {"KK", "1000"},
                                                   {"LL", "800" },
                                                   {"MM", "1"   } });
        startPump2Codes.put("zg", new String[][] { {"SS", "3500"},
                                                   {"PP", "1000"},
                                                   {"QQ", "800" },
                                                   {"RR", "1"   } });
        startPump2Codes.put("bb", new String[][] { {"CC", "1"   } });
        startPump2Codes.put("cc", new String[][] { {"AA", "1"   } });
    }

    private static final List<StartRule> startRules = new ArrayList<>();
    private static final List<StartRule> stopRules  = new ArrayList<>();
    static {
        startRules.add(new StartRule("de", 30, "myng", 80));
        startRules.add(new StartRule("gb", 30, "de",   50));
        startRules.add(new StartRule("zg", 30, "gb",   50));
        startRules.add(new StartRule("cc", 0,  "zg",   50));
        startRules.add(new StartRule("bb", 0,  "zg",   50));

        stopRules.add(new StartRule("de", 0, "myng", 95));
        stopRules.add(new StartRule("gb", 0, "de",   95));
        stopRules.add(new StartRule("zg", 0, "gb",   95));
        stopRules.add(new StartRule("cc", 0, "zg",   70));
        stopRules.add(new StartRule("bb", 0, "zg",   95));
    }

    private Map<String, Client> clients;
    private MainForm.UIUpdater uiUdpater;

    private Settings settings = Settings.load();

    private volatile boolean autoPilot;
    private volatile boolean toTank1;
    private volatile boolean toTank2;

    public AutoPilot(MainForm.UIUpdater uiUdpater, Map<String, Client> clients) {
        this.clients = clients;
        this.uiUdpater = uiUdpater;
    }

    @Override
    public void run() {
        try {
            String status = autoPilot();
            uiUdpater.queueAutoPilotStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String autoPilot() {
        String amReason = checkAutoMode();
        if (amReason != null) {
            return amReason;
        }

        boolean offHour = false;
        int now = LocalTime.now().getHour();
        if (now >= settings.getOffHourStart() &&
            now <= settings.getOffHourEnd()) {
            offHour = true;
        }

        if (offHour) {
            for (String code : new String[] {"cc", "bb", "zg", "gb", "de", "myng"}) {
                stopPump(code);
            }

            return "Хэмнэх цаг";
        }

        int tankNum = toTank1 ? 1 : 2;

        for (StartRule rule : startRules) {
            int srcPct = getPercentage(rule.srcCode, tankNum);
            int trgPct = getPercentage(rule.trgCode, tankNum);

            if (rule.minTrgPct >= trgPct) {
                if (rule.minSrcPct == 0 || rule.minSrcPct < srcPct) {
                    startPump(rule.srcCode, tankNum);
                }
            }
        }

        for (StartRule rule : stopRules) {
            int trgPct = getPercentage(rule.trgCode, tankNum);
            if (rule.minTrgPct <= trgPct) {
                stopPump(rule.srcCode);
            }
        }

        return "Идэвхитэй";
    }

    public String checkAutoMode() {
        if (!autoPilot) {
            return "Идэвхигүй";
        }

        if (!toTank1 && !toTank2) {
            return "Сан сонгогдоогүй";
        }

        for (String code : new String[] {"cc", "bb", "zg", "gb", "de", "myng"}) {
            ClientMsg msg = Client.statusMap.get(code);
            if (msg == null || msg.getType() == ClientMsg.TYPE_STOP) {
                return code + " унтраастай";
            }
        }

        return null;
    }

    private void stopPump(String code) {
        if (!isClientActive(code)) {
            return;
        }

        send(clients.get(code), stopPumpCodes.get(code));
    }

    private void startPump(String code, int tankNum) {
        if (isClientActive(code)) {
            return;
        }

        Map<String, String[][]> codes = tankNum == 1 ? startPump1Codes : startPump2Codes;
        send(clients.get(code), codes.get(code));
    }

    public static void send(Client client, String[][] msg) {
        if (client == null) {
            return;
        }

        for (String[] codes : msg) {
            client.send(codes[0]);

            long pause = Long.parseLong(codes[1]);
            if (pause > 0L) {
                try {
                    Thread.sleep(pause);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isClientActive(String code) {
        ClientMsg msg = Client.statusMap.get(code);
        return msg != null && msg.isActive();
    }

    private int getPercentage(String code, int tankNum) {
        ClientMsg msg = Client.statusMap.get(code);
        return msg.getHPercentage(tankNum);
    }

    public boolean isAutoPilot() {
        return autoPilot;
    }

    public void setAutoPilot(boolean autoPilot) {
        this.autoPilot = autoPilot;
    }

    public boolean isToTank1() {
        return toTank1;
    }

    public void setToTank1(boolean toTank1) {
        this.toTank1 = toTank1;
    }

    public boolean isToTank2() {
        return toTank2;
    }

    public void setToTank2(boolean toTank2) {
        this.toTank2 = toTank2;
    }

    public static class StartRule {
        private String srcCode;
        private int    minSrcPct;

        private String trgCode;
        private int    minTrgPct;

        public StartRule(String srcCode, int minSrcPct, String trgCode, int minTrgPct) {
            this.srcCode = srcCode;
            this.minSrcPct = minSrcPct;

            this.trgCode = trgCode;
            this.minTrgPct = minTrgPct;
        }

        public String getSrcCode() {
            return srcCode;
        }

        public void setSrcCode(String srcCode) {
            this.srcCode = srcCode;
        }

        public String getTrgCode() {
            return trgCode;
        }

        public void setTrgCode(String trgCode) {
            this.trgCode = trgCode;
        }

        public int getMinSrcPct() {
            return minSrcPct;
        }

        public void setMinSrcPct(int minSrcPct) {
            this.minSrcPct = minSrcPct;
        }

        public int getMinTrgPct() {
            return minTrgPct;
        }

        public void setMinTrgPct(int minTrgPct) {
            this.minTrgPct = minTrgPct;
        }
    }
}
