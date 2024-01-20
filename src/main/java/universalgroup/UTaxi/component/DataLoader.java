package universalgroup.UTaxi.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import universalgroup.UTaxi.Entity.Enums.RoleName;
import universalgroup.UTaxi.Entity.Role;
import universalgroup.UTaxi.Entity.User;
import universalgroup.UTaxi.Repository.AuthRepository;
import universalgroup.UTaxi.Repository.RoleRepository;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String init;

    private final AuthRepository authRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (init.equals("create-drop") || init.equals("create")) {
            for (RoleName value : RoleName.values()) {
                roleRepository.save(new Role(value));
            }
            User admin = authRepository.save(
                    new User(
                            "Dilbek", "Mukhtarovich", "500537027", passwordEncoder.encode("dilbekk070"), Collections.singleton(roleRepository.findById(1).orElseThrow(() -> new ResourceNotFoundException("getRole"))), true, true, true, true
                    )
            );
            authRepository.save(
                    new User(
                            "Qozi", "Gadayev", "123456789", passwordEncoder.encode("123456"), Collections.singleton(roleRepository.findById(2).orElseThrow(() -> new ResourceNotFoundException("getRole"))), true, true, true, true
                    )
            );
        }
    }
}
