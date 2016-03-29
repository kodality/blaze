CREATE OR REPLACE FUNCTION meta.restore_default_search_path()  
returns varchar 
language plpgsql volatile 
as
$BODY$
DECLARE
  l_user oid;
  l_search_path varchar;
BEGIN
-- get current user oid
  select usesysid into l_user from pg_user where usename=current_user;
  -- get search path for current user in current database
  select substr(param,ind+1) into l_search_path  
    from (
       select unnest(setconfig) param, strpos(unnest(setconfig),'=') ind from pg_db_role_setting where setrole=l_user 
    ) as t
   where substr(param,1,ind-1)='search_path'; 

  EXECUTE 'set search_path = ' || l_search_path;
  
  RETURN l_search_path;
END;
$BODY$;