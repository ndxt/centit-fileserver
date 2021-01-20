<template>
  <div class="over" >
    <div class="flexs" v-for="(i,key) in data" :key="key">
      <div>
        <span>{{i.userCode}}</span>
        <i>{{i.optMethodText}}</i>
      </div>
      <div>
        {{i.optTime}}
      </div>
    </div>
  </div>
</template>

<script>
import { log } from '@/api/file'

export default {
  name: 'FileLog',

  props: {
    value: String,
  },
  data () {
    return {
      data: []
    }
  },
  methods: {
    log,
    reload () {
      log(this.value)
        .then(res => {
          this.data = res
        })
    },
  },
  mounted () {
    this.reload()
  },
  watch: {
    value () {
      this.reload()
    },
  },
}
</script>
<style lang="less">
  .over{
    padding:20px 25px;
    overflow-y: auto;
    max-height:500px;
  }
  .flexs{
    display:flex;
    justify-content: space-between;
    line-height:30px;
    div{
      display: inline-block;
      color:#C4C4C5;
      span{
        color:#3DA1BE;
        display: inline-block;
        min-width:80px;
      }
      i{
        font-style: normal;
        color:#8C8D8F;
        margin-left: 20px;
      }
    }
  }
</style>
