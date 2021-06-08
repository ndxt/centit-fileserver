import apiFactory, { createUrlParams } from '@centit/api-core'
import $ from 'jquery'
const api = apiFactory.create('file', { useFormData: true })
const apis = apiFactory.create('file', { useFormData: false })

export function folder (params) {
  let libraryId = ''
  let folderId = ''
  if (params.params) {
    libraryId = params.params.libraryId
    folderId = params.params.folderId
  } else {
    libraryId = params.libraryId
    folderId = params.folderId
  }
  return api.get(`/fileserver/folder/${libraryId}/${folderId}`)
    .then(data => {
      const result = []
      data.forEach(v => {
        if (v.folder) { // 文件夹
          const handle = ['复制到', '移动到', '删除']
          v.accessToken = v.folderId
          v.handle = handle
          result.push(v)
        } else { // 文件
          let handle
          if (v.favoriteId) {
            handle = ['分享', '复制到', '移动到', '删除']
          } else {
            handle = ['分享', '收藏', '复制到', '移动到', '删除']
          }
          if (v.versions > 1) {
            handle.push('查看版本')
          }
          v.handle = handle
          result.push(v)
        }
      })
      return result
    })
}

/**
 * 新建文件夹
 */
export function folderNew (params) {
  return apis.post('/fileserver/folder', params)
}

/**
 * 查看单个文件夹信息
 */
export function lookFolder (floderId) {
  return api.get(`/fileserver/folder/${floderId}`)
    .then(res => {
      const result = [
        { name: '文件夹名称', feld: 'folderName' },
        { name: '文件夹Id', feld: 'folderId' },
        { name: '上级文件夹', feld: 'parentFolder' },
        { name: '文件夹路径', feld: 'folderPath' },
        { name: '是否可以创建子文件夹', feld: 'isCreateFolder' },
        { name: '是否可以上传文件', feld: 'isUpload' },
      ]
      result.forEach(v => {
        v.value = res.data[v.feld]
      })

      return result
    })
}
/**
 * 新建库
 */
export function addLibrary (library) {
  const { fileLibraryAccesss } = library
  if (fileLibraryAccesss) {
    library.fileLibraryAccesss = fileLibraryAccesss.map(userCode => {
      return {
        accessUsercode: userCode,
      }
    })
  }

  return apis.post('/fileserver/library', library
  )
}
/**
 * 删除单个文件夹信息
 */
export function deleteFolder (floderId) {
  return api.delete(`/fileserver/folder/${floderId}`)
}
/**
 * 新增文件收藏
 */
export function newfavorite (params) {
  return apis.post('/fileserver/favorite', params)
}
/**
 * 获取收藏列表
 */

export function GetFavoriteList ({ params = {}, sort = {} } = {}) {
  return api.get('/fileserver/favorite', {
    params: {
      ...params,
      ...sort,
    },
  }).then(({ objList }) => {
    const result = []
    objList.forEach(v => {
      v.handle = ['分享', '取消收藏']
      result.push(v)
    })
    return result
  })
}
/**
 * 获取文件库信息列表
 */
export function getlibrarylist (userCode, params) {
  return api.get(`/fileserver/library?userCode=${userCode}`, { params })
}
/**
 * 获取用户查询机构全路径
 */
export function getunitpath (userCode) {
  return api.get(`/fileserver/library/unitpath?userCode=${userCode}`)
}
/**
 * 文件--查看按钮--文件信息
 * @param params
 */
export function seeFileMes (params) {
  return api.get(`/fileserver/files/${params}`)
    .then(data => {
      const result = [
        /* {name: '文件名', feld: 'fileName'}, */
        { name: '创建时间', feld: 'createTime' },
        { name: '下载次数', feld: 'downloadTimes' },
        { name: '加密类型', feld: 'encryptType' },
        { name: '文件ID', feld: 'fileId' },
        { name: 'MD5', feld: 'fileMd5' },
        { name: '文件大小', feld: 'fileSize' },
        { name: '文件状态', feld: 'fileState' },
        { name: '文件类型', feld: 'fileType' },
        { name: '文件机构', feld: 'fileUnit' },
        { name: '索引状态', feld: 'indexState' },
        { name: '系统ID', feld: 'optId' },
        { name: '操作类别', feld: 'osId' },
      ]

      result.forEach(v => {
        v.value = data[v.feld]
      })

      return result
    })
}

