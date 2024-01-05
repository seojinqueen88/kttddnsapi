package com.kttddnsapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


 
@Getter
@Setter
@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AppStatustics {
Integer idx;
String jumin;
String app_name;
String app_type;
java.sql.Timestamp date;
}
