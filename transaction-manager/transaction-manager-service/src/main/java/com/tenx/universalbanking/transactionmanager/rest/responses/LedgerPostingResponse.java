package com.tenx.universalbanking.transactionmanager.rest.responses;


import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;

public class LedgerPostingResponse {

  private boolean postingSuccess;

  private ReasonDto reason;

  public boolean isPostingSuccess() {
    return postingSuccess;
  }

  public void setPostingSuccess(boolean postingSuccess) {
    this.postingSuccess = postingSuccess;
  }

  public ReasonDto getReason() {
    return reason;
  }

  public void setReason(ReasonDto reason) {
    this.reason = reason;
  }
}
