<template>
  <div v-if="showModal">
    <Modal
      :title=value.name
      width="500"
      v-model="showFls"
      @on-cancel="cancel"
      @on-ok="ok"
    >
      <div v-if="value.versions > 0" class="formDiv" style=" font-size: 16px;font-weight: 600;">
        <Icon type="md-help-circle" :size="30" color="#f90" />确认删除该文件么?
      </div>
      <div v-else>
        <Form ref="formCustom" :model="formCustom" :rules="ruleValidate" :label-width="125" style="margin-top:20px;">
          <FormItem :label="value.versions === '-1' ? '文件库名' : '文件夹名'" >
            <Input type="text" v-model="value.fileName" style="width:200px;margin-right:10px;" readonly />
            <Button type="info" @click="copy" class="tag-read" :data-clipboard-text=authcodesUrl>
              {{value.versions === '-1' ? '复制文件库名' : '复制文件夹名'}}
            </Button>
          </FormItem>
          <FormItem :label="value.versions === '-1' ? '请输入文件库名' : '请输入文件夹名'" prop="passwd">
            <Input type="text" v-model="formCustom.passwd" style="width:200px;" />
          </FormItem>
        </Form>
      </div>
    </Modal>
  </div>
</template>

<script>
  import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
  import {delectFile, deleteFolder, deletelibrary} from '@/api/file'
  import Clipboard from 'clipboard'

  export default {
  name: 'FileRemoveConfirm',

  mixins: [
    ModalOperatorMixin,
  ],
  data () {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入文件夹名称'))
      } else if (value !== this.value.fileName) {
        callback(new Error('文件夹名称不符'))
      } else {
        callback()
      }
    }
    return {
      showFls: false,
      showModal: false,
      formCustom: {
        passwd: '',
      },
      ruleValidate: {
        passwd: [
          { validator: validatePass, trigger: 'blur', required: true },
        ]
      },
      authcodesUrl: ''
    }
  },
  props: {
    value: Object
  },
  methods: {
    cancel () {
      this.showModal = false
    },
    toggle () {
      this.showModal = true
      this.$nextTick(() => {
        this.showFls = true
        if (this.value.versions === 0 || this.value.versions === '-1') {
          if (this.value.versions === '-1') {
            this.$Message.info('将删除该文件库中的所有文件,包括子目录中的所有文件!')
          } else {
            this.$Message.info('将删除所有文件,包括子目录中的所有文件!')
          }

          this.$refs.formCustom.resetFields()
        }
        // this.reload()
      })
    },
    ok () {
      if (this.value.versions > 0) {
        delectFile(this.value.accessToken)
          .then(res => { // 重新刷新列表 做删除操作即可
            this.$Message.success('删除成功!')
            this.$emit('loading', '')
          })
      } else {
        this.$refs.formCustom.validate((valid) => {
          if (valid) {
            if (this.value.versions === '-1') {
              deletelibrary(this.value.libraryId).then(res => {
                this.$Message.success('删除成功!')
                this.$emit('loading', '')
              })
            } else {
              deleteFolder(this.value.folderId)
                .then(res => { // 重新刷新列表 做删除操作即可
                  this.$Message.success('删除成功!')
                  this.$emit('loading', '')
                })
            }
          } else {
            this.formCustom.passwd = ''
          }
        })
      }
    },
    copy () {
      this.authcodesUrl = this.value.fileName
      var clipboard = new Clipboard('.tag-read')
      clipboard.on('success', e => {
        this.showSuccess = true
        // 释放内存
        clipboard.destroy()
      })
    }
  }

}
</script>
<style lang="less">
  .formDiv{
    margin:23px 0 17px 0;
    i{
      width: 108px;
      display: inline-block;
      text-align: right;
      font-style: normal;
      margin-right: 10px;
    }
  }
</style>
