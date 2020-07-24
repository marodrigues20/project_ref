package com.tenx.tsys.proxybatch.rest;

import com.tenx.idempotency.request.annotation.Idempotent;
import com.tenx.tsys.proxybatch.dto.request.SettlementRequestDto;
import com.tenx.tsys.proxybatch.service.TsysProxyBatchService;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TsysProxyBatchController {

  @Autowired
  private TsysProxyBatchService tsysProxyBatchService;


  @InitBinder
  public void initBinder(WebDataBinder binder) {
    String[] allowedFields = new String[]{"SettlementRequestDto.settlementRequest"};
    binder.setAllowedFields(allowedFields);
  }

  /**
   * This method takes the object of input dto from TSYS Batch Posting Processor.
   *
   * @return void
   */
  @Idempotent(expiry = 5)
  @PostMapping(value = "/v1/settlement-records")
  public void tsysBatchProxyMethod(HttpServletResponse response,
      @RequestBody @Valid SettlementRequestDto settlementRequestDto)
      throws Exception {
    SettlementRequestDto requestDto = new SettlementRequestDto();
    requestDto.setSettlementRequest(settlementRequestDto.getSettlementRequest());
    tsysProxyBatchService
        .findSubscriptionKey(requestDto);
    response.setStatus(200);
  }
}
