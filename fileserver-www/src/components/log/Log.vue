<template>
  <SearchLayout>
    <SearchBox slot="search" @search="search" @reset="reset">
      <zpa-select
        label="所属库"
        v-model="params.libraryIds"
        :values="libraryInfo"
        textField="libraryName"
        valueField="libraryId"
      />
      <zpa-date-range-input
        label="时间"
        v-model="params"
        start-field="optTimeBegin"
        end-field="optTimeEnd"
      />
    </SearchBox>
    <div class="zpa-column " style="width:100%">
    <DataList
      ref="Table"
      :columns="columns"
      :query="getlog"
      :border="false"
      size="large"
    >
    </DataList>
    </div>
  </SearchLayout>
</template>

<script>
import {mapState} from 'vuex'
import columns from './columns'
import {getlog} from '@/api/file'

export default {
  name: 'Log',
  data () {
    return {
      columns,
      params: {
        libraryIds: '',
        optTimeBegin: '',
        optTimeEnd: '',
      },
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
    getlog,
    search () {
      const params = this.params
      params.userCode = this.currentUser.userCode
      return this.$refs.Table.load({ params })
    },
    reset () {
      this.params.libraryIds = ''
      this.params.optTimeBegin = ''
      this.params.optTimeEnd = ''
      this.search()
    }
  },
  mounted () {
    this.search()
  }
}
</script>
