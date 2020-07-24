package com.tenx.universalbanking.transactionmanager.rest.request.dcm;

import lombok.Value;

@Value
class GetCardRequest {

  private final String panHash;
}
