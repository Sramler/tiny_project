package com.tiny.export.config;

import com.tiny.export.writer.WriterAdapter;
import com.tiny.export.writer.poi.POIWriterAdapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ExportConfig —— Spring Bean 配置
 *
 * 在此注册：
 *  - writerAdapter（POI 或 Fesod 任选其一）
 *  - providers 映射（exportType -> DataProvider）
 *  - aggregateMap 映射（aggregateKey -> AggregateStrategy）
 *  - topInfoDecorator
 *  - exportExecutor
 *
 * 注意：生产环境请把 providers 与 aggregateMap 注入为具体业务实现
 */
@Configuration
public class ExportConfig {

    @Bean
    public WriterAdapter writerAdapter() {
        // 使用 POI 作为默认实现（流式SXSSF）
        return new POIWriterAdapter(200);
    }

    @Bean
    public ThreadPoolTaskExecutor exportExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(4);
        t.setMaxPoolSize(8);
        t.setQueueCapacity(100);
        t.setThreadNamePrefix("export-exec-");
        t.initialize();
        return t;
    }
}