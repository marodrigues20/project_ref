package com.tenx.tsys.proxybatch.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tracing")
public class TracingProperties {

  /**
   * Address of XRay UDP service to forward tracing data to. No address means not forwarding it
   * XRay.
   */
  @Size(min = 1)
  private String xrayAddress;

  /**
   * Rate at which to sample requests at.
   */
  @NotNull
  @PositiveOrZero
  private Float samplingRate = 1.0f;

  public String getXrayAddress() {
    return xrayAddress;
  }

  public void setXrayAddress(final String xrayAddress) {
    this.xrayAddress = xrayAddress;
  }

  public Float getSamplingRate() {
    return samplingRate;
  }

  public void setSamplingRate(final Float samplingRate) {
    this.samplingRate = samplingRate;
  }
}
