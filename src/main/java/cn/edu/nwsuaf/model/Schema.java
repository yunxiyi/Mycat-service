package cn.edu.nwsuaf.model;

import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.TableConfig;

import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/21.
 */
public class Schema {
    private String name;
    private String dataNode;
    private boolean checkSQLSchema;
    private String sqlMaxLimit;

    public Schema() {
    }

    public Schema(String name, String dataNode, boolean checkSQLSchema, String sqlMaxLimit) {
        this.name = name;
        this.dataNode = dataNode;
        this.checkSQLSchema = checkSQLSchema;
        this.sqlMaxLimit = sqlMaxLimit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataNode() {
        return dataNode;
    }

    public void setDataNode(String dataNode) {
        this.dataNode = dataNode;
    }

    public boolean isCheckSQLSchema() {
        return checkSQLSchema;
    }

    public void setCheckSQLSchema(boolean checkSQLSchema) {
        this.checkSQLSchema = checkSQLSchema;
    }

    public String getSqlMaxLimit() {
        return sqlMaxLimit;
    }

    public void setSqlMaxLimit(String sqlMaxLimit) {
        this.sqlMaxLimit = sqlMaxLimit;
    }

    @Override
    public String toString() {
        return "Schema{" +
                "name='" + name + '\'' +
                ", dataNode='" + dataNode + '\'' +
                ", checkSQLSchema=" + checkSQLSchema +
                ", sqlMaxLimit='" + sqlMaxLimit + '\'' +
                '}';
    }
}
