package com.kttddnsapi.model;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

//@Getter
//@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode 
@ToString
@Slf4j
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ImergencyReport implements AutoCloseable 
{
	Logger log = LoggerFactory.getLogger("ImergencyReport");
	
	  private String log_data;
	  
	public String convertObjectToJsonString( ) 
	{
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		
		try 
		{
			jsonStr = mapper.writeValueAsString(this);
		}
		catch (JsonProcessingException e) 
		{
			//e.printStackTrace();
			log.debug(e.getLocalizedMessage());
		}
		return jsonStr;
	}
	
	public String getDate()
	{
		return this.log_data;
	}
	public void setLog_dump(String log_data) 
	{
		this.log_data = log_data;
	}
	
	
	public String getMac_macaddr()
	{
		return this.mac;
	}
	public void setMac(String apiMac)
	{
		this.mac = apiMac;
	}

	public String getIm_evt_body()
	{
		return this.im_evt_body;
	}
	public void setIm_evt_body(String infection_log_msg) 
	{
		this.im_evt_body = infection_log_msg;
	}
	
	public Short getLog_dump()
	{
		return this.im_evt_type;
	}
	public void setIm_evt_type(Short infection_type) 
	{
		this.im_evt_type = infection_type;
	}
	
	@JsonProperty("row_index")
	Integer idx;
	
	@JsonProperty("mac")
	String mac = "";
	
	@JsonProperty("im_evt_type")
	Short im_evt_type = 0;
	
	@JsonProperty("im_evt_body")
	String im_evt_body = "";
	
	@JsonProperty("im_evt_type")
	String log_dump = "";
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	java.sql.Timestamp date;
	String mac_macaddr;
	@Override
	public void close() throws Exception 
	{
		idx = null;
		mac = null;
		im_evt_type = null;
		im_evt_body = null;
		log_dump = null;
	}
}
