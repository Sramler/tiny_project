package com.tiny.export.demo;

import com.tiny.export.core.DataProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

/**
 * DemoExportUsageDataProvider —— 基于 demo_export_usage 表的示例 DataProvider
 *
 * 仅用于演示导出能力，不参与正式业务。
 */
@Component("demo_export_usage")
public class DemoExportUsageDataProvider implements DataProvider<DemoExportUsageRow> {

    private final JdbcTemplate jdbcTemplate;

    public DemoExportUsageDataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Iterator<DemoExportUsageRow> fetchIterator(int batchSize) {
        // 简化实现：一次性加载少量 demo 行数据。正式场景应改为分页流式查询。
        String sql = """
            SELECT tenant_code,
                   usage_date,
                   product_code,
                   product_name,
                   plan_tier,
                   region,
                   usage_qty,
                   unit,
                   unit_price,
                   amount,
                   currency,
                   tax_rate,
                   is_billable,
                   status
              FROM demo_export_usage
            ORDER BY usage_date DESC, tenant_code, product_code
            LIMIT ?
            """;
        List<DemoExportUsageRow> list = jdbcTemplate.query(sql, new Object[]{batchSize}, new RowMapper<DemoExportUsageRow>() {
            @Override
            public DemoExportUsageRow mapRow(ResultSet rs, int rowNum) throws SQLException {
                DemoExportUsageRow row = new DemoExportUsageRow();
                row.setTenantCode(rs.getString("tenant_code"));
                row.setUsageDate(rs.getObject("usage_date", LocalDate.class));
                row.setProductCode(rs.getString("product_code"));
                row.setProductName(rs.getString("product_name"));
                row.setPlanTier(rs.getString("plan_tier"));
                row.setRegion(rs.getString("region"));
                row.setUsageQty(rs.getBigDecimal("usage_qty"));
                row.setUnit(rs.getString("unit"));
                row.setUnitPrice(rs.getBigDecimal("unit_price"));
                row.setAmount(rs.getBigDecimal("amount"));
                row.setCurrency(rs.getString("currency"));
                row.setTaxRate(rs.getBigDecimal("tax_rate"));
                row.setBillable(rs.getBoolean("is_billable"));
                row.setStatus(rs.getString("status"));
                return row;
            }
        });
        return list.iterator();
    }
}


