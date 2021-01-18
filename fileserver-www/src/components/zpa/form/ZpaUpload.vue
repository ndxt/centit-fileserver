<template>
  <zpa-column height="auto">
    <Upload
      action="/"
      ref="Upload"
      type="drag"
      multiple
      :on-success="uploadCompleteCb"
      :showUploadList="false"
      :beforeUpload="beforeUpload"
    >
      <template v-if="small==true">
        <Button style="width:100%;" icon="ios-cloud-upload-outline">Upload files</Button>
      </template>
      <template v-if="small==false">
        <Button style="width:100%;" v-if="btnText !== ''">{{btnText}}</Button>
        <div v-else style="padding: 20px 0">
          <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
          <p>点击或者拖拽文件到方框内上传</p>
        </div>
      </template>
    </Upload>
    <ul class="zpa-column no-fit" style="width: 100%;">
      <li
        style="margin-top: 8px; width: 100%;"
        class="zpa-row no-fit"
        v-for="(up, index) in uploads"
        :key="index"
      >
        <img width="64" height="64" v-if="up.isImage" :src="up.url" :alt="up.name" />
        <zpa-column no-gutter padding="0 8px" style="overflow: hidden">
          <zpa-row middle>
            <Tooltip
              style="margin-right: 20px;"
              class="name zpa-row ellipsis"
              :content="up.name"
              :transfer="true"
            >
              <p>{{up.name}}</p>
            </Tooltip>
            <span class="size">{{up.size}}</span>
          </zpa-row>

          <zpa-row no-gutter middle>
            <zpa-row>
              <i-progress :storke-width="4" :percent="up.percentage" :status="up.status"></i-progress>
            </zpa-row>
            <!--<span>
              <Tooltip
                v-if="up.status === 'uploading' && up.isPaused"
                :transfer="true"
                content="上传"
              >
                <Icon type="ios-play" :size="28" color="#999"></Icon>
              </Tooltip>

              <Tooltip
                v-else-if="up.status === 'uploading' && !up.isPaused"
                :transfer="true"
                content="暂停"
              >
                <Icon type="ios-pause" :size="28" color="#999"></Icon>
              </Tooltip>

              <Tooltip v-if="up.status === 'wrong'" :transfer="true" content="刷新">
                <Icon type="ios-refresh" :size="28" color="#999"></Icon>
              </Tooltip>

              <Tooltip v-if="up.status === 'success'" :transfer="true" content="删除">
                <Icon type="ios-trash" :size="28" color="#999"></Icon>
              </Tooltip>

              <Tooltip v-else :transfer="true" content="取消">
                <Icon type="ios-close" :size="28" color="#999"></Icon>
              </Tooltip>
            </span>-->
          </zpa-row>
        </zpa-column>
      </li>
    </ul>
  </zpa-column>
</template>

<script>
  import Uploader from './uploader'

  export default {
  name: 'zpaUpload',

  data () {
    return {
      uploads: [],
      $Upload: ''
    }
  },

  props: {
    small: {
      default: false,
      type: Boolean
    },
    params: Object,
    btnText: {
      default: '',
      type: String
    },
    uploadCompleteCb: Function,
  },
  methods: {
    beforeUpload (file) {
      this.upload(file)

      return false
    },

    async upload (file) {
      const upload = new Uploader(file, this.params, this.uploadCompleteCb)
      this.$Upload = upload
      const uploads = this.$refs.Upload.fileList

      uploads.unshift(upload)
      this.uploads = uploads

      await upload.start()

      this.$nextTick(() => {
        this.$root.$emit('reload')
      })
    },

  }
}
</script>
