import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import pluginOxlint from 'eslint-plugin-oxlint'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

/**
 * ESLint 基础配置说明：
 * - 使用 `defineConfigWithVueTs` 让 Vue + TypeScript 项目零配置即用。
 * - `pluginVue` 提供 Vue 官方基础规则（flat/essential）。
 * - `vueTsConfigs.recommended` 覆盖 TypeScript 最佳实践。
 * - `pluginOxlint` 作为额外的静态检查补充，聚焦潜在 Bug。
 * - `skipFormatting` 禁止 ESLint 接管代码风格，让 Prettier 负责格式化。
 * - `globalIgnores` 跳过构建产物目录，避免 lint 无意义文件。
 *
 * 若需要在 `.vue` 中支持 TS 以外的 script 语言，可启用下方注释：
 *   import { configureVueProject } from '@vue/eslint-config-typescript'
 *   configureVueProject({ scriptLangs: ['ts', 'tsx'] })
 */

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  ...pluginOxlint.configs['flat/recommended'],
  skipFormatting,
  {
    rules: {
      // 项目中大量历史代码使用 `any`/未使用变量/单词组件名，为避免阻塞现有流程先行关闭；
      // 后续若要收紧规则，可逐步在各业务模块消除这些 pattern 再打开。
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      'vue/multi-word-component-names': 'off',
    },
  },
)
