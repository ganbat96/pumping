/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v2018.msg;

import java.sql.Timestamp;
import java.util.StringJoiner;
import mn.shand.v201807.Log;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public abstract class ClientMsg {
    public static final int TYPE_START  = 0;
    public static final int TYPE_STOP   = 1;
    public static final int TYPE_STATUS = 2;

    protected String code;
    protected int type;  // start, end, status

    public static ClientMsg create(String code) {
        switch (code) {
            case "cc"   : return new HudagMsg(true);
            case "bb"   : return new HudagMsg(false);
            case "zg"   : return new ZeegMsg();
            case "gb"   : return new GobiMsg();
            case "de"   : return new DenjMsg();
            case "myng" : return new MyangaMsg();
        }

        throw new RuntimeException("Failed to created new msg from : '" + code + "'");

    }

    public static ClientMsg process(String message) {
        message = message.trim();
        String[] tokens = message.split(":");

        ClientMsg stat = create(tokens[0]);
        if (stat == null) {
            return null;
        }

        stat.setCode(tokens[0]);
        stat.process(tokens);

        return stat;
    }

    public ClientMsg() {
        super();
    }

    public abstract void process(String[] message);

    public abstract Log changeLog(ClientMsg last);

    public abstract boolean isActive();

    public abstract int getHPercentage(int code);

    protected int calcPercentage(double h, double max) {
        if (h <= 0.0d) {
            return 0;
        }

        if (h >= max) {
            return 100;
        }

        return (int) (h * 100 / max);
    }

    protected Log createLog() {
        Log log = new Log();
        log.setClientCode(code);
        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return log;
    }

    protected double smoothChange(StringJoiner description, String attr, double cur, double last, double maxChange) {
        double diff = Math.abs(cur - last);
        if (diff < maxChange) {
            return cur;
        }

        int sign = cur > last ? 1 : -1;
        double newValue = last + maxChange * sign;

        //description.add(attr + ": " + cur + " -> " + newValue + " : last = " + last + " : diff = " + diff);
        return newValue;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
