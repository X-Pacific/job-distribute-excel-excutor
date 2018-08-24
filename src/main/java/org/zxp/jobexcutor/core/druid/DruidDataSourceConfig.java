package org.zxp.jobexcutor.core.druid;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * Druid的DataResource配置类
 */
@Configuration
@EnableTransactionManagement
public class DruidDataSourceConfig implements EnvironmentAware {
    private RelaxedPropertyResolver propertyResolver;


    public void setEnvironment(Environment env) {
        this.propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
    }
}
