package com.zh.dcsservertools.bean;

import java.util.ArrayList;
import java.util.List;

public class ServiceListBean {

    private int SERVERS_MAX_COUNT;
    private String SERVERS_MAX_DATE;
    private int PLAYERS_COUNT;
    private List<SERVERSBean> MY_SERVERS = new ArrayList<SERVERSBean>();
    private List<SERVERSBean> SERVERS = new ArrayList<SERVERSBean>();

    public int getSERVERS_MAX_COUNT() {
        return SERVERS_MAX_COUNT;
    }

    public void setSERVERS_MAX_COUNT(int SERVERS_MAX_COUNT) {
        this.SERVERS_MAX_COUNT = SERVERS_MAX_COUNT;
    }

    public String getSERVERS_MAX_DATE() {
        return SERVERS_MAX_DATE;
    }

    public void setSERVERS_MAX_DATE(String SERVERS_MAX_DATE) {
        this.SERVERS_MAX_DATE = SERVERS_MAX_DATE;
    }

    public int getPLAYERS_COUNT() {
        return PLAYERS_COUNT;
    }

    public void setPLAYERS_COUNT(int PLAYERS_COUNT) {
        this.PLAYERS_COUNT = PLAYERS_COUNT;
    }

    public List<SERVERSBean> getMY_SERVERS() {
        return MY_SERVERS;
    }

    public void setMY_SERVERS(List<SERVERSBean> MY_SERVERS) {
        this.MY_SERVERS = MY_SERVERS;
    }

    public List<SERVERSBean> getSERVERS() {
        return SERVERS;
    }

    public void setSERVERS(List<SERVERSBean> SERVERS) {
        this.SERVERS = SERVERS;
    }

    public static class SERVERSBean {

        private String NAME;
        private String IP_ADDRESS;
        private String PORT;
        private String MISSION_NAME;
        private String MISSION_TIME;
        private String PLAYERS;
        private String PLAYERS_MAX;
        private String PASSWORD;
        private String DESCRIPTION;
        private String MISSION_TIME_FORMATTED;

        public String getNAME() {
            return NAME;
        }

        public void setNAME(String NAME) {
            this.NAME = NAME;
        }

        public String getIP_ADDRESS() {
            return IP_ADDRESS;
        }

        public void setIP_ADDRESS(String IP_ADDRESS) {
            this.IP_ADDRESS = IP_ADDRESS;
        }

        public String getPORT() {
            return PORT;
        }

        public void setPORT(String PORT) {
            this.PORT = PORT;
        }

        public String getMISSION_NAME() {
            return MISSION_NAME;
        }

        public void setMISSION_NAME(String MISSION_NAME) {
            this.MISSION_NAME = MISSION_NAME;
        }

        public String getMISSION_TIME() {
            return MISSION_TIME;
        }

        public void setMISSION_TIME(String MISSION_TIME) {
            this.MISSION_TIME = MISSION_TIME;
        }

        public String getPLAYERS() {
            return PLAYERS;
        }

        public void setPLAYERS(String PLAYERS) {
            this.PLAYERS = PLAYERS;
        }

        public String getPLAYERS_MAX() {
            return PLAYERS_MAX;
        }

        public void setPLAYERS_MAX(String PLAYERS_MAX) {
            this.PLAYERS_MAX = PLAYERS_MAX;
        }

        public String getPASSWORD() {
            return PASSWORD;
        }

        public void setPASSWORD(String PASSWORD) {
            this.PASSWORD = PASSWORD;
        }

        public String getDESCRIPTION() {
            return DESCRIPTION;
        }

        public void setDESCRIPTION(String DESCRIPTION) {
            this.DESCRIPTION = DESCRIPTION;
        }

        public String getMISSION_TIME_FORMATTED() {
            return MISSION_TIME_FORMATTED;
        }

        public void setMISSION_TIME_FORMATTED(String MISSION_TIME_FORMATTED) {
            this.MISSION_TIME_FORMATTED = MISSION_TIME_FORMATTED;
        }
    }
}