USE Boletazo;

DROP TABLE IF EXISTS UserInfo;
DROP TABLE IF EXISTS LoginInfo;

CREATE TABLE
    LoginInfo(idLogin INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL);

CREATE TABLE IF NOT EXISTS
    UserInfo(iduser int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    email VARCHAR(64) NOT NULL,
    estado VARCHAR(40) NOT NULL, 
    idLogin INTEGER,
    FOREIGN KEY(idLogin) REFERENCES LoginInfo(idLogin));

INSERT INTO LoginInfo(username, password)
       VALUES('juanitoPerez', 'contrapassword');

INSERT INTO UserInfo(email,
                     estado,
                     idLogin)
       VALUES ('boletazo.mail+juanitoPerez@gmail.com',
               'hasta la monja',
               (SELECT idLogin
                FROM LoginInfo
                WHERE username = 'juanitoPerez'));
