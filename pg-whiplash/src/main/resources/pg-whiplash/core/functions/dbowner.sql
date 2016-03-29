CREATE OR REPLACE FUNCTION meta.dbowner() RETURNS varchar AS $$
DECLARE
  dbowner varchar;
BEGIN
  SELECT pg_catalog.pg_get_userbyid(d.datdba) as "Owner" INTO dbowner
    FROM pg_catalog.pg_database d
   WHERE d.datname = current_database();
   
  RETURN dbowner;
END;
$$ LANGUAGE plpgsql STABLE;