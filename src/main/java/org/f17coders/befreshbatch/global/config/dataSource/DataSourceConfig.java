package org.f17coders.befreshbatch.global.config.dataSource;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3305/befresh?serverTimezone=UTC&characterEncoding=UTF-8&rewriteBatchedStatements=true")
            .username("root")
            .password("befresh")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build();
    }
}
