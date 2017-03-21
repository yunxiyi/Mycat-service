package cn.edu.nwsuaf.util;


import cn.edu.nwsuaf.model.DBHost;
import cn.edu.nwsuaf.model.DataHost;
import cn.edu.nwsuaf.redis.RedisUtils;
import cn.edu.nwsuaf.service.TableService;
import cn.edu.nwsuaf.service.impl.TableServiceImpl;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.mongodb.DB;
import io.mycat.MycatServer;
import io.mycat.backend.datasource.PhysicalDBPool;
import io.mycat.backend.datasource.PhysicalDatasource;
import io.mycat.backend.jdbc.JDBCDatasource;
import io.mycat.backend.mysql.nio.MySQLDataSource;
import io.mycat.backend.postgresql.PostgreSQLDataSource;
import io.mycat.config.model.DBHostConfig;
import io.mycat.config.model.DataHostConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.util.ConfigException;
import io.mycat.manager.response.ReloadConfig;
import io.mycat.util.DecryptUtil;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.*;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public class LoadDataHostUtil{

    public static void loadDataHosts(List<DataHost> root) {
        Map<String, PhysicalDBPool> dataHosts = MycatServer.getInstance().getConfig().getDataHosts();
        for (int i = 0, n = root.size(); i < n; ++i){
            DataHost dataHost = root.get(i);

            if(dataHosts.containsKey(dataHost.getName())){
                throw new ConfigException("dataHost name " + dataHost.getName() + "duplicated!");
            }
            String name = dataHost.getName();
            String dbType = String.valueOf(dataHost.getDbType());
            int maxCon = dataHost.getMaxCon();
            int minCon = dataHost.getMinCon();
            String filters = "";
            long logTime = dataHost.getLogTime();
            String dbDriver = dataHost.getDbDriver();
            int switchType = dataHost.getSwitchType();
            int slaveThreshold = dataHost.getSlaveThredshold();
            int balance = dataHost.getBalance();
            int writeType = dataHost.getWriteType();
            String heartbeatSQL = dataHost.getHeartbeat();


            boolean tempReadHostAvailable = dataHost.isTempReadHostAvailable();
            Map<Integer, DBHostConfig[]> readHostsMap = new HashMap<>(2);
            DBHostConfig[] writeDbConfs = new DBHostConfig[dataHost.getWriteNodes().size()];
            for(int w = 0, len = writeDbConfs.length; w < len; ++w){
                DBHost writeNode = dataHost.getWriteNodes().get(w);
                writeDbConfs[w] = createDBHostConf(name, writeNode, dbType + "", dbDriver, maxCon, minCon,filters,logTime);
                List<DBHost> readNodes = writeNode.getReadHosts();
                //读取对应的每一个readHost
                if (readNodes.size() != 0) {
                    DBHostConfig[] readDbConfs = new DBHostConfig[readNodes.size()];
                    for (int r = 0; r < readDbConfs.length; r++) {
                        DBHost readNode =  readNodes.get(r);
                        readDbConfs[r] = createDBHostConf(name, readNode, dbType, dbDriver, maxCon, minCon,filters, logTime);
                    }
                    readHostsMap.put(w, readDbConfs);
                }
            }
            DataHostConfig hostConf = new DataHostConfig(name, dbType + "", dbDriver,
                writeDbConfs, readHostsMap, switchType, slaveThreshold, tempReadHostAvailable);

            hostConf.setMaxCon(maxCon);
            hostConf.setMinCon(minCon);
            hostConf.setBalance(balance);
            hostConf.setWriteType(writeType);
            hostConf.setHearbeatSQL(heartbeatSQL);
            hostConf.setConnectionInitSql(null);
            hostConf.setFilters(filters);
            hostConf.setLogTime(logTime);

            dataHosts.put(hostConf.getName(), getPhysicalDBPool(hostConf));

            //RedisUtils.set("dataHost:" + hostConf.getName(), hostConf);
        }
    }

    private static DBHostConfig createDBHostConf(String dataHost, DBHost node,
                                         String dbType, String dbDriver, int maxCon,
                                         int minCon, String filters, long logTime) {

        String nodeHost = node.getHost();
        String nodeUrl = node.getUrl();
        String user = node.getUser();
        String password = node.getPassword();
        String usingDecrypt = "0";
        String passwordEncryty= DecryptUtil.DBHostDecrypt(usingDecrypt, nodeHost, user, password);

        int weight = node.getWeight();
        // int weight = StringUtils.isBlank(weightStr) ? PhysicalDBPool.WEIGHT : Integer.parseInt(weightStr) ;

        String ip = null;
        int port = 0;
        if (StringUtils.isBlank(nodeHost) || StringUtils.isBlank(nodeUrl) || StringUtils.isBlank(user)) {
            throw new ConfigException(
                    "dataHost "
                            + dataHost
                            + " define error,some attributes of this element is empty: "
                            + nodeHost);
        }
        if ("native".equalsIgnoreCase(dbDriver)) {
            int colonIndex = nodeUrl.indexOf(':');
            ip = nodeUrl.substring(0, colonIndex).trim();
            port = Integer.parseInt(nodeUrl.substring(colonIndex + 1).trim());
        } else {
            URI url;
            try {
                url = new URI(nodeUrl.substring(5));
            } catch (Exception e) {
                throw new ConfigException("invalid jdbc url " + nodeUrl + " of " + dataHost);
            }
            ip = url.getHost();
            port = url.getPort();
        }

        DBHostConfig conf = new DBHostConfig(nodeHost, ip, port, nodeUrl, user, passwordEncryty,password);
        conf.setDbType(dbType);
        conf.setMaxCon(maxCon);
        conf.setMinCon(minCon);
        conf.setFilters(filters);
        conf.setLogTime(logTime);
        conf.setWeight(weight); 	//新增权重
        return conf;
    }

    private static PhysicalDBPool getPhysicalDBPool(DataHostConfig conf) {
        String name = conf.getName();
        //数据库类型，我们这里只讨论MySQL
        String dbType = conf.getDbType();
        //连接数据库驱动，我们这里只讨论MyCat自己实现的native
        String dbDriver = conf.getDbDriver();
        //针对所有写节点创建PhysicalDatasource
        PhysicalDatasource[] writeSources = createDataSource(conf, name,
                dbType, dbDriver, conf.getWriteHosts(), false);
        Map<Integer, DBHostConfig[]> readHostsMap = conf.getReadHosts();
        Map<Integer, PhysicalDatasource[]> readSourcesMap = new HashMap<Integer, PhysicalDatasource[]>(
                readHostsMap.size());
        //对于每个读节点建立key为writeHost下标value为readHost的PhysicalDatasource[]的哈希表
        for (Map.Entry<Integer, DBHostConfig[]> entry : readHostsMap.entrySet()) {
            PhysicalDatasource[] readSources = createDataSource(conf, name,
                    dbType, dbDriver, entry.getValue(), true);
            readSourcesMap.put(entry.getKey(), readSources);
        }
        PhysicalDBPool pool = new PhysicalDBPool(conf.getName(), conf,
                writeSources, readSourcesMap, conf.getBalance(),
                conf.getWriteType());
        return pool;
    }

    private static PhysicalDatasource[] createDataSource(DataHostConfig conf,
                                                  String hostName, String dbType, String dbDriver,
                                                  DBHostConfig[] nodes, boolean isRead) {
        SystemConfig systemConfig = MycatServer.getInstance().getConfig().getSystem();
        PhysicalDatasource[] dataSources = new PhysicalDatasource[nodes.length];
        if (dbType.equals("mysql") && dbDriver.equals("native")) {
            for (int i = 0; i < nodes.length; i++) {
                //设置最大idle时间，默认为30分钟
                nodes[i].setIdleTimeout(systemConfig.getIdleTimeout());
                MySQLDataSource ds = new MySQLDataSource(nodes[i], conf, isRead);
                dataSources[i] = ds;
            }

        } else if (dbDriver.equals("jdbc")) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setIdleTimeout(systemConfig.getIdleTimeout());
                JDBCDatasource ds = new JDBCDatasource(nodes[i], conf, isRead);
                dataSources[i] = ds;
            }
        } else if ("postgresql".equalsIgnoreCase(dbType) && dbDriver.equalsIgnoreCase("native")){
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setIdleTimeout(systemConfig.getIdleTimeout());
                PostgreSQLDataSource ds = new PostgreSQLDataSource(nodes[i], conf, isRead);
                dataSources[i] = ds;
            }
        } else{
            throw new ConfigException("not supported yet !" + hostName);
        }
        return dataSources;
    }

    public static void main(String [] args){
        TableService tableService = new TableServiceImpl();
        String sql = "create database bb";

        String dbType = JdbcConstants.MYSQL;

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

        if(stmtList.size() == 1){
            SQLStatement stmt = stmtList.get(0);
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);
            System.out.println(JSON.toJSONString(visitor));
        }
    }


}
