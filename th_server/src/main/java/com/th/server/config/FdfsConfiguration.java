package com.th.server.config;

/*
 * @author Lise
 * @date 2021年05月30日 21:07
 * @program: th
 */

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:fast_dfs.properties")
@Import(FdfsClientConfig.class) // 导入FastDFS-Client组件
public class FdfsConfiguration {

}
