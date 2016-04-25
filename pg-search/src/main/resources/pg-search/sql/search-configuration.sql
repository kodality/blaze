--liquibase formatted sql

--changeset blaze:search_configuration dbms:postgresql
create table search_configuration (
  id 			bigserial primary key,
  element_type 	text not null,
  param_type 	text not null,
  path 			jsonb[] not null,
  constraint search_configuration_element_type_param_type_ukey unique (element_type, param_type)
);
--rollback drop table search_configuration;

--changeset blaze:search_configuration-data dbms:postgresql
insert into search_configuration values (1,'date', 'date', array['{"start":"{}", "end":"{}"}']::jsonb[]);
insert into search_configuration values (2,'instant', 'date', array['{"start":"{}", "end":"{}"}']::jsonb[]);
insert into search_configuration values (3,'code', 'token', array['{"value":"{}"}']::jsonb[]);
insert into search_configuration values (4,'string', 'string', array['{"value":"{}"}']::jsonb[]);

insert into search_configuration values (5,'CodeableConcept', 'token', array['{"elements":"coding", "namespace":"{system}", "value":"{code}"}']::jsonb[]);
insert into search_configuration values (6,'Identifier', 'token', array['{"namespace":"{system}", "value":"{value}"}']::jsonb[]);
insert into search_configuration values (7,'HumanName', 'string', array['{"value":"{family}"}', '{"value":"{given}"}']::jsonb[]);
insert into search_configuration values (8,'Reference', 'token', array['{"value":"{reference}"}']::jsonb[]);
insert into search_configuration values (9,'Period', 'date', array['{"start":"{start}", "end":"{end}"}']::jsonb[]);
insert into search_configuration values (10,'Quantity', 'number', array['{"value":"{value}"}']::jsonb[]);
--rollback delete from search_configuration;
