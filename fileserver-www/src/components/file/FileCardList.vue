<template>
  <zpa-column @contextmenu.native="e => onVisibleChange(e)" @click.native="e => onVisibleChange(e)">

    <slot name="fab"/>
    <div class="allfile">
      <zpa-column class="">
        <p class="title">
          全部文件
          <span>已全部加载，共{{data.length}}个</span>
        </p>

      </zpa-column>
    <zpa-column ref="PapersContainer" :style="papersStyle">
      <div
        class="item"
        v-for="v in data"
        :key="v.name"
        :style="itemStyle(v)"
        @click="changeCurrent(v)"
        @dblclick="dblclickRow(v)"
        @contextmenu="e => onVisibleChange(e, v.accessToken || v.folderId || v.favoriteId)"
      >

        <Dropdown
          :ref="v.accessToken || v.folderId"
          trigger="custom"
          :visible="isFolderId === (v.accessToken || v.folderId || v.favoriteId) ? true : false"
          style="margin-left: 20px">
          <div style="position: relative" >
            <b v-if="v.versions > 1">{{v.versions}}</b>
            <img :src="v.versions === 0 ? getImgUrl('folder') : getImgUrl(v.fileType)" alt="" class="iconstyle" >
            <span class="names">{{v.fileName || v.folderName || '未知名'}}</span>
          </div>

          <template v-if="v.versions">
            <DropdownMenu slot="list">
              <DropdownItem v-for="(item,index) in handle" :key="index" @click.native.stop="onClickItem(item,v)">{{item}}</DropdownItem>
              <DropdownItem v-for="(i,key) in v.handle" :key="'info1' + key" @click.native.stop="onClickItem(i,v)">
                {{i}}
              </DropdownItem>
            </DropdownMenu>
          </template>
          <template v-else>
            <DropdownMenu slot="list">
              <DropdownItem v-for="(item,index) in handleFolder" :key="index" @click.native.stop="onClickItem(item,v)">{{item}}</DropdownItem>
              <DropdownItem v-for="(i,key) in v.handle" :key="'info2' + key" @click.native.stop="onClickItem(i,v)">
                {{i}}
              </DropdownItem>
            </DropdownMenu>
          </template>
        </Dropdown>
        </div>
    </zpa-column>

    <Spin v-if="isLoading" size="large" fix></Spin>
    </div>
  </zpa-column>
</template>

<script>
const defaultSort = {
  sort: 'updateDate',
  order: 'desc',
}

export default {
  name: 'FileCardList',

  data () {
    return {
      isLoading: false,
      data: [],
      params: {},
      sort: Object.assign({}, defaultSort),
      currentSelect: '',
      handle: ['详情', '下载文件'],
      handleFolder: ['详情'],
      timer: null,
      isFolderId: '',
    }
  },
  props: {
    query: {
      type: Function,
      require: true,
    },
  },

  computed: {
    papersStyle () {
      return {
        flexDirection: 'row',
        flexWrap: 'wrap',
        alignItems: 'start',
        alignContent: 'start',
        /* overflow: 'auto', */
      }
    },
  },

  methods: {
    itemStyle (data) {
      /* if (this.currentSelect === data.accessToken) {
        return {
          borderColor: '#57a3f3',
          background: '#fff9d1',
        }
      } */
    },

    async load ({ params = this.params, sort = this.sort } = {}) {
      this.data = []
      this.currentSelect = ''
      this.isLoading = true

      this.params = params

      // TODO data 的获取改为可配置
      this.data = await this.query({ params, sort })
      this.isLoading = false
    },

    // changeSort({key: sort, order}) {
    //   this.sort = {
    //     sort,
    //     order,
    //   }
    //
    //   // 默认排序
    //   if (!order || order === 'normal') {
    //     this.sort = Object.assign({}, defaultSort)
    //   }
    //   return this.load()
    // },

    changeCurrent (data) { // 单击
      clearTimeout(this.timer)
      const that = this// 首先要清除定时器
      this.timer = setTimeout(function () {
        that.$emit('on-selected-change', data)
      }, 200) // 定时器时间
    },

    dblclickRow (data) {
      clearTimeout(this.timer) // 清除定时器，然后在后面直接写双击事件的逻辑
      this.$emit('on-dblclick-row', data)
    },

    onClickItem (i, row) {
      this.$parent.$parent.onClickItem(i, row)
    },

    // 获取图片地址
    getImgUrl (img) {
      const arr = ['docx', 'exe', 'exel', 'folder', 'P', 'txt', 'xlsx', 'xml', 'png']
      if (arr.indexOf(img) === -1) {
        img = 'files'
      }
      return require('../../assets/img/' + img + '.png')
    },

    onVisibleChange (e, i) {
      e.preventDefault()
      e.stopPropagation()
      this.isFolderId = i
    },

  },
}
</script>

<style scoped lang="less">
  .item {
    width: 100px;
    margin: 5px;
    padding: 15px 25px 15px 5px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    border: 1px solid rgba(0, 0, 0, 0);
    border-radius: 3px;
    transition: all .2s ease-in-out;
    position: relative;
    &:hover {
      background: #ebf7ff;
    }
    b{
      right: 5px;
      position: absolute;
      width: 18px;
      height: 18px;
      background: #e4393c;
      color: #fff;
      border-radius: 50%;
      z-index:2;
      bottom: 21px;
    }
    span {
      /*white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 110%;*/
    }
  }
  .names{
    padding-top: 7px;
    display: inline-block;
  }
  .allfile{
    height: 100%;
    background: #fff;
    margin: 10px;
    border: 1px solid #dcdee2;
    .title{
      padding: 5px 22px;
      font-size: 15px;
      border-bottom: 1px solid #dcdee2;
      &:before{
        position: absolute;
        display:block;
        content:'';
        width:5px;
        height: 20px;
        left: 15px;
        top:13px;
        background: linear-gradient(#3BB59C, #3D9AC6);
      }
      span{
        color:#999999;
        font-size:14px;
        margin-left:10px;

      }
    }

  }
  .iconstyle{
    width: 70px;
    position: relative;
    top: 5px;
    cursor: pointer;
  }
</style>
