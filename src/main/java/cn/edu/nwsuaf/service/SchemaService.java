package cn.edu.nwsuaf.service;

import cn.edu.nwsuaf.model.Schema;
import io.mycat.config.model.SchemaConfig;

/**
 * Created by huangrongchao on 2017/3/21.
 */
public interface SchemaService {
    SchemaConfig loadSchema(Schema schema);
}
