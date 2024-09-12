<template>
  <zpa-column style="overflow-y: scroll">
    <zpa-column style="flex: auto;">
      <div v-for="(item,index) in List" :key="index">
        <h2 @click="preview(item.fileId)" style="cursor: pointer;">{{item.fileName}}</h2>
        <div v-html="item.highlight"> </div>
        <zpa-row>
          <zpa-column> <div> <Icon type="ios-time" /> {{item.createTime}}</div> </zpa-column>
          <zpa-column><div  @click="path(item.optTag,item.optId)"  style="cursor: pointer;color: #0d92ff"> <Icon type="md-git-merge" />{{item.showPath}}</div> </zpa-column>
          <zpa-column> <div @click="download(item.fileId)"  style="cursor: pointer;"> <Icon type="md-cloud-download" />下载</div> </zpa-column>
          <zpa-column>
            <div v-if="item.favoriteId !== ''">
              <div @click="deletestar(item.favoriteId)" style="cursor: pointer;"><Icon type="ios-heart" />取消收藏</div>
            </div>
            <div v-if="item.favoriteId === ''">
              <div @click="collections(item.fileId)" style="cursor: pointer;"> <Icon type="ios-heart" />收藏</div>
            </div>
          </zpa-column>
        </zpa-row>
      </div>
    </zpa-column>
    <zpa-row end height="100px" >
      <Page
        :total="params.page.totalRows"
        :current="params.page.pageNo"
        :pageSize="params.page.pageSize"
        size="small"
        show-elevator
        show-sizer
        @on-change="changePage"
        :transfer="true"
      />
    </zpa-row>
  </zpa-column>
</template>
<script>
import {deletefavorite, downs, downsPreview, GetSearch, newfavorite} from '@/api/file'

export default {
  name: 'Search',
  data () {
    return {
      params: {
        params: {
          libraryIds: this.$route.params.libraryIds,
          query: this.$route.params.query
        },
        page: {
          pageNo: 1,
          pageSize: 7
        }
      },
      List: []
    }
  },
  mounted () {
    GetSearch(this.params).then(({ objList, pageDesc }) => {
      this.List = objList
      this.params.page = pageDesc
    })
  },
  methods: {
    search () {
      return GetSearch(this.params)
        .then(({ objList, pageDesc }) => {
          this.List = objList
          this.params.page = pageDesc
        })
    },
    download (fileId) {
      downs(fileId)
    },
    preview (fileId) {
      downsPreview(fileId)
    },
    changePage (pageNo) {
      this.params.page.pageNo = pageNo
      this.search()
    },
    path (url, id) {
      if (url === '-1') {
        this.$router.replace({ path: `/myFile/${id}` })
      } else {
        this.$router.replace({ path: `/myFile/${id}?folderId=${url}` })
      }
    },
    collections (id) {
      const params = {
        fileId: id
      }
      newfavorite(params)
        .then(() => {
          this.$Message.success('收藏成功')
          this.search()
        })
        .catch(res => {
          this.$Message.success('收藏失败')
        })
    },
    deletestar (id) {
      const vm = this
      this.$Modal.confirm({
        title: '确定取消收藏吗?',
        loading: true,
        closable: true,
        onOk () {
          deletefavorite(id).then(() => {
            vm.$Modal.remove()
            this.search()
          })
        },
      })
    }
  },
  watch: {
    '$route' (to) {
      this.params.params.libraryIds = to.params.libraryIds
      this.params.params.query = to.params.query
      this.search()
    },
  },
}
</script>

<style >
.highlight{
  color: red!important;
}
</style>
