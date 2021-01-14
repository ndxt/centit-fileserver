<template>
  <div v-if="showModal">
    <Modal
        title="分享"
        width="400"
        v-model="showFls"
        @on-cancel="cancel"
        @on-ok="ok"
    >
      <Button @click="createdLink" v-if="!isShow" type="primary">创建连接</Button>
      <div v-if="isShow" class="urlAuthcode">
        <div>
          <Input v-model="url" style="width:200px"/>
          <Button type="success" :data-clipboard-text=authcodesUrl @click="copy" class="tag-read">复制链接及提取码</Button>
          <p v-show="showSuccess">复制链接成功</p>
        </div>
        <div style="margin-top:8px;">
          提取码: <span>{{ authcodes }}</span>
        </div>
      </div>
    </Modal>

  </div>
</template>

<script>
import Clipboard from 'clipboard'
import { authCode } from '@/api/file'

export default {
  name: 'FileShareModal',
  data () {
    return {
      showFls: false,
      showModal: false,
      isShow: false, // 判断创建链接前 还是创建链接后
      authcodes: '',
      url: '',
      authcodesUrl: '',
      showSuccess: false
    }
  },
  props: {
    value: Object
  },
  methods: {
    authCode,
    close () {
      this.showModal = false
      this.isShow = false
      this.showSuccess = false
    },
    cancel () {
      this.close()
    },
    ok () {
      this.close()
    },
    toggle () {
      this.showModal = true
      this.$nextTick(() => {
        this.showFls = true
      })
    },
    createdLink () {
      if (this.value.accessToken) {
        authCode(this.value.accessToken)
          .then(res => {
            this.isShow = true
            this.authcodes = res.authcode
            this.url = location.href.split('/file/')[0] + '/file/sharePage' + '?' + res.uri.replace(/\//g, '&')
          })
      } else {
        authCode(this.value.fileId)
          .then(res => {
            this.isShow = true
            this.authcodes = res.authcode
            this.url = location.href.split('/file/')[0] + '/file/sharePage' + '?' + res.uri.replace(/\//g, '&')
          })
      }
    },
    copy () { // 复制链接，验证码
      this.authcodesUrl = '链接：' + this.url + '    ' + '提取码：' + this.authcodes
      var clipboard = new Clipboard('.tag-read')
      clipboard.on('success', e => {
        this.showSuccess = true
        // 释放内存
        clipboard.destroy()
      })
    }
  },
  watch: {
    value () {
      this.isShow = false
    }
  }
}
</script>

<style scoped lang="less">
.urlAuthcode {
  > div {
    position: relative;

    p {
      position: absolute;
      right: 61px;
      color: #3d9248;
    }
  }

  i {
    font-style: normal;
    display: inline-block;
    width: 50%;
    height: 27px;
    border: 1px solid #999;
    border-radius: 4px;
    line-height: 27px;
  }

  span {
    display: inline-block;
    border: 1px solid #999;
    border-radius: 4px;
    padding: 3px;
  }
}
</style>
