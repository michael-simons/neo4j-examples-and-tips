package org.neo4j.tips.cluster.sdn_ogm.config;

import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/api/movies/watched").authenticated()
			.antMatchers("/api/movies", "/api/movies/").permitAll()
			.requestMatchers(to(HealthEndpoint.class, MetricsEndpoint.class)).permitAll()
			.and()
			.csrf().disable()
			.httpBasic();
	}
}
