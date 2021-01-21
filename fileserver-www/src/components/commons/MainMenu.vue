<template>
  <div class="zpa-column MenuContainer" :class="{isCollapsed: !isCollapsed}">
    <div @click="showClose" style="position:absolute;top: 10px;right: 0;z-index: 999">
      <Icon v-if="!isCollapsed" type="md-arrow-dropright" size="26" color="#fff"/>
      <Icon v-if="isCollapsed" type="md-arrow-dropleft" size="26" color="#fff"/>
    </div>
    <div class="content" v-if="isCollapsed">
      <ul>
        <li @click="goto('Homelist')">
          <div class="zpa-row">
            <div class="zpa-column cname"><span><Icon type="md-home" size="26" color="#fff"/>首页</span></div>
          </div>
        </li>
        <li v-for="(i,index) in menu" :key="index"
            @click="goto(i.libraryId,i.createUser,i.isCreateFolder,i.isUpload,i.libraryName,i.libraryType,i.ownUnit,i.ownUser)">
          <div class="zpa-row">
            <div class="zpa-column cname">
                <span>
                  <img :src="i.url" alt="" v-if="i.libraryType !== 'P'" style="vertical-align: middle">
                   <Icon v-if="i.libraryType === 'P'" type="md-person" size="26" color="#fff" :title="i.libraryName"/>
                  {{ i.libraryName }}
                </span>
            </div>
            <div class="dropdown" v-if="i.libraryType === 'I'">
              <Dropdown transfer>
                <a href="javascript:void(0)">
                  <Icon type="md-more" size="20"/>
                </a>
                <DropdownMenu slot="list">
                  <DropdownItem v-for="(j,index) in handle" :key="index" @click.native="onClickItem(j,i)">
                    {{ j }}
                  </DropdownItem>
                </DropdownMenu>
              </Dropdown>
            </div>
          </div>
        </li>
        <AddLibrary @success="submit">
          <li class="ivu-menu-item">
            <Icon type="ios-add-circle-outline" size="26" color="#fff"/>
            <span>创建项目库</span>
          </li>
        </AddLibrary>
      </ul>
    </div>
    <div class="content isCollapsed" v-if="!isCollapsed">
      <ul>
        <li @click="goto('Homelist')">
          <div class="zpa-row">
            <div class="zpa-column cname"><span><Icon type="md-home" size="26" color="#fff"/></span></div>
          </div>
        </li>
        <li v-for="(i,index) in menu" :key="index"
            @click="goto(i.libraryId,i.createUser,i.isCreateFolder,i.isUpload,i.libraryName,i.libraryType,i.ownUnit,i.ownUser)">
          <div class="zpa-row">
            <div class="zpa-column cname">
                  <span>
                  <img :src="i.url" alt="" v-if="i.libraryType !== 'P'" style="vertical-align: middle">
                    <Icon v-if="i.libraryType === 'P'" type="md-person" size="26" color="#fff" :title="i.libraryName"/>
                </span>
            </div>
          </div>
        </li>
        <AddLibrary @success="submit">
          <li class="ivu-menu-item">
            <Icon type="ios-add-circle-outline" size="26" color="#fff"/>
            <span>创建项目库</span>
          </li>
        </AddLibrary>
      </ul>
    </div>
    <Modal
      v-model="show"
      title="库的属性"
      draggable
      :width="500"
      @on-ok="ok"
    >
      <zpa-form ref="Form">
        <zpa-form-group>
          <zpa-text-input
            label="库名称"
            required
            v-model="params.libraryName"
            :disabled="params.libraryType !== 'I'"
          />
          <!--  <zpa-select
              required
              label="库类别"
              v-model="params.libraryType"
              :values="getlibraryType"
              textField="unitName"
              valueField="unitCode"
              disabled
            />-->
          <zpa-text-input
            label="库所属人员"
            v-model="params.ownName"
            disabled
          />
          <zpa-select
            label="所属机构"
            v-model="params.ownUnit"
            :query="() => getunitpath(currentUser.userCode)"
            textField="unitName"
            valueField="unitCode"
            :disabled="params.libraryType !== 'I'"
          />
          <UserSelect
            v-if="params.libraryType === 'I'"
            label="访问库人员"
            multiple
            v-model="params.fileLibraryAccesss"
          />
          <zpa-radio-group
            v-if="params.libraryType === 'I'"
            :values="[{text: '是', value: 'T'}, {text: '否', value: 'F'}]"
            label="可创建文件夹"
            v-model="params.isCreateFolder"
          />

          <zpa-radio-group
            v-if="params.libraryType === 'I'"
            :values="[{text: '是', value: 'T'}, {text: '否', value: 'F'}]"
            label="可上传文件"
            v-model="params.isUpload"
          />
        </zpa-form-group>
      </zpa-form>
    </Modal>
    <div class="Footer zpa-row">
      <div class=" zpa-row mycollection center" @click="tolink('MyCollections')">我的收藏</div>
      <div class=" zpa-row log center" @click="tolink('Log')"> 日志</div>
    </div>
    <!--删除提示框-->
    <FileRemoveConfirm ref="remove" v-model="removeRow" @loading="loading"/>
  </div>
