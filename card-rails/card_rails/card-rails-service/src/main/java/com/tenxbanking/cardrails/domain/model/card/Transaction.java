package com.tenxbanking.cardrails.domain.model.card;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Transaction {

  // == Fields ==

  private String Id;
  private String transactionDate;
  private String retrievalReferenceNumber;
  private Merchant Merchant;
  private String cardId;
  private Integer amount;
  private String authenticationCode;
  private int processingCode;
  //private Pos Pos;
  private int functionCode;
  private String transactionLifeCycleID;
  private String ICCData;
  //private String eCommerceSecurityLevelIndicator;
  private String mappingServiceAccountNumber;
  //private String walletIdentifier;
  private String dataRecord;
  private int messageNumber;
  //private String fileID;
  //private String acquirerReferenceData;






}
