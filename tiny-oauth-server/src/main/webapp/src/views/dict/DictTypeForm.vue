<template>
  <a-modal
    v-model:open="visible"
    :title="dictType ? '编辑字典类型' : '新建字典类型'"
    :width="600"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item
        label="字典编码"
        name="dictCode"
        :rules="[
          { required: true, message: '请输入字典编码' },
          { pattern: /^[A-Z][A-Z0-9_]{2,63}$/, message: '字典编码必须是大写字母开头，3-64字符' },
        ]"
      >
        <a-input
          v-model:value="form.dictCode"
          placeholder="如：GENDER, ORDER_STATUS"
          :disabled="!!dictType"
        />
      </a-form-item>

      <a-form-item
        label="字典名称"
        name="dictName"
        :rules="[{ required: true, message: '请输入字典名称' }]"
      >
        <a-input v-model:value="form.dictName" placeholder="如：性别, 订单状态" />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="form.description"
          placeholder="请输入字典描述"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="分类ID" name="categoryId">
        <a-input-number v-model:value="form.categoryId" placeholder="分类ID（可选）" />
      </a-form-item>

      <a-form-item v-if="dictType" label="启用状态" name="enabled">
        <a-switch v-model:checked="form.enabled" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { DictTypeDTO, DictTypeCreateDTO, DictTypeUpdateDTO } from '@/api/dict';
import { useDict } from '@/composables/useDict';

const props = defineProps<{
  open: boolean;
  dictType?: DictTypeDTO | null;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}>();

const visible = ref(false);
const form = ref<DictTypeCreateDTO | DictTypeUpdateDTO>({
  dictCode: '',
  dictName: '',
  description: '',
  categoryId: undefined,
});

// 假设从某个地方获取租户ID
const tenantId = 1; // TODO: 从用户上下文获取
const { createType, updateType } = useDict(tenantId);

watch(
  () => props.open,
  (val) => {
    visible.value = val;
    if (val) {
      if (props.dictType) {
        // 编辑模式
        form.value = {
          dictName: props.dictType.dictName,
          description: props.dictType.description || '',
          categoryId: props.dictType.categoryId,
          enabled: props.dictType.enabled,
        } as DictTypeUpdateDTO;
      } else {
        // 新建模式
        form.value = {
          dictCode: '',
          dictName: '',
          description: '',
          categoryId: undefined,
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
    if (props.dictType) {
      // 更新
      await updateType(props.dictType.id!, form.value as DictTypeUpdateDTO);
      message.success('更新成功');
    } else {
      // 创建
      await createType(form.value as DictTypeCreateDTO);
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

