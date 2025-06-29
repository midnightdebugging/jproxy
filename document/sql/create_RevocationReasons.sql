--DROP TABLE RevocationReasons;
create table RevocationReasons(
code	VARCHAR(16),--原因码（RFC5280）
description	VARCHAR(255)--描述（e.g., "密钥泄露"）
);


