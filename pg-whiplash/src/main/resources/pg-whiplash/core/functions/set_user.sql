CREATE OR REPLACE FUNCTION meta.set_user(in_user varchar default current_user)
  RETURNS text AS
$BODY$
declare 
  l_str text;
begin
  SELECT set_config('meta.client_identifier', in_user, false) into l_str; 
  return l_str;
end;
$BODY$
LANGUAGE plpgsql VOLATILE;