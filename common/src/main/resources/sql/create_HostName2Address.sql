--DROP TABLE HostName2Address;
create table HostName2Address(
seq integer primary key autoincrement,
label varchar(36),
hostName varchar(400),
address  varchar(300),
status  varchar(2),
updateTime char(29),
priority  integer
);

CREATE INDEX hostName2Address_idx1 ON hostName2Address (hostName);
CREATE INDEX hostName2Address_idx2 ON hostName2Address (updateTime);
