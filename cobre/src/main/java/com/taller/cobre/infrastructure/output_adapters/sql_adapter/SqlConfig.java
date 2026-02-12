package com.taller.cobre.infrastructure.output_adapters.sql_adapter;

import com.taller.cobre.infrastructure.output_adapters.sql_adapter.converters.JsonNodeReadingConverter;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.converters.JsonNodeWritingConverter;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;

import java.util.List;

@Configuration
@EnableR2dbcRepositories
@EnableConfigurationProperties(ConnectionProperties.class)
public class SqlConfig extends AbstractR2dbcConfiguration {

    private final ConnectionProperties properties;

    public SqlConfig(ConnectionProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.parse(properties.url())
            .mutate()
            .option(ConnectionFactoryOptions.USER, properties.username())
            .option(ConnectionFactoryOptions.PASSWORD, properties.password())
            .build());
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        return initializer;
    }

    @Override
    protected List<Object> getCustomConverters() {
        return List.of(new JsonNodeWritingConverter(), new JsonNodeReadingConverter());
    }
}
