--liquibase formatted sql

--changeset igor.bossenko@gmail.com:init-core-session-schema-pg dbms:postgresql runAlways:true failOnError:false
SET search_path TO util,meta,public;
--rollback select 1;

--changeset igor.bossenko@gmail.com:init-core-session-user dbms:postgresql dbms:oracle runAlways:true failOnError:false
select set_user('admin');
--rollback select 1 from dual;

