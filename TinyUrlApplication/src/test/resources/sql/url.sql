truncate table t_url;

insert into t_url
values (null, '9630f4ec-5798-4af8-9783-7880b3c901fe', '7ab2de96c1657beb5bb820a6c404115e',
        'https://www.youtube.com/watch?v=CVGmIp9Wv77', '7ab2de96');

insert into t_url
values (null, '9630f4ec-5798-4af8-9783-7880b3c90177', null, 'https://www.youtube.com/watch?v=CVGmIp9Wv77', 'alias');

insert into t_url
values ('2024-09-01T00:00:00', '9630f4ec-5798-4af8-9783-7880b3c90188', null,
        'https://www.youtube.com/watch?v=CVGmIp9Wv77', 'expiredAlias');