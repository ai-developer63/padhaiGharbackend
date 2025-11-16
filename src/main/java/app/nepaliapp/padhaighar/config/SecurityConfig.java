package app.nepaliapp.padhaighar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@SuppressWarnings("unused")
	@Autowired
	private CustomAuthenticationEntryPoint authenticationEntryPoint;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		return authenticationManagerBuilder.build();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .cors(cors -> cors.disable())
	        .authorizeHttpRequests(req -> req
	                .requestMatchers("/admin/**").hasRole("ADMIN")
	                .requestMatchers("/api/auth/**").permitAll()
	                .requestMatchers("/api/range/**").permitAll()
	                .requestMatchers("/api/**").authenticated()
	                .requestMatchers("/forgetpassword").permitAll()
	                .anyRequest().permitAll()
	        )
	        .formLogin(form -> form
	                .loginPage("/signin")
	                .loginProcessingUrl("/signin")
	                .successHandler(authenticationSuccessHandler)
	        )
	        .logout(logout -> logout
	                .logoutUrl("/logout")
	                .logoutSuccessUrl("/index")
	                .invalidateHttpSession(true)
	                .clearAuthentication(true)
	        )
	        // Unified exception handling
	        .exceptionHandling(exception -> exception
	            .authenticationEntryPoint((request, response, authException) -> {
	                String path = request.getRequestURI();
	                if (path.startsWith("/api/")) {
	                    // ✅ API requests (including video) → return 401, not redirect
	                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	                } else {
	                    // ✅ Web UI → redirect to login page
	                    response.sendRedirect("/signin?error=unauthorized");
	                }
	            })
	        )
	        .addFilterBefore(new JwtAuthenticationFilter(authenticationManager(http)),
	                UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}


}