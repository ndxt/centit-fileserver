
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

create table FILE_STORE_INFO  (
   FILE_ID              varchar2(36)                    not null,
   FILE_MD5             VARCHAR(36),
   file_name          varchar2(200),
   file_Show_path     varchar2(1000),
   file_Store_path    varchar2(200),
   file_type          varchar2(8),
   file_Desc          varchar2(200),
   file_state         CHAR,
   file_size          NUMBER(20),
   download_times     NUMBER(6),
   OS_ID                varchar2(20),
   Opt_ID             varchar2(64)                    not null,
   OPT_Method         varchar2(64),
   opt_Tag            varchar2(200),
   created            varchar2(8),
   create_time        date,
   index_state        CHAR,
   encrypt_type       CHAR,
   file_owner         VARCHAR(32),
   file_unit          VARCHAR(32),
   attached_Store_path varchar2(200),
   attached_type      varchar2(1),
   constraint PK_FILE_STORE_INFO primary key (FILE_ID)
);

comment on table FILE_STORE_INFO is
'这里只保留文件的目录信息';

comment on column FILE_STORE_INFO.FILE_MD5 is
'文件MD5编码';

comment on column FILE_STORE_INFO.file_name is
'原始文件名称';

comment on column FILE_STORE_INFO.file_type is
'文件后缀名';

comment on column FILE_STORE_INFO.file_state is
'C : 正在上传  N : 正常 Z:空文件 F:文件上传失败';

comment on column FILE_STORE_INFO.Opt_ID is
'模块，或者表';

comment on column FILE_STORE_INFO.OPT_Method is
'方法，或者字段';

comment on column FILE_STORE_INFO.opt_Tag is
'一般用于关联到业务主体';

comment on column FILE_STORE_INFO.index_state is
'N ：不需要索引 S：等待索引 I：已索引 F:索引失败';

comment on column FILE_STORE_INFO.encrypt_type is
'N : 没有加密   Z：zipFile    D:DES加密';

comment on column FILE_STORE_INFO.attached_type is
'附属文件类别：N :   没有  T：缩略图  P： pdf只读文件';


create index Index_file_md5 on FILE_STORE_INFO (
   FILE_MD5 ASC
);



