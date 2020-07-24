package com.tenxbanking.cardrails.adapter.primary.rest.resource;

import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DebitCardController {

  private final DebitCardService debitCardService;
  private final CardSettingsService cardSettingsService;

  @ApiOperation(value = "Debit Card Cache Eviction", nickname = "evictDebitCardData")
  @DeleteMapping("/v1/card/{panHash}")
  public ResponseEntity evictDebitCardData(@PathVariable("panHash") final String panHash) {
    debitCardService.evictDebitCardByCardIdHash(panHash);
    cardSettingsService.evictDebitCardByCardIdHash(panHash);
    return ResponseEntity.noContent().build();
  }

}
