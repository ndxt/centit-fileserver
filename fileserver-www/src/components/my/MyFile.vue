<template>
  <div class="zpa-column">
    <FileList
      :mainmenu="columns"
      :breadcrumb="breadcrumb"
      :general="general"
      :version="version"
      ref="fileList"
    >
      <span slot="handle" style="line-height: 7px;">
        <FolderCreateModal
          style="display: inline-block;position: relative;top: 6px;margin-right: 2px;"
          v-if="breadcrumb[breadcrumb.length - 1]"
          :disable="breadcrumb[breadcrumb.length - 1].createFolder === 'T'"
        />
        <Dropdown trigger="click" style="position: relative;top: -7px;">
          <Button
            v-if="breadcrumb[breadcrumb.length - 1]"
            style="background: #3CB5A2;color:#fff"
            :class="[breadcrumb[breadcrumb.length - 1].uploadFile === 'T' ? '' : 'gray']"
            :disabled="breadcrumb[breadcrumb.length - 1].uploadFile !== 'T'"
          >上传</Button>
          <DropdownMenu slot="list">
            <DropdownItem>
              <FileUpload
                @uploadCompleteCb="loading"
                :paramsData="dataUpload"
              />
            </DropdownItem>
            <DropdownItem>
              <FolderUpload
                :paramsData="dataUpload"
                @reload="loading()"
              />
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </span>

      <!--操作栏-->
      <div slot="operator" slot-scope="{ props }" class="opreator">
        <FileViewModal
          v-if="props.row.fileType"
          :params="props.row"
          @update="loading()"
          ref="See" />
        <FolderViewModal
          v-else
          :params="props.row"
          @update="loading()"
          ref="See"
        />
        <span v-if="props.row.versions" class="colorDown" @click="onClickItem('下载文件', props.row)">
          {{ props.row.versions > 1 ? '下载最新' : '下载' }}
        </span>
        <Dropdown transfer trigger="click">
          <Icon type="md-more" size="20"/>
            <DropdownMenu slot="list" v-if="props.row.versions">
              <DropdownItem
                v-for="(i, key) in props.row.handle"
                :key="key"
                @click.native="() => onClickItem(i, props.row)"
              >
                {{ i }}
              </DropdownItem>
            </DropdownMenu>
            <DropdownMenu slot="list" v-else>
              <DropdownItem
                  v-for="(i, key) in props.row.handle"
                  :key="key"
                  @click.native="() => onClickItem(i, props.row)"
              >
                {{ i }}
              </DropdownItem>
            </DropdownMenu>
        </Dropdown>
      </div>
    </FileList>
    <!--复制到,移动到-->
    <FileCopyModal ref="seeFile" v-model="copyRow" />

    <!--分享-->
    <FileShareModal ref="share" v-model="shareRow" />

    <!--删除提示框-->
    <FileRemoveConfirm ref="remove" v-model="removeRow" @loading="loading" />
  </div>
</template>

