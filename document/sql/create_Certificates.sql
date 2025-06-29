--DROP TABLE Certificates;
create table Certificates(
id	INTEGER PRIMARY KEY AUTOINCREMENT,--主键，自增ID
serialNumber	VARCHAR(64),--唯一证书序列号（十六进制）
subject	VARCHAR(255),--证书主题（e.g., CN=example.com）
issuerId	INTEGER,--关联issuers表的签发CA ID
notBefore	CHAR(29),--证书生效时间
notAfter	CHAR(29),--证书过期时间
revoked	BOOLEAN	,--	是否吊销（默认false）
revocationTime	CHAR(29),--吊销时间
revocationReason	VARCHAR(64),--吊销原因（RFC5280标准）
pemData	TEXT,--PEM格式证书内容
keyUsage	VARCHAR(255),--密钥用途（e.g., digitalSignature, keyEncipherment）
createdAt	CHAR(29)--创建时间
);

CREATE INDEX Certificates_idx1 ON Certificates (serialNumber);
CREATE INDEX Certificates_idx2 ON Certificates (issuerId);
CREATE INDEX Certificates_idx3 ON Certificates (revoked);
CREATE INDEX Certificates_idx4 ON Certificates (notAfter);

