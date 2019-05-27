-- this table save the information relative to sold tickets
reate table sold_tickets
(
    idticket bigint(10) not null,
    idcard int not null,
    iduser int(11) not null,
    datesale timestamp not null default current_timestamp, 
    foreign key (iduser) references userinfo(iduser),
    foreign key (idticket) references ticket(idticket) 
);

-- select for daily report 
select event.name Event,venue.name Place,section.cost,ticket.idstatus,sold_tickets.idcard, sold_tickets.iduser 
from ticket,event,venue,section,sold_tickets
where ticket.idstatus > 1
and event.idevent = ticket.idevent
and event.idvenue = venue.idvenue
and ticket.idsection = section.idsection
and ticket.idticket = sold_tickets.idticket
and date_format(sold_tickets.datesale,'%H:%i:%s') > '00:00:00'
and date_format(sold_tickets.datesale,'%H:%i:%s') < '23:59:59'
and date_format(sold_tickets.datesale,'%d-%m-%Y') == date_format(curdate()-1,'%d-%m-%Y')
order by section.cost,sold_tickets.idcard; 

-- update tickets sold
update ticket set idstatus = 1 where idstatus !=1 ;

-- how many tickets are sold
select count(idstatus) from ticket where idstatus != 1;

create procedure 'restoreticket'()
begin
    update ticket 
    set idstatus = 1
    where idstatus > 1;
end
