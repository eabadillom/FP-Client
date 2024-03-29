package com.hoth.fingerprint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
	
	@Bean
	public Docket getDocket() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(getApiInfo())
				.select().apis(RequestHandlerSelectors.basePackage("com.hoth.fingerprint.controller"))
				.paths(PathSelectors.any()).build()
				.useDefaultResponseMessages(false)
				;
	}
	
	private ApiInfo getApiInfo() {
		return new ApiInfoBuilder()
				.title("FERBO fingerprint api REST.")
				.version("1.0")
				.license("Apache 2.0")
				.contact(new Contact("@sistemas", "https://www.ferbomex.com", "sistemas@ferbomex.com"))
				.build();
	}
		
}
