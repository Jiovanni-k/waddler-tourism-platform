package org.example.rest.security.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Slf4j
public class UserSeeder {

    @Bean
    @Order(0)
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (!userRepository.existsByEmail("waddler.info@gmail.com")) {
                AppUser admin = new AppUser();
                admin.setEmail("waddler.info@gmail.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                admin.setFirstName("Admin");
                admin.setLastName("Waddler");
                admin.setBirthDate(LocalDate.of(1990, 1, 1));
                admin.setGender(UserGender.FEMALE);
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                userRepository.save(admin);
                log.info("Seeded ADMIN: waddler.info@gmail.com / Admin@123");
            }

            if (!userRepository.existsByEmail("salmamahmoudao@gmail.com")) {
                AppUser manager = new AppUser();
                manager.setEmail("salmamahmoudao@gmail.com");
                manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                manager.setFirstName("Salma");
                manager.setLastName("Mahmoud");
                manager.setBirthDate(LocalDate.of(2005, 1, 1));
                manager.setGender(UserGender.FEMALE);
                manager.setMaritalStatus(MaritalStatus.SINGLE);
                manager.setPhone("+970591234567");
                manager.setNationality("Palestinian");
                manager.setRole(UserRole.HOTEL_MANAGER);
                manager.setStatus(UserStatus.ACTIVE);
                userRepository.save(manager);
                log.info("Seeded HOTEL_MANAGER: salmamahmoudao@gmail.com / Manager@123");
            }

            if (!userRepository.existsByEmail("jkitlo2005@gmail.com")) {
                AppUser manager = new AppUser();
                manager.setEmail("jkitlo2005@gmail.com");
                manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                manager.setFirstName("Jiovanni");
                manager.setLastName("Elya");
                manager.setBirthDate(LocalDate.of(2005, 1, 1));
                manager.setGender(UserGender.MALE);
                manager.setMaritalStatus(MaritalStatus.SINGLE);
                manager.setPhone("+970591233214");
                manager.setNationality("Palestinian");
                manager.setRole(UserRole.HOTEL_MANAGER);
                manager.setStatus(UserStatus.ACTIVE);
                userRepository.save(manager);
                log.info("Seeded HOTEL_MANAGER: jkitlo2005@gmail.com / Manager@123");
            }

            if (!userRepository.existsByEmail("jiovannikitlo9@gmail.com")) {
                AppUser manager = new AppUser();
                manager.setEmail("jiovannikitlo9@gmail.com");
                manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                manager.setFirstName("Kitlo");
                manager.setLastName("Jiovanni");
                manager.setBirthDate(LocalDate.of(2005, 1, 1));
                manager.setGender(UserGender.MALE);
                manager.setMaritalStatus(MaritalStatus.SINGLE);
                manager.setPhone("+970591274167");
                manager.setNationality("Palestinian");
                manager.setRole(UserRole.HOTEL_MANAGER);
                manager.setStatus(UserStatus.ACTIVE);
                userRepository.save(manager);
                log.info("Seeded HOTEL_MANAGER: jiovannikitlo9@gmail.com / Manager@123");
            }

            if (!userRepository.existsByEmail("202302878@bethlehem.edu")) {
                AppUser user = new AppUser();
                user.setEmail("202302878@bethlehem.edu");
                user.setPasswordHash(passwordEncoder.encode("User@1234"));
                user.setFirstName("Salma");
                user.setLastName("Abu Odeh");
                user.setBirthDate(LocalDate.of(2005, 1, 1));
                user.setGender(UserGender.FEMALE);
                user.setMaritalStatus(MaritalStatus.SINGLE);
                user.setPhone("+970599876543");
                user.setNationality("Palestinian");
                user.setFamilyMembersCount(1);
                user.setHasKids(false);
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
                log.info("Seeded USER: 202302878@bethlehem.edu / User@1234");
            }

            if (!userRepository.existsByEmail("202404659@bethlehem.edu")) {
                AppUser user = new AppUser();
                user.setEmail("202404659@bethlehem.edu");
                user.setPasswordHash(passwordEncoder.encode("User@1234"));
                user.setFirstName("Jiovanni");
                user.setLastName("Kitlo");
                user.setBirthDate(LocalDate.of(2005, 1, 1));
                user.setGender(UserGender.MALE);
                user.setMaritalStatus(MaritalStatus.SINGLE);
                user.setPhone("+970599844443");
                user.setNationality("Palestinian");
                user.setFamilyMembersCount(1);
                user.setHasKids(false);
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
                log.info("Seeded USER: 202404659@bethlehem.edu / User@1234");
            }

            if (!userRepository.existsByEmail("202303706@bethlehem.edu")) {
                AppUser user = new AppUser();
                user.setEmail("202303706@bethlehem.edu");
                user.setPasswordHash(passwordEncoder.encode("User@1234"));
                user.setFirstName("Zeina");
                user.setLastName("Ibrahim");
                user.setBirthDate(LocalDate.of(2005, 1, 1));
                user.setGender(UserGender.FEMALE);
                user.setMaritalStatus(MaritalStatus.SINGLE);
                user.setNationality("Palestinian");
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.SUSPENDED);
                userRepository.save(user);
                log.info("Seeded SUSPENDED USER: 202303706@bethlehem.edu / User@1234");
            }

            if (!userRepository.existsByEmail("raidazawahra@gmail.com")) {
                AppUser manager = new AppUser();
                manager.setEmail("raidazawahra@gmail.com");
                manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                manager.setFirstName("Raida");
                manager.setLastName("Zawahra");
                manager.setBirthDate(LocalDate.of(2000, 5, 15));
                manager.setGender(UserGender.FEMALE);
                manager.setMaritalStatus(MaritalStatus.SINGLE);
                manager.setPhone("+970592345678");
                manager.setNationality("Palestinian");
                manager.setRole(UserRole.HOTEL_MANAGER);
                manager.setStatus(UserStatus.PENDING_APPROVAL);
                userRepository.save(manager);
                log.info("Seeded PENDING_APPROVAL HOTEL_MANAGER: raidazawahra@gmail.com / Manager@123");
            }

            if (!userRepository.existsByEmail("omarhaddad@gmail.com")) {
                AppUser manager = new AppUser();
                manager.setEmail("omarhaddad@gmail.com");
                manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
                manager.setFirstName("Omar");
                manager.setLastName("Haddad");
                manager.setBirthDate(LocalDate.of(1998, 3, 20));
                manager.setGender(UserGender.MALE);
                manager.setMaritalStatus(MaritalStatus.SINGLE);
                manager.setPhone("+970593456789");
                manager.setNationality("Palestinian");
                manager.setRole(UserRole.HOTEL_MANAGER);
                manager.setStatus(UserStatus.PENDING_APPROVAL);
                userRepository.save(manager);
                log.info("Seeded PENDING_APPROVAL HOTEL_MANAGER: omarhaddad@gmail.com / Manager@123");
            }
        };
    }
}