## 获取用户库列表

GET https://cloud.centit.com/locode/api/fileserver/fileserver/library

```json
{
  "code": 0,
  "data": {
    "objList": [
      {
        "createTime": "2023-07-04 15:09:32",
        "createUser": "U98PYTr6",
        "isCreateFolder": "T",
        "isUpload": "T",
        "libraryId": "342cdc7fe0b6459e8038510d217fe0c2",
        "libraryName": "我的文件",
        "libraryType": "P",
        "ownUnit": "Ttetp5a4",
        "ownUser": "U98PYTr6"
      }
    ]
  },
  "message": "OK"
}
```

## 获取用户上传的文件列表

GET https://cloud.centit.com/locode/api/fileserver/fileserver/files?owner=U98PYTr6&sort=createTime&order=desc

````json
{
    "code": 0,
    "data": {
        "objList": [
            {
                "fileId": "01cbbc892b574d2ba93a2e5571e8196f",
                "fileMd5": "11066839dd3511ad5b81c70b4a8f46a9",
                "fileName": "西桥12号不予受理(1).docx",
                "fileStorePath": "1/1/0/11066839dd3511ad5b81c70b4a8f46a9.dat",
                "fileType": "docx",
                "fileState": "N",
                "indexState": "N",
                "downloadTimes": 0,
                "osId": "NOTSET",
                "optId": "NOTSET",
                "createTime": "2025-11-12 11:32:44",
                "fileSize": 12148,
                "encryptType": "N",
                "fileOwner": "U98PYTr6",
                "fileUnit": "DxxkJ664",
                "attachedType": "pdf",
                "attachedFileMd5": "184a633c3e14ef112ef4fa85382c94b0",
                "fileShowPath": "/-1",
                "libraryId": "0siPtTbtS4yoH0UA5uW3zg",
                "parentFolder": "-1",
                "ownerName": "张凯"
            }
        ],
        "pageDesc": {
            "pageNo": 1,
            "pageSize": 20,
            "rowEnd": 20,
            "rowStart": 0,
            "totalRows": 597
        }
    },
    "message": "OK"
}

## 获取库下文件列表

GET https://cloud.centit.com/locode/api/fileserver/fileserver/folder/396a0917f4f944928a62d92281db3810/-1

```json
{
    "code": 0,
    "data": {
        "objList": [
            {
                "accessToken": "4b5b14d4e42b45028eeb3209933be05e",
                "catalogType": "p",
                "createTime": "2021-09-28 01:12:43",
                "downloadTimes": 42,
                "encrypt": false,
                "fileName": "常用图标.zip",
                "fileShowPath": "/-1",
                "fileSize": 616798,
                "fileType": "zip",
                "folder": false,
                "ownerName": "刘兆文",
                "versions": 1
            },
            {
                "createFolder": "T",
                "createTime": "2020-10-16 06:30:42",
                "downloadTimes": 0,
                "encrypt": false,
                "fileName": "操作系统",
                "fileShowPath": "/-1",
                "fileSize": 0,
                "folder": true,
                "folderId": "0f2ea692f7814cd1882fb041c7a43d88",
                "ownerName": "",
                "parentPath": "-1",
                "uploadFile": "T",
                "versions": 0
            }
        ]
    },
    "message": "OK"
}
````

## 获取文件信息

GET https://cloud.centit.com/locode/api/fileserver/fileserver/files/f0f5e85e9a1447bdb97ee501c737760e

```json
{
    "code": 0,
    "data": {
        "createTime": "2022-07-22 07:58:16",
        "downloadTimes": 26,
        "encryptType": "未加密",
        "fileCatalog": "C",
        "fileId": "f0f5e85e9a1447bdb97ee501c737760e",
        "fileMd5": "acb6dadbb252735f2836414d1334af62",
        "fileName": "更新nginx.png",
        "fileOwner": "",
        "fileShowPath": "/-1",
        "fileSize": 28674,
        "fileState": "正常",
        "fileType": "png",
        "indexState": "未检索",
        "libraryId": "396a0917f4f944928a62d92281db3810",
        "optId": "NOTSET",
        "osId": "NOTSET",
        "parentFolder": "-1"
    },
    "message": "OK"
}
```

## 下载文件

GET https://cloud.centit.com/locode/api/fileserver/fileserver/download/downloadwithauth/f0f5e85e9a1447bdb97ee501c737760e?userCode=U98PYTr6&?accessToken=1c74be17-75a6-4857-a9c2-d2471dc35797

GET https://cloud.centit.com/locode/api/fileserver/fileserver/download/downloadwithauth/f0f5e85e9a1447bdb97ee501c737760e

## 获取用户信息

GET https://cloud.centit.com/locode/api/framework/system/mainframe/currentuser

```json
{
    "code": 0,
    "data": {
        "accountNonExpired": true,
        "accountNonLocked": true,
        "authenticated": true,
        "belongUnitCode": "D4BtpSxJ",
        "credentialsNonExpired": true,
        "currentStationId": "s000000449",
        "currentUnitCode": "D4BtpSxJ",
        "currentUnitName": "技术管理中心",
        "enabled": true,
        "loginIp": "192.168.137.11",
        "name": "zk",
        "tenantRole": "ZHGLY",
        "topUnitCode": "DxxkJ664",
        "topUnitName": "南大先腾",
        "userInfo": {
            "activeTime": "2025-11-26 13:36:37",
            "createDate": "2022-07-06 09:59:09",
            "creator": "U22AJpoL",
            "currentStationId": "s000000449",
            "isValid": "T",
            "lastModifyDate": "2025-10-14 16:53:34",
            "loginName": "zk",
            "primaryUnit": "D4BtpSxJ",
            "regCellPhone": "134****8350",
            "regEmail": "z*@centit.com",
            "topUnit": "DxxkJ664",
            "updateDate": "2025-10-14 16:53:34",
            "updator": "U98PYTr6",
            "userCode": "U98PYTr6",
            "userDesc": "技术管理中心前端研发组",
            "userName": "张凯",
            "userOrder": 1,
            "userTag": "CN=张凯,CN=Users,DC=centit,DC=com",
            "userType": "U"
        },
    }
}
```

## 内部登录

POST https://cloud.centit.com/locode/api/framework/system/ldap/login

```form-data
username=encode%3Aems%3D&password=encode%3Ac29mdHN0YXJwYWwzIw%3D%3D&j_checkcode=txsk&ajax=true
```

## 通用登录

POST https://cloud.centit.com/locode/api/framework/login

```form-data
username=encode%3Aems%3D&password=encode%3Ac29mdHN0YXJwYWwzIw%3D%3D&j_checkcode=txsk&ajax=true
```

## 获取验证码

GET https://cloud.centit.com/locode/api/framework/system/mainframe/captchaimage?rand=0.8746782321587773

