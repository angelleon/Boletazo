USE Boletazo;

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

