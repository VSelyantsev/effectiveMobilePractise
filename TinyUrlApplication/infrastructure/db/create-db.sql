DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tiny_url') THEN
        CREATE DATABASE tiny_url;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE pg_user.usename = 'postgres') THEN
        CREATE USER postgres;
    END IF;
END $$;

GRANT ALL PRIVILEGES ON DATABASE tiny_url TO postgres;