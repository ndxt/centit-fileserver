<template>
  <div class="zpa-column">
    <zpa-row
        style="flex: none; margin-bottom: -15px;justify-content: space-between;    padding: 8px 16px;align-items: center;">
      <div class="breads">
        <Breadcrumb separator=">">
          <BreadcrumbItem
              v-for="(name, i) in breadcrumb"
              :key="i"
              class="bread"
              @click.native="getRoute(name,i)"
          >{{ name.fileName }}
          </BreadcrumbItem>
        </Breadcrumb>
        <Icon type="ios-photos-outline" :data-clipboard-text=imgCopyAddressUrl size="18" class="imgCopyAddress"
              @click="copyAddress"/>
      </div>
      <div class="btnOp">
        <slot name="handle"></slot>
        <RadioGroup v-model="radio" type="button" @on-change="radioChange">
          <Radio label="列表">
            <Icon type="ios-menu" size="30"/>
          </Radio>
          <Radio label="图标">
            <Icon type="ios-apps-outline" size="30"/>
          </Radio>
        </RadioGroup>
        <Button @click="reload()">
          <Icon type="ios-refresh" size="20"/>
        </Button>
      </div>
    </zpa-row>
    <div class="zpa-column lists" v-show="!shape">
      <DataList
          :sortRule="sortType"
          ref="Table"
          :columns="mainmenu"
          :query="getQuery"
          :operWidth="170"
          isHidePage
          showLoading
          @on-dblclick-row="dblclickRow"
          :border="false"
          size="large"
      >
        <!--文件名-->
        <template slot="fileName" slot-scope="{row}">
          <div style="position: relative;cursor: pointer" @click="middleMethods(row)">
            <b v-if="row.versions > 1">{{ row.versions }}</b>
            <img :src="row.versions === 0 ? getImgUrl('folder') : getImgUrl(row.fileType)" alt="" class="iconstyle">
            <span :title="row.fileName || row.folderName ||'未知名'">{{ row.fileName || row.folderName || '未知名' }}</span>
          </div>

        </template>
        <!--大小-->
        <template slot="fileSize" slot-scope="{row}">
          <div v-if="row.versions > 0">
            {{row.fileSize | fileSizes}}
          </div>
        </template>
        <!--上传时间-->
        <template slot="createTime" slot-scope="{row}">
          {{ row.createTime | time }}
        </template>
        <template slot-scope="{ row }">
          <slot name="operator" :props="{ row }"></slot>
        </template>
        <!--路径-->
        <template slot="showPath" slot-scope="{row}">
          <span style="cursor: pointer;color: #0d92ff" @click="toshowpath(row)">{{ row.showPath }}</span>
        </template>
      </DataList>
    </div>
    <FileCardList
        v-show="shape"
        ref="Papers"
        :query="getQuery"
        @on-dblclick-row="dblclickRow"
        @on-selected-change="middleMethods"
    >
    </FileCardList>
    <FileVideoPlayer
      v-if="showVideo"
      :video="videoSrc"
    >
    </FileVideoPlayer>
  </div>
</template>

