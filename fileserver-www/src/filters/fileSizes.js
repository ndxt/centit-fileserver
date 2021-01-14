export default function fileSizes (num) {
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (num > 1024 && i < 3) {
    i++
    num /= 1024
  }
  return Math.floor(num) + units[i]
}
