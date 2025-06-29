--DROP TABLE Crl;
create table Crl(
id	INTEGER PRIMARY KEY AUTOINCREMENT,--主键
issuerId	INTEGER,--关联issuers表
thisUpdate	CHAR(29),--CRL发布时间
nextUpdate	CHAR(29),--下次更新截止时间
crlNumber	INTEGER,--单调递增的CRL版本号
crlData	TEXT --DER格式CRL文件（可选）
);

CREATE INDEX Crl_idx1 ON Crl (issuerId);
CREATE INDEX Crl_idx2 ON Crl (nextUpdate);

