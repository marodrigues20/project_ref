package com.tenxbanking.cardrails.adapter.primary.rest.resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DebitCardControllerTest {

  private MockMvc mvc;

  @InjectMocks
  private DebitCardController controller;

  @Mock
  private DebitCardService service;

  @Mock
  private CardSettingsService cardSettingsService;

  @BeforeEach
  public void setupMvc() {
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void shouldRespondWithNoContentWhenDeletionSuccessful() throws Exception {

    mvc.perform(delete("/v1/card/{panHash}", "12345"))
        .andExpect(status().isNoContent());

    verify(service).evictDebitCardByCardIdHash("12345");
    verify(cardSettingsService).evictDebitCardByCardIdHash("12345");
  }

}