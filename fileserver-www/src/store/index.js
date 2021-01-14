import Vue from 'vue'
import Vuex from 'vuex'
import core from '@centit/module-core/src/store/modules/core'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    isCollapsed: false,
    libraryInfo: [],
    currentUser: '',
  },
  mutations: {
    setLibraryInfo (state, libraryInfo) {
      state.libraryInfo = libraryInfo || []
    },
    setCurrentUser (state, currentUser) {
      state.currentUser = currentUser
    },
    setisCollapsed (state, isCollapsed) {
      state.isCollapsed = isCollapsed
    },
  },
  actions: {
  },
  modules: {
    core,
  }
})
