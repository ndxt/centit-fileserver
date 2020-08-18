alter table file_info add column auth_code varchar(32) comment '验证码';
alter table file_info add column folder_id  varchar(32) comment '文件夹id';
alter table file_info add column library_id   varchar(32) comment '库id';