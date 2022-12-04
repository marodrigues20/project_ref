
package lbg.example.kafkasemantics.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryAddress {

  @JsonProperty("AddressLine")
  private String addressLine;
  @JsonProperty("City")
  private String city;
  @JsonProperty("State")
  private String state;
  @JsonProperty("PinCode")
  private String pinCode;
  @JsonProperty("ContactNumber")
  private String contactNumber;

}
