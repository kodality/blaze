CREATE OR REPLACE FUNCTION ref(type varchar, id varchar) RETURNS text[] AS 
$$
    SELECT ARRAY[type || '/' || id]::text[]
$$ LANGUAGE SQL;
