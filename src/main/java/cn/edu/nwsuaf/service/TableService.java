package cn.edu.nwsuaf.service;

/**
 * Created by huangrongchao on 2017/3/19.
 */
public interface TableService {
    void createTable(String name,  String schema);

    void deleteTable(String name, String schema);

    String getTableName(String stmt);
}
