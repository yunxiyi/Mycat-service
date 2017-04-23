/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.mycat.MycatServer;
import io.mycat.config.loader.zkprocess.comm.ZkConfig;
import io.mycat.config.model.SystemConfig;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@EnableAutoConfiguration
@ComponentScan("cn.edu.nwsuaf.*")
public class SampleWebJspApplication extends SpringBootServletInitializer {
	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static Logger logger = Logger.getLogger(SampleWebJspApplication.class);

	//DataSource配置


	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SampleWebJspApplication.class);
	}

	public static void main(String[] args) throws Exception {
		ZkConfig.getInstance().initZk();
		try {
			String home = SystemConfig.getHomePath();
			if (home == null) {
				System.out.println(SystemConfig.SYS_HOME + "  is not set.");
				System.exit(-1);
			}
			// init
			MycatServer server = MycatServer.getInstance();
			server.beforeStart();

			// startup
			server.startup();
			System.out.println("MyCAT Server startup successfully. see logs in logs/mycat.log");
			//LoadDataHostUtil.main();

		} catch (Exception e) {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			logger.error(sdf.format(new Date()) + " startup error", e);
			System.exit(-1);
		}

		SpringApplication.run(SampleWebJspApplication.class, args);

		logger.info(SampleWebJspApplication.class.getSimpleName() + " is started ");
	}
}
