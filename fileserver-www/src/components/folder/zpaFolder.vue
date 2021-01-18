<template>
  <div>
    <div class="uploadDiv" >
      <!--<Button>点击上传文件夹</Button>-->
      <!-- //页面的遮罩层 -->
      <div id="cover" :style="{display:(showGray ? 'block' : 'none')}"></div>
      <div>
        <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
        <p>{{showGray ? '上传中...' : '点击上传文件夹'}}</p>
      </div>
      <input class="fileUploaderClass" accept="*/*" type='file' name="file" webkitdirectory style="position: absolute;cursor: pointer;opacity: 0;left: 0;top: 0;width: 100%;height: 100%;" @change.stop="changesData"/>
    </div>
    <ul class="zpa-column no-fit" style="width: 100%;" v-if="showUpload">
      <li
        style="margin-top: 8px; width: 100%;"
        class="zpa-row no-fit"
        v-for="(up, index) in uploads"
        :key="index"
      >
        <Icon type="ios-folder-open" :size="28" color="rgb(236, 182, 30)"/>
        <zpa-column no-gutter padding="0 8px" style="overflow: hidden">
          <zpa-row middle>
            <Tooltip
              style="margin-right: 20px;"
              class="name zpa-row ellipsis"
              :content="up[0].webkitRelativePath.split('/')[0]"
              :transfer="true"
            >
              <p>{{up[0].webkitRelativePath.split('/')[0]}}</p>
            </Tooltip>
          </zpa-row>

          <!-- <span>
           <Tooltip
             v-if="up.status === 'uploading' && up.isPaused"
             :transfer="true"
             content="上传"
           >
             <Icon type="ios-play" :size="28" color="#999"></Icon>
           </Tooltip>

           <Tooltip
             v-else-if="up.status === 'uploading' && !up.isPaused"
             :transfer="true"
             content="暂停"
           >
             <Icon type="ios-pause" :size="28" color="#999"></Icon>
           </Tooltip>

           <Tooltip v-if="up.status === 'wrong'" :transfer="true" content="刷新">
             <Icon type="ios-refresh" :size="28" color="#999"></Icon>
           </Tooltip>

           <Tooltip v-if="up.status === 'success'" :transfer="true" content="删除">
             <Icon type="ios-trash" :size="28" color="#999"></Icon>
           </Tooltip>

           <Tooltip v-else :transfer="true" content="取消">
             <Icon type="ios-close" :size="28" color="#999"></Icon>
           </Tooltip>
         </span>-->
        </zpa-column>
      </li>
    </ul>
    <span class="size" v-if="showUpload">当前需上传  {{filesLength}}  文件,已上传  {{percentage}}  个文件</span>
    <zpa-column v-if="showUploadProgress">
      <zpa-row no-gutter middle>
        <zpa-row>
          <i-progress :storke-width="4" :percent="getFloat((100 / uploads[uploads.length - 1].length) * percentage,2)"></i-progress>
        </zpa-row>
      </zpa-row>
    </zpa-column>

  </div>
</template>

