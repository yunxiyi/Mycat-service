package cn.edu.nwsuaf.redis;

import io.mycat.config.model.DataHostConfig;
import io.mycat.config.model.DataNodeConfig;
import io.mycat.config.model.SchemaConfig;

import java.util.Map;

/**
 * Created by yunxiyi on 2017/2/18.
 */
public class RedisShchemaLoader {

    public static void load(Map<String, DataHostConfig> dataHosts,
                     Map<String, DataNodeConfig> dataNodes,
                      Map<String, SchemaConfig> schemas) {
        dataHosts = load("dataHost:*", DataHostConfig.class);
        dataNodes = load("dataNode:*", DataNodeConfig.class);
        schemas = load("schema:*", SchemaConfig.class);

    }
    public static <T> Map<String, T> load(String pattern, Class<T> clazz){
        return RedisUtils.getAll(pattern, clazz);
    }
}
