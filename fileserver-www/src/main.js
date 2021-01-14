import Vue from 'vue'
import apiCore from '@centit/api-core'

import iView from 'view-design'
import 'view-design/dist/styles/iview.css'

import Layout from '@centit/ui-layout'
import '@centit/ui-layout/dist/ui-layout.css'

import Form from '@centit/ui-form'
import '@centit/ui-form/dist/ui-form.css'

import Admin from '@centit/ui-admin'
import '@centit/ui-admin/dist/ui-admin.css'

import App from './App.vue'
import router from './router'
import store from './store'
import './filters'
import './global'

import VideoPlayer from 'vue-video-player'
require('video.js/dist/video-js.css')
require('vue-video-player/src/custom-theme.css')
Vue.use(VideoPlayer)

Vue.config.productionTip = false

apiCore.setBaseURL('/api')

Vue.use(iView)
Vue.use(Layout)
Vue.use(Form)
Vue.use(Admin)

new Vue({
  provide () {
    return {
      $Row: this,
    }
  },
  router,
  store,
  render: h => h(App)
}).$mount('#app')
