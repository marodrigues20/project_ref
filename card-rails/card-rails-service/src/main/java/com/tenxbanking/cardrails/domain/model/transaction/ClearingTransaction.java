package com.tenxbanking.cardrails.domain.model.transaction;

import com.tenxbanking.cardrails.domain.model.Cain003;

public interface ClearingTransaction extends CardTransaction {

  Cain003 getCain003();

}
