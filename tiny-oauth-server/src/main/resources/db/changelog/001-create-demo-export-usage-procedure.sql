-- Liquibase SQL changelog for demo_export_usage stored procedure
-- 仅管理存储过程，表结构仍由 schema.sql 管理

CREATE PROCEDURE sp_generate_demo_export_usage(
    IN p_days INT,
    IN p_rows_per_day INT,
    IN p_target_rows INT,
    IN p_clear_existing TINYINT(1)
)
BEGIN
    DECLARE d INT DEFAULT 0;
    DECLARE i INT;
    DECLARE usage_dt DATE;
    DECLARE tenant_code VARCHAR(64);
    DECLARE product_code VARCHAR(64);
    DECLARE product_name VARCHAR(128);
    DECLARE plan_tier VARCHAR(32);
    DECLARE region VARCHAR(64);
    DECLARE unit VARCHAR(32);
    DECLARE unit_price DECIMAL(12,4);
    DECLARE qty DECIMAL(18,4);
    DECLARE status_val VARCHAR(20);
    DECLARE currency_val CHAR(3);
    DECLARE tax_val DECIMAL(5,4);
    DECLARE target_rows INT;
    DECLARE inserted_rows INT DEFAULT 0;

    IF p_clear_existing IS NOT NULL AND p_clear_existing = 1 THEN
        TRUNCATE TABLE demo_export_usage;
    END IF;

    IF p_rows_per_day IS NULL OR p_rows_per_day <= 0 THEN SET p_rows_per_day = 2000; END IF;
    IF p_days IS NULL OR p_days <= 0 THEN SET p_days = 7; END IF;

    IF p_target_rows IS NOT NULL AND p_target_rows > 0 THEN
        SET target_rows = p_target_rows;
    ELSE
        SET target_rows = p_days * p_rows_per_day;
    END IF;

    WHILE inserted_rows < target_rows DO
        SET d = inserted_rows DIV p_rows_per_day;
        SET usage_dt = CURDATE() - INTERVAL d DAY;
        SET i = 0;
        WHILE i < p_rows_per_day AND inserted_rows < target_rows DO
            SET tenant_code = ELT(1 + FLOOR(RAND() * 3), 'tenant-alpha', 'tenant-beta', 'tenant-gamma');
            SET product_code = ELT(1 + FLOOR(RAND() * 5), 'cdn', 'oss', 'api', 'mq', 'db');
            SET product_name = CASE product_code
                WHEN 'cdn' THEN 'CDN 流量'
                WHEN 'oss' THEN '对象存储'
                WHEN 'api' THEN 'API 调用'
                WHEN 'mq'  THEN '消息队列'
                ELSE '托管数据库'
            END;
            SET plan_tier = ELT(1 + FLOOR(RAND() * 3), 'basic', 'standard', 'enterprise');
            SET region = ELT(1 + FLOOR(RAND() * 4), 'cn-north-1', 'ap-southeast-1', 'us-west-1', 'eu-central-1');
            SET unit = CASE product_code
                WHEN 'cdn' THEN 'GB'
                WHEN 'oss' THEN 'GB'
                WHEN 'api' THEN 'req'
                WHEN 'mq'  THEN 'msg'
                ELSE 'hours'
            END;
            SET unit_price = CASE product_code
                WHEN 'cdn' THEN 0.1200
                WHEN 'oss' THEN 0.0800
                WHEN 'api' THEN 0.0008
                WHEN 'mq'  THEN 0.0005
                ELSE 2.8000
            END;
            SET qty = ROUND(
                CASE unit
                    WHEN 'GB' THEN (50 + RAND() * 150)
                    WHEN 'req' THEN (50000 + RAND() * 150000)
                    WHEN 'msg' THEN (10000 + RAND() * 80000)
                    ELSE (10 + RAND() * 80)
                END, 4);
            SET currency_val = ELT(1 + FLOOR(RAND() * 2), 'CNY', 'USD');
            SET tax_val = CASE currency_val WHEN 'USD' THEN 0.0725 ELSE 0.0600 END;
            SET status_val = ELT(1 + FLOOR(RAND() * 3), 'UNBILLED', 'BILLED', 'ADJUSTED');

            INSERT INTO demo_export_usage (
                tenant_code, usage_date, product_code, product_name, plan_tier, region,
                usage_qty, unit, unit_price, amount, currency, tax_rate, is_billable,
                status, metadata, created_at
            ) VALUES (
                tenant_code,
                usage_dt,
                product_code,
                product_name,
                plan_tier,
                region,
                qty,
                unit,
                unit_price,
                ROUND(qty * unit_price, 4),
                currency_val,
                tax_val,
                ELT(1 + FLOOR(RAND() * 2), TRUE, FALSE),
                status_val,
                JSON_OBJECT(
                    'run', CONCAT('day-', d, '-row-', i),
                    'tier', plan_tier,
                    'region', region
                ),
                NOW() - INTERVAL d DAY
            );
            SET i = i + 1;
            SET inserted_rows = inserted_rows + 1;
        END WHILE;
    END WHILE;
END;


