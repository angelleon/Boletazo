CREATE TABLE LoginInfo(idlogin int PRIMARY KEY AUTO_INCREMENT NOT NULL,
username varchar(64) NOT null unique,
password varchar(64) NOT null);

CREATE TABLE UserInfo(iduser int PRIMARY KEY AUTO_INCREMENT NOT NULL,
email varchar(64) NOT NULL,
estado varchar(40) NOT NULL, 
idlogin int,
FOREIGN KEY(idlogin) REFERENCES LoginInfo(idlogin));
