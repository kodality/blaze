--liquibase formatted sql

--changeset blaze:insert_resource_trigger dbms:postgresql
CREATE TRIGGER insert_resource_trigger
    BEFORE INSERT ON resource
    FOR EACH ROW EXECUTE PROCEDURE resource_insert_trigger();
--rollback drop trigger insert_resource_trigger;
