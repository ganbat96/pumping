/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.shand.v201807;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mn.shand.v2018.msg.ClientMsg;
import mn.shand.v2018.msg.DenjMsg;
import mn.shand.v2018.msg.GobiMsg;
import mn.shand.v2018.msg.HudagMsg;
import mn.shand.v2018.msg.MyangaMsg;
import mn.shand.v2018.msg.ZeegMsg;

/**
 *
 * @author Ganbat Bayarbaatar <ganbat96@gmail.com>
 */
public class Alert {
    private static final List<Item> alertItems = new ArrayList<>();
    static {
        alertItems.add(new Item(new String[] {"de", "gb", "zg", "cc", "bb"}, "temp", "<",  5d));
        alertItems.add(new Item(new String[] {"de", "gb", "zg", "cc", "bb"}, "temp", ">", 50d));

        alertItems.add(new Item(new String[] {"myng"},                       "temp", "<", 5d));
        alertItems.add(new Item(new String[] {"myng"},                       "temp", ">", 80d));

        alertItems.add(new Item(new String[] {"de", "gb", "zg", "cc", "bb"}, "rh", ">",  80d));
        alertItems.add(new Item(new String[] {"myng"},                       "rh", ">", 220d));

        alertItems.add(new Item(new String[] {"de", "gb", "zg"},             "h",  "<", 0.8d));
        alertItems.add(new Item(new String[] {"de", "gb", "zg"},             "h",  ">", 3.1d));

        alertItems.add(new Item(new String[] {"myng"},                       "hh", "<", 0.8d));
        alertItems.add(new Item(new String[] {"myng"},                       "hh", ">", 3.25d));

        alertItems.add(new Item(new String[] {"de", "gb", "zg"},             "pp", ">", 353d));
        alertItems.add(new Item(new String[] {"cc", "bb"},                   "p",  ">", 178d));

        alertItems.add(new Item(new String[] {"de", "gb", "zg"},             "dd", ">", 132d));

        alertItems.add(new Item(new String[] {"de", "gb", "zg"},             "smoke", "in", Arrays.asList("<", ">", ":")));
        alertItems.add(new Item(new String[] {"cc", "bb"},                   "smoke", "in", Arrays.asList("<", "=")));
        alertItems.add(new Item(new String[] {"myng"},                       "smoke", "<",  100d));
    }

    private Map<String, StationReader.Value> stationValues;

    public Alert(Map<String, StationReader.Value> stationValues) {
        this.stationValues = stationValues;
    }

    public List<Result> check() {
        List<Result> resultList = new ArrayList<>();

        for (Item item : alertItems) {
            for (String code : item.codes) {
                Object value1 = null;
                Object value2 = null;

                switch (item.type) {
                    case "temp" : value1 = getTemp(code); break;
                    case "rh"   : value1 = getRH(code); break;
                    case "h"    : value1 = getH(code, 0); break;
                    case "hh"   : value1 = getH(code, 1);
                                  value2 = getH(code, 2); break;
                    case "p"    : value1 = getPressure(code, 0); break;
                    case "pp"   : value1 = getPressure(code, 1);
                                  value2 = getPressure(code, 2); break;
                    case "dd"   : value1 = getQuake(code, 1);
                                  value2 = getQuake(code, 2); break;
                    case "smoke": value1 = getSmoke(code);
                }

                check(resultList, item, code, value1, false);
                check(resultList, item, code, value2, true);
            }
        }

        return resultList;
    }

    private void check(List<Result> resultList, Item item, String code, Object value, boolean second) {
        if (value == null) {
            return;
        }

        if (!check(item, value)) {
            return;
        }

        Result r = new Result();
        r.code = code;
        r.item = item;
        r.value = value;
        r.second = second;
        resultList.add(r);
    }

    
    private boolean check(Item item, Object value) {
        switch (item.operator) {
            case "="  : return Objects.equals(value, item.value);
            case ">"  : return doubleValue(value) >  doubleValue(item.value);
            case "<"  : return doubleValue(value) <  doubleValue(item.value);
            case ">=" : return doubleValue(value) >= doubleValue(item.value);
            case "<=" : return doubleValue(value) <= doubleValue(item.value);
            case "in" : ((List) item.value).contains(value);
        }

        return false;
    }

