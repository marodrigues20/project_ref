package com.tenxbanking.cardrails.domain.model;

import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CARD_HOLDER_NOT_PRESENT;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static com.tenxbanking.cardrails.domain.model.card.Channel.MAG_STRIPE;

import com.tenxbanking.cardrails.domain.model.card.Channel;
import java.util.List;
import lombok.Getter;

@Getter
public enum PaymentMethodType {
  DOMESTIC_CASH_WITHDRAWAL(
      ATM
  ),
  INTERNATIONAL_CASH_WITHDRAWAL(
      ATM,
      INTERNATIONAL
  ),
  CHIP_PIN(
      CHIP_AND_PIN
  ),
  CONTACTLESS(
      Channel.CONTACTLESS
  ),
  DOMESTIC_POS_CHIP_AND_PIN(
      CHIP_AND_PIN
  ),
  INTERNATIONAL_POS_CHIP_AND_PIN(
      CHIP_AND_PIN,
      INTERNATIONAL
  ),
  POS_MAG_STRIPE(
      MAG_STRIPE
  ),
  ATM_MAG_STRIPE(
      MAG_STRIPE,
      ATM
  ),
  MAIL_TELEPHONE_ORDER(
      CARD_HOLDER_NOT_PRESENT
  ),
  ONLINE,
  UNKNOWN;

  private final List<Channel> requiredChannels;


  PaymentMethodType(Channel... requiredChannels) {
    this.requiredChannels = List.of(requiredChannels);
  }

}
