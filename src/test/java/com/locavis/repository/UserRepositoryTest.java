package com.locavis.repository;

import com.locavis.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    void ItsShouldCheckIfUserfundByUserName() {
        //given
        String username = "Zineb";
        User user = new User();
        user.setUserName("Zineb");
        user.setEmail("zineb.safoui@gmail.com");
        user.setPassword("password");
        underTest.save(user);

        //when
        Optional<User> userResult = underTest.findByUserName(username);

        //then
        assertThat(userResult.get()).isEqualTo(user);
    }

    @Test
    void ItsShouldCheckIfUserIsNotfundByUserName() {
        //given
        String username = "Zineb";

        //when
        Optional<User> userResult = underTest.findByUserName(username);

        //then
        assertThat(userResult).isEmpty();
    }
}