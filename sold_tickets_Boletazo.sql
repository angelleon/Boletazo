USE Boletazo;

DROP TABLE IF EXISTS Sold_tickets;

-- this table save the information relative to sold tickets
CREATE TABLE Sold_tickets
(
    idTicket BIGINT(10) NOT NULL,
    idCard int NOT NULL,
    idUser int(11) NOT NULL,
    dateSale timestamp NOT NULL DEFAULT current_timestamp, 
    FOREIGN KEY (idUser) REFERENCES UserInfo(idUser),
    FOREIGN KEY (idTicket) REFERENCES Ticket(idTicket) 
);

-- TODO: Crear un tipo record para completar la funcion
-- 
-- CREATE FUNCTION SELECT_DAILY_REPORT ()
--      RETURN DR_RECORD_T
-- BEGIN
--     SELECT E.name event, V.name place, SE.cost, T.idStatus status,
--            ST.idCard card, ST.idUser user
--     FROM Ticket T, Event E, Venue V, Section SE, Sold_tickets ST
--     WHERE T.idStatus = 2 
--     AND E.idEvent = T.idEvent 
--     AND E.idVenue = V.idVenue 
--     AND T.idSection = SE.idSection 
--     AND T.idTicket = ST.idTicket 
--     AND DATE_FORMAT(ST.dateSale, '%H:%i:%s') > '00:00:00' 
--     AND DATE_FORMAT(ST.datesale, '%H:%i:%s') < '23:59:59' 
--     AND DATE_FORMAT(ST.datesale, '%d-%m-%Y') = DATE_FORMAT(CURDATE()-1,'%d-%m-%Y') 
--     ORDER BY SE.cost, ST.idCard;
-- END;

-- UPDATE tickets sold
-- UPDATE ticket SET idstatus = 1 WHERE idstatus !=1 ;

-- how many tickets are sold
-- SELECT count(idstatus) FROM ticket WHERE idstatus != 1;

DROP PROCEDURE IF EXISTS ResetBoletazo;

DELIMITER //
CREATE PROCEDURE ResetBoletazo()
BEGIN
    DELETE FROM Sold_tickets;
    UPDATE Ticket 
    SET idStatus = 1
    WHERE idStatus != 1;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS Sell_tickets;

DELIMITER //

CREATE PROCEDURE Sell_tickets (n_tickets INTEGER)
L_selll_tickets:
BEGIN 
    DECLARE i INT DEFAULT 1;
    IF n_tickets > (SELECT COUNT(*) FROM Ticket) OR n_tickets <= 0 THEN 
        LEAVE L_selll_tickets;
    END IF;
    UPDATE Ticket
    SET idStatus = 3
    WHERE idTicket <= n_tickets;
    
    WHILE i <= n_tickets DO
        INSERT INTO Sold_tickets
               VALUES (i, 1234567890 + i * 10, 1, CURRENT_TIMESTAMP);
        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

