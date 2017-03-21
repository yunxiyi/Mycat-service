package cn.edu.nwsuaf.util;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import io.mycat.MycatServer;
import io.mycat.config.model.SchemaConfig;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/20.
 */
public class SQLFilterUtil {
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

            if(oldTableName == null){
                return sql;
            }

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
}
