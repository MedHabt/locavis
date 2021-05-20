package com.locavis.service;

import com.locavis.dto.LoginRequest;
import com.locavis.dto.RegisterRequest;
import com.locavis.emailService.EmailCategorie;
import com.locavis.emailService.EmailSender;
import com.locavis.exception.apiException.ApiRequestException;
import com.locavis.model.ConfirmationToken;
import com.locavis.model.User;
import com.locavis.repository.UserRepository;
import com.locavis.security.JwtProvider;
import com.locavis.util.EmailValidator;
import com.locavis.util.PasswordValidationAndMatch;
import com.locavis.util.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private EmailValidator emailValidator;

    private final EmailSender emailSender;

    @Autowired
    private PasswordValidationAndMatch passwordValidationAndMatch;

    @Autowired
    private final ConfirmationTokenService confirmationTokenService;

    public AuthService(EmailSender emailSender, ConfirmationTokenService confirmationTokenService) {
        this.emailSender = emailSender;
        this.confirmationTokenService = confirmationTokenService;
    }


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

        boolean userExist = userRepository.findByEmail(registerRequest.getEmail()).isPresent();

        if(userExist){
            User userAlreadyExist = userRepository.findByEmail(registerRequest.getEmail()).get();
            //Si l'utilisateur n'as pas valider sans mail
            if(!userAlreadyExist.getEnabled()){
                emailSender.send( registerRequest.getEmail(), EmailCategorie.buildEmailRegistration(registerRequest.getUsername(), confirmationTokenService.createAndSaveToken(userAlreadyExist)));
                //throw new IllegalStateException("Email already exist with different user - "+userAlreadyExist.getEmail());
                return;
            }
            throw new ApiRequestException("mail already exist with different user - "+registerRequest.getEmail());
        }

        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setPassword(encoderPassword(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        //j'ai mis la valeur par défault.
        //user.setUserRoles(UserRoles.USER);

        userRepository.save(user);


        //TODO : Envoie mail vers l'utilisateur

        emailSender.send( registerRequest.getEmail(), EmailCategorie.buildEmailRegistration(registerRequest.getUsername(), confirmationTokenService.createAndSaveToken(user)));

    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                new ApiRequestException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new ApiRequestException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new ApiRequestException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userRepository.enableUser(confirmationToken.getUser().getEmail());
        return "confirmed";
    }

    private String encoderPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        boolean emailValid = emailValidator.test(loginRequest.getEmail());

        if(!emailValid)
        {
            throw new ApiRequestException(loginRequest.getEmail()+" - Email/Username not valid ");
        }

        boolean userExist = userRepository.findByEmail(loginRequest.getEmail()).isPresent();

        if(userExist){
            User userAlreadyExist = userRepository.findByEmail(loginRequest.getEmail()).get();
            //Si l'utilisateur n'as pas valider sans mail
            if(!userAlreadyExist.getEnabled()){
                throw new ApiRequestException("Thanks You to validate your email or do registration- "+loginRequest.getEmail());
            }
        }else {
            throw new ApiRequestException("No user found please do registration");
        }
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authenticationToken =  jwtProvider.generateToken(authentication);
        return new AuthenticationResponse(authenticationToken, loginRequest.getEmail());
    }

    public Optional<org.springframework.security.core.userdetails.User> getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().
                getAuthentication().getPrincipal();
        return Optional.of(principal);
    }

    public void forgivenPasswordSendMail(String email) {
        boolean emailValid = emailValidator.test(email);

        if(!emailValid)
        {
            throw new ApiRequestException(email+" - Email not valid ");
        }

        boolean userExist = userRepository.findByEmail(email).isPresent();

        if(userExist){
            User userAlreadyExist = userRepository.findByEmail(email).get();
            //Si l'utilisateur n'as pas valider sans mail
            if(userAlreadyExist.getEnabled()){
                String link = "http://localhost:4200/changePassword/"+email;
                emailSender.send( email, EmailCategorie.buildEmailSetNewPassword(userAlreadyExist.getUserName(), link));
                return;
            }
            throw new ApiRequestException("mail is not valid please try to register - "+email);
        }
    }

    public void changePassword(RegisterRequest registerRequest) {
        boolean emailValid = emailValidator.test(registerRequest.getEmail());

        if(!emailValid)
        {
            throw new ApiRequestException(registerRequest.getEmail()+" - Email not valid ");
        }

        if (!passwordValidationAndMatch.isPasswordMatch(registerRequest.getPassword(),registerRequest.getConfirmPassword())) {
            throw new ApiRequestException(registerRequest.getPassword()+" - Passwords do not match ");
        }

        boolean userExist = userRepository.findByEmail(registerRequest.getEmail()).isPresent();

        if(userExist){
            User userAlreadyExist = userRepository.findByEmail(registerRequest.getEmail()).get();
            //Si l'utilisateur n'as pas valider sans mail
            if(userAlreadyExist.getEnabled()){
                encoderPassword(registerRequest.getPassword());
                userRepository.changePassword(userAlreadyExist.getId(),encoderPassword(registerRequest.getPassword()));
                return;
            }
            throw new ApiRequestException("mail account is not enabled - "+registerRequest.getEmail());
        }
    }
}