<script>
  import Clipboard from 'clipboard'
  import {downsPreview} from '@/api/file'
  import FileCardList from './FileCardList'
  import FileVideoPlayer from './FileVideoPlayer'

  export default {
  name: 'MainContent',
  inject: ['getLibraryIds'],
  components: {
    FileCardList,
    FileVideoPlayer,
  },
  props: {
    mainmenu: Array,
    breadcrumb: Array,
    general: Function,
    version: Function,
    sortType: Object
  },
  data () {
    return {
      shape: false, // 判断是列表形式还是文件夹形式 false代表列表形式
      url: '', // 路径
      radio: '列表', // 区分列表形式还是文件夹形式
      root: false, // 判断调用普通文件列表（false）或版本文件列表（true）接口
      files: {},
      imgCopyAddressUrl: '', // 复制地址
      showVideo: false,
      videoSrc: '',
    }
  },
  methods: {
    reload () {
      this.$nextTick(() => {
        let folderId = ''
        if (location.href.indexOf('folderId=') > -1) {
          folderId = location.href.split('folderId=')[1]
        } else {
          folderId = '-1'
        }
        const params = this.root ? this.files : { folderId: folderId }
        params.libraryId = this.getLibraryIds()
        this.shape ? this.$refs.Papers.load({ params }) : this.$refs.Table.load({ params })
      })
    },
    // 获取图片地址
    getImgUrl (img) {
      if (this.global.arrImgType.indexOf(img) === -1) {
        img = 'files'
      }
      if (img === 'webm' ||
        img === 'ogg' ||
        img === '3gp' ||
        img === 'avi' ||
        img === 'flv' ||
        img === 'mkv' ||
        img === 'mov' ||
        img === 'mpg' ||
        img === 'swf' ||
        img === 'ts' ||
        img === 'vob' ||
        img === 'mxf' ||
        img === 'rm') {
        return require('../../assets/img/mp4.png')
      }
      return require('../../assets/img/' + img + '.png')
    },

    radioChange (v) {
      this.shape = !this.shape
      this.reload()
    },
    toshowpath (i) {
      if (i.parentFolder === '-1') {
        this.$router.replace({ path: `myFile/${i.libraryId}` })
      } else {
        this.$router.replace({ path: `myFile/${i.libraryId}?folderId=${i.parentFolder}` })
      }
    },
    dblclickRow (v) {
      const {
        versions,
        fileName,
        folderId,
        fileShowPath,
        uploadFile,
        createFolder,
      } = v
      const params = {
        folderId: folderId, // 路径
        fileName: fileName,
        fileShowPath: fileShowPath,
        uploadFile: uploadFile,
        createFolder: createFolder,
      }
      this.files.fileName = fileName
      this.files.fileShowPath = fileShowPath
      if (versions === 1) {
        return false
      } else if (versions > 1) { // 当是版本列表时
        this.root = true
        this.reload()
      } else if (versions === 0) { // 当是文件夹时
        this.root = false
        this.breadcrumb.push(params)
        this.changeUrl(v.folderId)
        this.reload()
      }
    },

    copyAddress () { // 复制地址
      this.imgCopyAddressUrl = location.href
      var clipboard = new Clipboard('.imgCopyAddress')
      clipboard.on('success', e => {
        // 释放内存
        clipboard.destroy()
      })
    },

    changeUrl (folderId) {
      var href = window.location.href
      if (folderId === '-1') {
        history.pushState('', 'Title', href.split('?')[0])
      } else {
        if (href.indexOf('folderId') <= 0) {
          history.pushState('', 'Title', href + '?' + 'folderId=' + folderId)
        } else {
          history.pushState('', 'Title', href.split('?')[0] + '?' + 'folderId=' + folderId)
        }
      }
    },

    getRoute (name, i) { // 面包屑
      this.root = false
      if (i < this.breadcrumb.length - 1) {
        this.breadcrumb.splice(i + 1, this.breadcrumb.length - 1)
      }
      this.changeUrl(name.folderId)
      this.reload()
    },

    preview (row) {
      const fileType = row.fileType
      downsPreview(row.accessToken || row.fileId)
      if (fileType === 'mp4' || fileType === 'webm' ||
        fileType === 'ogg' ||
        fileType === '3gp' ||
        fileType === 'avi' ||
        fileType === 'flv' ||
        fileType === 'mkv' ||
        fileType === 'mov' ||
        fileType === 'mpg' ||
        fileType === 'swf' ||
        fileType === 'ts' ||
        fileType === 'vob' ||
        fileType === 'mxf' ||
        fileType === 'rm') {
        this.showVideo = true
        this.videoSrc.type = fileType
        this.videoSrc.src = downsPreview(row.accessToken || row.fileId)
      }
    },
    middleMethods (row) { // 当是文件时 单击预览文件，文件夹时 单击进入下一级
      if (row.fileType !== undefined) {
        return this.preview(row)
      } else {
        return this.dblclickRow(row)
      }
    }
  },
  computed: {
    getQuery () {
      return !this.root ? this.general() : this.version()
    },
  },
  mounted () {
    this.reload()
  }
}
</script>

<style scoped lang="less">
.demo-badge-alone {
  width: 5px;
  height: 5px;
}

.breads {
  background: #fff;
  display: flex;
  align-items: center;
  padding-left: 10px;
  flex: 1;
  position: relative;
  height: 30px;
  border-radius: 5px;

  .bread {
    cursor: pointer;
  }

  .imgCopyAddress {
    position: absolute;
    right: 10px;
    cursor: pointer;
  }
}

.iconstyle {
  margin-right: 10px;
  width: 30px;
  position: relative;
  top: 5px;
  cursor: pointer;
}

b {
  left: 20px;
  position: absolute;
  width: 15px;
  height: 15px;
  background: #e4393c;
  color: #fff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  bottom: 0px;
  z-index: 2;
}

.btnOp {
  width: 353px;
  display: flex;
  justify-content: space-between;
  margin-left: 10px;
  align-items: center;
}

.ivu-radio-group-button .ivu-radio-wrapper-checked:first-child {
  border-color: transparent;
  background: #999;
  color: #fff;
}

.ivu-radio-group-button .ivu-radio-wrapper-checked {
  background: #999;
  border-color: transparent;
  color: #fff;
  box-shadow: none;
}

.ivu-radio-group-button .ivu-radio-wrapper-checked.ivu-radio-focus:first-child {
  box-shadow: none;
}

.lists {
  margin: 16px;
  background: #fff;
  padding: 8px;
}

.ivu-radio-group-button .ivu-radio-wrapper-checked:before {
  background: transparent;
  opacity: .1;
}

.ivu-radio-group-button .ivu-radio-wrapper:after {
  height: 36px;
  left: -1px;
  top: -3px;
  background: transparent;
  opacity: 0;
}

.ivu-radio-group-button .ivu-radio-wrapper:hover {
  position: relative;
  color: #ddd;
}

.ivu-btn:hover {
  color: #747b8b;
  background-color: #fff;
  border-color: #e3e5e8;
}
</style>
