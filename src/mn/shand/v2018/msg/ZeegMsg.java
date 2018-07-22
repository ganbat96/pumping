/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v2018.msg;

import java.util.Objects;
import java.util.StringJoiner;
import mn.shand.v201807.Log;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class ZeegMsg extends ClientMsg {
    public static final double maxH1 = 3.0d;
    public static final double maxChange = 0.004d;

    private double temp;
    private double rh;
    private double p1;
    private double p2;
    private double h1;
    private double dorgio1;
    private double dorgio2;
    private double inverter;
    private String portb;


    public void process(String[] messsage) {
        temp     = Integer.parseInt(messsage[1]);
        rh       = Integer.parseInt(messsage[2]);
        p1       = Integer.parseInt(messsage[3]);
        p2       = Integer.parseInt(messsage[4]);
        h1       = Integer.parseInt(messsage[5]);
        dorgio1  = Integer.parseInt(messsage[6]);
        dorgio2  = Integer.parseInt(messsage[7]);
        inverter = Integer.parseInt(messsage[8]);
        portb    = messsage[9];

       // temp = temp * 150 / 256 - 74;
        //temp = Math.ceil(temp);

       // rh=rh*125/256-25;
       // rh=Math.ceil(rh);

        p1=p1*2/256-0.4;
        p2=p2*2/256-0.4;

        h1=(h1-50.5)*1.4;
        h1=Math.ceil(h1)/100;
    }

    @Override
    public Log changeLog(ClientMsg last) {
        Log log = createLog();
        log.setAction("changed");

        ZeegMsg zeeg = (ZeegMsg) last;

        StringJoiner description = new StringJoiner(" ");
        if (!Objects.equals(zeeg.portb, this.portb)) {
            description.add("portb = " + portb);
        }

        this.h1 = smoothChange(description, "h1", h1, zeeg.h1, maxChange);

        if (description.length() > 0) {
            log.setDescription(description.toString());
            return log;
        }

        return null;
    }

    @Override
    public boolean isActive() {
        return portb != null && !Objects.equals("?", portb);
    }

    @Override
    public int getHPercentage(int code) {
        return calcPercentage(h1, maxH1);
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getRh() {
        return rh;
    }

    public void setRh(double rh) {
        this.rh = rh;
    }

    public double getP1() {
        return p1;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public double getP2() {
        return p2;
    }

    public void setP2(double p2) {
        this.p2 = p2;
    }

    public double getH1() {
        return h1;
    }

    public void setH1(double h1) {
        this.h1 = h1;
    }

    public double getDorgio1() {
        return dorgio1;
    }

    public void setDorgio1(double dorgio1) {
        this.dorgio1 = dorgio1;
    }

    public double getDorgio2() {
        return dorgio2;
    }

    public void setDorgio2(double dorgio2) {
        this.dorgio2 = dorgio2;
    }

    public double getInverter() {
        return inverter;
    }

    public void setInverter(double inverter) {
        this.inverter = inverter;
    }

    public String getPortb() {
        return portb;
    }

    public void setPortb(String portb) {
        this.portb = portb;
    }
}
