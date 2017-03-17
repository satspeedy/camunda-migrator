package com.satspeedy.bpm.camuda.migrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.SpringProcessEngineServicesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Camunda Process Engine Configuration.
 */
@Configuration
@ComponentScan("com.satspeedy.bpm.camuda.migrator")
@Import(SpringProcessEngineServicesConfiguration.class)
public class CamundaProcessEngineConfiguration {

    @Value("${camunda.bpm.history-level:full}")
    private String historyLevel;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourcePatternResolver resourceLoader;

    /**
     * Process engine configuration.
     *
     * @return SpringProcessEngineConfiguration
     * @throws IOException IOException
     */
    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();

        config.setDataSource(dataSource);
        config.setDatabaseSchemaUpdate("NOOP"); //set to an non existing value to skip database checking and updating completely
        // see https://app.camunda.com/jira/browse/CAM-2097
        config.setDatabaseType("postgres"); // h2, mysql, oracle, postgres, mssql, db2 or mariadb
        config.setTransactionManager(transactionManager());
        config.setHistory(historyLevel);
        config.setJobExecutorDeploymentAware(true);
        config.setJobExecutorActivate(false);
        config.setMetricsEnabled(false);
        config.setIdGenerator(idGenerator());

        return config;
    }

    /**
     * Id generator.
     *
     * @return IdGenerator
     */
    @Bean
    public IdGenerator idGenerator() {
        return new StrongUuidGenerator();
    }

    /**
     * Transaction manager.
     *
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Camunda process engine.
     *
     * @return ProcessEngineFactoryBean
     * @throws IOException IOException
     */
    @Bean
    public ProcessEngineFactoryBean processEngine() throws IOException {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }

    /**
     * Camunda repository service.
     *
     * @param processEngine processEngine
     * @return RepositoryService
     */
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    /**
     * Camunda runtime service.
     *
     * @param processEngine processEngine
     * @return RuntimeService
     */
    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    /**
     * Validator.
     *
     * @return validator
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Object mapper.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
