import SparkMD5 from 'spark-md5'

class BMF {
  md5 (file, md5Fn, progressFn) {
    this.aborted = false
    this.progress = 0
    let currentChunk = 0
    const blobSlice =
      File.prototype.slice ||
      File.prototype.mozSlice ||
      File.prototype.webkitSlice
    const chunkSize = 2097152
    const chunks = Math.ceil(file.size / chunkSize)
    const spark = new SparkMD5.ArrayBuffer()
    const reader = new FileReader()

    loadNext()

    reader.onloadend = e => {
      spark.append(e.target.result) // Append array buffer
      currentChunk++
      this.progress = currentChunk / chunks

      if (progressFn && typeof progressFn === 'function') {
        progressFn(this.progress)
      }

      if (this.aborted) {
        md5Fn('aborted')
        return
      }

      if (currentChunk < chunks) {
        loadNext()
      } else {
        md5Fn(null, spark.end())
      }
    }

    /// //////////////////////
    function loadNext () {
      const start = currentChunk * chunkSize
      const end = start + chunkSize >= file.size ? file.size : start + chunkSize
      reader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }
  }

  abort () {
    this.aborted = true
  }
}

export default BMF
