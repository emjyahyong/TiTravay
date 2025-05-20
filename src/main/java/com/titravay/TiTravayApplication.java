package com.titravay;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.titravay.model.User;
import com.titravay.repository.UserRepository;

@SpringBootApplication
public class TiTravayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiTravayApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                repo.save(new User(null, "admin", encoder.encode("1234"), "ROLE_ADMIN"));
            }
        };
    }
}
