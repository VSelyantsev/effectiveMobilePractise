DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_service_db') THEN
        CREATE DATABASE auth_service_db;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE pg_user.usename = 'postgres') THEN
        CREATE USER postgres;
    END IF;
END $$;

GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO postgres;