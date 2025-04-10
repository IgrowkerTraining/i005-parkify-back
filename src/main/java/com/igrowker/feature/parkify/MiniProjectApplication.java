package com.igrowker.feature.parkify;

import com.igrowker.feature.parkify.features.auth.config.JwtProperties;
import com.igrowker.feature.parkify.features.config.config.InitialConfigProperties;
import com.igrowker.feature.parkify.features.content.config.FooterProperties;
import com.igrowker.feature.parkify.features.content.config.HomeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		JwtProperties.class,
		FooterProperties.class,  HomeProperties.class, InitialConfigProperties.class
})
public class MiniProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniProjectApplication.class, args);
	}

}
