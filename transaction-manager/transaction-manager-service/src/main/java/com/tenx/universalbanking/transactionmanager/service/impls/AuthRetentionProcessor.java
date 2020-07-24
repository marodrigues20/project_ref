package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.reconciliation.logger.model.Event.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.CARD_PROCESSOR_ACCOUNTID;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.EXPIRED_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.EXPIRED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;

import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthRetentionProcessor {


  @Autowired
  private AuthorisationFinder authorisationFinder;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private DateConversionUtils dateUtils;

  @Autowired
  private PaymentAuthorisations authorisationsRepository;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  private static final int DAYS_TO_RETAIN = 4;

  public void authRetention() {
    LocalDate date = LocalDate.now().minusDays(DAYS_TO_RETAIN);
    LocalDate prevBusinessDate = dateUtils.getLastBusinessDate(date);
    List<Authorisations> authsToBeCleared = authorisationFinder
        .getUnmatchedNonReversedAuthorisations(prevBusinessDate, false, false);
        updateDB(authsToBeCleared).stream().forEach(auth -> {
              TransactionMessage transactionMessage = createExpireAuthMessage(auth);
              messageSender.sendPaymentMessage(auth.getTsysAccountId(), transactionMessage);
              reconciliationHelper.saveReconciliationMessage(
                  buildReconciliationMessage(transactionMessage));
            }
    );
  }

  private List<Authorisations> updateDB(List<Authorisations> authsToBeCleared) {
    authsToBeCleared.stream().forEach(
        auth -> auth.setExpired(true)
    );
    authorisationsRepository.saveAll(authsToBeCleared);
    return authsToBeCleared;
  }

  private TransactionMessage createExpireAuthMessage(Authorisations auth){
    TransactionMessage transactionMessage = new TransactionMessage();

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.EXPIRED_AUTH.name());
    Map<String, Object> message =  new HashMap<>();
    message.put(TRANSACTION_AMOUNT.name(), auth.getTransactionAmount());
    message.put(TRANSACTION_DATE.name(), auth.getId().getTransactionDate());
    message.put(AUTHORISATION_CODE.name(), auth.getId().getAuthorisationCode());
    message.put(MERCHANT_CATEGORY_CODE.name(), auth.getMcc());
    message.put(BANKNET_REFERENCE_NUMBER.name(), auth.getId().getBankNetReferenceNumber());
    message.put(CARD_PROCESSOR_ACCOUNTID.name(), auth.getTsysAccountId());
    paymentMessage.setMessage(message);
    Map<String, Object> paymentAddtnlInfo =  new HashMap<>();
    paymentAddtnlInfo.put(TRANSACTION_ID.name(), auth.getTransactionId());
    paymentMessage.setAdditionalInfo(paymentAddtnlInfo);

    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(EXPIRED_AUTH.name());
    transactionMessage.setHeader(header);
    transactionMessage.setMessages(Collections.singletonList(paymentMessage));
    Map<String, Object> addtnlInfo =  new HashMap<>();
    addtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), auth.getCorrelationId());
    transactionMessage.setAdditionalInfo(addtnlInfo);
    messageServiceProcessorHelper.addTransactionStatus(transactionMessage, EXPIRED);
    return  transactionMessage;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(SUCCESS).
        scope(TransactionMessageTypeEnum.EXPIRED_AUTH).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }
}

