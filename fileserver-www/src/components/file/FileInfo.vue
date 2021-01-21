<template>
  <zpa-form>
    <zpa-form-group style="position: relative">

      <zpa-text-input
        label="文件名"
        v-model="inputVal"
        :readonly="readonly"
        :span="8"
        @input="onInputChange"
      />
      <Button
        type="info"
        @click="reset"
        style="    position: relative;
    top: 8px;"
      >重命名
      </Button>
    </zpa-form-group>
    <div class="flex">
      <div v-for="(i,key) in value" :key="key">

        <span class="names">{{ i.name }}</span>&nbsp;&nbsp;
        <span class="values">{{ i.value }}</span>
      </div>

    </div>
  </zpa-form>
</template>

<script>
export default {
  name: 'FileInfo',
  data () {
    return {
      dataInfo: [],
      readonly: true,
      inputVal: ''
    }
  },
  props: {
    value: Array,
    fileName: String
  },
  methods: {
    reset () {
      this.readonly = false
    },
    onInputChange (e) {
      this.$emit('inputVal', this.inputVal)
    },
    reload () {}
  },
  mounted () {
    this.inputVal = this.fileName
  },
}
</script>
<style lang="less">
  .flex {
    padding: 17px 25px 0 17px;;

    div {
      line-height: 30px;
      display: inline-block;
      width: 50%;

      span {
        display: inline-block;
        color: #C4C4C5;
      }

      span.names {
        min-width: 100px;
        color: #A5A6A8;
      }
    }
  }
</style>
