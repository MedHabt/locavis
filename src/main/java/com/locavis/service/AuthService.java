package com.locavis.service;

import com.locavis.dto.LoginRequest;
import com.locavis.dto.RegisterRequest;
import com.locavis.exception.apiException.ApiRequestException;
import com.locavis.model.User;
import com.locavis.repository.UserRepository;
import com.locavis.security.JwtProvider;
import com.locavis.util.EmailValidator;
import com.locavis.util.PasswordValidationAndMatch;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    private EmailValidator emailValidator;

    private PasswordValidationAndMatch passwordValidationAndMatch;

    public void signup(RegisterRequest registerRequest) {
        boolean emailValid = emailValidator.test(registerRequest.getEmail());

        if(!emailValid)
        {
            throw new ApiRequestException(registerRequest.getEmail()+" - Email not valid ");
            //throw new IllegalStateException(registerRequest.getEmail()+" - Email not valid ");
        }

        /*if(!passwordValidationAndMatch.isValidPassword(registerRequest.getPassword()))
        {
            throw new ApiRequestException(registerRequest.getPassword()+" - Password not valid ");
        }*/

        if (!passwordValidationAndMatch.isPasswordMatch(registerRequest.getPassword(),registerRequest.getConfirmPassword())) {
            throw new ApiRequestException(registerRequest.getPassword()+" - Passwords do not match ");
        }

        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setPassword(encoderPassword(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        boolean userExist = userRepository.findByEmail(registerRequest.getEmail()).isPresent();

        if(userExist){
            User userAlreadyExist = userRepository.findByEmail(registerRequest.getEmail()).get();
            if(userAlreadyExist.equals(user)){
                throw new ApiRequestException("mail already exist with different user - "+userAlreadyExist.getEmail());
                //throw new IllegalStateException("Email already exist with different user - "+userAlreadyExist.getEmail());
            }
        }

        //TODO : utilise Optional pour vérifier si l'utilisateur existe déjà ou non comme dans l'exemple d'amigosCode
        userRepository.save(user);
    }

    private String encoderPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authenticationToken =  jwtProvider.generateToken(authentication);
        return new AuthenticationResponse(authenticationToken, loginRequest.getUsername());
    }

    public Optional<org.springframework.security.core.userdetails.User> getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().
                getAuthentication().getPrincipal();
        return Optional.of(principal);
    }
}
