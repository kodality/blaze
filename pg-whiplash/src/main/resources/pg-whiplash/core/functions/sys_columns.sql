CREATE OR REPLACE FUNCTION meta.sys_columns()
  RETURNS trigger AS
$BODY$
DECLARE
  l_client_id   VARCHAR := null;
  l_ts          TIMESTAMP := NOW();
  l_sys_columns VARCHAR;
BEGIN
  -- collect list of system columns presented in changed table
  SELECT ','||string_agg(column_name,',')||',' into l_sys_columns
    FROM information_schema.columns
   WHERE table_name = TG_TABLE_NAME
     AND column_name like 'sys%';
  
  IF (TG_OP = 'INSERT') THEN
    IF strpos(l_sys_columns,'sys_delete_status')>0 THEN
      NEW.sys_delete_status := 'N';
    END IF;
    IF strpos(l_sys_columns,'sys_status')>0 THEN
      NEW.sys_status := 'A';
    END IF;
    IF strpos(l_sys_columns,'sys_create_time')>0 THEN
      NEW.sys_create_time := l_ts;
    END IF;
    IF strpos(l_sys_columns,'sys_create_uid')>0 THEN
      IF NEW.sys_create_uid IS NULL THEN
        SELECT meta.session_user() INTO l_client_id;
        NEW.sys_create_uid := l_client_id;
      end if;
    END IF;
    IF strpos(l_sys_columns,'sys_version')>0 THEN
      NEW.sys_version := coalesce(NEW.sys_version,0)+1;
    END IF;
  END IF;
  IF (TG_OP = 'UPDATE') THEN
    IF strpos(l_sys_columns,'sys_version')>0 THEN
      NEW.sys_version := coalesce(NEW.sys_version,OLD.sys_version,0)+1;
    END IF;
    IF strpos(l_sys_columns,'sys_status')>0 THEN
      IF NEW.sys_status is null THEN
        NEW.sys_status := OLD.sys_status;
      END IF;
    END IF;
  END IF;
  IF strpos(l_sys_columns,'sys_modify_time')>0 THEN
    NEW.sys_modify_time := l_ts;
  END IF;
  IF strpos(l_sys_columns,'sys_modify_uid')>0 THEN
    IF NEW.sys_modify_uid IS NULL THEN
       IF l_client_id IS NULL THEN
         SELECT meta.session_user() INTO l_client_id;
       END IF;
       NEW.sys_modify_uid := l_client_id;
    end if;
  END IF;
  IF strpos(l_sys_columns,'sys_modify_sid')>0 THEN
    NEW.sys_modify_sid := pg_backend_pid();
  END IF;
  RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 10;


