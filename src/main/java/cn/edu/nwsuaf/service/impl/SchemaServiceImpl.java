package cn.edu.nwsuaf.service.impl;

import cn.edu.nwsuaf.model.Schema;
import cn.edu.nwsuaf.redis.RedisUtils;
import cn.edu.nwsuaf.service.SchemaService;
import com.alibaba.fastjson.JSON;
import io.mycat.MycatServer;
import io.mycat.backend.datasource.PhysicalDBNode;
import io.mycat.backend.datasource.PhysicalDBPool;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.TableConfig;
import io.mycat.config.util.ConfigException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by huangrongchao on 2017/3/21.
 */
public class SchemaServiceImpl implements SchemaService {
    Map<String, PhysicalDBNode> dataNodes = MycatServer.getInstance().getConfig().getDataNodes();
    Map<String, PhysicalDBPool> dataHosts = MycatServer.getInstance().getConfig().getDataHosts();
    Map<String, SchemaConfig> schemas = MycatServer.getInstance().getConfig().getSchemas();
    @Override
    public SchemaConfig loadSchema(Schema schema) {
        //读取各个属性
        String name = schema.getName();
        String dataNode = schema.getDataNode();
        String checkSQLSchemaStr = schema.isCheckSQLSchema() + "";
        String sqlMaxLimitStr = schema.getSqlMaxLimit();
        int sqlMaxLimit = -1;
        //读取sql返回结果集限制
        if (sqlMaxLimitStr != null && !sqlMaxLimitStr.isEmpty()) {
            sqlMaxLimit = Integer.parseInt(sqlMaxLimitStr);
        }

        // check dataNode already exists or not,看schema标签中是否有datanode
        String defaultDbType = null;
        //校验检查并添加dataNode
        if (dataNode != null && !dataNode.isEmpty()) {
            List<String> dataNodeLst = new ArrayList<String>(1);
            dataNodeLst.add(dataNode);
            checkDataNodeExists(dataNodeLst);

            defaultDbType = "mysql";
        } else {
            dataNode = null;
        }
        //加载schema下所有tables
        Map<String, TableConfig> tables = new HashMap<>();
        //判断schema是否重复
        if (schemas.containsKey(name)) {
            throw new ConfigException("schema " + name + " duplicated!");
        }

        // 设置了table的不需要设置dataNode属性，没有设置table的必须设置dataNode属性
        if (dataNode == null && tables.size() == 0) {
            throw new ConfigException(
                    "schema " + name + " didn't config tables,so you must set dataNode property!");
        }

        SchemaConfig schemaConfig = new SchemaConfig(name, dataNode,
                tables, sqlMaxLimit, "true".equalsIgnoreCase(checkSQLSchemaStr));

        //设定DB类型，这对之后的sql语句路由解析有帮助
        if (defaultDbType != null) {
            schemaConfig.setDefaultDataNodeDbType(defaultDbType);
            if (!"mysql".equalsIgnoreCase(defaultDbType)) {
                schemaConfig.setNeedSupportMultiDBType(true);
            }
        }

        // 判断是否有不是mysql的数据库类型，方便解析判断是否启用多数据库分页语法解析
        for (TableConfig tableConfig : tables.values()) {
            if (isHasMultiDbType(tableConfig)) {
                schemaConfig.setNeedSupportMultiDBType(true);
                break;
            }
        }
        //记录每种dataNode的DB类型
        Map<String, String> dataNodeDbTypeMap = new HashMap<>();
        for (String dataNodeName : dataNodes.keySet()) {
            PhysicalDBNode dataNodeConfig = dataNodes.get(dataNodeName);
            String dataHost = dataNodeConfig.getDbPool().getHostName();
            PhysicalDBPool dataHostConfig = dataHosts.get(dataHost);
            if (dataHostConfig != null && StringUtils.equals(dataNode, dataNodeName)) {
                String dbType = "mysql";
                dataNodeDbTypeMap.put(dataNodeName, dbType);
            }
        }
        schemaConfig.setDataNodeDbTypeMap(dataNodeDbTypeMap);
        schemas.put(schemaConfig.getName(), schemaConfig); //add to config (memory)
        save(schemaConfig); //save to redis

        return schemaConfig;
    }

    private void checkDataNodeExists(Collection<String> nodes) {
        Map<String, PhysicalDBNode> dataNodes = MycatServer.getInstance().getConfig().getDataNodes();
        if (nodes == null || nodes.size() < 1) {
            return;
        }
        for (String node : nodes) {
            if (!dataNodes.containsKey(node)) {
                throw new ConfigException("dataNode '" + node + "' is not found!");
            }
        }
    }

    private boolean isHasMultiDbType(TableConfig table) {
        Set<String> dbTypes = table.getDbTypes();
        for (String dbType : dbTypes) {
            if (!"mysql".equalsIgnoreCase(dbType)) {
                return true;
            }
        }
        return false;
    }

    private void save(SchemaConfig config){
        String prefix = "schema:";
        RedisUtils.set(prefix + config.getName(), config);
    }

    public static void main(String [] args){
        Schema schema = new Schema();
        schema.setName("schema_1");
        schema.setDataNode("dn1");
        schema.setSqlMaxLimit("100");
        schema.setCheckSQLSchema(true);

        SchemaService schemaService = new SchemaServiceImpl();

        System.out.println(JSON.toJSONString(schemaService.loadSchema(schema)));
    }
}
