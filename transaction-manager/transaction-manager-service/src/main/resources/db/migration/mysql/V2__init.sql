alter table authorisations modify transaction_id varchar(10);
alter table authorisations add correlation_id varchar(10) AFTER transaction_id;