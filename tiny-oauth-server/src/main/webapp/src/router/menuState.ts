import { reactive, readonly } from 'vue'

interface MenuRouteState {
  loading: boolean
  loaded: boolean
  error: string | null
  lastLoadedAt?: number
}

const state = reactive<MenuRouteState>({
  loading: false,
  loaded: false,
  error: null,
  lastLoadedAt: undefined,
})

export function useMenuRouteState() {
  return readonly(state)
}

export function updateMenuRouteState(patch: Partial<MenuRouteState>) {
  Object.assign(state, patch)
}

