package com.trihydro.odewrapper.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {
    
    // @Bean
    // public Docket productApi() {
    //     return new Docket(DocumentationType.SWAGGER_2)
    //             .select()
    //             .apis(RequestHandlerSelectors.basePackage("com.trihydro.odewrapper.controller"))            
    //             .build()         
    //             .apiInfo(metaData());
    // }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()                 
                .apis(RequestHandlerSelectors.basePackage("com.trihydro.odewrapper.controller"))
                .paths(PathSelectors.any())   
                .build()
                .apiInfo(metaData());       
    }

    // @Bean
    // public Docket api() { 
    //     return new Docket(DocumentationType.SWAGGER_2)  
    //       .select()                                  
    //       .apis(RequestHandlerSelectors.any())              
    //       .paths(PathSelectors.any())                          
    //       .build();                                           
    // }

    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo("WYDOT Connected Vehicle REST API", "API Endpoints to create and manage CV TIMs from WYDOT Applications.", "1.0", "Terms of service", new Contact("Trihydro", "www.trihydro.com", "kperry@trihydro.com"), "Apache License Version 2.0", "https://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
        return apiInfo;
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    
}