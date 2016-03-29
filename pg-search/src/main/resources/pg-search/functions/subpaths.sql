CREATE OR REPLACE FUNCTION subpaths(_resource_type text, _path text, _param_type text) RETURNS SETOF jsonb AS $$
  select unnest(path) from search_configuration
    where element_type IN (select unnest(element_types) from resource_structure where path = _resource_type || '.' || _path)
    AND param_type = _param_type;
$$ LANGUAGE SQL IMMUTABLE;