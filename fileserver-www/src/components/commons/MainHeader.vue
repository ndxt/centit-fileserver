<template>
  <div class="app-header zpa-row middle">
    <div class="logo">
      <h2>
       <span class="folder"> <Icon type="md-folder-open" size="30" color="#fff"/></span>
        文件服务器
      </h2>
    </div>
    <div class="right zpa-row end middle">
      <zpa-form ref="Form" class="form">
        <zpa-select
            v-model="params.libraryIds"
            :values="libraryInfo"
            textField="libraryName"
            valueField="libraryId"
        />
      </zpa-form>
      <div >
        <Input search placeholder="请输入搜索内容" v-model="params.query" @on-search="search" clearable />
      </div>
      <div>
         <span class="userRank" v-if="currentUser">
           <span>{{currentUser.unitName}}</span>
            <span> {{currentUser.userStationText}}</span>
            <span> {{currentUser.userName}}（{{currentUser.userRankText}}）</span>
        </span>
        <span class="userRank" v-else>
            {{unitName}} - {{name}} - {{userStationText}}（{{userRankText}}）
          </span>
        <UserDropMenu />
      </div>
    </div>
  </div>

</template>

<script>
import {mapState} from 'vuex'
import UserDropMenu from './UserDropMenu'

export default {
  name: 'MainHeader',
  components: {
    UserDropMenu,
  },
  data () {
    return {
      List: [],
      selectcode: '1',
      key: '',
      params: {
        libraryIds: '',
        query: '',
      },
    }
  },
  computed: {
    ...mapState('core', {
      currentUser: 'userInfo',
    }),
    ...mapState({
      libraryInfo: 'libraryInfo',
    }),
  },
  methods: {
    search () {
      const params = this.params
      this.$router.push({ name: 'Search', params })
    },
  },
  watch: {
    '$route.params.libraryId': {
      handler (id) {
        this.params.libraryIds = id || ''
      }
    },
  },
  mounted () {
    console.log(this.currentUser)
  }
}
</script>

<style lang="less" scoped>
.app-header {
  padding: 0 16px;
  height: 94px;

  .logo {
    flex: none;
    font-size: 20px;
  }
  .folder{
    background-color: #00bf92;
    padding: 0 5px 3px 5px;
    border-radius: 5px;
  }

  .right {
    overflow: hidden;
    padding: 0 10px;
    flex: auto;

    .form {
      width: 200px;
    }

    .search {
      flex: auto;
    }

    .user {
      flex: none;
    }
  }
  .userRank{
    font-size: 14px;
    span{
      margin: 0 5px;
    }
  }
}
</style>
