--liquibase formatted sql

--changeset priitrs:position_lots
create table position_lots (
                            id serial primary key,
                            asset text,
                            qty_remaining	integer,
                            unit_cost	decimal
);
