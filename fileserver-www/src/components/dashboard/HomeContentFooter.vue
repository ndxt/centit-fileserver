<template>
  <zpa-row>
    <zpa-column>
      <Card>
        <div class="zpa-row info">
          <div class="zpa-column icon">
            <div class="colorbox">
              <div class="colorbox">
                <Icon type="ios-cloud-done-outline"  color="#4ABCA1" size="52"/>
              </div>
            </div>
          </div>
          <h3 class="zpa-column text">我的上传</h3>
        </div>
        <div class="zpa-row number">{{ upload }}</div>
      </Card>
    </zpa-column>
    <zpa-column>
      <Card :to="{name: '/MyCollections'}">
        <div class="zpa-row info">
          <div class="zpa-column icon">
            <div class="colorbox">
              <div class="colorbox">
                <Icon type="ios-heart-outline"   color="#4ABCA1" size="52"/>
              </div>
            </div>

          </div>

          <h3 class="zpa-column text">我的收藏</h3>
        </div>
        <div class="zpa-row number">{{ collections }}</div>
      </Card>
    </zpa-column>
    <zpa-column>
      <Card>

        <div class="zpa-row info">
          <div class="zpa-column icon">
            <div class="colorbox">
              <div class="colorbox">
                <Icon  type="ios-cloud-download-outline"  color="#4ABCA1" size="52"/>
              </div>
            </div>

          </div>

          <h3 class="zpa-column text">我的下载</h3>
        </div>
        <div class="zpa-row number">{{ download}}</div>

      </Card>
    </zpa-column>
    <zpa-column>
      <Card>
        <div class="zpa-row info">
          <div class="zpa-column icon">
            <div class="colorbox">
              <div class="colorbox">
                <Icon  type="ios-git-branch"  color="#4ABCA1" size="52"/>
              </div>
            </div>

          </div>

          <h3 class="zpa-column text">我的分享</h3>
        </div>
        <div class="zpa-row number">{{ share }}</div>
      </Card>
    </zpa-column>
  </zpa-row>
</template>

<script>
export default {
  name: 'HomeContentFooter',
  props: {
    value: Array
  },
  data () {
    return {
      share: 0,
      collections: 0,
      download: 5,
      upload: 0
    }
  },
  mounted () {
  },
  watch: {
    value (res) {
      for (var i = 0; i < res.length; i++) {
        if (res[i].optMethod === '分享') {
          this.share = this.share + 1
        } if (res[i].optMethod === '收藏') {
          this.collections = this.collections + 1
        } if (res[i].optMethod === '下载') {
          this.download = this.download + 1
        } if (res[i].optMethod === '上传') {
          this.upload = this.upload + 1
        }
      }
    }
  },
  components: {}
}
</script>

<style scoped lang="less">
.number{
  justify-content:flex-end;
  color:#4ABCA1;
  font-size: 40px;
  font-style: italic;
}
.info{
  height: 180px;
  .icon,.text{
    margin: auto auto;
    height: 130px;
    line-height: 130px;
    text-align: center;
    position: relative;
  }
  .text{
    text-align: right;
  }

  .colorbox{
    position: absolute;
    width: 130px;
    height: 130px;
    background: #F6FAFB;
    top: 20px;
    left: 20px;
  }
}
.ivu-card{
  height: 300px;
}
.ivu-card-body{
  padding-right: 30px;
}
</style>
