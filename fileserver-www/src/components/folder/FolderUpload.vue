<template>
  <ModalOperator
    label="上传"
    title="上传文件夹"
    :width="400"
    :disabled="disabled"
    :maskClosable="false"
    ref="modal"
  >
    <div slot="button">
      上传文件夹
    </div>
    <zpa-form ref="Form">
      <zpa-folder
        type="drag"
        :paramsData="paramsData"
        :uploadCompleteCb="uploadCompleteCb"
        ref="Upload"
        @reload="$emit('reload')"
        @loading="loading"
      />
    </zpa-form>
    <div slot="footer">
      <Button type="text"  size="large" @click.native="del">取消</Button>
      <Button type="primary"  size="large" :loading="modal_loading" @click.native="del">确定</Button>
    </div>
  </ModalOperator>
</template>

<script>
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
import zpaFolder from './zpaFolder'

export default {
  name: 'UploadFileOperator',
  inject: ['getLibraryIds'],
  components: {
    zpaFolder
  },
  mixins: [
    ModalOperatorMixin,
  ],

  props: {
    root: String,
    paramsData: Object,
    disableds: Boolean
  },

  data () {
    return {
      datas: {
        index: true,
      },
      modal_loading: false,
    }
  },
  computed: {
    // eslint-disable-next-line vue/no-dupe-keys
    disabled () {
      return this.root === undefined
    },
  },

  methods: {
    initialize () {
      this.data = {

      }
    },
    uploadCompleteCb () {
      this.$emit('uploadCompleteCb')
    },
    beforeOpen () {
      this.paramsData.libraryId = this.getLibraryIds()
    },
    onSubmit () {
      this.$refs.Upload.stopUpload()
    },
    onCancel () {
      this.$refs.Upload.stopUpload()
    },
    loading (i) {
      if (i) {
        this.modal_loading = true
      } else {
        this.modal_loading = false
      }
    },
    del () {
      this.$refs.modal.showModal = false
      this.modal_loading = false
    }
  },
}
</script>
<style>
  .ivu-btn:hover {
    border-color: transparent;
  }
  ivu-icon:focus{
    outline: 0;
  }
  .gray{
    filter: grayscale(1);
  }
</style>
