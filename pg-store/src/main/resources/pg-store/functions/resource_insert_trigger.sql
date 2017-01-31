CREATE OR REPLACE FUNCTION resource_insert_trigger() RETURNS TRIGGER AS 
$$
BEGIN
  if new.id is not null then
    UPDATE resource SET sys_status = 'T' WHERE id = new.id AND type = new.type;
  end if;
  new.id := coalesce(new.id, new.key::text);
  EXECUTE 'INSERT INTO ' || lower(new.type) ||' VALUES (($1).*)' USING NEW ;
  return null;
END;
$$ LANGUAGE plpgsql VOLATILE;