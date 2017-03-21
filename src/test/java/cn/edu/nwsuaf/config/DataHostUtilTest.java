package cn.edu.nwsuaf.config;

import cn.edu.nwsuaf.model.DataHost;
import cn.edu.nwsuaf.util.LoadDataHostUtil;
import com.alibaba.fastjson.JSON;
import io.mycat.MycatServer;
import io.mycat.config.model.DBHostConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public class DataHostUtilTest {

    /**
     * "balance":0,
     * "dbDriver":"native","dbType":"mysql","filters":"","hearbeatSQL":"select user()","logTime":300000,"maxCon":1000,"minCon":10,"name":"localhost1",
     */
    @Test
    public void load(){
        List<DataHost> root = new ArrayList<>();
        DataHost dataHost = new DataHost();
        root.add(dataHost);
        LoadDataHostUtil.loadDataHosts(root);

        System.out.println(JSON.toJSONString(MycatServer.getInstance().getConfig().getDataHosts()));
    }

}
