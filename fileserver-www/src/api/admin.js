import apiFactory from '@centit/api-core'

const api = apiFactory.create('admin', { useFormData: true })
/**
 * 获取当前用户信息
 */
export function getCurrposition () {
  return api.get('system/mainframe/usercurrstation')
    .then(res => res.data)
}
