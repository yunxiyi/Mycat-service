package cn.edu.nwsuaf.service.impl;

import cn.edu.nwsuaf.model.User;
import cn.edu.nwsuaf.redis.RedisUtils;
import cn.edu.nwsuaf.service.UserService;
import com.alibaba.fastjson.JSON;
import io.mycat.MycatServer;
import io.mycat.config.model.UserConfig;
import io.mycat.config.model.UserPrivilegesConfig;
import io.mycat.util.SplitUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangrongchao on 2017/3/21.
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserConfig add(User user) {
        Map<String, UserConfig> users = MycatServer.getInstance().getConfig().getUsers();

        UserConfig userConfig = new UserConfig();

        UserPrivilegesConfig privilegesConfig = new UserPrivilegesConfig();
        userConfig.setPrivilegesConfig(privilegesConfig);

        userConfig.setName(user.getName());
        userConfig.setPassword(user.getPassword());
        userConfig.setReadOnly(user.isReadOnly());

        String schemas = user.getSchemas();
        if (schemas != null) {
            String[] strArray = SplitUtil.split(schemas, ',', true);
            userConfig.setSchemas(new HashSet<String>(Arrays.asList(strArray)));
        }

        save(userConfig);
        users.put(userConfig.getName(), userConfig);

        return userConfig;
    }

    @Override
    public Set<UserConfig> getAll() {
        Map<String, UserConfig> userConfigMap = RedisUtils.getAll("user:*", UserConfig.class);
        Set<UserConfig> userConfigs = new HashSet<>();
        userConfigMap.values().forEach(userConfig->{
            userConfigs.add(userConfig);
        });

        return userConfigs;
    }

    private void save(UserConfig userConfig){
        String prefix = "user:";
        RedisUtils.set(prefix + userConfig.getName(), userConfig);
    }

    public static void main(String [] args){
        User user = new User();
        user.setName("user_1");
        user.setPassword("123456");
        user.setSchemas("schema_1");

        UserService userService = new UserServiceImpl();

        System.out.println(JSON.toJSONString(userService.add(user)));
    }
}
