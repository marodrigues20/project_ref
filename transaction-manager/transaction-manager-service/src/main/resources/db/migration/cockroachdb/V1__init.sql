create table authorisations (
  transaction_id varchar(64) not null,
  tsys_account_id varchar(36) not null,
  bank_net_reference_number varchar(9) not null,
  transaction_date date not null,
  transaction_amount decimal(17,5) not null,
  authorisation_code varchar(6) not null,
  mcc varchar(4),
  correlation_Id  varchar(64),
  matched boolean,
  expired boolean,
  transaction_type varchar(15),
  primary key (bank_net_reference_number, transaction_date, authorisation_code)
);