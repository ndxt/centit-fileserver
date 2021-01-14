export default [
  {
    path: '/collection',
    name: 'MyCollections',
    component: () => import(/* webpackChunkName: "collection" */ '../components/collection/Collection'),
  },
]
