export default [
  {
    path: '/Search/:libraryIds/:query',
    name: 'Search',
    component: () => import(/* webpackChunkName: "search" */ '../components/search/Search'),
  }
]
