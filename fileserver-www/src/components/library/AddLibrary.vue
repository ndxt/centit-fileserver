<template>
  <ModalOperator title="创建项目库" width="700" style="width: 100%;">
    <template slot="button">
      <slot></slot>
    </template>
    <LibraryInfo ref="LibraryInfo"></LibraryInfo>
  </ModalOperator>
</template>

<script>
import LibraryInfo from './LibraryInfo'
import {addLibrary} from '@/api/file'
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'

export default {
  name: 'AddLibraryOperator',

  components: {
    LibraryInfo,
  },

  mixins: [ModalOperatorMixin],

  methods: {
    beforeSubmit () {
      return this.$refs.LibraryInfo.validate()
    },

    onSubmit () {
      this.params = this.$refs.LibraryInfo.params
      return addLibrary(this.params)
    },
  }
}
</script>
