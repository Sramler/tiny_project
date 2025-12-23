<template>
  <a-modal
    v-model:open="visible"
    :title="dictItem ? '编辑字典项' : '新建字典项'"
    :width="600"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item
        v-if="!dictItem"
        label="字典编码"
        name="dictCode"
        :rules="[{ required: true, message: '请输入字典编码' }]"
      >
        <a-input v-model:value="form.dictCode" :value="dictCode" disabled />
      </a-form-item>

      <a-form-item
        v-if="!dictItem"
        label="字典值"
        name="value"
        :rules="[
          { required: true, message: '请输入字典值' },
          { pattern: /^[A-Z0-9_]{1,64}$/, message: '字典值必须符合规范：大写字母、数字、下划线，1-64字符' },
        ]"
      >
        <a-input v-model:value="form.value" placeholder="如：MALE, FEMALE, PENDING" />
      </a-form-item>

      <a-form-item
        v-else
        label="字典值"
      >
        <a-input :value="dictItem.value" disabled />
      </a-form-item>

      <a-form-item
        label="字典标签"
        name="label"
        :rules="[
          { required: true, message: '请输入字典标签' },
          { max: 128, message: '字典标签长度不能超过128字符' },
        ]"
      >
        <a-input v-model:value="form.label" placeholder="如：男, 女, 待支付" />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="请输入字典项描述"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="排序" name="sortOrder">
        <a-input-number v-model:value="form.sortOrder" :min="0" placeholder="排序顺序" />
      </a-form-item>

      <a-form-item label="启用状态" name="enabled">
        <a-switch v-model:checked="form.enabled" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { DictItemDTO, DictItemCreateDTO, DictItemUpdateDTO } from '@/api/dict';
import { useDict } from '@/composables/useDict';

const props = defineProps<{
  open: boolean;
  dictItem?: DictItemDTO | null;
  dictCode?: string;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}>();

const visible = ref(false);
const form = ref<DictItemCreateDTO | DictItemUpdateDTO>({
  dictCode: '',
  value: '',
  label: '',
  description: '',
  sortOrder: 0,
  enabled: true,
});

// 假设从某个地方获取租户ID
const tenantId = 1; // TODO: 从用户上下文获取
const { createItem, updateItem } = useDict(tenantId);

watch(
  () => props.open,
  (val) => {
    visible.value = val;
    if (val) {
      if (props.dictItem) {
        // 编辑模式
        form.value = {
          label: props.dictItem.label,
          description: props.dictItem.description || '',
          sortOrder: props.dictItem.sortOrder || 0,
          enabled: props.dictItem.enabled,
        } as DictItemUpdateDTO;
      } else {
        // 新建模式
        form.value = {
          dictCode: props.dictCode || '',
          value: '',
          label: '',
          description: '',
          sortOrder: 0,
          enabled: true,
        };
      }
    }
  }
);

watch(visible, (val) => {
  emit('update:open', val);
});

const handleSubmit = async () => {
  try {
    if (props.dictItem) {
      // 更新
      await updateItem(props.dictItem.id!, form.value as DictItemUpdateDTO);
      message.success('更新成功');
    } else {
      // 创建
      await createItem(form.value as DictItemCreateDTO);
      message.success('创建成功');
    }
    emit('success');
    visible.value = false;
  } catch (error: any) {
    message.error(error.message || '操作失败');
  }
};

const handleCancel = () => {
  visible.value = false;
};
</script>

