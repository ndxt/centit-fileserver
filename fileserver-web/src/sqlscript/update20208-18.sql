alter table file_info add column auth_code varchar(32) comment '验证码';
alter table file_info add column parent_folder  varchar(32) comment '所属文件夹id';
alter table file_info add column library_id   varchar(32) comment '库id';