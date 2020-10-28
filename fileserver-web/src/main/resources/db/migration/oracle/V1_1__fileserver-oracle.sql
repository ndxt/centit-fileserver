
create table FILE_ACCESS_LOG  (
   access_token       varchar2(36)                    not null,
   FILE_ID              varchar2(36),
   auth_time          date                            not null,
   access_usercode    varchar2(8),
   access_usename     varchar2(50),
   access_right       varchar2(2)                     not null,
   token_expire_time  date                            not null,
   access_times       NUMBER(6)                      default 0 not null,
   last_access_time   date,
   last_access_host   varchar2(100),
   constraint PK_FILE_ACCESS_LOG primary key (access_token)
);
comment on column FILE_ACCESS_LOG.access_right is
'A： 所有权限  S: 下载源文件  T ：下载附属文件 ';

create table FILE_UPLOAD_AUTHORIZED  (
   UPLOAD_TOKEN       varchar2(36) not null,
   MAX_UPLOAD_FILES   NUMBER(10) not null,
   REST_UPLOAD_FILES   NUMBER(10)  not null,
   CREATE_TIME        date,
   LAST_UPLOAD_TIME   date,
   constraint PK_FILE_UPLOAD_AUTHORIZED primary key (UPLOAD_TOKEN)
);

create table FILE_INFO  (
   FILE_ID              VARCHAR2(36)                    NOT NULL,
   FILE_MD5             VARCHAR2(36),
   FILE_NAME          VARCHAR2(200),
   FILE_SHOW_PATH     VARCHAR2(1000),
   FILE_TYPE          VARCHAR2(8),
   FILE_DESC          VARCHAR2(200),
   FILE_STATE         CHAR,
   DOWNLOAD_TIMES     NUMBER(6),
   OS_ID              VARCHAR2(20),
   OPT_ID             VARCHAR2(64)                    NOT NULL,
   OPT_METHOD         VARCHAR2(64),
   OPT_TAG            VARCHAR2(200),
   CREATED            VARCHAR2(8),
   CREATE_TIME        DATE,
   INDEX_STATE        CHAR,
   ENCRYPT_TYPE       CHAR,
   FILE_OWNER         VARCHAR2(32),
   FILE_UNIT          VARCHAR2(32),
   ATTACHED_FILE_MD5 VARCHAR2(200),
   ATTACHED_TYPE      VARCHAR2(8),
   constraint PK_FILE_INFO primary key (FILE_ID)
);

create table FILE_STORE_INFO  (
   FILE_MD5              VARCHAR(36) not null ,
   FILE_STORE_PATH       varchar2(200),
   FILE_SIZE             NUMBER(20),
   FILE_REFERENCE_COUNT  NUMBER(6),
   CREATE_TIME        date,
   constraint PK_FILE_STORE_INFO primary key (FILE_MD5)
);

comment on table FILE_INFO is
'这里只保留文件的目录信息';

comment on column FILE_INFO.FILE_MD5 is
'文件MD5编码';

comment on column FILE_INFO.file_name is
'原始文件名称';

comment on column FILE_INFO.file_type is
'文件后缀名';

comment on column FILE_INFO.file_state is
'C : 正在上传  N : 正常 Z:空文件 F:文件上传失败';

comment on column FILE_INFO.Opt_ID is
'模块，或者表';

comment on column FILE_INFO.OPT_Method is
'方法，或者字段';

comment on column FILE_INFO.opt_Tag is
'一般用于关联到业务主体';

comment on column FILE_INFO.index_state is
'N ：不需要索引 S：等待索引 I：已索引 F:索引失败';

comment on column FILE_INFO.encrypt_type is
'N : 没有加密   Z：zipFile    D:DES加密';

comment on column FILE_INFO.attached_type is
'附属文件后缀名';

create index Index_file_md5 on FILE_INFO (
   FILE_MD5 ASC
);



