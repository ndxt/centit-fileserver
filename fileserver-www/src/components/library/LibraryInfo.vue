<template>
  <zpa-form ref="Form">
    <zpa-form-group>
      <zpa-text-input
          label="库名称"
          required
          v-model="params.libraryName"
          :labelWidth="labelWidth"
      />
     <!-- <zpa-select
          required
          label="库类别"
          v-model="params.libraryType"
          :values="libraryTypes"
          textField="unitName"
          valueField="unitCode"
          disabled
      />-->

      <UserSelect
          label="访问库人员"
          multiple
          v-model="params.fileLibraryAccesss"
          :labelWidth="labelWidth"
      />

      <zpa-text-input
          v-if="params.libraryType === 'P'"
          readonly
          label="库所属人员"
          v-model="params.ownUser"
          :labelWidth="labelWidth"
      />

      <zpa-select
          label="所属机构"
          v-model="params.ownUnit"
          :query="getunitpath"
          textField="unitName"
          valueField="unitCode"
          :labelWidth="labelWidth"
      />

      <zpa-radio-group
          :values="[{text: '是', value: 'T'}, {text: '否', value: 'F'}]"
          label="可创建文件夹"
          v-model="params.isCreateFolder"
          :labelWidth="labelWidth"
      />

      <zpa-radio-group
          :values="[{text: '是', value: 'T'}, {text: '否', value: 'F'}]"
          label="可上传文件"
          v-model="params.isUpload"
          :labelWidth="labelWidth"
      />
    </zpa-form-group>
  </zpa-form>
</template>

<script>
import { getunitpath } from '@/api/file'
import { mapState } from 'vuex'

export default {
  name: 'AddDepot',
  data () {
    return {
      params: {
        fileLibraryAccesss: [],
        isCreateFolder: 'T',
        isUpload: 'T',
        libraryName: '',
        libraryType: 'I',
        ownUnit: '',
        ownUser: '',
      },
      libraryTypes: [
        { unitCode: 'I', unitName: '项目' },
        { unitCode: 'O', unitName: '机构' },
        { unitCode: 'P', unitName: '个人' }
      ],
      labelWidth: 100
    }
  },
  computed: {
    ...mapState('core', {
      currentUser: 'userInfo',
    }),
    ...mapState({
      libraryInfo: 'libraryInfo',
    }),
  },
  methods: {
    getunitpath,
    validate () {
      return this.$refs.Form.validate()
    },
  },
  mounted () {
    this.params.ownUser = this.currentUser.userCode
    this.params.fileLibraryAccesss.push(this.currentUser.userCode)
  }
}
</script>

<style scoped>

</style>
