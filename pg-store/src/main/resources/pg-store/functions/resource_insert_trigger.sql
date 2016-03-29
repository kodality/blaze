CREATE OR REPLACE FUNCTION resource_insert_trigger() RETURNS TRIGGER AS 
$$
BEGIN
  UPDATE resource SET sys_status = 'T' WHERE id = new.id AND type = new.type AND sys_status = 'A';
  EXECUTE 'INSERT INTO ' || lower(new.type) ||' VALUES (($1).*)' USING NEW ;
  return null;
END;
$$ LANGUAGE plpgsql VOLATILE;