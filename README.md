# 如何启动工程

## 后台启动

## 前台启动

[文件服务器前台](http://gitlab.centit.com/gitlab/ctm/products/centit-fileserver/-/tree/master/fileserver-www) 基于Vue的项目，默认已经配置好了公用后台地址，可以直接启动访问。

### 安装依赖
```
npm install
```
_注意：如果 npm 使用 taobao 源或者 cnpm 下载可能会出现找不到模块的报错，请使用 npm 官方源下载_


### 开发
```
npm run serve
```

See [Configuration Reference](https://cli.vuejs.org/config/).

### 如何修改后台地址

修改 [vue.config.js](http://gitlab.centit.com/gitlab/ctm/products/centit-fileserver/-/blob/master/fileserver-www/vue.config.js)
需要添加代理：

``` javascript

devServer: {
    proxy: {
          '/api/admin': {
                target: 'http://localhost:8888/fileserver',
                pathRewrite: {
                    '/api/admin': '',
                },
                cookiePathRewrite: {
                  '/fileserver/': '/api',
                },
          },
          '/api/file': {
                target: 'http://localhost:8888/fileserver',
                pathRewrite: {
                    '/api/file': '',
                },
          },
    }
}
```

重新启动即可

