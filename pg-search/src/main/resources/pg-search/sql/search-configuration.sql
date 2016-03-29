--liquibase formatted sql

--changeset blaze:search_configuration dbms:postgresql
create table search_configuration (
  id 			bigserial primary key,
  element_type 	text not null,
  param_type 	ext not null,
  path 			jsonb[] not null,
  constraint search_configuration_element_type_param_type_ukey unique (element_type, param_type)
);
--rollback drop table search_configuration;

--changeset blaze:search_configuration-data dbms:postgresql
insert into search_configuration values ('date', 'date', array['{"start":"{}", "end":"{}"}']::jsonb[]);
insert into search_configuration values ('instant', 'date', array['{"start":"{}", "end":"{}"}']::jsonb[]);
insert into search_configuration values ('code', 'token', array['{"value":"{}"}']::jsonb[]);
insert into search_configuration values ('string', 'string', array['{"value":"{}"}']::jsonb[]);

insert into search_configuration values ('CodeableConcept', 'token', array['{"elements":"coding", "namespace":"{system}", "value":"{code}"}']::jsonb[]);
insert into search_configuration values ('Identifier', 'token', array['{"namespace":"{system}", "value":"{value}"}']::jsonb[]);
insert into search_configuration values ('HumanName', 'string', array['{"value":"{family}"}', '{"value":"{given}"}']::jsonb[]);
insert into search_configuration values ('Reference', 'token', array['{"value":"{reference}"}']::jsonb[]);
insert into search_configuration values ('Period', 'date', array['{"start":"{start}", "end":"{end}"}']::jsonb[]);
insert into search_configuration values ('Quantity', 'number', array['{"value":"{value}"}']::jsonb[]);
--rollback delete from search_configuration;
