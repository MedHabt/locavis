package com.locavis.Controller;

import com.locavis.dto.LoginRequest;
import com.locavis.dto.RegisterRequest;
import com.locavis.service.AuthService;
import com.locavis.service.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.http.HttpRequest;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600L)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder){
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        webDataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@Valid @RequestBody RegisterRequest registerRequest, final BindingResult bindingResult){

        //registerRequest.toString();

        //try{
            authService.signup(registerRequest);
        /*}catch(Exception e){
            bindingResult.rejectValue("email", "userData.email","An account already exists for this email.");
            //model.addAttribute("registrationForm", registerRequest);
            return new ResponseEntity(HttpStatus.BAD_REQUEST); //BAD_REQUEST);
        }*/

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}
