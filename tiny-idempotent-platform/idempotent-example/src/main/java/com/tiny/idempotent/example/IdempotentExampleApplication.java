package com.tiny.idempotent.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 幂等平台示例应用
 * 
 * <p>展示如何使用 tiny-idempotent-platform 实现幂等性</p>
 * <p>包含 HTTP 接口、MQ 消费、Job 等多种场景的示例</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@SpringBootApplication
public class IdempotentExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdempotentExampleApplication.class, args);
    }
}

