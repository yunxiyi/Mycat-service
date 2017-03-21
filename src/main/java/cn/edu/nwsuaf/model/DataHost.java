package cn.edu.nwsuaf.model;

import io.mycat.backend.datasource.PhysicalDBPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public class DataHost {
    private String name = "localhost2";
    private int maxCon = 300;
    private int minCon = 10;
    private int balance = 1;
    private int writeType = 1;
    private String dbType = "mysql";
    private String dbDriver = "native";
    private int switchType = 1;
    private int slaveThredshold = 300;
    private long logTime = PhysicalDBPool.LONG_TIME;
    private String heartbeat = "select user()";
    private boolean tempReadHostAvailable = false;

    private List<DBHost> writeNodes;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxCon() {
        return maxCon;
    }

    public void setMaxCon(int maxCon) {
        this.maxCon = maxCon;
    }

    public int getMinCon() {
        return minCon;
    }

    public void setMinCon(int minCon) {
        this.minCon = minCon;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getWriteType() {
        return writeType;
    }

    public void setWriteType(int writeType) {
        this.writeType = writeType;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public int getSwitchType() {
        return switchType;
    }

    public void setSwitchType(int switchType) {
        this.switchType = switchType;
    }

    public int getSlaveThredshold() {
        return slaveThredshold;
    }

    public void setSlaveThredshold(int slaveThredshold) {
        this.slaveThredshold = slaveThredshold;
    }

    public String getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(String heartbeat) {
        this.heartbeat = heartbeat;
    }

    public List<DBHost> getWriteNodes() {
        return writeNodes;
    }

    public void setWriteNodes(List<DBHost> writeNodes) {
        this.writeNodes = writeNodes;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public boolean isTempReadHostAvailable() {
        return tempReadHostAvailable;
    }

    public void setTempReadHostAvailable(boolean tempReadHostAvailable) {
        this.tempReadHostAvailable = tempReadHostAvailable;
    }
}
