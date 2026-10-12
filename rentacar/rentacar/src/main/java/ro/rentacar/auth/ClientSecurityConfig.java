package ro.rentacar.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ClientSecurityConfig {

    @Bean
    public PasswordEncoder clientPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService clientUserDetailsService(
            ClientAccountRepository accounts) {

        return email -> {
            ClientAccount account = accounts.findByEmailIgnoreCase(
                    ClientAccountService.normalizeEmail(email)
            ).orElseThrow(() ->
                    new UsernameNotFoundException("Cont inexistent.")
            );

            return User.withUsername(account.getEmail())
                    .password(account.getPasswordHash())
                    .roles("CLIENT")
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http,
            UserDetailsService users,
            PasswordEncoder encoder) throws Exception {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(users);

        provider.setPasswordEncoder(encoder);

        http.authenticationProvider(provider);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/client/account",
                        "/client/account/**"
                ).hasRole("CLIENT")
                .anyRequest().permitAll()
        );

        http.formLogin(login -> login
        .loginPage("/client/login")
        .loginProcessingUrl("/client/login")
        .usernameParameter("email")
        .defaultSuccessUrl("/", true)
        .failureUrl("/client/login?error")
        .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/client/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        return http.build();
    }
}