alter table authorisations add transaction_type varchar(15) AFTER expired;

update authorisations set transaction_type = 'AUTH' where transaction_type is null