CREATE OR REPLACE FUNCTION create_blindex(_resource_type text, _path text) RETURNS void AS $$
DECLARE
  _struct resource_structure;
  _element_type text;
  _param_type text;
  _idx_type text;
  _idx_name text;
BEGIN
  SELECT * INTO _struct FROM resource_structure WHERE path = _resource_type || '.' || _path;
  IF _struct IS NULL THEN
    RAISE EXCEPTION '% not found in resource_structure', _resource_type || '.' || _path;
  END IF;
  FOREACH _element_type IN ARRAY _struct.element_types LOOP
    IF NOT EXISTS (SELECT 1 FROM search_configuration WHERE element_type = _element_type) THEN
      RAISE EXCEPTION '% not configured. (search_configuration)', _element_type;
    END IF;
    FOR _param_type IN (SELECT param_type FROM search_configuration WHERE element_type = _element_type) LOOP
      CONTINUE WHEN EXISTS (SELECT 1 FROM blindex WHERE resource_type = _resource_type AND path = _path AND param_type = _param_type);

      _idx_type := CASE WHEN _param_type IN ('string','token') THEN 'pizzelle' ELSE 'parasol' END;
      _idx_name := lower(_resource_type) || '_' || _param_type || '_' || lower(replace(_path, '.', '_'))  || '_' || _idx_type ;

      CASE
        WHEN _param_type = 'string' THEN
          EXECUTE format('CREATE INDEX %3$s ON %1$s USING gin (string(%1$s::resource, %2$L) gin_trgm_ops)', lower(_resource_type), _path, _idx_name);
        WHEN _param_type = 'token' THEN
          EXECUTE format('CREATE INDEX %3$s ON %1$s USING gin (token(%1$s::resource, %2$L))', lower(_resource_type), _path, _idx_name);
        WHEN _param_type = 'date' THEN
          EXECUTE format('CREATE TABLE %1$s (resource_key bigint %3$s REFERENCES %2$s (key) ON DELETE CASCADE, range tstzrange)', _idx_name, _resource_type, CASE WHEN _struct.is_many THEN '' ELSE 'PRIMARY KEY' END);
          EXECUTE format('CREATE INDEX %2$s ON %1$s USING gist (resource_key, range);', _idx_name, 'idx_' || _idx_name);
          EXECUTE format('INSERT INTO %1$s (select key, unnest(date(r, %3$L)) from %2$s r)', _idx_name, _resource_type, _path);
          EXECUTE format('DROP TRIGGER IF EXISTS trigger_parasolindex ON %s', _resource_type);
          EXECUTE format('CREATE TRIGGER trigger_parasolindex AFTER INSERT OR UPDATE ON %s FOR EACH ROW EXECUTE PROCEDURE merge_parasolindex()', _resource_type);
        ELSE
          RAISE EXCEPTION 'unknown type %', _param_type;
      END CASE;
      INSERT INTO blindex (resource_type, path, param_type, index_type, index_name) VALUES (_resource_type, _path, _param_type, _idx_type, _idx_name);
    END LOOP;
  END LOOP;
END;
$$ LANGUAGE plpgsql VOLATILE;