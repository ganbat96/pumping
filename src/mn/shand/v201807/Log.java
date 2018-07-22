/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.sql.Timestamp;
import java.util.StringJoiner;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class Log {
    private String    clientCode;
    private Timestamp createdAt;
    private String    action;
    private String    description;

    public Log() {
    }

    public Log(String clientCode, String action) {
        this(clientCode, action, null);
    }

    public Log(String clientCode, String action, String description) {
        this.clientCode = clientCode;
        this.action = action;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.description = description;
    }

    public static Log fromStr (String line) {
        line = line.trim();
        String[] part = line.split(":");

        Log log = new Log();
        log.clientCode  = part[0];
        log.createdAt   = new Timestamp(Long.parseLong(part[1]));
        log.action      = part[2];
        log.description = part[3];
        return log;
    }

    public String toStr() {
        StringJoiner joiner = new StringJoiner(":");
        joiner.add(clientCode);
        joiner.add(String.valueOf(createdAt.getTime()));
        joiner.add(action);
        joiner.add(description != null ? description.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replace(':', ' ').trim() : "null");
        return joiner.toString();
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
