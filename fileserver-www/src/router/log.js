export default [
  {
    path: '/log',
    name: 'Log',
    component: () => import(/* webpackChunkName: "log" */ '../components/log/Log'),
  },
]
