/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.StringJoiner;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class PumpData {
    private String pumpCode;
    private Timestamp readAt;
    private BigDecimal counterValue;
    private BigDecimal flowRate;

    public PumpData() {
    }

    public PumpData(String pumpCode, Timestamp readAt, BigDecimal counterValue, BigDecimal flowRate) {
        this.pumpCode = pumpCode;
        this.readAt = readAt;
        this.counterValue = counterValue;
        this.flowRate = flowRate;
    }

    public static String toStr(PumpData data) {
        StringJoiner joiner = new StringJoiner(":");
        joiner.add(data.pumpCode);
        joiner.add(String.valueOf(data.readAt.getTime()));
        joiner.add(data.counterValue.stripTrailingZeros().toPlainString());
        joiner.add(data.flowRate.stripTrailingZeros().toPlainString());
        return joiner.toString();
    }

    public static PumpData fromStr(String line) {
        String[] part = line.trim().split(":");

        PumpData data = new PumpData();
        data.setPumpCode(part[0]);
        data.setReadAt(new java.sql.Timestamp(Long.parseLong(part[1])));
        data.setCounterValue(new BigDecimal(part[2]));
        data.setFlowRate(    new BigDecimal(part[3]));
        return data;
    }

    public String getPumpCode() {
        return pumpCode;
    }

    public void setPumpCode(String pumpCode) {
        this.pumpCode = pumpCode;
    }

    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
    }

    public BigDecimal getCounterValue() {
        return counterValue;
    }

    public void setCounterValue(BigDecimal counterValue) {
        this.counterValue = counterValue;
    }

    public BigDecimal getFlowRate() {
        return flowRate;
    }

    public void setFlowRate(BigDecimal flowRate) {
        this.flowRate = flowRate;
    }
}