<script>
  import FileList from '../file/FileList'
  import FolderCreateModal from '../folder/FolderCreateModal'
  import FileUpload from '../file/FileUpload'
  import columns from './columns'
  import FileViewModal from '../file/FileViewModal'
  import FolderViewModal from '../folder/FolderViewModal'
  import FileCopyModal from '../file/FileCopyModal'
  import FileShareModal from '../file/FileShareModal'
  import FileRemoveConfirm from '../file/FileRemoveConfirm'
  import FolderUpload from '../folder/FolderUpload'
  import {downs, folder, newfavorite, prev, queryUserVerLocal, seeLibrary} from '@/api/file'

  export default {
  name: 'MyFile',
  components: {
    FileList,
    FolderCreateModal,
    FileUpload,
    FileViewModal,
    FolderViewModal,
    FileCopyModal,
    FileShareModal,
    FileRemoveConfirm,
    FolderUpload,
  },
  data () {
    return {
      columns,
      libraryId: this.$route.params.libraryId,
      breadcrumb: [], // 面包屑 第一层的导航是左边带过来的值,
      copyRow: {},
      shareRow: {},
      alertFav: false,
      dataUpload: { // 默认第一级上传
        index: true,
        filePath: '/-1'
      },
      showRemoveFile: false, // 控制删除提示框显示隐藏
      removeRow: {}
    }
  },
  provide () {
    return {
      handles: this.handles,
      getLibraryIds: () => this.libraryId,
    }
  },
  watch: {
    breadcrumb (v) {
      if (v[v.length - 1].folderId === '-1') { // 当是第一级的时
        this.dataUpload.filePath = v[v.length - 1].fileShowPath
      } else {
        this.dataUpload.filePath = v[v.length - 1].fileShowPath + '/' + v[v.length - 1].folderId
      }
    },
    '$route' (now, old) {
      this.libraryId = now.params.libraryId
      this.hrefUrl()
      this.loading()
    },
  },

  methods: {
    folder,
    newfavorite,
    queryUserVerLocal,
    downs,
    general () { // 普通列表
      return folder
    },
    version () { // 版本列表
      return queryUserVerLocal
    },
    onClickItem (i, row) {
      console.log(i, row)
      if (i === '分享') {
        this.shareRow = row
        this.$refs.share.toggle()
      } else if (i === '收藏') {
        const params = {
          fileId: row.accessToken
        }
        newfavorite(params)
          .then(res => {
            this.$Message.success('收藏成功')
            this.loading()
          })
          .catch(res => {
            this.$Message.success('收藏失败')
          })
      } else if (i === '复制到' || i === '移动到') {
        this.copyRow = row
        this.copyRow.name = i
        this.copyRow.libraryName = this.breadcrumb[0].fileName
        this.$refs.seeFile.toggle()
      } else if (i === '删除') {
        this.removeRow = row
        this.$refs.remove.toggle()
      } else if (i === '详情') {
        this.$refs.See.$children[0].operator.current = row
        this.$refs.See.$children[0].open()
      } else if (i === '下载文件') {
        downs(row.accessToken)
      } else if (i === '查看版本') {
        this.$refs.fileList.dblclickRow(row)
      }
    },
    loading () {
      this.$refs.fileList.reload()
    },

    sortCompare (i) {
      return function (a, b) {
        var v1 = a[i].split('/').length
        var v2 = b[i].split('/').length
        return v1 - v2
      }
    },
    hrefUrl () { // 判断路由
      if (location.href.indexOf('folderId=') > -1) { // 代表带有参数
        prev(location.href.split('folderId=')[1])
          .then(res => {
            res.sort(this.sortCompare('folderPath')).forEach(i => {
              const obj = {}
              if (i.folderPath === '/') {
                obj.folderId = '-1'
                obj.fileShowPath = '/-1'
                obj.fileName = i.folderName
                obj.createFolder = i.isCreateFolder || 'T'
                obj.uploadFile = i.isUpload || 'T'
                this.breadcrumb.unshift(obj)
              } else {
                obj.folderId = i.folderId
                obj.fileShowPath = i.folderPath
                obj.fileName = i.folderName
                obj.createFolder = i.isCreateFolder || 'T'
                obj.uploadFile = i.isUpload || 'T'
                this.breadcrumb.push(obj)
              }
            })
          })
      } else {
        seeLibrary(this.libraryId)
          .then(res => {
            this.breadcrumb = []
            this.breadcrumb.push({
              fileName: res.libraryName,
              folderId: '-1', // 当前文件夹的路径id
              fileShowPath: '/-1', // 当前文件所属的路径
              createFolder: 'T', // 是否允许新建文件夹
              uploadFile: 'T', // 是否支持上传文件
            })
          })
      }
    }
  },
  created () {
    this.hrefUrl()
  },
}
</script>

<style scoped>

.colorDown {
  cursor: pointer;
  color: #3B96C7;
}

.opreator {
  width: 100%;
  display: flex;
  justify-content: space-around;
}

.gray {
  filter: grayscale(1);
}

.ivu-dropdown-item {
  height: 30px;
}
</style>
