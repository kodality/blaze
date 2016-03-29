CREATE OR REPLACE FUNCTION drop_blindex(_resource_type text, _path text) RETURNS void AS $$
DECLARE
  _blindex blindex;
BEGIN
  FOR _blindex IN (SELECT * FROM blindex WHERE resource_type = _resource_type AND path = _path AND index_type = 'parasol') LOOP
    EXECUTE format('DROP TABLE %1$s', _blindex.index_name);
  END LOOP;
  FOR _blindex IN (SELECT * FROM blindex WHERE resource_type = _resource_type AND path = _path AND index_type = 'pizzelle') LOOP
    EXECUTE format('DROP INDEX %1$s', _blindex.index_name);
  END LOOP;
  DELETE FROM blindex WHERE resource_type = _resource_type AND path = _path;
END;
$$ LANGUAGE plpgsql VOLATILE;