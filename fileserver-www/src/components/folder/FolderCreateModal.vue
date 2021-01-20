<template>
    <div >
      <ModalOperator title="新建文件夹"  width="500" ref="Modal">
        <div slot="button">
          <Button
            style="background: #F7B700;color:#fff;"
            :class="[!disable ? 'gray' : '']"
            :disabled="!disable" >新建文件夹</Button>
        </div>
        <zpa-form ref="Form">
          <zpa-form-group>
            <zpa-text-input :span="12" label="文件夹名称" v-model="params.folderName" />
            <zpa-radio-group
              label="是否可以创建文件夹"
              v-model="params.isCreateFolder"
              :values="isCreate"
              :span="12"
              :labelWidth="labelWidth"
            />
            <zpa-radio-group
              label="是否可以上传文件"
              v-model="params.isUpload"
              :values="isUpload"
              :span="12"
              :labelWidth="labelWidth"
            />
          </zpa-form-group>
        </zpa-form>
      </ModalOperator>
    </div>
</template>

<script>
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
import { folderNew } from '@/api/file'

export default {
  name: 'FolderCreateModal',
  inject: ['getLibraryIds'],
  mixins: [
    ModalOperatorMixin,
  ],
  props: {
    disable: Boolean
  },
  data () {
    return {
      params: {
        folderName: '',
        folderPath: '/-1', // 地址
        isCreateFolder: 'T',
        isUpload: 'T'
      },
      labelWidth: 150,
      isCreate: [
        {
          value: 'T',
          text: '是',
        },
        {
          value: 'F',
          text: '否',
        }
      ],
      isUpload: [
        {
          value: 'T',
          text: '是',
        },
        {
          value: 'F',
          text: '否',
        }
      ]
    }
  },

  methods: {
    folderNew,
    onSubmit () {
      const breadcrumb = this.$parent.$parent.breadcrumb
      if (breadcrumb[breadcrumb.length - 1].folderId === '-1') { // 当是第一级的时
        this.params.folderPath = breadcrumb[breadcrumb.length - 1].fileShowPath
      } else {
        this.params.folderPath = breadcrumb[breadcrumb.length - 1].fileShowPath + '/' + breadcrumb[breadcrumb.length - 1].folderId
      }
      this.params.libraryId = this.getLibraryIds()
      folderNew(this.params)
        .then(res => {
          if (res.msg === '100文件夹已存在') {
            this.$Message.success('该文件夹已存在')
          } else {
            this.params.folderName = ''
            this.$parent.$parent.reload()
          }
        })
    },
  }
}
</script>

<style scoped>
  .ivu-btn:hover {
    border-color: transparent;
  }
  .gray{
    filter: grayscale(1);
  }
</style>
