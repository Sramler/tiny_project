/**
 * BPMN 上下文菜单翻译
 * 对应 bpmn-js-i18n-zh/lib/bpmn-js/index.js 中的 context-pad 部分
 */

export interface TranslationMap {
  [key: string]: string
}

// 上下文菜单翻译
export const contextMenuTranslations: TranslationMap = {
  // 对齐相关
  'Align elements': '对齐元素',
  'Align elements left': '靠左对齐',
  'Align elements right': '靠右对齐',
  'Align elements top': '靠上对齐',
  'Align elements bottom': '靠下对齐',
  'Align elements center': '垂直居中对齐',
  'Align elements middle': '水平居中对齐',

  // 分布相关
  'Distribute elements horizontally': '水平分布',
  'Distribute elements vertically': '垂直分布',

  // 添加元素
  'Append end event': '添加结束事件',
  'Append gateway': '添加网关',
  'Append task': '添加任务',
  'Append intermediate/boundary event': '添加中间/边界事件',
  'Add text annotation': '添加文本注释',
  'Connect to other element': '连接到其他元素',
  'Append receive task': '添加接收任务',
  'Append compensation activity': '追加补偿活动',
  'Append conditional intermediate catch event': '添加中间条件捕获事件',
  'Append message intermediate catch event': '添加消息中间捕获事件',
  'Append signal intermediate catch event': '添加信号中间捕获事件',
  'Append timer intermediate catch event': '添加定时器中间捕获事件',
  'Append text annotation': '添加文本注解',

  // 泳道相关
  'Add lane above': '添加到通道之上',
  'Add lane below': '添加到通道之下',
  'Divide into three lanes': '分成三条通道',
  'Divide into two lanes': '分成两条通道',

  // 连接相关
  'Connect using association': '文本关联',
  'Connect using data input association': '数据关联',
  'Connect using sequence/message flow or association': '消息关联',

  // 操作相关
  Remove: '移除元素',
  Delete: '移除元素',
  'Change type': '更改类型',
  'Change element': '更改元素',

  // 工具相关
  'Activate create/remove space tool': '启动创建/删除空间工具',
  'Activate global connect tool': '启动全局连接工具',
  'Activate hand tool': '启动手动工具',
  'Activate lasso tool': '启动 Lasso 工具',

  // 创建元素
  'Create data object reference': '创建数据对象引用',
  'Create data store reference': '创建数据存储引用',
  'Create start event': '创建开始事件',
  'Create intermediate/boundary event': '创建中间/边界事件',
  'Create end event': '创建结束事件',
  'Create expanded sub-process': '创建可折叠子流程',
  'Create gateway': '创建网关',
  'Create task': '创建任务',
  'Create group': '创建组',
  'Create pool/participant': '创建池/参与者',

  // 替换相关
  'Parallel multi-instance': '并行多实例',
  'Sequential multi-instance': '串行多实例',
  'Participant multiplicity': '参与者多重性',
  Loop: '循环',
  Collection: '集合',
  'Ad-hoc': 'Ad-hoc子流程',
  'Toggle non-interrupting': '切换非中断',
  'Toggle interrupting': '切换中断',

  // 其他
  'Create Element': '创建元素',
  'Append Element': '添加元素',
  Templates: '模板',

  // 搜索相关
  'Search in diagram': '在图中搜索',
}

export default contextMenuTranslations
