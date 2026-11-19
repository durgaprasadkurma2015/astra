
package com.astra.backend.databaseactivity;

import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSelectMonitorConfig {

    @Bean
    public HibernatePropertiesCustomizer databaseSelectInspectorCustomizer(
            DatabaseSelectStatementInspector inspector) {

        return properties -> properties.put(
                "hibernate.session_factory.statement_inspector",
                inspector
        );
    }
}
