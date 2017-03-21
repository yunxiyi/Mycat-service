package cn.edu.nwsuaf.service.impl;

import cn.edu.nwsuaf.model.DataHost;
import cn.edu.nwsuaf.service.DataService;
import cn.edu.nwsuaf.redis.RedisUtils;

import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public class DataServiceImpl implements DataService {
    public static String prefix = "dataHost:";
    @Override
    public DataHost save(DataHost dataHost) {
        String key = prefix + dataHost.getName();
        RedisUtils.set(key, dataHost);
        return dataHost;
    }

    @Override
    public DataHost delete(String name) {
        String key = prefix + name;
        return RedisUtils.remove(key, DataHost.class);
    }

    @Override
    public DataHost getDataHost(String name) {
        String key = prefix + name;
        return RedisUtils.get(key, DataHost.class);
    }

    @Override
    public Map<String, DataHost> getAllDataHost() {
        String key = prefix + "*";

        return RedisUtils.getAll(key, DataHost.class);
    }
}
