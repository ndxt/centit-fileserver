<template>
  <ModalOperator :config="{ title: '文件详情', width: 720 }">
    <div slot="button" class="colorLook">
      详情
    </div>
    <TabList :tabData="tabs">
      <FileInfo slot="fileInfo" v-model="dataInfo" :fileName="fileName" @inputVal="inputVal"/>
      <FileLog slot="fileLog" v-model="dataLog" />
    </TabList>
  </ModalOperator>
</template>
<script>
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
import {addFolder, addMes, log, seeFileMes} from '@/api/file'
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
      inputValName: '',
      dataInfo: [],
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
  },
  methods: {
    addFolder,
    seeFileMes,
    log,
    reload () {
      seeFileMes(this.fileId)
        .then(res => {
          this.dataInfo = res
        })
      log(this.fileId)
        .then(res => {
          this.dataLog = res
        })
    },
    beforeOpen () {
      this.reload()
    },
    submit () {
      // 修改文件名
      if (this.current.versions !== 0 && this.inputValName !== '' && this.inputValName !== this.params.fileName.substring(0, this.params.fileName.lastIndexOf('.'))) {
        this.params.fileName = this.inputValName + this.params.fileName.substring(this.params.fileName.lastIndexOf('.'))
        this.params.fileId = this.params.accessToken
        addMes(this.params.accessToken, this.params)
          .then(res => {
            this.$emit('update', '')
          })
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
