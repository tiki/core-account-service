CREATE VIEW report AS
    SELECT user_id, created_utc
    FROM user_info;


CREATE USER report_user WITH PASSWORD '<SECRET>';
GRANT CONNECT ON DATABASE l0_auth TO report_user;
GRANT USAGE ON SCHEMA public TO report_user;
GRANT SELECT ON report TO report_user;