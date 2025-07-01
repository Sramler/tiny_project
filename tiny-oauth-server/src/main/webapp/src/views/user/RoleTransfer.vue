<template>
  <a-modal v-model:open="visible" title="分配角色" @ok="handleOk" @cancel="handleCancel">
    <a-transfer
      v-model:target-keys="localRoleIds"
      :data-source="allRoles"
      :titles="['可选角色', '已分配角色']"
      :render="(item: any) => `${item.name}（${item.code}）`"
      :list-style="{ width: '200px', height: '300px' }"
    />
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue'
const props = defineProps({
  modelValue: { type: Array as () => string[], default: () => [] },
  allRoles: { type: Array as () => { key: string, name: string, code: string }[], default: () => [] },
  open: Boolean
})
const emit = defineEmits(['update:modelValue', 'update:open'])

const visible = ref(props.open)
watch(() => props.open, v => visible.value = v)
watch(visible, v => emit('update:open', v))

const localRoleIds = ref<string[]>([...(props.modelValue || [])])
watch(() => props.modelValue, v => localRoleIds.value = [...(v || [])])

function handleOk() {
  emit('update:modelValue', localRoleIds.value)
  visible.value = false
}
function handleCancel() {
  visible.value = false
}
</script>