/**
 * 文件--查看按钮--下载日志
 * @param params
 */
export function seeUploadFileMes (param) {
  return api.get(`/fileserver/access/list/${param}`)
}

/**
 * 文件夹--双击进去查看里面的文件
 * @param params
 */
export function queryUserLocal ({ params = {} } = {}) {
  return api.get(`/fileserver/local/userdir/${params.path || ''}`)
    .then(res => {
      res.data.filter(d => d.fileType === 'd')
        .forEach((v, i) => {
          v.accessToken = i
        })
      return res.data
    })
}

/**
 * 文件--双击列表查看文件版本列表
 * @param params
 */
export function queryUserVerLocal (params) {
  console.log(params)
  return api.get(`/fileserver/local/userfile/${params.params.fileShowPath}/${params.params.fileName || ''}`)
    .then(res => {
      const arr = []
      res.forEach((i, key) => {
        let handle
        if (i.favoriteId) {
          handle = ['分享', '复制到', '移动到', '删除']
        } else {
          handle = ['分享', '收藏', '复制到', '移动到', '删除']
        }
        res[key].handle = handle
        arr.push(res[key])
      })
      return arr
    })
}

/**
 * 文件--下载按钮
 * @param params
 */
export function downs (param, userCode) {
  const baseURL = apiFactory.baseURL
  window.open(`${baseURL}/file/fileserver/download/downloadwithauth/${param}?userCode=${userCode}`)
}
/**
 * 文件分享--下载按钮
 * @param params
 */
export function downsShare (param, authCode) {
  const baseURL = apiFactory.baseURL
  window.open(`${baseURL}/file/fileserver/download/downloadwithauth/${param}?authCode=${authCode}`)
}
/**
 * 文件--预览
 * @param params
 */
export function downsPreview (param, userCode) {
  const baseURL = apiFactory.baseURL
  window.open(`${baseURL}/file/fileserver/download/preview/${param}?userCode=${userCode}`)
}
/**
 * 文件分享--预览
 * @param params
 */
export function downsPreviewShare (param, authCode) {
  const baseURL = apiFactory.baseURL
  window.open(`${baseURL}/file/fileserver/download/preview/${param}?authCode=${authCode}`)
}
/**
 * 删除单个文件库
 */
export function deletelibrary (libraryId) {
  return api.delete(`/fileserver/library/${libraryId}`)
}
/**
 * 查询单个文件库
 * @param params
 */
export function seeLibrary (libraryId) {
  return api.get(`/fileserver/library/${libraryId}`)
    .then(data => {
      // 修改fileLibraryAccesss格式
      const { fileLibraryAccesss } = data
      if (fileLibraryAccesss) {
        data.fileLibraryAccesss = fileLibraryAccesss.map(f => f.accessUsercode)
      }

      return data
    })
}
/**
 * 取消单个文件收藏
 */
export function deletefavorite (favoriteId) {
  return api.delete(`/fileserver/favorite/${favoriteId}`)
}

/**
 * 文件--查询文件日志
 * @param params
 */
export function log (optTag) {
  return api.get(`/fileserver/log?optTag=${optTag}`)
}

/**
 * 更新文件夹信息,新增信息
 * @param id
 */
export function addFolder (params) {
  return apis.put('fileserver/folder', params)
}

/**
 * 获取分享时的验证码
 * @param id
 */

export function authCode (fileId) {
  return api.get(`/fileserver/files/authcode/${fileId}`)
}
/**
 * 更新文件库信息
 * @param library
 */
export function updatelibraryr (library) {
  const { fileLibraryAccesss } = library
  if (fileLibraryAccesss) {
    library.fileLibraryAccesss = fileLibraryAccesss.map(userCode => {
      return {
        accessUsercode: userCode,
        libraryId: library.libraryId
      }
    })
  }
  return apis.put('fileserver/library', library).then(res => res.data)
}

/**
 * 检查验证码
 * @param id
 */
export function checkAuth (params) {
  return api.get(`/fileserver/files/checkauth/${params.fileId}/${params.authCode}`)
}

