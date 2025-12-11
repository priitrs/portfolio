--liquibase formatted sql

--changeset priitrs:transactions
create table transactions (
                           id uuid default gen_random_uuid() primary key,
                           asset text not null,
                           timestamp timestamptz not null,
                           type text not null,
                           quantity integer not null,
                           price decimal not null,
                           fee decimal not null
);
