package com.kttddnsapi.model;
import java.sql.Time;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;
@Data
//@Getter
//@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class kttReport {
  @JsonProperty("mac")
  private String mac;
  @JsonProperty("auth")
  @JsonSetter(nulls = Nulls.SKIP)
  private String auth = "";
  @JsonProperty("eventtype")
  private Integer eventtype;
  @JsonProperty("eventbody")
  @JsonSetter(nulls = Nulls.SKIP)
  private String eventbody="";
  @JsonProperty("log_msg")
  @JsonSetter(nulls = Nulls.SKIP)
  private String log_msg;
  @JsonProperty("date")
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private Time date;
  
  public String toString()
  {
    return "mac" + mac + "eventtype" + eventtype;
  }
  
  public String getMac()
  {
    return this.mac;
  }
  
  public Integer getEventtype()
  {
    return this.eventtype;
  }
  
  public String getEventbody()
  {
    return this.eventbody;
  }
  
}
