package com.kttddnsapi;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
//@MapperScan("org.kttddnsapi.dao")
//@SpringBootApplication
public class KttddnsapiApplication extends SpringBootServletInitializer
{
	/**
	 * Used when run as WAR
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(KttddnsapiApplication.class);
	}


	/**
	 * Used when run as JAR

	public static void main(String[] args) {
		SpringApplication.run(KttddnsapiApplication.class, args);
	}
	 */
}
