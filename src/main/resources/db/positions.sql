--liquibase formatted sql

--changeset priitrs:positions
create table positions (
                            asset text primary key,
                            quantity	integer,
                            average_cost	decimal,
                            total_cost	decimal,
                            realized_pl	decimal
);
