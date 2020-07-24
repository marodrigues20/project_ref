package com.tenx.tsys.proxybatch.rest;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenx.tsys.proxybatch.dto.request.SettlementRequestDto;
import com.tenx.tsys.proxybatch.service.TsysProxyBatchService;
import com.tenx.tsys.proxybatch.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TsysProxyBatchControllerTest {

  private MockMvc mockMvc;

  @Mock
  private TsysProxyBatchService tsysProxyBatchService;

  @InjectMocks
  private TsysProxyBatchController controller;

  @Before
  public void init() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .build();
  }

  @Test
  public void tsysProxyBatchMethod_ShouldReturnStatusOk_WhenSuccess() throws Exception {

    SettlementRequestDto testData = new SettlementRequestDto();
    testData.setSettlementRequest("TEST_SETTLEMENT_REQUEST");
    doNothing().when(tsysProxyBatchService).findSubscriptionKey(testData);
    this.mockMvc.perform(post("/v1/settlement-records").
        content(JsonUtils.jsonToString(testData)).
        contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void tsysProxyBatchMethod_ShouldThrowException_WhenSettlementRequestNull()
      throws Exception {

    SettlementRequestDto testData = new SettlementRequestDto();
    testData.setSettlementRequest(null);
    this.mockMvc.perform(post("/v1/settlement-records").content(JsonUtils.jsonToString(testData))
        .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }
}
