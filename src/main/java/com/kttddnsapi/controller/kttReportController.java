package com.kttddnsapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kttddnsapi.model.kttReport;
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
@RestController
@RequestMapping("report")
@CrossOrigin(origins = "*")
public class kttReportController {
  Logger log = LoggerFactory.getLogger("kttReport");
  /// emergency report
  @PostMapping("/show_reporter")
  public ResponseEntity<InputStreamResource> show_er_info(@RequestBody Map<String, Object> request)
  {
    String[] csvHeader = { "번호", "이름" };
    ByteArrayInputStream byteArrayOutputStream;
    try (
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter("UTF-8"),CSVFormat.DEFAULT.withHeader(csvHeader));
    ){
      csvPrinter.flush();
      byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
      InputStreamResource fileInputStream = new InputStreamResource(byteArrayOutputStream);

      String csvFileName = "people.csv";

      //////Http 헤더설정
      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
          
      //////Content Type 설정(text/csv)
      headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
      headers.set(HttpHeaders.CONTENT_ENCODING, "UTF-8");
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName)
          .contentType(MediaType.parseMediaType("application/csv"))
          .body(fileInputStream);
      
      //return new ResponseEntity<Resource>(fileInputStream,headers,HttpStatus.OK);
    }catch (IOException e) {
      throw new RuntimeException(e.getMessage());
   }
    catch(Exception e) {
    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
  }
   
  /**
   * 
  * @Method Name : reporting
  * @작성일 : 2023. 2. 14. 
  * @작성자 : Foryoucom
  * @변경이력 :
  * @Method 설명 :emergency 리포트 기록 함수
  * @param info
  * @param resource
  * https://github.com/taptap/ratelimiter-spring-boot-starter
   */
  @PutMapping(value = "/er/{info}")
  @ResponseStatus(HttpStatus.OK)
 // @RateLimit(rate = 5, rateInterval = "10s", keys = {"#info"})
  public void emergency_reporting(@PathVariable( "info" ) String info, @RequestBody kttReport resource) {
      Preconditions.checkNotNull(resource);
     // System.out.println("info :" + info);
      //System.out.println(resource.toString());

      if(info.equals(resource.getMac())){
       
       log.info("emergency infection {} {}", info, resource.getEventtype());
        log.debug(resource.getEventbody());
      } 
  }
  
}
