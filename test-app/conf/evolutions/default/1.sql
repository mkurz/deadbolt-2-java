# --- !Ups

create table security_permission (
  id                        bigint not null,
  value                     varchar(255) not null,
  constraint uq_security_permission_value unique (value),
  constraint pk_security_permission primary key (id))
;

create table security_role (
  id                        bigint not null,
  role_name                 varchar(255) not null,
  constraint uq_security_role_role_name unique (role_name),
  constraint pk_security_role primary key (id))
;

create table user (
  user_name                 varchar(255) not null,
  constraint pk_user primary key (user_name))
;


create table user_security_role (
  user_user_name                 varchar(255) not null,
  security_role_id               bigint not null,
  constraint pk_user_security_role primary key (user_user_name, security_role_id))
;

create table user_security_permission (
  user_user_name                 varchar(255) not null,
  security_permission_id         bigint not null,
  constraint pk_user_security_permission primary key (user_user_name, security_permission_id))
;
create sequence security_permission_seq;

create sequence security_role_seq;

create sequence user_seq;




alter table user_security_role add constraint fk_user_security_role_user_01 foreign key (user_user_name) references user (user_name) on delete restrict on update restrict;

alter table user_security_role add constraint fk_user_security_role_securit_02 foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

alter table user_security_permission add constraint fk_user_security_permission_u_01 foreign key (user_user_name) references user (user_name) on delete restrict on update restrict;

alter table user_security_permission add constraint fk_user_security_permission_s_02 foreign key (security_permission_id) references security_permission (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists security_permission;

drop table if exists security_role;

drop table if exists user;

drop table if exists user_security_role;

drop table if exists user_security_permission;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists security_permission_seq;

drop sequence if exists security_role_seq;

drop sequence if exists user_seq;

