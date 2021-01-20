<template>
  <div v-if="showModal">
    <Modal
        :title=value.name
        width="500"
        v-model="showFls"
        @on-cancel="cancel"
        @on-ok="ok"
    >
      <Tree
          :data="treeData"
          :load-data="loadData"
          ref="Tree"
          :show-checkbox="true"
          :check-strictly="true"
          :check-directly="true"
          @on-check-change="treeChange"
      ></Tree>
    </Modal>
  </div>
</template>

<script>
import { addFolder, addMes, folder, folderNew, getlibrarylist } from '@/api/file'
import { mapState, } from 'vuex'

export default {
  name: 'FileCopyModal',
  inject: ['getLibraryIds'],
  data () {
    return {
      showFls: false,
      showModal: false,
      treeData: [],
      multiple: false
    }
  },
  props: {
    value: Object,
  },
  computed: {
    ...mapState('core', {
      currentUser: 'userInfo',
    }),
  },
  methods: {
    folder,
    addFolder,
    folderNew,
    addMes,
    getlibrarylist,
    cancel () {
      this.showModal = false
    },
    ok () {
      const nodes = this.$refs.Tree.getCheckedNodes()
      this.showModal = false
      const params = {
        folderName: this.value.fileName,
        folderPath: nodes[nodes.length - 1].folderId === '-1' ? nodes[nodes.length - 1].fileShowPath : nodes[nodes.length - 1].fileShowPath + '/' + nodes[nodes.length - 1].folderId,
        isCreateFolder: this.value.createFolder,
        isUpload: this.value.uploadFile,
        libraryId: nodes[nodes.length - 1].libraryId,
        oldFoldId: this.value.folderId
      }
      this.value.libraryId = params.libraryId
      if (this.value.name === '复制到') {
        if (this.value.versions > 0) { // 表示文件
          this.value.fileShowPath = params.folderPath
          this.addMes(this.value.accessToken, this.value)
            .then(res => {
              this.$parent.loading()
            })
        } else {
          this.folderNew(params)
            .then(res => {
              if (res.msg === '100文件夹已存在') {
                this.$Message.success('该文件夹中已存在同名文件夹')
              } else {
                this.$parent.loading()
              }
            })
        }
      } else {
        if (this.value.versions > 0) { // 表示文件
          this.value.fileShowPath = params.folderPath
          this.value.fileId = this.value.accessToken
          this.addMes(this.value.accessToken, this.value)
            .then(res => {
              this.$parent.loading()
            })
        } else { // 表示文件夹
          const pas = {
            folderId: this.value.folderId,
            ...params
          }
          this.addFolder(pas)
            .then(res => {
              if (res.msg === '100文件夹已存在') {
                this.$Message.success('该文件夹中已存在同名文件夹')
              } else {
                this.$parent.loading()
              }
            })
        }
      }
    },

    treeChange (arr, obj) {
      // 清空所有已选中的
      arr.forEach(item => {
        item.checked = false
      })
      // 只选中最后一次选中的
      obj.checked = true
    },

    reload () {
      var userCode = this.currentUser.userCode
      var params = {
        libraryType: 'I',
        sort: 'createTime',
        order: 'asc',
      }
      getlibrarylist(userCode, params)
        .then(res => { // 第一层所有库的信息
          res.objList.forEach(i => {
            i.title = i.libraryName
            i.folderId = '-1'
            i.fileShowPath = '/-1'
            i.loading = false
            i.children = []
            this.treeData.push(i)
          })
        })
    },

    toggle () {
      this.treeData = []
      this.showModal = true
      this.$nextTick(() => {
        this.showFls = true
        this.reload()
      })
    },
    loadData (item, callback) {
      const params = {
        libraryId: item.libraryId, // 库id
        folderId: item.folderId
      }
      folder(params)
        .then(res => {
          const Data = []
          res.forEach(i => {
            if (i.folder) {
              i.title = i.fileName
              i.loading = false
              i.children = []
              i.libraryId = item.libraryId
              Data.push(i)
            }
          })
          callback(Data)
        })
    }
  }
}
</script>

<style scoped>

</style>
