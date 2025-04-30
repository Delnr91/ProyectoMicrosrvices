package com.dani.spring_boot_microservice_3_api_gateway.controller;


import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import com.dani.spring_boot_microservice_3_api_gateway.service.AuthenticationService;
import com.dani.spring_boot_microservice_3_api_gateway.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    //inyeccion de objeto authentication service y userservice

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    //Metodo para dar de alta un nuevo usuario

    @PostMapping("sign-up")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        //Evaluar si el usuario registrado existe
        if(userService.findByUsername(user.getUsername()).isPresent()){

            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    //Metodo para hacer el login
    @PostMapping("sign-in")
    public ResponseEntity<?> signIn(@RequestBody User user){

        return new ResponseEntity<>(authenticationService.signInAndReturnJWT(user), HttpStatus.OK);
    }

}
