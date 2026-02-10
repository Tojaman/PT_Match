-- 1. 시퀀스를 현재 최대값(350000)보다 큰 값으로 설정
SELECT setval('users_user_id_seq', (SELECT COALESCE(MAX(user_id), 1) FROM users), true);

-- 2. 설정 후 다음에 생성될 값 확인 (350001이 나와야 함)
SELECT nextval('users_user_id_seq');

-- 3. 시퀀스를 다시 350001로 되돌리기 (위에서 nextval로 하나 증가했으므로)
SELECT setval('users_user_id_seq', 350001, false);
