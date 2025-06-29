--DROP TABLE NameList;
create table NameList(
seq integer primary key autoincrement,
label varchar(36),
directive varchar(50),
matchType  varchar(30),
data  varchar(500)
);

CREATE INDEX NameList_idx1 ON NameList (directive);
