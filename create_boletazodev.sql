CREATE USER 'boletazodev'@'localhost' IDENTIFIED BY 'contrapass';
GRANT ALL PRIVILEGES ON Boletazo.* TO 'boletazodev'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

