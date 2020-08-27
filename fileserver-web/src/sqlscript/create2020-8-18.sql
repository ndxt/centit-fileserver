/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2020-08-18 10:57:52                          */
/*==============================================================*/


drop table if exists file_access_log;

drop table if exists file_folder_info;

drop table if exists file_info;

drop table if exists file_library_access;

drop table if exists file_library_info;

drop table if exists file_favorite;

drop table if exists file_store_info;
drop table if exists file_upload_authorized;

/*==============================================================*/
/* Table: file_access_log                                       */
/*==============================================================*/
create table file_access_log
(
   access_token         varchar(36) not null,
   file_id              varchar(36),
   auth_time            datetime not null,
   access_usercode      varchar(8),
   access_usename       varchar(50),
   access_right         varchar(2) not null comment 'A： 所有权限  S: 下载源文件  T ：下载附属文件 ',
   token_expire_time    datetime not null,
   access_times         numeric(6,0) not null default 0,
   last_access_time     datetime,
   last_access_host     varchar(100),
   primary key (access_token)
);

/*==============================================================*/
/* Table: file_folder_info                                      */
/*==============================================================*/
create table file_folder_info
(
   library_id           varchar(32) comment '库id',
   folder_id            varchar(32) not null comment '文件夹id',
   parent_folder        varchar(32) default '-1' comment '上级文件夹',
   folder_path          text  comment '文件夹路径',
   is_create_folder     varchar(1) comment '是否可以创建子目录',
   is_upload            varchar(1) comment '是否可以上传文件',
   auth_code            varchar(32) comment '验证码',
   folder_name          varchar(255) comment '文件夹名称',
   create_user          varchar(32) comment '创建人',
   create_time          datetime comment '创建时间',
   update_user          varchar(32) comment '修改人',
   update_time          datetime comment '修改时间',
   primary key (folder_id)
);

/*==============================================================*/
/* Table: file_info                                             */
/*==============================================================*/
create table file_info
(
   file_id              varchar(32) not null,
   file_md5             varchar(32) comment '文件MD5编码',
   file_name            varchar(200) comment '原始文件名称',
   file_store_path      varchar(200) comment '文件存储在服务器上的相对路径',
   file_show_path       varchar(1000) comment '文件在服务端展示是的相对路径',
   file_type            varchar(32) comment '文件后缀名',
   file_Desc            varchar(200),
   file_state           char(1) comment 'C : 正在上传  N : 正常 Z:空文件 F:文件上传失败',
   file_size            numeric(20,0),
   download_times       numeric(6,0),
   opt_id               varchar(64) not null comment '模块，或者表',
   opt_method           varchar(64) comment '方法，或者字段',
   opt_tag              varchar(200) comment '一般用于关联到业务主体',
   created              varchar(8),
   create_time          datetime,
   index_state          char(1) comment 'N ：不需要索引 S：等待索引 I：已索引 F:索引失败',
   encrypt_type         char(1) comment 'N : 没有加密   Z：zipFile    D:DES加密',
   file_owner           varchar(32),
   file_unit            varchar(32),
   attached_store_path  varchar(200),
   attached_type        varchar(1) comment '附属文件类别：N :   没有  T：缩略图  P： pdf只读文件',
   auth_code            varchar(32) comment '验证码',
   parent_folder            varchar(32) default '-1' comment '所属文件夹id',
   library_id           varchar(32) comment '库id',
   primary key (file_id)
);

alter table file_info comment '这里只保留文件的目录信息';



/*==============================================================*/
/* Table: file_library_access                                   */
/*==============================================================*/
create table file_library_access
(
   access_id            varchar(32) not null comment '授权id',
   library_id           varchar(32) comment '库id',
   access_usercode      varchar(32) comment '被授权人员',
   create_user          varchar(32) comment '创建人',
   create_time          datetime comment '创建时间',
   primary key (access_id)
);

/*==============================================================*/
/* Table: file_library_info                                     */
/*==============================================================*/
create table file_library_info
(
   library_id           varchar(32) not null comment '库id',
   library_name         varchar(255) comment '库名称',
   library_type         varchar(1) comment '类别(个人、组织、项目)',
   create_user          varchar(32) comment '创建人',
   create_time          datetime comment '创建时间',
   own_unit             varchar(32) comment '所属机构',
   own_user             varchar(32) comment '所属人员',
   is_create_folder     varchar(1) comment '是否可以创建子目录',
   is_upload            varchar(1) comment '是否可以上传文件',
   auth_code            varchar(32) comment '验证码',
   update_user          varchar(32) comment '修改人',
   update_time          datetime comment '修改时间',
   primary key (library_id)
);

/*==============================================================*/
/* Table: file_favorite                                        */
/*==============================================================*/
create table file_favorite
(
   file_id              varchar(32) comment '文件ID',
   favorite_id          varchar(32) not null comment '收藏id',
   favorite_user        varchar(32) comment '收藏人',
   favorite_time        datetime comment '收藏时间',
   primary key (favorite_id)
);

/*==============================================================*/
/* Table: file_store_info                                       */
/*==============================================================*/
create table file_store_info
(
   file_md5             varchar(32) not null comment '文件MD5编码',
   file_store_path      varchar(200) comment '文件存储位置',
   file_size            decimal(20) comment '文件大小',
   file_reference_count decimal(6) comment '文件引用次数',
   is_temp              varchar(1) comment '是否临时文件',
   create_time          datetime comment '创建时间',
   primary key (file_md5)
);
create table file_upload_authorized
(
   upload_token         varchar(32) not null,
   max_upload_files     decimal(10) not null,
   rest_upload_files    decimal(10),
   create_time          datetime,
   last_upload_time     datetime,
   primary key (upload_token)
);
CREATE TABLE f_opt_log  (
  log_id varchar(32),
  log_level varchar(200),
  user_code varchar(32),
  opt_time datetime(0) NOT NULL,
  opt_content varchar(1000),
  new_value text,
  old_value text,
  opt_id varchar(32),
  opt_method varchar(500),
  opt_tag varchar(200),
  unit_code varchar(32),
  correlation_id decimal(32, 0) NULL DEFAULT NULL,
  PRIMARY KEY (log_id)
);
