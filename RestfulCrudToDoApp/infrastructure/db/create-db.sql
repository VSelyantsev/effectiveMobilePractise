DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'crud_app') THEN
        CREATE DATABASE crud_app;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE pg_user.usename = 'postgres') THEN
        CREATE USER postgres;
    END IF;
END $$;

GRANT ALL PRIVILEGES ON DATABASE crud_app TO postgres;