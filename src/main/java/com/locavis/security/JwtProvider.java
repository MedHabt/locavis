package com.locavis.security;

import com.locavis.exception.SpringBlogException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try{
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());//KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/locavisapplication.jks");
            keyStore.load(resourceAsStream, "password".toCharArray());
        }catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
            throw new SpringBlogException("Exception occured while loading keystore");
        }
    }

    public String generateToken(Authentication authentication){
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(((UserDetails) authentication.getPrincipal()).getUsername())//principal.getUserName())
                .signWith(getPrivateKey())
                .compact();
    }

    private Key getPrivateKey() {
        try{
            return (PrivateKey) keyStore.getKey("locavisapplication", "password".toCharArray());
        }catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            throw new SpringBlogException("Exception occured while retrieving private key from keystore");
        }
    }

    public Boolean validateToken(String jwt){
        Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
        return true;
    }

    private PublicKey getPublicKey() {
        try{
            return keyStore.getCertificate("locavisapplication").getPublicKey();
        }catch (KeyStoreException ex) {
            throw new SpringBlogException("Exception occured while retrieving public key from keystore");
        }
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                        .setSigningKey(getPublicKey())
                        .parseClaimsJws(token)
                        .getBody();
        return claims.getSubject();
    }
}
