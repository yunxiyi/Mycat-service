package cn.edu.nwsuaf.model;

/**
 * Created by huangrongchao on 2017/3/19.
 */
public class Table {

    private String name;
    private String primaryKey;
    private String type;
    private String dataNode;
    private String rule;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataNode() {
        return dataNode;
    }

    public void setDataNode(String dataNode) {
        this.dataNode = dataNode;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", primaryKey='" + primaryKey + '\'' +
                ", type='" + type + '\'' +
                ", dataNode='" + dataNode + '\'' +
                ", rule='" + rule + '\'' +
                '}';
    }
}
