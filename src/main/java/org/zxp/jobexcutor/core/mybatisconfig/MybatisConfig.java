package org.zxp.jobexcutor.core.mybatisconfig;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;


@Configuration
@EnableTransactionManagement
@ComponentScan
@MapperScan("org.zxp.jobexcutor.mapper")
public class MybatisConfig {

        @Value("${spring.datasource.type}")
        private Class<? extends DataSource> dataSourceType;

        @Bean(name = "dataSource", destroyMethod = "close", initMethod = "init")
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource dataSource() {
            return DataSourceBuilder.create().type(dataSourceType).build();
        }

        @Bean
        public SqlSessionFactory sqlSessionFactory() throws Exception {
            SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(dataSource());
            sqlSessionFactoryBean.setTypeAliasesPackage("org.zxp.jobexcutor.entity");
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:org/zxp/jobexcutor/entity/xml/*.xml"));
            sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
            return sqlSessionFactoryBean.getObject();
        }

        /**
         * 配置事务管理器
         */
        @Bean(name = "transactionManager")
        @Primary
        public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) throws Exception {
            return new DataSourceTransactionManager(dataSource);
        }
    }
