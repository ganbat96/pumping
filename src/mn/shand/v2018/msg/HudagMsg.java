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
public class HudagMsg extends ClientMsg {
    private double temp;
    private double rh;
    private double p1;
    private double inverter;
    private String portb;

    private boolean hasInventer;

    public HudagMsg(boolean hasInventer) {
        this.hasInventer = hasInventer;
    }

    public void process(String[] message) {
        try{
            temp  = Integer.parseInt(message[1]);
            rh    = Integer.parseInt(message[2]);
            p1    = Integer.parseInt(message[3]);
            inverter = Integer.parseInt(message[4]);
            portb = message[9];
        }catch(Exception ex){System.out.println("hudag2 array exception");}
        //temp = temp * 150 / 256 - 74;
        //temp = Math.ceil(temp);

        //rh = rh * 125 / 256 - 25;
        //rh = Math.ceil(rh);
        if(inverter < 10){inverter =0;}
        p1 = (p1-50)*0.08;
        //p1 = Math.ceil(p1);
    }

    @Override
    public Log changeLog(ClientMsg last) {
        Log log = createLog();
        log.setAction("changed");

        HudagMsg hudag = (HudagMsg) last;

        StringJoiner description = new StringJoiner(" ");
        if (!Objects.equals(hudag.portb, this.portb)) {
            description.add("portb = " + portb);
        }

        this.inverter = smoothChange(description, "inverter", inverter, hudag.inverter, maxInverterChange);
        this.p1 = smoothChange(description, "p1", p1, hudag.p1, maxPressureChange);

        if (description.length() > 0) {
            log.setDescription(description.toString());
            return log;
        }

        return null;
    }

    @Override
    public boolean isActive() {
        if (hasInventer) {
             return inverter > 100.0d;
        } else {
            return portb != null && !Objects.equals("?", portb);
        }
    }

    @Override
    public int getHPercentage(int code) {
        return 0;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getTemp() {
        return temp;
    }

    public void setRh(double rh) {
        this.rh = rh;
    }

    public double getRh() {
        return rh;
    }

    public void setP1(double p1) {
        this.p1 = p1;
    }

    public double getP1() {
        return p1;
    }

    public void setInverter(double inverter) {
        this.inverter = inverter;
    }

    public double getInverter(){
        return inverter;
    }

    public void setPortb(String portb) {
        this.portb = portb;
    }

    public String getPortb() {
        return portb;
    }
}
