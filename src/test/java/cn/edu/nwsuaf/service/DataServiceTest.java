package cn.edu.nwsuaf.service;

import cn.edu.nwsuaf.model.DataHost;
import cn.edu.nwsuaf.service.impl.DataServiceImpl;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

/**
 * Created by huangrongchao on 2017/3/15.
 */
public class DataServiceTest {
    DataService dataService = new DataServiceImpl();

    @Test
    public void testGetDataHost(){
        DataHost dataHost = dataService.getDataHost("localhost1");

        System.out.println(JSON.toJSONString(dataHost));
    }
}
