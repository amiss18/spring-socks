package lol.maki.socks.config;

import java.net.URI;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class SecurityConfig {
	private final URI authorizationServerLogoutUrl;

	public SecurityConfig(OAuth2ClientProperties clientProperties) {
		this.authorizationServerLogoutUrl = clientProperties.getProvider().values().stream().findFirst()
				.map(OAuth2ClientProperties.Provider::getIssuerUri)
				.map(UriComponentsBuilder::fromHttpUrl)
				.map(builder -> builder.replacePath("logout").build().toUri())
				.orElseThrow();
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		final RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
		logoutSuccessHandler.setLogoutSuccessUrl(this.authorizationServerLogoutUrl);
		return http
				.authorizeExchange(exchanges -> exchanges
						.matchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
						.pathMatchers("/demo").permitAll()
						.anyExchange().permitAll()
				)
//				.oauth2Login(Customizer.withDefaults())
//				.logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
				.csrf(csrf -> csrf.disable() /* TODO */)
				.build();
	}

	@Bean
	public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
			ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
				.clientCredentials()
				.build();
		final DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		return authorizedClientManager;
	}
}
