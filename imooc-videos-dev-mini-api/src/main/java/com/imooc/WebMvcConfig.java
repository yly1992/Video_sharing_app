package com.imooc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/**")
		        .addResourceLocations("classpath:/META-INF/resources/")
				.addResourceLocations("file:/Users/liyuanyan/Documents/imooc_videos_dev/");
		super.addResourceHandlers(registry);
		
	}

}
