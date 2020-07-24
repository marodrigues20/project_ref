package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Fee;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

public interface FeesCheckerService {

  Optional<Fee> check(@NonNull final Cain001 cain001, @NonNull final UUID subscriptionKey);

  Optional<Fee> check(@NonNull final Cain003 cain003, @NonNull final UUID subscriptionKey);

}
