<template>
  <!-- 弹窗包裹 transfer，和 RoleTransfer.vue 结构一致 -->
  <a-modal v-model:open="visible" :title="title" @ok="handleOk" @cancel="handleCancel">
    <a-transfer
      v-model:target-keys="localUserIds"
      :data-source="allUsers"
      :titles="transferTitles"
      :render="(item: any) => item.title"
      :row-key="(item: any) => item.key"
      show-search
      :filter-option="filterUser"
      :list-style="{ width: '220px', height: '320px' }"
    />
  </a-modal>
</template>
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
// 定义props
const props = defineProps({
  modelValue: { type: Array as () => string[], default: () => [] },
  allUsers: { type: Array as () => { key: string, title: string }[], default: () => [] },
  open: Boolean,
  title: { type: String, default: '配置用户' }
})
// 定义emit
const emit = defineEmits(['update:modelValue', 'update:open'])
// 控制弹窗显示
const visible = ref(props.open)
watch(() => props.open, v => visible.value = v)
watch(visible, v => emit('update:open', v))
// 本地用户ID数组
const localUserIds = ref<string[]>([...(props.modelValue || [])])
watch(() => props.modelValue, v => localUserIds.value = [...(v || [])])
// 动态标题：xxx个用户未配置角色/已配置角色
const transferTitles = computed(() => [
  `用户未配置角色`,
  `用户已配置角色`
])
// 未分配用户数量
const allUsersUnselected = computed(() => props.allUsers.length - localUserIds.value.length)
// 搜索过滤
function filterUser(input: string, item: { title: string }) {
  return item.title.toLowerCase().includes(input.toLowerCase())
}
// 确认分配
function handleOk() {
  emit('update:modelValue', localUserIds.value)
  visible.value = false
}
// 取消分配
function handleCancel() {
  visible.value = false
}
</script>
<style scoped>
</style>