/**
 * 更新文件信息,新增信息
 * @param id
 */
export function addMes (fileId, params) {
  return apis.post(`fileserver/files/j/${fileId}`, params).then(res => res.data)
}

/**
 * 业务菜单--本地文件--上传按钮--删除文件
 * @param params
 */
export function delectFile (param) {
  return api.delete(`/fileserver/files/${param}`)
}
/**
 * 根据相关的文件获取查询文件
 * @param id
 */
export function getfileslist ({ params = {}, sort = {}, page = {} } = {}) {
  return api.get('/fileserver/files', {
    params: {
      ...params,
      ...sort,
      ...page,
    },
  })
}
/**
 * 获取日志
 * @param id
 */

export function getlog ({ params = {}, sort = {}, page = {} } = {}) {
  return api.get('/fileserver/log', {
    params: {
      ...params,
      ...sort,
      ...page,
    },
  })
    .then(res => {
      if (!res) {
        return []
      }
      return res
    })
}

/**
 * 查询文件夹所有上级文件夹
 * @param id
 */
export function prev (folderId) {
  return api.get(`/fileserver/folder/prev/${folderId}`)
}
/**
 * 全文检索
 * @param id
 */

export function GetSearch ({ params = {}, sort = {}, page = {} } = {}) {
  return api.get('/fileserver/files/search', {
    params: {
      ...params,
      ...sort,
      ...page,
    },
  })
}
/**
 * 初始化更新个人库
 * @param id
 */
export function initpersonlib () {
  return apis.post('/fileserver/library/initpersonlib')
}
/**
 * 初始化更新机构库
 * @param id
 */
export function initunitlib (param) {
  return apis.post(`/fileserver/library/initunitlib/${param}`)
}
/**
 * 根据库名获取icon
 * @param id
 */
export function libraryimage (libraryName, params) {
  return api.get(`/fileserver/library/libraryimage/${libraryName}`, { params })
}

/**
 * 上传文件的接口
 */
export function flashUpload (token, size, params) {
  return api.post('/fileserver/upload/secondpass', null, {
    params: {
      token,
      size,
      ...params,
    },
  })
}

export function getFileRange (token, size, params) {
  return api.get('fileserver/upload/range', {
    params: {
      token,
      size,
      ...params
    }
  }).then(res => res.data.data ? res.data.data : res.data)
}

export function uploadFileRange (range, file, params, { onUploadProgress } = {}) {
  const size = file.size
  const begin = range.begin
  const end = range.end

  // eslint-disable-next-line no-undef
  return $.ajax(createUrlParams(apiFactory.baseURL + '/file/fileserver/upload/range', {
    token: file.token,
    name: file.name,
    size,
    ...params,
  }), {
    data: range.data,
    contentType: false,
    processData: false,
    dataType: 'json',
    type: 'POST',
    mimeType: 'application/octet-stream',
    xhr: function () {
      const xhr = new XMLHttpRequest()
      // 使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
      xhr.upload.addEventListener('progress', onUploadProgress)
      return xhr
    },
    headers: {
      'Content-Type': 'application/octet-stream',
      'Content-Range': `bytes ${begin}-${end}/${size}`,
    },
  })

  // return api.post('fileserver/upload/range', range.data, {
  //   data: range.data,
  //
  //   params: {
  //     token: file.token,
  //     name: file.name,
  //     size,
  //     ...params,
  //   },
  //
  //   onUploadProgress,
  //
  //   headers: {
  //     'Content-Type': 'application/octet-stream',
  //     'Content-Range': `bytes ${begin}-${end}/${size}`,
  //   },
  // }).then(res => res.data)
}

export function uploadFile (file, params, { onUploadProgress } = {}) {
  return api.post('fileserver/upload/file', file, {
    params: {
      token: file.token,
      name: file.name,
      size: file.size,
      ...params,
    },

    onUploadProgress,

    headers: {
      'Content-Type': 'application/octet-stream',
    },
  }).then(res => res.data)
}
/**
 * 下载文件夹
 */
export function zipDown (folderId) {
  const baseURL = apiFactory.baseURL
  window.open(`${baseURL}/file/fileserver/folder/downloadZip/${folderId}`)
}
