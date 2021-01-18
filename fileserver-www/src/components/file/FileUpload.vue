<template>
  <ModalOperator
    label="上传"
    title="上传文件"
    :width="400"
    :disabled="disabled"
    :maskClosable="false"
    >
    <div slot="button">
      上传文件
    </div>
    <zpa-form ref="Form">
      <zpa-upload
        type="drag"
        :params="paramsData"
        :uploadCompleteCb="uploadCompleteCb"
        ref="Upload"

      />
    </zpa-form>
  </ModalOperator>
</template>

<script>
  import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
  import zpaUpload from '../zpa/form/ZpaUpload'

  export default {
  name: 'UploadFileOperator',
  inject: ['getLibraryIds'],
  mixins: [
    ModalOperatorMixin,
  ],
  components: {
    zpaUpload
  },
  props: {
    root: String,
    paramsData: Object,
  },
  computed: {
    disabled () {
      return this.root === undefined
    },
  },

  methods: {
    initialize () {
    },
    uploadCompleteCb () {
      this.$emit('uploadCompleteCb')
    },
    beforeOpen () {
      this.paramsData.libraryId = this.getLibraryIds()
    },
    onSubmit () {
      this.$refs.Upload.$Upload.stop()
    },
    onCancel () {
      this.$refs.Upload.$Upload.stop()
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
