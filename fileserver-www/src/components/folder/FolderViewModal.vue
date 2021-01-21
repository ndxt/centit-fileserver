<template>
  <ModalOperator :config="{ title: '文件夹详情', width: 720 }">
    <div slot="button" class="colorLook">
      详情
    </div>
    <FolderInfo v-model="current" ref="paper" />
  </ModalOperator>
</template>

<script>
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
import FolderInfo from './FolderInfo'
import { addFolder } from '@/api/file'

export default {
  name: 'FileViewModal',
  mixins: [
    ModalOperatorMixin,
  ],
  inject: ['getLibraryIds'],
  components: {
    FolderInfo,
  },
  data () {
    return {
      current: {},
      paramsObj: {},
      inputValName: ''
    }
  },
  props: {
    params: Object
  },
  mounted () {
    this.current = this.params
    this.paramsObj.fileName = this.params.fileName
    this.paramsObj.createFolder = this.params.createFolder
    this.paramsObj.uploadFile = this.params.uploadFile
    this.paramsObj.fileName = this.params.fileName
    this.paramsObj.name = this.params.fileName
  },
  methods: {
    addFolder,
    onSubmit () {
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
            folderName: this.$refs.paper.inputVal,
            folderPath: this.current.fileShowPath, // 地址
            isCreateFolder: this.current.createFolder,
            isUpload: this.current.uploadFile
          }
          addFolder(params)
            .then(res => {
              if (res.msg === '100文件夹已存在') {
                this.$Message.success('该文件夹已存在')
              } else {
                this.current.fileName = res.folderName
                this.$emit('update', '')
              }
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
