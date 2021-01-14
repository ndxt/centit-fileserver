<template>
  <div>
    <zpa-form ref="Form">
      <zpa-form-group style="position: relative">

        <zpa-text-input
            label="文件夹名称"
            v-model="inputVal"
            :readonly="readonly"
        />
        <Button
            type="info"
            @click="reset"
            style="position: absolute;
    right: -99px;
    top: 8px;"
        >重命名
        </Button>
        <zpa-radio-group
            label="是否可以创建文件夹"
            v-model="value.createFolder"
            :values="isCreate"
            :span="12"
            :labelWidth="labelWidth"
            :readonly="false"
        />
        <zpa-radio-group
            label="是否可以上传文件"
            v-model="value.uploadFile"
            :values="isUpload"
            :span="12"
            :labelWidth="labelWidth"
            :readonly="false"
        />
      </zpa-form-group>
    </zpa-form>
  </div>
</template>

<script>
export default {
  name: 'FolderInfo',
  props: {
    value: Object
  },
  data () {
    return {
      labelWidth: 150,
      readonly: true,
      inputVal: '',
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
    reset () {
      this.readonly = false
    }
  },
  mounted () {
    this.inputVal = this.value.fileName
  }

}
</script>

<style scoped lang="less">
.ivu-btn:hover {
  border-color: transparent;
}

div {
  line-height: 30px;
  display: inline-block;
  width: 50%;

  span {
    display: inline-block;
    color: #515a6e;
  }

  span.names {
    min-width: 120px;
    color: #515a6e;
    display: inline-block;
    text-align: right;
  }
}
</style>