    private double doubleValue(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        }   
        
        if (value instanceof Integer) {
            return (double) ((Integer) value).intValue();
        }
        
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        
        throw new RuntimeException("Can not conver "  + value + " to double");
    }

    private Double getTemp(String code) {
        ClientMsg msg = Client.statusMap.get(code);
        if (msg != null) {
            switch (code) {
                case "cc" : return ((HudagMsg) msg).getTemp();
                case "bb" : return ((HudagMsg) msg).getTemp();
                case "de" : return ((DenjMsg)  msg).getTemp();
                case "gb" : return ((GobiMsg)  msg).getTemp();
                case "zg" : return ((ZeegMsg)  msg).getTemp();
                case "myng" : return ((MyangaMsg)  msg).getTemp();
            }
        }

        return null;
    }

    private Double getRH(String code) {
        ClientMsg msg = Client.statusMap.get(code);
        if (msg != null) {
            switch (code) {
                case "cc" : return ((HudagMsg) msg).getRh();
                case "bb" : return ((HudagMsg) msg).getRh();
                case "de" : return ((DenjMsg)  msg).getRh();
                case "gb" : return ((GobiMsg)  msg).getRh();
                case "zg" : return ((ZeegMsg)  msg).getRh();
                case "myng" : return ((MyangaMsg)  msg).getRh();
            }
        }

        return null;
    }

    private Double getH(String code, int idx) {
        ClientMsg msg = Client.statusMap.get(code);
        if (msg != null) {
            switch (code) {
                case "de" : return ((DenjMsg)  msg).getH1();
                case "gb" : return ((GobiMsg)  msg).getH1();
                case "zg" : return ((ZeegMsg)  msg).getH1();
                case "myng" : return (idx == 1 ? ((MyangaMsg)  msg).getH1() : ((MyangaMsg)  msg).getH2());
            }
        }

        return null;
    }

    private Double getPressure(String code, int idx) {
        if (code.equals("cc") || code.equals("bb")) {
            ClientMsg msg = Client.statusMap.get(code);
            if (msg != null) {
                return ((HudagMsg) msg).getP1();
            }
        } else if (code.equals("de") || code.equals("gb") || code.equals("zg")) {
            StationReader.Value value = stationValues.get(code);
            if (value != null) {
                return (double) (idx == 1 ? value.getP11() : value.getP22());
            }
        }

        return null;
    }

    private Double getQuake(String code, int idx) {
        if (code.equals("de") || code.equals("gb") || code.equals("zg")) {
            StationReader.Value value = stationValues.get(code);
            if (value != null) {
                return (double) (idx == 1 ? value.getD11() : value.getD22());
            }
        }

        return null;
    }

    private Object getSmoke(String code) {
        ClientMsg msg = Client.statusMap.get(code);
        if (msg != null) {
            switch (code) {
                case "cc" : return ((HudagMsg) msg).getPortb();
                case "bb" : return ((HudagMsg) msg).getPortb();
                case "de" : return ((DenjMsg)  msg).getPortb();
                case "gb" : return ((GobiMsg)  msg).getPortb();
                case "zg" : return ((ZeegMsg)  msg).getPortb();
                case "myng" : 
                    int smoke = ((MyangaMsg) msg).getSmoke();
                    if (smoke != 0) {
                        return smoke;
                    }
            }
        }

        return null;
    }

    public static class Result {
        private Item item;
        private String code;
        private Object value;
        private boolean second;

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public boolean isSecond() {
            return second;
        }

        public void setSecond(boolean second) {
            this.second = second;
        }
    }

    public static class Item {
        private String[] codes;
        private String type;
        private String operator;
        private Object value;

        public Item() {
        }

        public Item(String[] codes, String type, String operator, Object value) {
            this.codes = codes;
            this.type = type;
            this.operator = operator;
            this.value = value;
        }

        public String[] getCodes() {
            return codes;
        }

        public void setCodes(String[] codes) {
            this.codes = codes;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
