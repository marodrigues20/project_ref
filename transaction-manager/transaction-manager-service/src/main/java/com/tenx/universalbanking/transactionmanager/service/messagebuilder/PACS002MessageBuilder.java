package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.Pacs002Enum;
import com.tenx.universalbanking.transactionmessage.enums.Pacs008Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;

@Component
public class PACS002MessageBuilder {

    @Autowired
    private MessageServiceProcessorHelper messageServiceProcessorHelper;


    public TransactionMessage buildPacs002Response(TransactionMessage fpsInTransactionMessage, PaymentDecisionTransactionResponse paymentDecisionResponse){

        TransactionMessage transactionMessage = new TransactionMessage();
        TransactionMessageHeader header = new TransactionMessageHeader();
        header.setType(fpsInTransactionMessage.getHeader().getType());
        transactionMessage.setHeader(header);
        transactionMessage.setMessages(
                Collections.singletonList(
                        buildPacs002PaymentMessage(fpsInTransactionMessage.getMessages().get(0),paymentDecisionResponse)
                )
        );

        transactionMessage.setAdditionalInfo(buildTransactionMessageAdditionalInfo(fpsInTransactionMessage, paymentDecisionResponse));

        return transactionMessage;
    }

    private Map<String, Object> buildTransactionMessageAdditionalInfo(TransactionMessage fpsInTransactionMessage, PaymentDecisionTransactionResponse paymentDecisionResponse){

        Map<String, Object> responseAdditionalInfo = new HashMap<>();

        responseAdditionalInfo.putAll(fpsInTransactionMessage.getAdditionalInfo());
        responseAdditionalInfo.put(TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS.name(),paymentDecisionResponse.getDecisionResponse());
        return responseAdditionalInfo;
    }

    private PaymentMessage buildPacs002PaymentMessage(PaymentMessage pacs008TransactionMessage, PaymentDecisionTransactionResponse paymentDecisionResponse){

        PaymentMessage paymentMessage = new PaymentMessage();

        Map<String, Object> message = new HashMap<>();

        Map<String, Object> pacs008MessageMap = pacs008TransactionMessage.getMessage();
        message.put(Pacs002Enum.OUTCOME.name(), paymentDecisionResponse.getDecisionResponse());
        message.put(Pacs002Enum.EXTERNAL_REFERENCE_ID.name(), pacs008MessageMap.get(Pacs008Enum.MESSAGE_ID.name()));
        message.put(Pacs002Enum.CORE_BANKING_REFERENCE_ID.name(),"");
        message.put(Pacs002Enum.POSTING_DATE.name(),"");
        message.put(Pacs002Enum.REQUESTING_SYSTEM_ID.name(),"");
        message.put(Pacs002Enum.SUPPLEMENTARY_DATA_CBS_RESPONSE_CODE.name(),"");
        message.put(Pacs002Enum.ERRORS.name(),"");
        message.put(Pacs002Enum.RESPONSE_CODE.name(),"");
        message.put(Pacs002Enum.RESPONSE_DESCRIPTION.name(),"");


        if(!messageServiceProcessorHelper.isPaymentDecisionSuccess(paymentDecisionResponse)){
            message.put(Pacs002Enum.ERRORS.name(), paymentDecisionResponse.getDecisionReason().getMessage());
            message.put(Pacs002Enum.RESPONSE_CODE.name(), paymentDecisionResponse.getDecisionReason().getCode());
            message.put(Pacs002Enum.RESPONSE_DESCRIPTION.name(), paymentDecisionResponse.getDecisionReason().getMessage());
        }

        paymentMessage.setType(PaymentMessageTypeEnum.PACS002.name());
        paymentMessage.setMessage(message);
        paymentMessage.setAdditionalInfo(pacs008TransactionMessage.getAdditionalInfo());

        return paymentMessage;

    }
}
