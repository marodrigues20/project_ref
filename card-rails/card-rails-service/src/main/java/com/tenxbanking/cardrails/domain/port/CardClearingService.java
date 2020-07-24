package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.Cain003;
import lombok.NonNull;

public interface CardClearingService {

  void process(@NonNull final Cain003 cain003);

}
