// src/auth/oidc.ts
import { UserManager, WebStorageStateStore } from 'oidc-client-ts'

export const settings = {
  authority: 'http://localhost:9000', // 授权服务器地址
  client_id: 'vue-client',
  redirect_uri: 'http://localhost:5173/callback',
  post_logout_redirect_uri: 'http://localhost:5173/',
  response_type: 'code',
  scope: 'openid profile',
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  loadUserInfo: true,
  automaticSilentRenew: true,
  silent_redirect_uri: 'http://localhost:5173/silent-renew.html', // 用于静默续期
}

export const userManager = new UserManager(settings)
