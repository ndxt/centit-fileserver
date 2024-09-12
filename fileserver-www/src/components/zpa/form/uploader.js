import BMF from '@/bmf/dist/index.cjs.js'

import {flashUpload, getFileRange, uploadFile, uploadFileRange,} from '@/api/file'

let index = 1

export default class Uploader {
  constructor (file, params = {}, completeCb, cbParams) {
    this.file = file

    this.size = file.size

    this.name = file.name

    this.ext = file.name.split('.').pop()

    if (['bmp', 'png', 'jpeg', 'gif', 'jpg'].includes(this.ext)) {
      this.isImage = true
      this.url = ''
      this.getImageUrl()
    }

    this.showProgress = true

    this.percentage = 0

    this.uuid = Date.now() + (index++)

    this.cbParams = cbParams
    // 100KB
    this.chunkSize = (1000 * 1024)
    if (completeCb) {
      this.completeCb = completeCb
    } else {
      this.completeCb = function () {

      }
    }

    this.params = params
  }

  // 分割文件
  slice (begin, end) {
    const file = this.file
    let method

    if ('mozSlice' in this.file) {
      method = 'mozSlice'
    } else if ('webkitSlice' in this.file) {
      method = 'webkitSlice'
    } else {
      method = 'slice'
    }

    return file[method](begin, end)
  }

  getImageUrl () {
    const fr = new FileReader()
    fr.onload = e => {
      this.url = e.target.result
    }
    fr.readAsDataURL(this.file)
  }

  // 传统上传文件
  uploadFile () {
    const file = this.file

    uploadFile(file, {
      client: 'html5',
      osId: 'FILE_SVR',
      optId: 'LOCAL_FILE',
      // optMethod: 'test',
      // optTag: 'test',
      fileOwner: 'u0000000',
    }, {
      onUploadProgress: e => this.onUploadProgress(e),
    }).then((res) => this.onUploadComplete(res))
  }

  // 分段上传文件
  uploadChunk () {
    const chunk = this.slice(this.rangeStart, this.rangeEnd + 1)

    const range = {
      begin: this.rangeStart,
      end: this.rangeEnd,
      data: chunk,
    }

    const file = this.file

    uploadFileRange(range, file, {
      client: 'html5',
      osId: 'FILE_SVR',
      optId: 'LOCAL_FILE',
      optTag: 'test',
      optMethod: 'test',
      fileOwner: 'u0000000',
      fileUnit: '',
      ...this.params,
    }, {
      onUploadProgress: e => this.onUploadProgress(e),
    }).then((res) => this.onChunkComplete(res.data))
  }

  nextChunk (begin) {
    if (this.rangeStart === undefined) {
      this.rangeStart = begin || 0
    } else {
      this.rangeStart = begin || (this.rangeEnd + 1)
    }

    this.rangeEnd = this.rangeStart + this.chunkSize - 1

    if (this.rangeEnd > this.size) {
      this.rangeEnd = this.size - 1
    }
  }

  computePercentage (loaded) {
    const size = this.size
    const begin = this.rangeStart

    // 计算与上一次时间差
    const now = Date.now()
    const diffTime = now - this.lastTime
    const fromBegin = now - this.beginTime
    this.lastTime = now

    if (begin !== undefined) {
      loaded += begin
    }

    // 计算与上一次加载差
    const diffLoaded = loaded - this.lastLoaded
    this.lastLoaded = loaded

    // 速度
    this.speed = diffLoaded * 1000 / diffTime

    // 平均速度
    this.averageSpeed = loaded * 1000 / fromBegin

    this.percentage = (100 * loaded / size).toFixed(0)
  }

  onUploadProgress (e) {
    this.computePercentage(e.loaded)
  }

  onChunkComplete (res) {
    // If the end range is already the same size as our file, we
    // can assume that our last chunk has been processed and exit
    // out of the function.
    if (this.rangeEnd >= this.size - 1) {
      this.onUploadComplete(res)
      return
    }

    this.nextChunk()

    if (!this.isPaused) {
      this.uploadChunk()
    }
  }

  onUploadComplete (res) {
    this.status = 'success'
    this.percentage = 100
    this.completeCb(res, this.cbParams)
  }

  async start () {
    const file = this.file
    const fileSize = file.size

    // 解析
    this.status = 'parse'

    const token = await Uploader.md5(file)
    file.token = token

    // 开始上传，保存一些状态
    this.status = 'uploading'
    this.beginTime = Date.now()
    this.lastTime = this.beginTime
    this.lastLoaded = 0

    // this.uploadFile()

    const res = await getFileRange(token, fileSize, this.params)

    let size = res.fileSize
    if (isNaN(size)) {
      if (typeof res === 'number') {
        size = res
      } else {
        size = 0
      }
    }
    // 秒传
    const params = Object.assign({
      name: file.name,
    }, this.params)
    if (size >= fileSize) {
      flashUpload(token, size, params)
        .then(data => this.onUploadComplete(data))
      return
    }

    this.nextChunk(res.fileSize)
    this.uploadChunk()
  }

  stop () {
    this.isPaused = true
  }

  static async md5 (file, progressFn = () => {}) {
    const bmf = new BMF()
    return new Promise((resolve, reject) => {
      bmf.md5(file, (error, token) => {
        if (error) return reject(error)
        resolve(token)
      }, progressFn)
    })
  }
}
