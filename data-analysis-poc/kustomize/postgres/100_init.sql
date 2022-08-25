-- treat our multi-line json formatted data.log as a single column csv
create table tmp ( j jsonb );
-- import to an intermedia table
\copy tmp from '/tmp/stat.log';
-- select * from tmp limit 1;

-- normalize JSON column into our table
create table ide_event as
(
    select
        to_timestamp((j->>'ts')::double precision / 1000) as ts,
        (j->>'action')::text                              as action,
        (j->>'file')::text                                as file,
        j->'tags'                                         as tags
    from tmp
);
