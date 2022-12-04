
package lbg.example.kafkasemantics.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HadoopRecord {

  @JsonProperty("InvoiceNumber")
  private String invoiceNumber;
  @JsonProperty("CreatedTime")
  private Long createdTime;
  @JsonProperty("StoreID")
  private String storeID;
  @JsonProperty("PosID")
  private String posID;
  @JsonProperty("CustomerType")
  private String customerType;
  @JsonProperty("PaymentMethod")
  private String paymentMethod;
  @JsonProperty("DeliveryType")
  private String deliveryType;
  @JsonProperty("City")
  private String city;
  @JsonProperty("State")
  private String state;
  @JsonProperty("PinCode")
  private String pinCode;
  @JsonProperty("ItemCode")
  private String itemCode;
  @JsonProperty("ItemDescription")
  private String itemDescription;
  @JsonProperty("ItemPrice")
  private Double itemPrice;
  @JsonProperty("ItemQty")
  private Integer itemQty;
  @JsonProperty("TotalValue")
  private Double totalValue;

}
