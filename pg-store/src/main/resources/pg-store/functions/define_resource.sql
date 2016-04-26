CREATE OR REPLACE FUNCTION define_resource(_type text) RETURNS void AS 
$$
DECLARE 
_tbl_name text;
BEGIN
  _tbl_name := lower(_type);
  IF EXISTS(SELECT * FROM pg_class WHERE relname = _tbl_name) THEN
    RETURN;
  END IF;
  
  EXECUTE FORMAT('create table %s (like resource including all)', _tbl_name);
  EXECUTE FORMAT('alter  table %s add constraint %s_type_check check(type=%L)', _tbl_name, _tbl_name, _type);
  EXECUTE FORMAT('alter  table %s alter column type set default %L', _tbl_name, _type);
  EXECUTE FORMAT('alter  table %s inherit resource', _tbl_name);
  EXECUTE FORMAT('create index %s_reference_idx on %s using gin (ref(type,id))', _tbl_name, _tbl_name);
  EXECUTE FORMAT('create index %s_id_type_idx on %s (id,type)', _tbl_name, _tbl_name);
  
END;
$$ LANGUAGE plpgsql;