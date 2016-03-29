CREATE OR REPLACE FUNCTION util.string2tab(varchar, varchar default ',') RETURNS SETOF varchar AS 
$$
  select regexp_split_to_table($1, $2);
$$ 
LANGUAGE SQL IMMUTABLE;