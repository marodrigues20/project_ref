package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Pacs002Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PACS002MessageBuilderTest {

    @InjectMocks
    private PACS002MessageBuilder pacs002MessageBuilder;

    @Mock
    private MessageServiceProcessorHelper messageProcessorhelper;


    private FileReaderUtil fileReader;
    private TransactionMessage message;
    private PaymentDecisionTransactionResponse paymentDecisionSuccessResponse;


    @Before
    public void init() throws IOException{
        final String PACS008_FILE = "message/pacs008TransactionMessage.json";
        fileReader = new FileReaderUtil();
        message = createTransactionMessage();
    }

    @Test
    public void shouldBuildHeaderType(){
        paymentDecisionSuccessResponse = createPaymentDecisionSuccessResponse();
        isPaymentDecisionSuccess();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getHeader().getType()).isEqualTo(TransactionMessageTypeEnum.FPS_IN.name());
    }

    @Test
    public void shouldSetTransactionStatus(){
        paymentDecisionSuccessResponse = createPaymentDecisionSuccessResponse();
        isPaymentDecisionSuccess();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getAdditionalInfo().get(TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS.name())).isEqualTo(PaymentDecisionResponse.SUCCESS.name());
    }

    @Test
    public void shouldCreatePac002(){
        paymentDecisionSuccessResponse = createPaymentDecisionSuccessResponse();
        isPaymentDecisionSuccess();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getMessages().get(0).getType()).isEqualTo(PaymentMessageTypeEnum.PACS002.name());
    }

    @Test
    public void shouldSetTransactionStatusFailedWhenPaymentDecisionResponseFailed(){
        paymentDecisionSuccessResponse = createPaymentDecisionFailedResponse();
        isPaymentDecisionFailed();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getAdditionalInfo().get(TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS.name())).isEqualTo(PaymentDecisionResponse.FAILED.name());
    }

    @Test
    public void shouldSetPorperty_OUTCOME_FailedWhenPaymentDecisionResponseFailed(){
        paymentDecisionSuccessResponse = createPaymentDecisionFailedResponse();
        isPaymentDecisionFailed();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getMessages().get(0).getMessage().get(Pacs002Enum.OUTCOME.name())).isEqualTo(PaymentDecisionResponse.FAILED.name());
    }

    @Test
    public void shouldSetErrorCodesEmptyWhenPaymentDecisionFailed(){
        paymentDecisionSuccessResponse = createPaymentDecisionFailedResponse();
        isPaymentDecisionFailed();
        TransactionMessage response = pacs002MessageBuilder.buildPacs002Response(message, paymentDecisionSuccessResponse);
        assertThat(response.getMessages().get(0).getMessage().get(Pacs002Enum.ERRORS.name())).isEqualTo(paymentDecisionSuccessResponse.getDecisionReason().getMessage());
        assertThat(response.getMessages().get(0).getMessage().get(Pacs002Enum.RESPONSE_CODE.name())).isEqualTo(paymentDecisionSuccessResponse.getDecisionReason().getCode());
        assertThat(response.getMessages().get(0).getMessage().get(Pacs002Enum.RESPONSE_DESCRIPTION.name())).isEqualTo(paymentDecisionSuccessResponse.getDecisionReason().getMessage());
    }


    private PaymentDecisionTransactionResponse createPaymentDecisionSuccessResponse(){

        PaymentDecisionTransactionResponse paymentDecisionResponse = new PaymentDecisionTransactionResponse();
        paymentDecisionResponse.setDecisionResponse(PaymentDecisionResponse.SUCCESS.name());

        return paymentDecisionResponse;
    }

    private PaymentDecisionTransactionResponse createPaymentDecisionFailedResponse(){

        PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
        reasonDTO.setCode(2100);
        reasonDTO.setMessage("Error Messsage");

        PaymentDecisionTransactionResponse paymentDecisionResponse = new PaymentDecisionTransactionResponse();
        paymentDecisionResponse.setDecisionResponse(PaymentDecisionResponse.FAILED.name());
        paymentDecisionResponse.setDecisionReason(reasonDTO);

        return paymentDecisionResponse;

    }

    private TransactionMessage createTransactionMessage() throws IOException {
        return stringToJson(
                fileReader.getFileContent("message/pacs008TransactionMessage.json"),
                TransactionMessage.class);
    }

    private void isPaymentDecisionSuccess(){
        when(messageProcessorhelper.isPaymentDecisionSuccess(any())).thenReturn(true);
    }

    private void isPaymentDecisionFailed(){
        when(messageProcessorhelper.isPaymentDecisionSuccess(any())).thenReturn(false);
    }
}
