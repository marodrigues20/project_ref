package com.tenxbanking.cardrails.domain.validator.rule;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;

public interface RequestValidationRule {

  ValidationRule getRule();

  List<ValidationRule> getApplicableRequest();

  Optional<ValidationFailure> validate(SchemeMessage schemeMessage);

}
