package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenx.universalbanking.transactionmanager.service.impls.AuthRetentionProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class ExpiredAuthControllerTest {

  private MockMvc mvc;

  @Mock
  private AuthRetentionProcessor retentionProcessor;

  @InjectMocks
  private ExpiredAuthController expiredAuthController;

  @Before
  public void init() {
    mvc = MockMvcBuilders.standaloneSetup(expiredAuthController)
        .build();
  }

  @Test
  public void shouldProcessCain003Message() throws Exception {
    mvc.perform(post("/transaction-manager/v1/expired-auth-dropping")
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

}