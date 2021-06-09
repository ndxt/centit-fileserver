import apiFactory from '@centit/api-core'

const api = apiFactory.create('admin', { useFormData: true })
export function getCurrposition () {
  return api.get('system/mainframe/usercurrstation')
    .then(res => res.data)
}
