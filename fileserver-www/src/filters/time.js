export default function time (value) {
  if (typeof value === 'string') {
    try {
      value = value.replace(/-/g, '/')
    } catch (error) {

    }
  }

  if (!value) {
    return '-'
  }

  const date = new Date(value)
  const Y = date.getFullYear() + '-'
  const M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-'
  const D = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate())
  const H = date.getHours() < 10 ? '0' + date.getHours() : date.getHours()
  const Mi = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()
  const S = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds()
  return Y + M + D + ' ' + H + ':' + Mi + ':' + S
}
