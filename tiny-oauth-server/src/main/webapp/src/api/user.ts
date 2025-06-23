// user.ts 用户相关 API 封装
import axios from 'axios'

// userList 方法，接收 params 参数，返回 Promise，模拟后端接口
export function userList(params: {
  username?: string
  nickname?: string
  current?: number
  pageSize?: number
}) {
  // 用 axios 请求本地 user.json 文件
  return axios.get('/user.json').then((res) => {
    let data: any = res.data
    // 兼容字符串和 undefined/null
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data)
      } catch (e) {
        data = []
      }
    }
    if (!Array.isArray(data)) {
      data = []
    }
    // 前端模拟查询条件过滤
    if (params.username) {
      data = data.filter((item) => item.username.includes(params.username!))
    }
    if (params.nickname) {
      data = data.filter((item) => item.nickname && item.nickname.includes(params.nickname!))
    }
    // 分页
    const current = Number(params.current) || 1
    const pageSize = Number(params.pageSize) || 10
    const total = data.length
    const start = Math.max(0, (current - 1) * pageSize)
    const end = start + pageSize
    const records = data.slice(start, end)
    // 返回结构模拟后端接口格式
    return Promise.resolve({
      total,
      records,
    })
  })
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}
