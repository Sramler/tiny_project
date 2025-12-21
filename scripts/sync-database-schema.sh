#!/bin/bash
# 数据库 Schema 同步工具
# 用途：导出当前数据库结构，与 schema.sql 对比，生成差异报告

set -e

# 配置
DB_NAME="tiny_web"
DB_USER="root"
DB_PASS="Tianye0903."
SCHEMA_FILE="tiny-oauth-server/src/main/resources/schema.sql"
DATA_FILE="tiny-oauth-server/src/main/resources/data.sql"
OUTPUT_DIR="docs/database-sync"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=== 数据库 Schema 同步工具 ==="
echo "时间: $(date)"
echo "数据库: $DB_NAME"
echo ""

# 创建输出目录
mkdir -p $OUTPUT_DIR

# 1. 导出当前数据库结构
echo "1. 导出当前数据库结构..."
mysqldump -u $DB_USER -p$DB_PASS \
  --no-data \
  --skip-triggers \
  --skip-routines \
  --skip-events \
  --single-transaction \
  --routines=false \
  --compact \
  --skip-add-drop-table \
  $DB_NAME > $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql 2>/dev/null || {
    echo "错误: 无法连接数据库，请检查配置"
    exit 1
}

# 清理导出文件
sed -i '' '/^CREATE DATABASE/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql 2>/dev/null || \
sed -i '/^CREATE DATABASE/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^USE /d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql 2>/dev/null || \
sed -i '/^USE /d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^\/\*!40/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql 2>/dev/null || \
sed -i '/^\/\*!40/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^\/\*!50/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql 2>/dev/null || \
sed -i '/^\/\*!50/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql

echo "   ✓ 导出完成: $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql"
echo ""

# 2. 对比差异
echo "2. 对比 schema.sql 与当前数据库结构..."
if [ -f "$SCHEMA_FILE" ]; then
    diff -u "$SCHEMA_FILE" "$OUTPUT_DIR/current_schema_${TIMESTAMP}.sql" > "$OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt" || true
    
    if [ -s "$OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt" ]; then
        DIFF_LINES=$(wc -l < "$OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt")
        echo "   ⚠️  发现差异 ($DIFF_LINES 行)，请查看: $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt"
        echo ""
        echo "   差异摘要（前20行）:"
        grep "^[+-]" "$OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt" | head -20 | sed 's/^/      /'
    else
        echo "   ✓ 无差异，schema.sql 与当前数据库结构一致"
    fi
else
    echo "   ⚠️  schema.sql 文件不存在: $SCHEMA_FILE"
fi

echo ""

# 3. 列出所有表
echo "3. 检查表列表..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "
SELECT TABLE_NAME as '表名', 
       TABLE_ROWS as '行数',
       ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) as '大小(MB)'
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME' 
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
" > "$OUTPUT_DIR/tables_list_${TIMESTAMP}.txt" 2>/dev/null || {
    echo "   ⚠️  无法查询表列表"
}

if [ -f "$OUTPUT_DIR/tables_list_${TIMESTAMP}.txt" ]; then
    echo "   ✓ 表列表已保存: $OUTPUT_DIR/tables_list_${TIMESTAMP}.txt"
    cat "$OUTPUT_DIR/tables_list_${TIMESTAMP}.txt"
fi

echo ""
echo "=== 完成 ==="
echo ""
echo "输出文件:"
echo "  - 当前数据库结构: $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql"
echo "  - 差异报告: $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt"
echo "  - 表列表: $OUTPUT_DIR/tables_list_${TIMESTAMP}.txt"
echo ""
echo "下一步:"
echo "  1. 查看差异报告: cat $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt"
echo "  2. 根据差异更新 schema.sql"
echo "  3. 创建对应的 Liquibase changelog 记录差异"

