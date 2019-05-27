CREATE USER IF NOT EXISTS 'boletazodev'@'localhost' IDENTIFIED BY 'contrapass';
GRANT ALL PRIVILEGES ON Boletazo.* TO 'boletazodev'@'localhost' WITH GRANT OPTION;
CREATE USER IF NOT EXISTS 'boletazodev'@'%' IDENTIFIED BY 'contrapass';
GRANT ALL PRIVILEGES ON Boletazo.* TO 'boletazodev'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

