<template>
    <div>
      <Modal
        title="验证码"
        width="400"
        v-model="showFls"
        @on-ok="ok"
      >
        <zpa-form ref="Form">
          <zpa-text-input :span="12" label="请输入验证码" v-model="params.authCode" :labelWidth="110"/>
        </zpa-form>
      </Modal>
      <div v-if="!showFls">
        <DataList
          ref="Table"
          :columns="columns"
          isHidePage
          :query="getQuery"
          :border="false"
          size="large"
        >
          <!--文件名-->
          <template slot="fileName" slot-scope="{row}">
            <div style="position: relative;cursor: pointer" @click="middleMethods(row)">
              <img :src="getImgUrl(row.fileType)" alt="" class="iconstyle" >
              <span :title="row.fileName || row.folderName ||'未知名'">{{row.fileName || row.folderName ||'未知名'}}</span>
            </div>

          </template>
          <!--上传时间-->
          <template slot="createTime" slot-scope="{row}">
            {{row.createTime | timeType}}
          </template>
          <!--操作栏-->
          <template  slot-scope="props" class="opreator">
            <span  class="colorDown" @click="onClickItem('下载文件',props.row)">
              下载
            </span><!--文件夹无下载-->
          </template>
        </DataList>
      </div>
    </div>
</template>

<script>
import { checkAuth, downs, downsPreview } from '@/api/file'
import columns from '../my/columns'

export default {
  name: 'sharePage',
  data () {
    return {
      params: {
        fileId: '',
        authCode: ''
      },
      showFls: true,
      columns,
      datas: [],
    }
  },
  methods: {
    checkAuth,
    reload () {
      const arr = location.href.split('?')[1].split('&')
      this.params.fileId = arr[1]
      checkAuth(this.params)
        .then(res => {
          this.showFls = false
          this.datas.push(res)
          this.$refs.Table.load()
        })
    },
    ok () {
      if (this.params.authCode !== '') {
        this.reload()
      }
    },
    getQuery () {
      return this.datas
    },
    preview (row) {
      const fileType = row.fileType
      if (this.global.arrImgType.indexOf(fileType) !== -1) {
        downsPreview(row.accessToken || row.fileId)
      }
    },
    middleMethods (row) { // 当是文件时 单击预览文件，文件夹时 单击进入下一级
      if (row.fileType !== undefined) {
        return this.preview(row, this.params.authCode)
      } else {
        return this.dblclickRow(row)
      }
    },
    // 获取图片地址
    getImgUrl (img) {
      if (this.global.arrImgType.indexOf(img) === -1) {
        img = 'files'
      }
      return require('../../assets/img/' + img + '.png')
    },
    onClickItem (i, row) {
      console.log(i, row)
      downs(row.fileId, this.params.authCode)
    },
  }
}
</script>

<style scoped>
  .iconstyle{
    margin-right: 10px;
    width: 30px;
    position: relative;
    top: 5px;
    cursor: pointer;
  }
  .colorDown{
    cursor: pointer;
    color:#3B96C7;
  }
</style>
