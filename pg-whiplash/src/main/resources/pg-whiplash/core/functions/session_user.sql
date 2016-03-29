CREATE OR REPLACE FUNCTION meta.session_user()
  RETURNS text AS
$BODY$
declare 
  l_str text;
begin
  select util.string2null(current_setting('meta.client_identifier')) into l_str; 
  return l_str;
exception when others then
  raise exception 'Session user is not initialized.' USING HINT = 'Please initialize user with command: select meta.set_user(''YOUR_USER''); ';
  return null;
end;
$BODY$
LANGUAGE plpgsql VOLATILE;