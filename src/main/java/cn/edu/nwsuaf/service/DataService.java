package cn.edu.nwsuaf.service;

import cn.edu.nwsuaf.model.DataHost;

import java.util.List;
import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public interface DataService {
    DataHost save(DataHost dataHost);
    DataHost delete(String name);
    DataHost getDataHost(String name);
    Map<String, DataHost> getAllDataHost();
}
