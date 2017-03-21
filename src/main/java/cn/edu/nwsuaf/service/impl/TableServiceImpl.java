package cn.edu.nwsuaf.service.impl;

import cn.edu.nwsuaf.service.TableService;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import io.mycat.MycatServer;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.TableConfig;
import io.mycat.config.util.ConfigException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangrongchao on 2017/3/19.
 */
public class TableServiceImpl implements TableService {
    Logger logger = Logger.getLogger(TableServiceImpl.class);

    @Override
    public void createTable(String tableName,String schema) {
        String primaryKey = null;
        tableName = StringUtils.upperCase(tableName);
        Map<String, SchemaConfig> schemas = MycatServer.getInstance().getConfig().getSchemas();
        SchemaConfig schemaConfig = schemas.get(schema);
        int tableType = 1;
        String dataNode = "";

        for(String node : schemaConfig.getAllDataNodes()){
            dataNode += node + ",";
        }
        dataNode = dataNode.substring(0, dataNode.length() - 1);

        boolean needAddLimit = false;

        boolean ruleRequired = false;
        TableConfig tableConfig =new TableConfig(tableName, primaryKey,
                false, needAddLimit, Integer.valueOf(tableType), dataNode,
                getDbType(dataNode), null,
                ruleRequired, null, false, null, null,null);



        if(schemaConfig != null){
            schemaConfig.getTables().put(tableName, tableConfig);
            logger.info(tableName + " create successful");
            return;
        }
        throw new ConfigException("schema : " + schema + " is not exists");
    }

    @Override
    public void deleteTable(String tableName, String schema) {
        Map<String, SchemaConfig> schemas = MycatServer.getInstance().getConfig().getSchemas();
        SchemaConfig schemaConfig = schemas.get(schema);

        if(schemaConfig != null){
            schemaConfig.getTables().remove(StringUtils.upperCase(tableName));
            logger.info(tableName + " delete successful");
            return;
        }

        throw new ConfigException("schema : " + schema + " is not exists");
    }

    @Override
    public String getTableName(String sql) {
        String dbType = JdbcConstants.MYSQL;

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

        if(stmtList.size() == 1){
            SQLStatement stmt = stmtList.get(0);
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);

            return visitor.getCurrentTable();
        }

        return null;
    }


    public static String filterSql(String sql){
        String newSql = sql.trim();
        String dbType = JdbcConstants.MYSQL;

        Map<String, SchemaConfig> schemas = MycatServer.getInstance().getConfig().getSchemas();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

        if(stmtList.size() == 1){
            SQLStatement stmt = stmtList.get(0);
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);
            String oldTableName = visitor.getCurrentTable();

            for(String schemaName : schemas.keySet()){
                if(oldTableName.startsWith("`" + schemaName + "`.")){
                    int pos = oldTableName.indexOf(".");
                    String currentTable = (oldTableName.substring(pos + 1)).replaceAll("`", "");
                    newSql = StringUtils.replace(newSql, oldTableName, currentTable);
                }
            }

        }

        return newSql;
    }
    private Set<String> getDbType (String dataNode) {
        Set<String> dbTypes = new HashSet<>();
        dbTypes.add("mysql");
        return dbTypes;
    }

    public static void main(String [] args){
        String sql = new String("insert into `TESTDB`.`bb` value(1)");
        sql = filterSql(sql);
        System.out.println(sql);
    }

}
