-- MySQL Script generated by MySQL Workbench
-- 11/04/16 12:39:37
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema Boletazo
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `Boletazo` ;

-- -----------------------------------------------------
-- Schema Boletazo
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `Boletazo` DEFAULT CHARACTER SET utf8 ;
SHOW WARNINGS;
USE `Boletazo` ;

SOURCE create_boletazodev.sql

-- -----------------------------------------------------
-- Table `Boletazo`.`Status`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Status` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Status` (
  `idStatus` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`idStatus`))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Section`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Section` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Section` (
  `idSection` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(15) NOT NULL,
  `totalCapacity` INT NOT NULL,
  `availableCapacity` INT NOT NULL,
  `cost` DECIMAL(4,2) NOT NULL,
  PRIMARY KEY (`idSection`))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Ticket`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Ticket` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Ticket` (
  `idTicket` BIGINT(10) NOT NULL,
  `seatNumber` VARCHAR(10) NOT NULL,
  `idStatus` INT NOT NULL,
  `idSection` INT NOT NULL,
 `idEvent` INT NOT NULL,  PRIMARY KEY (`idTicket`),
  CONSTRAINT `fk_Ticket_Status`
    FOREIGN KEY (`idStatus`)
    REFERENCES `Boletazo`.`Status` (`idStatus`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Ticket_Section1`
    FOREIGN KEY (`idSection`)
    REFERENCES `Boletazo`.`Section` (`idSection`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION, CONSTRAINT `fk_Ticket_Event`
    FOREIGN KEY (`idEvent`)
    REFERENCES `Boletazo`.`Event` (`idEvent`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `fk_Ticket_Status_idx` ON `Boletazo`.`Ticket` (`idStatus` ASC);

SHOW WARNINGS;
CREATE INDEX `fk_Ticket_Section1_idx` ON `Boletazo`.`Ticket` (`idSection` ASC);

SHOW WARNINGS;


CREATE INDEX `fk_Ticket_Event_idx` ON `Boletazo`.`Ticket` (`idEvent` ASC);

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Venue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Venue` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Venue` (
  `idVenue` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `address` VARCHAR(45) NOT NULL,
  `city` VARCHAR(45) NOT NULL,
  `country` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idVenue`))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Event` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Event` (
  `idEvent` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL,
  `date` DATETIME NOT NULL,
  `idVenue` INT NOT NULL,
  PRIMARY KEY (`idEvent`),
  CONSTRAINT `fk_Event_Venue1`
    FOREIGN KEY (`idVenue`)
    REFERENCES `Boletazo`.`Venue` (`idVenue`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `fk_Event_Venue1_idx` ON `Boletazo`.`Event` (`idVenue` ASC);

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Section_has_Event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Section_has_Event` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Section_has_Event` (
  `idSection` INT NOT NULL,
  `idEvent` INT NOT NULL,
  PRIMARY KEY (`idSection`, `idEvent`),
  CONSTRAINT `fk_Section_has_Event_Section1`
    FOREIGN KEY (`idSection`)
    REFERENCES `Boletazo`.`Section` (`idSection`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Section_has_Event_Event1`
    FOREIGN KEY (`idEvent`)
    REFERENCES `Boletazo`.`Event` (`idEvent`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `fk_Section_has_Event_Event1_idx` ON `Boletazo`.`Section_has_Event` (`idEvent` ASC);

SHOW WARNINGS;
CREATE INDEX `fk_Section_has_Event_Section1_idx` ON `Boletazo`.`Section_has_Event` (`idSection` ASC);

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Participant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Participant` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Participant` (
  `idParticipant` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`idParticipant`))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `Boletazo`.`Event_has_Participant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Boletazo`.`Event_has_Participant` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `Boletazo`.`Event_has_Participant` (
  `idEvent` INT NOT NULL,
  `idParticipant` INT NOT NULL,
  PRIMARY KEY (`idEvent`, `idParticipant`),
  CONSTRAINT `fk_Event_has_Participant_Event1`
    FOREIGN KEY (`idEvent`)
    REFERENCES `Boletazo`.`Event` (`idEvent`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Event_has_Participant_Participant1`
    FOREIGN KEY (`idParticipant`)
    REFERENCES `Boletazo`.`Participant` (`idParticipant`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `fk_Event_has_Participant_Participant1_idx` ON `Boletazo`.`Event_has_Participant` (`idParticipant` ASC);

SHOW WARNINGS;
CREATE INDEX `fk_Event_has_Participant_Event1_idx` ON `Boletazo`.`Event_has_Participant` (`idEvent` ASC);

SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

SOURCE InitialDataLoad.sql
SOURCE CREATE_LOGINFO_USERINFO.sql
SOURCE sold_tickets_Boletazo.sql