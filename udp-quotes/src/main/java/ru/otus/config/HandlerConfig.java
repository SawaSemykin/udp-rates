package ru.otus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import ru.otus.service.QuotesHandler;

import java.util.concurrent.ExecutorService;

/**
 * @author Aleksandr Semykin
 */
@Configuration
public class HandlerConfig implements AsyncConfigurer {

    @Bean
    public ExecutorService elvlExecutor() {
        ThreadPoolExecutorFactoryBean factoryBean = new ThreadPoolExecutorFactoryBean();
        factoryBean.setCorePoolSize(QuotesHandler.POOL_SIZE);
        factoryBean.setMaxPoolSize(QuotesHandler.POOL_SIZE);
        factoryBean.setQueueCapacity(0);
        factoryBean.setThreadNamePrefix("elvl_thread_");
        factoryBean.initialize();
        return factoryBean.getObject();
    }
}
