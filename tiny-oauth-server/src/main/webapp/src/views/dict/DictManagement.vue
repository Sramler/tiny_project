<template>
  <div class="dict-management">
    <a-layout>
      <a-layout-sider width="300" style="background: #fff; border-right: 1px solid #f0f0f0">
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0">
          <a-input-search
            v-model:value="typeSearchText"
            placeholder="搜索字典类型"
            @search="handleTypeSearch"
            allow-clear
          />
        </div>
        <div style="padding: 16px">
          <a-button type="primary" block @click="openTypeForm">
            <template #icon>
              <PlusOutlined />
            </template>
            新建字典类型
          </a-button>
        </div>
        <a-list
          :data-source="typeList"
          :loading="typeLoading"
          style="height: calc(100vh - 200px); overflow-y: auto"
        >
          <template #renderItem="{ item }">
            <a-list-item
              :class="{ 'selected': selectedDictType?.id === item.id }"
              @click="selectDictType(item)"
              style="cursor: pointer"
            >
              <a-list-item-meta>
                <template #title>
                  <span>{{ item.dictName }}</span>
                  <a-tag v-if="item.tenantId === 0" color="blue" style="margin-left: 8px">
                    平台
                  </a-tag>
                </template>
                <template #description>
                  <div>{{ item.dictCode }}</div>
                  <div v-if="item.description" style="color: #999; font-size: 12px">
                    {{ item.description }}
                  </div>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click.stop="editType(item)">
                  编辑
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  danger
                  @click.stop="handleDeleteType(item)"
                  v-if="item.tenantId !== 0"
                >
                  删除
                </a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-layout-sider>

      <a-layout-content style="padding: 24px">
        <div v-if="selectedDictType">
          <div style="margin-bottom: 16px">
            <h2>{{ selectedDictType.dictName }}（{{ selectedDictType.dictCode }}）</h2>
            <a-space>
              <a-button type="primary" @click="openItemForm">
                <template #icon>
                  <PlusOutlined />
                </template>
                新建字典项
              </a-button>
              <a-button @click="refreshCache">
                <template #icon>
                  <ReloadOutlined />
                </template>
                刷新缓存
              </a-button>
            </a-space>
          </div>

          <a-table
            :columns="itemColumns"
            :data-source="itemList"
            :loading="itemLoading"
            :pagination="{
              current: itemPagination.current,
              pageSize: itemPagination.pageSize,
              total: itemPagination.total,
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条`,
            }"
            @change="handleItemTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'enabled'">
                <a-switch
                  :checked="record.enabled"
                  @change="(checked) => toggleItemEnabled(record, checked)"
                />
              </template>
              <template v-if="column.key === 'tenantId'">
                <a-tag v-if="record.tenantId === 0" color="blue">平台</a-tag>
                <a-tag v-else color="green">租户</a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="editItem(record)">
                    编辑
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    danger
                    @click="handleDeleteItem(record)"
                    v-if="record.tenantId !== 0"
                  >
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
        <a-empty v-else description="请选择字典类型" />
      </a-layout-content>
    </a-layout>

    <!-- 字典类型表单 -->
    <DictTypeForm
      v-model:open="typeFormVisible"
      :dict-type="currentType"
      @success="handleTypeFormSuccess"
    />

    <!-- 字典项表单 -->
    <DictItemForm
      v-model:open="itemFormVisible"
      :dict-item="currentItem"
      :dict-code="selectedDictType?.dictCode"
      @success="handleItemFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { useDict } from '@/composables/useDict';
import type { DictTypeDTO, DictItemDTO, DictItemUpdateDTO } from '@/api/dict';
import DictTypeForm from './DictTypeForm.vue';
import DictItemForm from './DictItemForm.vue';

// 假设从某个地方获取租户ID（实际应该从用户上下文获取）
const tenantId = ref(1); // TODO: 从用户上下文获取

const {
  typeList,
  typeLoading,
  typePagination,
  itemList,
  itemLoading,
  itemPagination,
  selectedDictType,
  loadDictTypes,
  createType,
  updateType,
  removeType,
  loadDictItems,
  updateItem,
  removeItem,
  refreshCache,
  selectDictType,
} = useDict(tenantId.value);

const typeSearchText = ref('');
const typeFormVisible = ref(false);
const itemFormVisible = ref(false);
const currentType = ref<DictTypeDTO | null>(null);
const currentItem = ref<DictItemDTO | null>(null);

const itemColumns = [
  {
    title: '字典值',
    dataIndex: 'value',
    key: 'value',
    width: 150,
  },
  {
    title: '字典标签',
    dataIndex: 'label',
    key: 'label',
    width: 200,
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80,
  },
  {
    title: '启用',
    key: 'enabled',
    width: 100,
  },
  {
    title: '类型',
    key: 'tenantId',
    width: 100,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
  },
];

onMounted(() => {
  loadDictTypes();
});

const handleTypeSearch = () => {
  loadDictTypes({
    dictCode: typeSearchText.value,
    dictName: typeSearchText.value,
  });
};

const openTypeForm = () => {
  currentType.value = null;
  typeFormVisible.value = true;
};

const editType = (type: DictTypeDTO) => {
  currentType.value = { ...type };
  typeFormVisible.value = true;
};

const handleDeleteType = (type: DictTypeDTO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除字典类型"${type.dictName}"吗？删除后该字典类型下的所有字典项也将被删除。`,
    onOk: async () => {
      try {
        await removeType(type.id!);
        message.success('删除成功');
      } catch (error) {
        message.error('删除失败');
      }
    },
  });
};

const handleTypeFormSuccess = () => {
  typeFormVisible.value = false;
  loadDictTypes();
};

const openItemForm = () => {
  currentItem.value = null;
  itemFormVisible.value = true;
};

const editItem = (item: DictItemDTO) => {
  currentItem.value = { ...item };
  itemFormVisible.value = true;
};

const handleDeleteItem = (item: DictItemDTO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除字典项"${item.label}"吗？`,
    onOk: async () => {
      try {
        await removeItem(item.id!);
        message.success('删除成功');
      } catch (error) {
        message.error('删除失败');
      }
    },
  });
};

const toggleItemEnabled = async (item: DictItemDTO, enabled: boolean) => {
  try {
    const dto: DictItemUpdateDTO = {
      label: item.label,
      enabled,
    };
    await updateItem(item.id!, dto);
    message.success('更新成功');
  } catch (error) {
    message.error('更新失败');
  }
};

const handleItemFormSuccess = () => {
  itemFormVisible.value = false;
  if (selectedDictType.value) {
    loadDictItems(selectedDictType.value.dictCode);
  }
};

const handleItemTableChange = (pag: any) => {
  itemPagination.value.current = pag.current;
  itemPagination.value.pageSize = pag.pageSize;
  if (selectedDictType.value) {
    loadDictItems(selectedDictType.value.dictCode);
  }
};
</script>

<style scoped>
.dict-management {
  height: calc(100vh - 64px);
}

.selected {
  background-color: #e6f7ff;
}
</style>