<script>
  import Uploader from '@/components/zpa/form/uploader'
  import {folderNew} from '@/api/file'

  export default {
  name: 'UploadFolder',
  data () {
    return {
      formArray: [],
      uploads: [],
      showUpload: false,
      showUploadProgress: false,
      $Upload: '',
      filesLength: 0,
      percentage: 0,
      uploadContinue: false,
      showGray: false,
    }
  },
  props: {
    paramsData: Object,
  },
  inject: ['getLibraryIds'],
  methods: {
    folderNew,

    findElem (arrayToSearch, attr, val, level) { // 判断数组对象中是否有某个属性值
      const arr = []
      for (var i = 0; i < arrayToSearch.length; i++) {
        if (arrayToSearch[i][attr] === val && arrayToSearch[i].level === level) {
          arr.push(i)
        }
      }
      return arr.length === 0 ? -1 : arr[arr.length - 1]
    },

    changesData (content) {
      console.log(content)
      this.filesLength = content.target.files.length
      this.showUpload = true
      this.showUploadProgress = true
      this.formArray = []
      this.percentage = 0
      this.uploads.push(content.target.files)
      this.createdFolder(content)
    },

    async createdFolder (content) {
      const files = content.target.files
      const params = {
        isCreateFolder: 'T',
        isUpload: 'T',
        libraryId: this.getLibraryIds()
      }
      for (let i = 0; i < content.target.files.length; i++) {
        // 循环创建文件夹
        for (let r = 0; r < files[i].webkitRelativePath.split('/').length; r++) {
          if (r !== files[i].webkitRelativePath.split('/').length - 1) {
            // 当不是最后一层文件时
            if (r === 0 && this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r) === -1) {
              // 当是第一层文件夹时,且没有在数组中找到相同层级的文件夹名
              params.folderName = files[i].webkitRelativePath.split('/')[r]
              params.folderPath = this.paramsData.filePath
              await folderNew(params).then(res => {
                const vm = this
                return new Promise(function (resolve, reject) {
                  if (res.msg === '100文件夹已存在') {
                    vm.$Modal.confirm({
                      title: '已存在该文件夹,是否继续上传?',
                      okText: '上传',
                      onOk () {
                        vm.formArray.push(
                          {
                            folderName: res.folderName,
                            folderId: res.folderId,
                            folderPath: res.folderPath,
                            level: r,
                            nextFolderId: res.folderPath + '/' + res.folderId
                          }
                        )
                        // vm.handleSpinCustom()
                        vm.createdFolder(content)
                      },
                      onCancel () {
                        vm.uploads.pop()
                        vm.showUploadProgress = false
                        return false
                      }
                    })
                  } else {
                    // vm.handleSpinCustom()
                    vm.formArray.push(
                      {
                        folderName: res.folderName,
                        folderId: res.folderId,
                        folderPath: res.folderPath,
                        level: r,
                        nextFolderId: res.folderPath + '/' + res.folderId
                      }
                    )
                    vm.createdFolder(content)
                  }
                })
              })
            } else if (r !== 0 &&
                this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r) !== -1) {
              // 当不是第一层文件夹时,且在数组中找到相同文件夹名时
              if (this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r)].level !== r) {
                // 相同文件名, 且不同等级,
                params.folderName = files[i].webkitRelativePath.split('/')[r]
                params.folderPath = this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r - 1], r - 1)].nextFolderId
                await folderNew(params).then(res => {
                  this.formArray.push(
                    {
                      folderName: res.folderName,
                      folderId: res.folderId,
                      folderPath: res.folderPath,
                      level: r,
                      nextFolderId: res.folderPath + '/' + res.folderId
                    }
                  )
                })
              } else if (this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r)].level === r &&
                  this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r)].folderPath !==
                  this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r - 1], r - 1)].nextFolderId
              ) {
                // 相同文件名, 相同等级, 不同所属文件路径
                params.folderName = files[i].webkitRelativePath.split('/')[r]
                params.folderPath = this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r - 1], r - 1)].nextFolderId
                await folderNew(params).then(res => {
                  this.formArray.push(
                    {
                      folderName: res.folderName,
                      folderId: res.folderId,
                      folderPath: res.folderPath,
                      level: r,
                      nextFolderId: res.folderPath + '/' + res.folderId
                    }
                  )
                })
              }
            } else if (r !== 0 && this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r], r) === -1) {
              // 当不是第一层文件夹时,且在数组中没有找到相同文件夹名
              params.folderName = files[i].webkitRelativePath.split('/')[r]
              params.folderPath = this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r - 1], r - 1)].nextFolderId
              await folderNew(params).then(res => {
                this.formArray.push(
                  {
                    folderName: res.folderName,
                    folderId: res.folderId,
                    folderPath: res.folderPath,
                    level: r,
                    nextFolderId: res.folderPath + '/' + res.folderId
                  }
                )
              })
            }
          } else if (r === files[i].webkitRelativePath.split('/').length - 1) {
            // 当是最后一层文件名
            if (this.formArray.length !== 0) {
              const paramsData = {
                ...this.paramsData,
                libraryId: this.getLibraryIds(),
                filePath: this.formArray[this.findElem(this.formArray, 'folderName', files[i].webkitRelativePath.split('/')[r - 1], r - 1)].nextFolderId,
              }
              this.upload(files[i], paramsData)
            }
          }
        }
      }
      // content.target.value = ''//不可以连续上传同名文件夹
    },

    async upload (file, paramsData) {
      const upload = new Uploader(file, paramsData, this.uploadCompleteCb, '', this.progressFn)
      this.$Upload = upload
      if (!this.uploadContinue) {
        await upload.start()
      }
    },

    uploadCompleteCb () {
      this.percentage++
      this.showGray = true
      this.$emit('loading', true)
      if (this.percentage === this.filesLength) {
        this.showGray = false
        this.$emit('loading', false)
        this.$emit('reload')
      }
    },

    stopUpload () {
      this.uploadContinue = true
      if (this.$Upload) {
        this.$Upload.stop()
      }
    },

    getFloat (number, n) {
      n = n ? parseInt(n) : 0
      if (n <= 0) return Math.round(number)
      number = Math.round(number * Math.pow(10, n)) / Math.pow(10, n)
      return number
    },
  }

}

</script>

<style scoped>
  .uploadDiv{
    position: relative;
    text-align: center;
    border: 1px dotted #ddd;
    padding: 20px 0;
  }
  .demo-spin-icon-load{
    animation: ani-demo-spin 1s linear infinite;
  }
  .size{
    display: inline-block;
    height: 30px;
    line-height: 41px;
    color: gray;
  }
  #cover {
    position: absolute;
    left: 0px;
    top: 0px;
    background: rgba(0, 0, 0, 0.4);
    width: 100%; /*宽度设置为100%，这样才能使隐藏背景层覆盖原页面*/
    height: 100%;
    filter: alpha(opacity=60); /*设置透明度为60%*/
    opacity: 0.6; /*非IE浏览器下设置透明度为60%*/
    display: none;
    z-Index: 1;
  }
</style>
