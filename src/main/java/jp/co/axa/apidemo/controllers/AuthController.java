package jp.co.axa.apidemo.controllers;

import io.swagger.annotations.ApiOperation;
import jp.co.axa.apidemo.dto.*;
import jp.co.axa.apidemo.entities.User;
import jp.co.axa.apidemo.services.AuthService;
import jp.co.axa.apidemo.utilities.HeaderUtilities;
import jp.co.axa.apidemo.utilities.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    @ApiOperation(value = "Used to Register for the system")
    public ResponseEntity registerUser(@RequestBody RegisterDTO registerDto) {
        ArrayList<String> validationErrors = registerDto.validate(); // Validate input
        if (validationErrors.size() > 0) { // If any errors, return with 400
            return new ResponseEntity(new ValidationErrorsDTO(validationErrors), HttpStatus.BAD_REQUEST);
        }
        User user = authService.register(registerDto);
        logger.info("User registered Successfully");
        return new ResponseEntity(user, HeaderUtilities.commonHeaders(), HttpStatus.OK); // Success
    }

    @PostMapping("/login")
    @ApiOperation(value = "Used to Login to the system")
    public ResponseEntity loginUser(@RequestBody LoginDTO loginDTO) {
        ArrayList<String> validationErrors = loginDTO.validate(); // Validate input
        if (validationErrors.size() > 0) { // If any errors, return with 400
            return new ResponseEntity(new ValidationErrorsDTO(validationErrors), HttpStatus.BAD_REQUEST);
        }
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

            authManager.authenticate(authInputToken); //

            String token = jwtUtil.generateToken(loginDTO.getUsername()); // Generate JWT

            return new ResponseEntity(new LoginSuccessDTO(token), HeaderUtilities.commonHeaders(), HttpStatus.OK); // Success
        } catch (AuthenticationException authExc) {
            return new ResponseEntity(new LoginFailureDTO("Username or Password Incorrect"), HeaderUtilities.commonHeaders(), HttpStatus.UNAUTHORIZED); // failure
        }
    }
}
