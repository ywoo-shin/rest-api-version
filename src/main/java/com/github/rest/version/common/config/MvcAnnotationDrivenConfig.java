package com.github.rest.version.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import sun.awt.windows.WEmbeddedFrame;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MvcAnnotationDrivenConfig {
    @Bean
    public ConversionService conversionService() {
        return new FormattingConversionServiceFactoryBean().getObject();
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ConfigurableWebBindingInitializer webBindingInitializer() {
        ConfigurableWebBindingInitializer configurableWebBindingInitializer = new ConfigurableWebBindingInitializer();
        configurableWebBindingInitializer.setConversionService(conversionService());
        configurableWebBindingInitializer.setValidator(validatorFactoryBean());
        return configurableWebBindingInitializer;
    }

    @Bean
    public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
        ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setMessageConverters(messageConverters());
        return exceptionHandlerExceptionResolver;
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
        requestMappingHandlerAdapter.setWebBindingInitializer(webBindingInitializer());
        requestMappingHandlerAdapter.setMessageConverters(messageConverters());
        return requestMappingHandlerAdapter;
    }

    private List<HttpMessageConverter<?>> messageConverters() {
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(new ByteArrayHttpMessageConverter());
        list.add(new StringHttpMessageConverter());
        list.add(new ResourceHttpMessageConverter());
        list.add(new Jaxb2RootElementHttpMessageConverter());
        list.add(new MappingJackson2HttpMessageConverter());
        return list;
    }
}
