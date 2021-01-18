<template>
  <ModalOperator width="800" title="详情">
    <div slot="button" class="colorLook">
      详情
    </div>
    <TabList :tabData="tabs">
      <FileInfo slot="fileInfo" v-model="fileId" :fileName="fileName" @inputVal="inputVal"/>
      <FileLog slot="fileLog" v-model="fileId" />
    </TabList>
  </ModalOperator>
</template>
<script>
  import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
  import {addFolder, addMes} from '@/api/file'
  import FileInfo from './FileInfo'
  import FileLog from './FileLog'

  export default {
  name: 'FileViewModal',
  mixins: [
    ModalOperatorMixin,
  ],
  inject: ['getLibraryIds'],
  components: {
    FileInfo,
    FileLog,
  },
  data () {
    return {
      tabs: [
        {
          name: '文件信息',
          component: 'fileInfo',
        },
        {
          name: '操作日志',
          component: 'fileLog',
        },
      ],
      current: {},
      paramsObj: {},
      inputValName: '',
      dataLog: []
    }
  },
  props: {
    params: Object
  },
  computed: {
    fileId () {
      return this.current.accessToken || this.current.fileId
    },
    fileName () {
      return this.params.fileName.substring(0, this.params.fileName.lastIndexOf('.'))
    },
  },
  mounted () {
    this.current = this.params
    this.paramsObj.fileName = this.params.fileName
    this.paramsObj.createFolder = this.params.createFolder
    this.paramsObj.uploadFile = this.params.uploadFile
    this.paramsObj.fileName = this.params.fileName
  },
  methods: {
    addFolder,
    onSubmit () {
      // 修改文件名
      if (this.current.versions !== 0 && this.inputValName !== '' && this.inputValName !== this.params.fileName.substring(0, this.params.fileName.lastIndexOf('.'))) {
        this.params.fileName = this.inputValName + this.params.fileName.substring(this.params.fileName.lastIndexOf('.'))
        this.params.fileId = this.params.accessToken
        addMes(this.params.accessToken, this.params)
          .then(res => {
            this.$emit('update', '')
          })
      }
      // 修改文件夹名
      if (this.current.versions === 0) {
        const arr = []
        for (const i in this.paramsObj) {
          if (this.current[i] !== this.paramsObj[i]) {
            arr.push(1)
          }
        }
        if (arr.length !== 0) {
          const params = {
            folderId: this.current.folderId,
            folderName: this.current.fileName,
            folderPath: this.current.fileShowPath, // 地址
            isCreateFolder: this.current.createFolder,
            isUpload: this.current.uploadFile
          }
          addFolder(params)
            .then(res => {
              this.$emit('update', '')
            })
        }
      }
    },
    inputVal (i) {
      this.inputValName = i
    }
  }
}
</script>

<style scoped>
.colorLook {
  cursor: pointer;
  color: #3CB5A2;
}
</style>
