--DROP TABLE Issuers;
create table Issuers(
id	INTEGER PRIMARY KEY AUTOINCREMENT,--主键
commonName	VARCHAR(255),--CA标识（e.g., MyRoot-CA）
pemCert	TEXT,--CA证书PEM内容
isRoot	BOOLEAN	,--是否为根证书
parentId	INTEGER,--父级CA ID（根证书为空）
privateKeyRef	VARCHAR(255),--私钥存储路径（或HSM标识）
active	BOOLEAN --是否启用（默认true）
);

CREATE INDEX Issuers_idx1 ON Issuers (commonName);

