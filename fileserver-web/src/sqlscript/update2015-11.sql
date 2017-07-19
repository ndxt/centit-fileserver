alter table f_userinfo add usertype char(1) default 'U'
/

update f_userinfo set usertype ='U'
/

commit
/
