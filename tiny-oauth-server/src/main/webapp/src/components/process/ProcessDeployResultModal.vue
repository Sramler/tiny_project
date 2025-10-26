<template>
    <a-modal v-model:open="localOpen" title="流程保存成功" :footer="null" width="600px">
        <a-result status="success" title="流程保存成功！" sub-title="您的流程已成功部署到 Camunda 引擎中">
            <template #extra>
                <a-space>
                    <a-button type="primary" @click="$emit('go-deployment')">查看部署列表</a-button>
                    <a-button @click="$emit('go-definition')">查看流程定义</a-button>
                    <a-button @click="close">关闭</a-button>
                </a-space>
            </template>
        </a-result>

        <a-card title="保存详情" class="save-details-card" style="margin-top: 24px;">
            <a-descriptions :column="2" bordered>
                <a-descriptions-item label="部署名称">
                    {{ result?.deploymentName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="部署ID">
                    <a-typography-text code>{{ result?.deploymentId || '-' }}</a-typography-text>
                </a-descriptions-item>
                <a-descriptions-item label="保存时间">
                    {{ formatDate(new Date()) }}
                </a-descriptions-item>
                <a-descriptions-item label="描述" :span="2">
                    {{ result?.description || '无描述' }}
                </a-descriptions-item>
            </a-descriptions>
        </a-card>

        <a-card v-if="actions && actions.length" title="后续操作" style="margin-top: 16px;">
            <a-list :data-source="actions" size="small">
                <template #renderItem="{ item, index }">
                    <a-list-item>
                        <a-list-item-meta>
                            <template #avatar>
                                <a-avatar :style="{ backgroundColor: item.color || '#1677ff' }">
                                    {{ item.icon || '⚙️' }}
                                </a-avatar>
                            </template>
                            <template #title>
                                <a-typography-text strong>{{ item.title }}</a-typography-text>
                            </template>
                            <template #description>
                                {{ item.description || '' }}
                            </template>
                        </a-list-item-meta>
                        <template #actions>
                            <a-button type="link" size="small" @click="$emit('run-action', index)">执行</a-button>
                        </template>
                    </a-list-item>
                </template>
            </a-list>
        </a-card>
    </a-modal>

</template>

<script setup lang="ts">
import { computed } from 'vue'

interface ResultInfo {
    deploymentId?: string
    deploymentName?: string
    description?: string
}

const props = defineProps<{
    open: boolean
    result?: ResultInfo
    actions?: Array<{ title: string; description?: string; icon?: string; color?: string }>
}>()

const emit = defineEmits<{
    (e: 'update:open', value: boolean): void
    (e: 'go-deployment'): void
    (e: 'go-definition'): void
    (e: 'run-action', index: number): void
}>()

const localOpen = computed({
    get: () => props.open,
    set: (val: boolean) => emit('update:open', val)
})

const close = () => emit('update:open', false)

const formatDate = (date: Date) => date.toLocaleString('zh-CN')
</script>

<style scoped>
.save-details-card {
    margin-top: 24px;
}
</style>
