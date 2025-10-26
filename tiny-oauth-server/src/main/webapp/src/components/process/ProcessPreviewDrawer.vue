<template>
    <a-drawer v-model:open="innerOpen" title="流程预览" :width="width" :destroyOnClose="true" :zIndex="zIndex"
        :styles="{ body: { padding: '16px' } }" @afterOpenChange="onAfterOpenChange">
        <a-card size="small" title="基本信息" style="margin-bottom: 12px;">
            <a-descriptions :column="3" bordered size="small">
                <a-descriptions-item label="流程名称">
                    {{ record?.name || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="流程Key">
                    <a-typography-text code>{{ record?.key || '-' }}</a-typography-text>
                </a-descriptions-item>
                <a-descriptions-item label="版本">
                    <a-tag color="blue" v-if="record?.version">v{{ record?.version }}</a-tag>
                    <span v-else>-</span>
                </a-descriptions-item>
            </a-descriptions>
        </a-card>
        <div class="preview-wrapper">
            <div ref="previewContainer" class="bpmn-preview-canvas"></div>
        </div>

        <template #footer>
            <a-space>
                <a-button @click="onStartClick" :disabled="!record">启动流程</a-button>
                <a-button @click="onClose">关闭</a-button>
            </a-space>
        </template>
    </a-drawer>

</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import type { ProcessDefinition } from '@/api/process'
import { processApi } from '@/api/process'
import { message } from 'ant-design-vue'
import BpmnViewer from 'bpmn-js/lib/Viewer'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'

interface Props {
    open: boolean
    record: ProcessDefinition | null
    zIndex?: number
    width?: string | number
}

const props = withDefaults(defineProps<Props>(), {
    zIndex: 1300,
    width: '70%'
})

const emit = defineEmits<{
    (e: 'update:open', value: boolean): void
    (e: 'close'): void
    (e: 'start', record: ProcessDefinition): void
    (e: 'error', err: Error): void
}>()

const innerOpen = ref<boolean>(props.open)
watch(() => props.open, v => (innerOpen.value = v))
watch(innerOpen, v => emit('update:open', v))

const record = ref<ProcessDefinition | null>(props.record)
watch(() => props.record, v => (record.value = v))

const previewContainer = ref<HTMLDivElement | null>(null)
// 使用 unknown，避免直接 any
const bpmnViewer = ref<unknown | null>(null)

async function renderBpmn() {
    try {
        await nextTick()
        if (!innerOpen.value || !record.value?.id) return
        const res = await processApi.getProcessDefinitionXml(record.value.id)
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const xml = (res as any)?.bpmnXml || ''
        if (!xml) {
            message.error('未获取到流程XML')
            return
        }
        if (!previewContainer.value) return
        try {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ; (bpmnViewer.value as any)?.destroy?.()
        } catch { }
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        bpmnViewer.value = new (BpmnViewer as any)({ container: previewContainer.value })
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const viewer = bpmnViewer.value as any
        await viewer.importXML(xml)
        const canvas = viewer.get('canvas')
        canvas.zoom('fit-viewport')
    } catch (err) {
        const error = err as Error
        console.error('渲染预览失败:', error)
        message.error('渲染预览失败：' + (error.message || '未知错误'))
        emit('error', error)
    }
}

function cleanup() {
    try {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ; (bpmnViewer.value as any)?.destroy?.()
    } catch { }
    bpmnViewer.value = null
}

function onAfterOpenChange(open: boolean) {
    if (open) {
        renderBpmn()
    } else {
        cleanup()
    }
}

function onClose() {
    innerOpen.value = false
    emit('close')
}

function onStartClick() {
    if (record.value) emit('start', record.value)
}
// no additional changes below
</script>

<style scoped>
.preview-wrapper {
    width: 100%;
    height: 100%;
}

.bpmn-preview-canvas {
    width: 100%;
    height: 78vh;
    min-height: 480px;
}
</style>
