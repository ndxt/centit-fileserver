<template>
  <div class="zpa-column">
    <FileList
        :mainmenu="columns"
        :breadcrumb="breadcrumb"
        :general="general"
        :version="version"
        ref="FileList"
        :sortType="sortType"
    >
      <div slot="operator" slot-scope="{ props }">
        <FileViewModal
          v-if="props.row.fileType"
          :params="props.row"
          @update="load()"
          ref="See" />
        <FolderViewModal
          v-else
          :params="props.row"
          @update="load()"
          ref="See" />
        <span class="colorDown" @click="deletestar(props.row.favoriteId)">取消收藏</span>
        <Dropdown transfer trigger="click" style="margin-left: 10px;">
          <Icon type="md-more" size="20"/>
          <DropdownMenu slot="list">
            <DropdownItem v-for="(i,key) in handle" :key="key" @click.native="() => onClickItem(i, props.row)">
              {{ i }}
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </div>
    </FileList>
    <!--复制到,移动到-->
    <FileCopyModal ref="seeFile" v-model="copyRow"/>
    <!--分享-->
    <FileShareModal ref="share" v-model="shareRow"/>
  </div>
</template>

<script>
import FileList from '../file/FileList'
import columns from './columns'
import FileViewModal from '../file/FileViewModal'
import FileCopyModal from '../file/FileCopyModal'
import FileShareModal from '../file/FileShareModal'
import FolderViewModal from '../folder/FolderViewModal'

import {deletefavorite, GetFavoriteList, queryUserVerLocal,} from '@/api/file'

export default {
  name: 'Collection',
  components: {
    FileList,
    FileViewModal,
    FolderViewModal,
    FileCopyModal,
    FileShareModal,
  },
  data () {
    return {
      columns,
      breadcrumb: [
        {
          fileName: '我的收藏',
          libraryId: this.$route.params.libraryId, // 库
          fileShowPath: '',
          folderPath: '/-1'
        },
      ],
      copyRow: {},
      shareRow: {},
      alertFav: false,
      handle: ['分享'],
      sortType: {
        sort: 'favoriteTime',
        order: 'desc',
      },
    }
  },
  provide () {
    return {
      handles: this.handles,
      getLibraryIds: () => '',
    }
  },
  methods: {
    general () {
      return GetFavoriteList
    },
    version () {
      return queryUserVerLocal
    },
    onClickItem (i, row) {
      console.log(row)
      if (i === '分享') {
        this.shareRow = row
        this.$refs.share.toggle()
      } else if (i === '复制到' || i === '移动到') {
        this.copyRow = row
        this.copyRow.name = i
        this.$refs.seeFile.toggle()
      } else if (i === '取消收藏') {
        this.deletestar(row.favoriteId)
      }
    },
    deletestar (favoriteId) {
      const vm = this
      this.$Modal.confirm({
        title: '确定取消收藏吗?',
        loading: true,
        closable: true,
        onOk () {
          deletefavorite(favoriteId).then(res => {
            vm.$Modal.remove()
            vm.load()
          })
        },
      })
    },
    load () {
      this.$refs.FileList.reload()
    },
  }
}
</script>

<style scoped>

.colorDown {
  cursor: pointer;
  color: #3B96C7;
  position: relative;
  top: -2px;
}
</style>
