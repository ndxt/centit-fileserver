CREATE TABLE FILE_ACCESS_LOG  (
  ACCESS_TOKEN       VARCHAR(36)                    NOT NULL,
  FILE_ID              VARCHAR(36),
  AUTH_TIME          DATETIME                            NOT NULL,
  ACCESS_USERCODE    VARCHAR(8),
  ACCESS_USENAME     VARCHAR(50),
  ACCESS_RIGHT       VARCHAR(2)                     NOT NULL COMMENT
    'A： 所有权限  S: 下载源文件  T ：下载附属文件 ' ,
  TOKEN_EXPIRE_TIME  DATETIME                            NOT NULL,
  ACCESS_TIMES       DECIMAL(6)                      DEFAULT 0 NOT NULL,
  LAST_ACCESS_TIME   DATETIME,
  LAST_ACCESS_HOST   VARCHAR(100),
  PRIMARY KEY (ACCESS_TOKEN)
);


CREATE TABLE FILE_STORE_INFO  (
  FILE_ID              VARCHAR(36)                    NOT NULL,
  FILE_MD5             VARCHAR(36) COMMENT '文件MD5编码' ,
  FILE_NAME          VARCHAR(200) COMMENT '原始文件名称',
  FILE_SHOW_PATH     VARCHAR(1000),
  FILE_STORE_PATH    VARCHAR(200),
  FILE_TYPE          VARCHAR(8) COMMENT '文件后缀名',
  FILE_DESC          VARCHAR(200),
  FILE_STATE         CHAR COMMENT 'C : 正在上传  N : 正常 Z:空文件 F:文件上传失败',
  FILE_SIZE          DECIMAL(20),
  DOWNLOAD_TIMES     DECIMAL(6),
  OS_ID                VARCHAR(20),
  OPT_ID             VARCHAR(64)                    NOT NULL COMMENT '模块，或者表',
  OPT_METHOD         VARCHAR(64)  COMMENT '方法，或者字段',
  OPT_TAG            VARCHAR(200) COMMENT '一般用于关联到业务主体',
  CREATED            VARCHAR(8),
  CREATE_TIME        DATETIME,
  INDEX_STATE        CHAR COMMENT 'N ：不需要索引 S：等待索引 I：已索引 F:索引失败',
  ENCRYPT_TYPE       CHAR COMMENT 'N : 没有加密   Z：ZIPFILE    D:DES加密',
  FILE_OWNER         VARCHAR(32),
  FILE_UNIT          VARCHAR(32),
  ATTACHED_STORE_PATH VARCHAR(200),
  ATTACHED_TYPE      VARCHAR(1) COMMENT '附属文件类别：N :   没有  T：缩略图  P： PDF只读文件',
  PRIMARY KEY (FILE_ID)
);


CREATE INDEX INDEX_FILE_MD5 ON FILE_STORE_INFO (
  FILE_MD5 ASC
);
