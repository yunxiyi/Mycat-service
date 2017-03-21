package cn.edu.nwsuaf.service;

import cn.edu.nwsuaf.model.User;
import io.mycat.config.model.UserConfig;

/**
 * Created by huangrongchao on 2017/3/21.
 */
public interface UserService {
    UserConfig add(User user);
}
