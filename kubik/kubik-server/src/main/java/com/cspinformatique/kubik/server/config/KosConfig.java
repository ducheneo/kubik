package com.cspinformatique.kubik.server.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.cspinformatique.kubik.server.domain.kos.rest.KosTemplate;

@Configuration
public class KosConfig {
	@Resource
	private Environment environment;

	public @Bean KosTemplate kosTemplate() {
		return new KosTemplate(environment);
	}
}
