--liquibase formatted sql

--changeset blaze:resource dbms:postgresql
CREATE SEQUENCE resource_key_seq INCREMENT 1 MINVALUE 1;

create table resource (
  key           bigint not null default nextval('resource_key_seq'),
  type          text not null,
  id            text not null,
  last_version  smallint not null default 1,
  last_updated  timestamp not null default localtimestamp,
  content       jsonb not null,
  sys_status    char(1) not null default 'A'
) PARTITION BY LIST (type);
--rollback drop table resource cascade;