</template>

<script>
import {
  mapState,
  mapMutations,
} from 'vuex'
import AddLibrary from '../library/AddLibrary'
import {
  getlibrarylist,
  getunitpath,
  seeLibrary,
  updatelibraryr,
  initpersonlib,
  initunitlib,
} from '@/api/file'
import FileRemoveConfirm from '../file/FileRemoveConfirm'

export default {
  name: 'MainMenu',
  components: {
    AddLibrary,
    FileRemoveConfirm
  },
  data () {
    return {
      obj: {},
      menu: [],
      handle: ['删除', '查看'],
      show: false,
      getlibraryType: [{
        unitCode: 'I',
        unitName: '项目'
      }, {
        unitCode: 'O',
        unitName: '机构'
      }, {
        unitCode: 'P',
        unitName: '个人'
      }],
      params: {},
      updateinfo: {
        fileLibraryAccesss: [{
          accessUsercode: '',
          libraryId: ''
        }],
        isCreateFolder: '',
        isUpload: '',
        libraryName: '',
        libraryType: '',
        ownUnit: '',
        ownUser: '',
        libraryId: '',
      },
      addlibraryId: {
        fileLibraryAccesss: [{}],
        isCreateFolder: '',
        isUpload: '',
        libraryName: '',
        libraryType: '',
        ownUnit: '',
        ownUser: '',
      },
      name: '',
      isActive: '',
      List: '',
      url: '',
      removeRow: {}
    }
  },
  computed: {
    ...mapState('core', {
      currentUser: 'userInfo',
    }),
    isCollapsed () {
      return this.$store.state.isCollapsed
    }
  },
  methods: {
    ...mapMutations(['setLibraryInfo']),
    showClose () {
      this.$store.state.isCollapsed = !this.$store.state.isCollapsed
    },
    ok () {
      this.updateinfo.libraryId = this.params.libraryId
      this.updateinfo.ownUnit = this.params.ownUnit
      this.updateinfo.ownUser = this.params.ownUser
      this.updateinfo.libraryName = this.params.libraryName
      this.updateinfo.libraryType = this.params.libraryType
      this.updateinfo.fileLibraryAccesss = this.params.fileLibraryAccesss
      this.updateinfo.isCreateFolder = this.params.isCreateFolder
      this.updateinfo.isUpload = this.params.isUpload
      updatelibraryr(this.updateinfo).then(res => {
        this.params = res
        this.submit()
      })
    },
    getunitpath,
    goto (name, createUser, isCreateFolder, isUpload, libraryName, libraryType, ownUnit, ownUser) {
      if (!name) {
        // i.libraryId,i.createUser,i.isCreateFolder,i.isUpload,i.libraryName,i.libraryType,i.ownUnit,i.ownUser
        this.addlibraryId.fileLibraryAccesss[0].accessUsercode = createUser
        this.addlibraryId.isCreateFolder = isCreateFolder
        this.addlibraryId.isUpload = isUpload
        this.addlibraryId.libraryName = libraryName
        this.addlibraryId.libraryType = libraryType
        this.addlibraryId.ownUnit = ownUnit
        this.addlibraryId.ownUser = ownUser
        // addLibrary(this.addlibraryId).then(res => {
        //  this.$router.push({name: 'myFile', params: {libraryId: res.libraryId}})
        //    this.submit()
        // })
        if (libraryType === 'P') {
          initpersonlib().then(res => {
            this.submit()
          })
        } else if (libraryType === 'O') {
          initunitlib(ownUnit).then(res => {
            this.submit()
          })
        }
      }
      this.name = name
      if (name === 'Homelist') {
        this.$router.push('/')
      } else {
        this.$router.push({
          name: 'myFile',
          params: { libraryId: this.name }
        })
      }
    },
    tolink (name) {
      if (name === 'MyCollections') {
        this.$router.push({ name: 'MyCollections' })
      } else if (name === 'Log') {
        this.$router.push({ name: 'Log' })
      }
    },
    loading () {
      this.submit()
    },
    submit () {
      var userCode = this.currentUser.userCode
      var params = {
        libraryType: 'I',
        sort: 'createTime',
        order: 'asc',
      }
      getlibrarylist(userCode, params).then(res => {
        const libraries = res ? res.objList || [] : []
        this.setLibraryInfo(libraries)
        this.menu = libraries
        for (let i = 0; i < this.menu.length; i++) {
          const ctx = window.$contextPath || ''
          this.menu[i].url = `${ctx}/api/file/fileserver/library/libraryimage/${this.menu[i].libraryName}?blue=255&green=255&red=255&size=30`
        }
      })
    },
    onClickItem (i, j) {
      if (i === '删除') {
        j.versions = '-1'
        j.fileName = j.libraryName
        this.removeRow = j
        this.$refs.remove.toggle()
      } else if (i === '查看') {
        this.show = true
        seeLibrary(j.libraryId).then(res => {
          this.params = res
        })
      }
    }
  },
  watch: {
    currentUser: {
      handler: function currentUser (user) {
        if (user) {
          this.submit()
        }
      },
      immediate: true,
    },
  },
}
</script>

