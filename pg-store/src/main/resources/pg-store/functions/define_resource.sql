CREATE OR REPLACE FUNCTION define_resource(_type text) RETURNS void AS 
$$
DECLARE 
_tbl_name text;
BEGIN
  _tbl_name := lower(_type);
  IF EXISTS(SELECT * FROM pg_class WHERE relname = _tbl_name) THEN
    RETURN;
  END IF;
  
  EXECUTE FORMAT('create table %s partition of resource for values in (%L)', _tbl_name, _type);
  EXECUTE FORMAT('alter table %s alter column type set default %L', _tbl_name, _tbl_name);
  EXECUTE FORMAT('select core.create_table_metadata(%L);', _tbl_name);
  EXECUTE FORMAT('CREATE TRIGGER insert_%s_trigger BEFORE INSERT ON %s FOR EACH ROW EXECUTE PROCEDURE resource_insert_trigger();', _tbl_name, _tbl_name);
  
  EXECUTE FORMAT('create index %s_reference_idx on %s using gin (ref(%L,id))', _tbl_name, _tbl_name, _tbl_name);
  EXECUTE FORMAT('create unique index %s_udx on %s (id,last_version)', _tbl_name, _tbl_name);
  EXECUTE FORMAT('alter table %s add primary key (key)', _tbl_name);
  
END;
$$ LANGUAGE plpgsql;