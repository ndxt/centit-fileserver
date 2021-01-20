<template>
  <zpa-row>
    <zpa-column class="column">
      <Card>
        <p slot="title" class="title">
          最近访问
        </p>
        <!--<a href="#" slot="extra" @click.prevent="getmore">
          更多
        </a>-->

        <CellGroup>
          <Cell v-for="(item,index) in see"  :key="index"  :title="item.fileName" :extra="item.createTime | time" />
        </CellGroup>
      </Card>
    </zpa-column>
    <zpa-column class="column">
      <Card>
        <p slot="title" class="title">
          最近上传
        </p>
        <!--<a href="#" slot="extra" @click.prevent="getmore">
          更多
        </a>-->
        <div class="box" @click="path(item.libraryId,item.parentFolder)" v-for="(item,index) in upload"   :key="index">
          <div class="left">{{item.fileName}}</div>
          <div>{{item.createTime  | time}}</div>
        </div>
      </Card>
    </zpa-column>
    <zpa-column class="column">
      <Card>
        <p slot="title" class="title">
          个人收藏
        </p>
        <!--<a href="#" slot="extra" @click.prevent="getmore">
          更多
        </a>-->
          <div class="box" @click="path(item.libraryId,item.parentFolder)" v-for="(item,index) in Collections"  :key="index">
            <div class="left">{{item.fileName}}</div>
            <div>{{item.favoriteTime}}</div>
          </div>
      </Card>
    </zpa-column>
  </zpa-row>
</template>
<script>
import { mapState, } from 'vuex'

import { GetFavoriteList, getfileslist, getlog } from '@/api/file'

export default {
  name: 'HomeContentList',
  props: {
    value: Array
  },
  data () {
    return {
      upload: [],
      Collections: [],
      see: [],
    }
  },
  computed: {
    ...mapState('core', {
      currentUser: 'userInfo',
    }),

    uploadParams () {
      return {
        sort: {
          sort: 'createTime',
          order: 'desc'
        },
        params: {
          owner: this.currentUser.userCode,
        }
      }
    },

    favoriteParams () {
      return {
        sort: {
          sort: 'favoriteTime',
          order: 'desc'
        },
        params: {
          favoriteUser: this.currentUser.userCode,
        }
      }
    },

    logParams () {
      return {
        params: {
          userCode: this.currentUser.userCode,
          optMethod: '下载',
          sort: 'optTime',
          order: 'desc',
        }
      }
    },
  },
  mounted () {
    getfileslist(this.uploadParams).then(res => {
      this.upload = res.objList.splice(0, 15)
    })
    GetFavoriteList(this.favoriteParams).then(res => {
      this.Collections = res.splice(0, 15)
    })

    getlog(this.logParams).then(res => {
      if (!res) return
      for (let i = 0; i < res.length; i++) {
        if (res[i].optMethod === '下载') {
          const arr = JSON.parse(res[i].newValue)
          this.see.push(arr)
        }
      }
    })
  },
  watch: {
  },
  methods: {
    getmore () {

    },
    path (libraryId, parentFolder) {
      if (parentFolder === '-1') {
        this.$router.push({ path: `myFile/${libraryId}` })
      } else {
        this.$router.push({ path: `myFile/${libraryId}?folderId=${parentFolder}` })
      }
    },
  }
}
</script>

<style scoped  lang="less">
.column {
  flex: 0 0 33.33333%;
}
.title:before{
  position: absolute;
  display:block;
  content:'';
  width:5px;
  height: 26px;
  left: 9px;
  top:12px;
  background: linear-gradient(#3BB59C, #3D9AC6);
}
.zpa-row, .ivu-card{
  overflow: hidden;
}
.ivu-card-head p, .ivu-card-head-inner{
  padding-left: 8px;
}
.zpa-row,.ivu-card{
  height: 100%;
  .ivu-card-extra a{
    color:#CDCED0 ;
  }
}
.box{
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  line-height: normal;
  padding: 9px 16px;
  &:hover {
    background: #f0f0f0;
  }
  .left{
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    width: 200px;
  }
}
</style>
