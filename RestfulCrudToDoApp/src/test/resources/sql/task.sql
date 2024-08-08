truncate table t_task;

insert into t_user
values ('9c186286-0ecb-422d-b19b-3e10c13221db', 'userForTestingTask', 'userForTestingTask');

insert into t_task
values ('435cb8f4-470d-4343-bdff-5209002e3c8c', 'FirstTaskName', false, '9c186286-0ecb-422d-b19b-3e10c13221db');

