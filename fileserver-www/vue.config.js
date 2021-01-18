// vue.config.js

const build = {
  '/api': {
    target: 'http://ceshi.centit.com/platform',
    cookiePathRewrite: {
      '/platform': '/api/',
    },
  },
}

const dev = {
  '/api/admin': {
    target: 'http://192.168.134.7:18000/fileserver',
    pathRewrite: {
      '/api/admin': '',
    },
    cookiePathRewrite: {
      '/fileserver/': '/api',
    },
  },
  '/api/file': {
    target: 'http://192.168.134.7:18000/fileserver',
    pathRewrite: {
      '/api/file': '',
    },
  }
}

module.exports = {
  publicPath: '/file',

  devServer: {
    disableHostCheck: true,
    proxy: {
      ...dev,
    },
  },
}
