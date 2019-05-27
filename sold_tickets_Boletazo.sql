USE Boletazo;

DROP TABLE IF EXISTS Sold_tickets

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
--     SELECT E.name event, V.name place, SE.cost, Ticket.idStatus, ST.idCard, ST.idUser 
--     FROM Ticket T, Event E, Venue V, Section SE, Sold_Tickets ST
--     WHERE ticket.idstatus > 1 
--     AND E.idEvent = Ticket.idEvent 
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
    UPDATE Ticket 
    SET idStatus = 1
    WHERE idStatus != 1;
END //
DELIMITER ;
