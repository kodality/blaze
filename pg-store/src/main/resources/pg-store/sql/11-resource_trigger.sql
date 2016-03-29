--liquibase formatted sql

--changeset blaze:resource dbms:postgresql
CREATE SEQUENCE resource_key_seq INCREMENT 1 MINVALUE 1;

create table resource (
  key           bigint not null default nextval('resource_key_seq') PRIMARY KEY,
  type          varchar(30) not null,
  id            varchar(50) not null,
  last_version  smallint not null default 1,
  last_updated  timestamp not null default localtimestamp,
  content       jsonb not null,
  sys_status    char(1) not null default 'A'
);
--rollback drop table resource cascade;

--changeset blaze:insert_resource_trigger dbms:postgresql
CREATE TRIGGER insert_resource_trigger
    BEFORE INSERT ON resource
    FOR EACH ROW EXECUTE PROCEDURE resource_insert_trigger();
--rollback drop trigger insert_resource_trigger;
