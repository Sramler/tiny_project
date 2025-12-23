package com.tiny.export.demo;

import com.tiny.export.core.FilterAwareDataProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DemoExportUsageDataProvider —— 基于 demo_export_usage 表的示例 DataProvider
 *
 * 仅用于演示导出能力，不参与正式业务。
 *
 * <p>注意：通过 ThreadLocal 存储当前线程的过滤条件，配合 FilterAwareDataProvider 接口使用。</p>
 */
@Component("demo_export_usage")
public class DemoExportUsageDataProvider implements FilterAwareDataProvider<DemoExportUsageRow> {

    private final JdbcTemplate jdbcTemplate;

    // 使用 ThreadLocal 存储当前线程的过滤条件
    private static final ThreadLocal<Map<String, Object>> FILTERS_HOLDER = new ThreadLocal<>();

    public DemoExportUsageDataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setFilters(Map<String, Object> filters) {
        FILTERS_HOLDER.set(filters);
    }

    @Override
    public void clearFilters() {
        FILTERS_HOLDER.remove();
    }

    @Override
    public Iterator<DemoExportUsageRow> fetchIterator(int batchSize) {
        Map<String, Object> filters = FILTERS_HOLDER.get();

        // 构建 WHERE 子句和参数
        List<Object> params = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();

        if (filters != null) {
            if (filters.containsKey("tenantCode") && filters.get("tenantCode") != null) {
                String tenantCode = filters.get("tenantCode").toString().trim();
                if (!tenantCode.isEmpty()) {
                    whereClause.append(" AND tenant_code = ?");
                    params.add(tenantCode);
                }
            }
            if (filters.containsKey("productCode") && filters.get("productCode") != null) {
                String productCode = filters.get("productCode").toString().trim();
                if (!productCode.isEmpty()) {
                    whereClause.append(" AND product_code = ?");
                    params.add(productCode);
                }
            }
            if (filters.containsKey("status") && filters.get("status") != null) {
                String status = filters.get("status").toString().trim();
                if (!status.isEmpty()) {
                    whereClause.append(" AND status = ?");
                    params.add(status);
                }
            }
        }

        // 构建基础 SQL
        // 构建基础 SQL
        String baseSql = """
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
            """;

        String whereSql = whereClause.length() > 0
            ? " WHERE " + whereClause.toString().substring(5) // 移除开头的 " AND "
            : "";

        String orderBySql = " ORDER BY usage_date DESC, tenant_code, product_code";

        // 如果显式指定了“只导出当前页”，按当前页分页一次性返回
        if (filters != null && "page".equals(filters.get("__mode"))) {
            Object pageObj = filters.get("__page");
            Object pageSizeObj = filters.get("__pageSize");
            int page = 1;
            int pageSizeForPage = batchSize;
            if (pageObj instanceof Number) {
                page = ((Number) pageObj).intValue();
            } else if (pageObj instanceof String) {
                try {
                    page = Integer.parseInt((String) pageObj);
                } catch (NumberFormatException ignored) {
                }
            }
            if (pageSizeObj instanceof Number) {
                pageSizeForPage = ((Number) pageSizeObj).intValue();
            } else if (pageSizeObj instanceof String) {
                try {
                    pageSizeForPage = Integer.parseInt((String) pageSizeObj);
                } catch (NumberFormatException ignored) {
                }
            }
            if (page <= 0) {
                page = 1;
            }
            if (pageSizeForPage <= 0) {
                pageSizeForPage = batchSize;
            }
            int offset = (page - 1) * pageSizeForPage;

            String sql = baseSql + whereSql + orderBySql + " LIMIT ? OFFSET ?";
            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(pageSizeForPage);
            pageParams.add(offset);

            List<DemoExportUsageRow> list = jdbcTemplate.query(sql, pageParams.toArray(), (rs, rowNum) -> {
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
            });

            return list.iterator();
        }

        // 默认：导出全部匹配记录 —— 先查询总数（用于分页流式写出）
        String countSql = "SELECT COUNT(*) FROM demo_export_usage" + whereSql;
        Long totalCount = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);
        if (totalCount == null || totalCount == 0) {
            return new ArrayList<DemoExportUsageRow>().iterator();
        }

        // 创建分页迭代器
        return new PaginatedIterator(batchSize, totalCount.intValue(), baseSql + whereSql + orderBySql, params);
    }
    
    /**
     * 分页迭代器，实现真正的分页循环查询
     */
    private class PaginatedIterator implements Iterator<DemoExportUsageRow> {
        private final int batchSize;
        private final int totalCount;
        private final String baseSql;
        private final List<Object> baseParams;
        private int currentOffset = 0;
        private List<DemoExportUsageRow> currentBatch = null;
        private int currentBatchIndex = 0;
        private boolean hasMore = true;

        public PaginatedIterator(int batchSize, int totalCount, String baseSql, List<Object> baseParams) {
            this.batchSize = batchSize;
            this.totalCount = totalCount;
            this.baseSql = baseSql;
            this.baseParams = new ArrayList<>(baseParams);
            loadNextBatch();
        }

        private void loadNextBatch() {
            if (currentOffset >= totalCount) {
                hasMore = false;
                currentBatch = null;
                return;
            }
            
            String sql = baseSql + " LIMIT ? OFFSET ?";
            List<Object> params = new ArrayList<>(baseParams);
            params.add(batchSize);
            params.add(currentOffset);
            
            currentBatch = jdbcTemplate.query(sql, params.toArray(), new RowMapper<DemoExportUsageRow>() {
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
            
            currentBatchIndex = 0;
            currentOffset += batchSize;
            
            if (currentBatch.isEmpty() || currentOffset >= totalCount) {
                hasMore = false;
            }
        }

        @Override
        public boolean hasNext() {
            if (currentBatch == null || currentBatchIndex >= currentBatch.size()) {
                if (hasMore) {
                    loadNextBatch();
                } else {
                    return false;
                }
            }
            return currentBatch != null && currentBatchIndex < currentBatch.size();
        }

        @Override
        public DemoExportUsageRow next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            DemoExportUsageRow row = currentBatch.get(currentBatchIndex);
            currentBatchIndex++;
            return row;
        }
    }
}


