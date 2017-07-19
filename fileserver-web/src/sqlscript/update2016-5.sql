drop table F_USERSETTING;

create table F_USERSETTING  (
   USERCODE             VARCHAR2(8)                     not null,
   ParamCode            VARCHAR2(16)                    not null,
   ParamValue           VARCHAR2(2048)                  not null,
   optID                VARCHAR2(16)                    not null,
   ParamName            VARCHAR2(200),
   CreateDate           DATE
);

comment on column F_USERSETTING.USERCODE is
'DEFAULT:为默认设置
SYS001~SYS999: 为系统设置方案
是一个用户号,或者是系统的一个设置方案';

alter table F_USERSETTING
   add constraint PK_F_USERSETTING primary key (USERCODE, ParamCode);
