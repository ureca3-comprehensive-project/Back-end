-- Plan 데이터
INSERT INTO `plan` (plan_id, name, base_price, network_type) VALUES
    (1, 'LTE 다이렉트 45', 45000.00, 'LTE'),
    (2, '5G 프리미어 슈퍼', 115000.00, 'FIVE_G'),
    (3, '추가 요금 걱정 없는 데이터 69', 69000.00, 'LTE');

-- PlanItem 데이터 (300분 = 18000초)
INSERT INTO `plan_item` (item_id, plan_id, item_type, limit_amount, unit_type) VALUES
    (1, 1, 'VOICE', 18000.00, 'SEC'),
    (2, 1, 'VIDEO', 18000.00, 'SEC'),
    (3, 2, 'VOICE', 18000.00, 'SEC'),
    (4, 2, 'VIDEO', 18000.00, 'SEC'),
    (5, 3, 'VOICE', 18000.00, 'SEC'),
    (6, 3, 'VIDEO', 18000.00, 'SEC');

-- 초과 과금 규칙 데이터(음성 초과 1.98원, 영상 초과 3.3원)
INSERT INTO `over_usage_rule` (over_usage_rule_id, item_id, charge_unit, additional_price) VALUES
    -- LTE 다이렉트 45
    (1, 1, '1SEC', 1.98),
    (2, 2, '1SEC', 3.30),
    -- 5G 프리미어 슈퍼
    (3, 3, '1SEC', 1.98),
    (4, 4, '1SEC', 3.30),
    -- 추가 요금 걱정 없는 데이터 69
    (5, 5, '1SEC', 1.98),
    (6, 6, '1SEC', 3.30);

-- 할인 정책 데이터
INSERT INTO `discount_policy` (policy_id, name, rate, discount_limit, category) VALUES
    (1, '장애인/국가유공자', 0.3500, 9999999.00, 'AGREEMENT'),
    (2, '생계/의료급여 수급자', 0.5000, 36850.00, 'AGREEMENT'),
    (3, '차상위계층', 0.3500, 11550.00, 'BENEFIT'),
    (4, '기초연금수급자', 0.5000, 12100.00, 'BENEFIT');

-- 부가서비스 데이터
INSERT INTO `vas` (vas_id, name, monthly_price) VALUES
    (1, 'V컬러링', 3300.00),
    (2, '매너콜', 1100.00);

-- 납부예정일 데이터
INSERT INTO `due_date` (due_date_id, date) VALUES
    (1, 15),
    (2, 18),
    (3, 22),
    (4, 26);

-- 템플릿 데이터
INSERT INTO `template` (type, title, body, created_at, updated_at) VALUES
(
    'EMAIL',
    '[청구안내] {{billingMonth}} 회선 {{phone}} 요금 청구 안내',
    '안녕하세요.

회선 {{phone}}의 {{billingMonth}} 이용 요금 청구서가 발행되었습니다.',
    NOW(),
    NOW()
),
(
    'SMS',
    '[청구안내]',
    '{{billingMonth}} {{phone}} 요금 {{totalAmount}}원 청구되었습니다.',
    NOW(),
    NOW()
),
(
    'PUSH',
    '*****청구안내*****',
    '{{billingMonth}} {{phone}} 요금 {{totalAmount}}원 청구되었습니다.',
    NOW(),
    NOW()
);