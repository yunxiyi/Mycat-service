package cn.edu.nwsuaf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangrongchao on 2017/3/16.
 */
public class DBHost {

    private String host = "hostM1";
    private String url = "localhost:3306";
    private String user = "root";
    private String password = "123456";
    private int weight = 0;
    List<DBHost> readHosts;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DBHost> getReadHosts() {
        return readHosts == null ? new ArrayList<DBHost>() : readHosts;
    }

    public void setReadHosts(List<DBHost> readHosts) {
        this.readHosts = readHosts;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
