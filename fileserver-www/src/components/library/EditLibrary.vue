<template>
  <ModalOperator title="创建库" width="700" style="width: 100%;">
    <template slot="button">
      <slot></slot>
    </template>
    <LibraryInfo
        ref="LibraryInfo"
    ></LibraryInfo>
  </ModalOperator>
</template>

<script>
import LibraryInfo from './LibraryInfo'
import { updatelibraryr, seeLibrary } from '@/api/file'
import ModalOperatorMixin from '@centit/ui-admin/src/components/ModalOperatorMixin'
export default {
  name: 'EditLibraryOperator',

  components: {
    LibraryInfo,
  },

  mixins: [ModalOperatorMixin],

  data () {
    return {
      params: {},
    }
  },

  props: {
    value: Object,
  },

  methods: {
    beforeOpen () {
      if (this.value && this.value.libraryId) {
        return seeLibrary(this.value.libraryId)
      }
    },

    beforeSubmit () {
      return this.$refs.LibraryInfo.validate()
    },

    onSubmit () {
      this.params = this.$refs.LibraryInfo.params
      return updatelibraryr(this.params)
    },
  }
}
</script>
