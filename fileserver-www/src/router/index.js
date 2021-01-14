import Vue from 'vue'
import VueRouter from 'vue-router'
import {
  checkLogin,
} from '@centit/module-core/src/router/login'
import store from '../store'
import dashboard from './dashboard'
import log from './log'
import collection from './collection'
import search from './search'

Vue.use(VueRouter)

const routes = [
  ...dashboard,
  ...log,
  ...collection,
  ...search,
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* webpackChunkName: "login" */ '../views/Login.vue'),
    meta: {
      fullscreen: true,
      needLogin: false,
    },
  },

  {
    path: '/myFile/:libraryId',
    name: 'myFile',
    component: () => import(/* webpackChunkName: "files" */ '../components/my/MyFile'),
    meta: {
      title: '文件夹列表',
    },
  },

  {
    path: '/sharePage',
    name: 'sharePage',
    component: () => import(/* webpackChunkName: "files" */ '../components/file/FileSharePage'),
    meta: {
      fullscreen: true,
      title: '分享文件',
    },
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  checkLogin({ router, to, from, store }, false)
    .then(result => {
      if (result) next()
    })
})

export default router
