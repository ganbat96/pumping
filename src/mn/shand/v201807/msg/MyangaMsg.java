/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807.msg;

import java.util.Objects;
import java.util.StringJoiner;
import mn.shand.v201807.Log;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class MyangaMsg extends ClientMsg {
    public static final double maxH1 = 3.15d;
    public static final double maxH2 = 3.15d;

    public static final double maxChange = 0.004d;

    private double temp;
    private double rh;
    private double h1;
    private double h2;
    private String portb;

    public void process(String[] myBuffer) {
        temp = Integer.parseInt(myBuffer[1]);
        rh   = Integer.parseInt(myBuffer[2]);
        h1   = Integer.parseInt(myBuffer[4]);
        h2   = Integer.parseInt(myBuffer[5]);

        temp=temp*19.5/32-79;
        temp=Math.ceil(temp);

        rh=rh*125/256-25;
        rh=Math.ceil(rh);

        h1=h1/40.4-1.87;
        h1=(Math.ceil(h1*100));
        h1=h1/100;

        h2=h2/40.4-1.5;
        h2=(Math.ceil(h2*100));
        h2=h2/100;
    }

    @Override
    public Log changeLog(ClientMsg last) {
        Log log = createLog();
        log.setAction("changed");

        MyangaMsg mynga = (MyangaMsg) last;

        StringJoiner description = new StringJoiner(" ");
        if (!Objects.equals(mynga.portb, this.portb)) {
            description.add("portb = " + portb);
        }

        this.h1 = smoothChange(description, "h1", h1, mynga.h1, maxChange);
        this.h2 = smoothChange(description, "h2", h2, mynga.h2, maxChange);

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
        if (code == 1) {
            return calcPercentage(h1, maxH1);
        }

        if (code == 2) {
            return calcPercentage(h2, maxH2);
        }

        return 0;
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

    public double getH1() {
        return h1;
    }

    public void setH1(double h1) {
        this.h1 = h1;
    }

    public double getH2() {
        return h2;
    }

    public void setH2(double h2) {
        this.h2 = h2;
    }

    public String getPortb() {
        return portb;
    }

    public void setPortb(String portb) {
        this.portb = portb;
    }

}