<style scoped lang="less">
  .MenuContainer {
    height: 100%;
    width: 280px;
    flex: 0 0 280px;
    overflow: hidden;
    transition: width ease-in 200ms;
    display: flex;
    flex-flow: column;

    &.isCollapsed {
      width: 100px;
      flex-basis: 100px;
    }

    .Menu {
      flex: 1;
    }

    .Footer {
      flex: 0 0 32px;
      height: 32px;
      margin-bottom: 80px;
    }
  }

  .content {
    display: flex;
    flex: 1;
    overflow: auto;

    a {
      color: #fff;
    }

    ul {
      flex: 0 0 280px;
      background: none;

      li {
        color: #fff;
        border-bottom: 1px solid #47B3B0;
        padding: 14px 24px;
        position: relative;
        cursor: pointer;
        z-index: 1;
        transition: all .2s ease-in-out;

        &:hover {
          background: rgb(81, 190, 169, .7);
          color: #fff;
        }

      }
    }

    .dropdown {
      text-align: right;
    }

    .ivu-menu-light.ivu-menu-vertical .ivu-menu-item-active:not(.ivu-menu-submenu) {
      background: rgb(81, 190, 169, .7);
      color: #fff;
    }
  }

  .isCollapsed {
    ul {
      flex: 0 0 100px;
    }
  }

  .mycollection, .log {
    color: #fff;
    cursor: pointer;
  }

  .ivu-icon {
    margin-right: 13px;
  }

  .cname {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }

</style>
