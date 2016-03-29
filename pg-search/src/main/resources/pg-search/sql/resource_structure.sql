--liquibase formatted sql

--changeset blaze:resource_struct dbms:postgresql
create table resource_structure (
  path 			text not null primary key,
  element_types text[] not null,
  is_many 		boolean not null
);
--rollback drop table resource_structure;
