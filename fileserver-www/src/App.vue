<template>
  <transition name="fade">
    <router-view v-if="fullscreen"></router-view>
    <MainLayout id="app" v-else>
      <MainHeader slot="header" />
      <MainMenu slot="menu" />
      <router-view slot="content" />
    </MainLayout>
  </transition>
</template>

<script>
import MainLayout from './components/commons/MainLayout'
import MainHeader from './components/commons/MainHeader'
import MainMenu from './components/commons/MainMenu'

export default {
  name: 'App',

  components: {
    MainLayout,
    MainHeader,
    MainMenu,
  },
  computed: {
    fullscreen () {
      const meta = this.$route.meta || {}
      return !!meta.fullscreen
    },
  },
  methods: {
    changeLibrary (library) {
      this.library = library
    }
  }
}
</script>

<style lang="less">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-size: 12px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>

<style scoped>

</style>
