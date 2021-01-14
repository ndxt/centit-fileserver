// vue.config.js
module.exports = {
  publicPath: '/file/',

  devServer: {
    disableHostCheck: true,
    proxy: {
      '/api': {
        target: 'http://ceshi.centit.com/platform',
        // target: 'http://192.168.134.6:11000/framework',
        // target: 'http://192.168.137.82:8888/framework',
        // pathRewrite: {
        //   '/api': '',
        // },
        cookiePathRewrite: {
          '/platform/': '/',
        },
        changeOrigin: false,
      },
    },
  },
}
