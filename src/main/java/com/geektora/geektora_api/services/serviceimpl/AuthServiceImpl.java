package com.geektora.geektora_api.services.serviceimpl;

import com.geektora.geektora_api.DTO.request.LoginRequest;
import com.geektora.geektora_api.DTO.request.RegisterRequest;
import com.geektora.geektora_api.model.entity.User;
import com.geektora.geektora_api.repository.users.UserRepository;
import com.geektora.geektora_api.services.AuthService;
import com.geektora.geektora_api.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;


    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private final Map<String, String> verificationCodes = new HashMap<>();

    @Override
    public String login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByName(loginRequest.getIdentifier());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(loginRequest.getIdentifier());
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isVerified()) {
                return "Debes verificar tu cuenta antes de iniciar sesión.";
            }

            if (user.getContrasena().equals(loginRequest.getContrasena())) {
                return "Inicio de sesión exitoso";
            }
        }
        return "Credenciales inválidas";
    }

    @Override
    public String register(RegisterRequest registerRequest) {
        if (!isEmailValidFormat(registerRequest.getEmail())) {
            return "El formato del correo es inválido";
        }

        if (userRepository.findByName(registerRequest.getName()).isPresent()) {
            return "El nombre de usuario ya está en uso";
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return "El correo ya está en uso";
        }

        // Crear usuario en la BD (aún no verificado)
        User newUser = new User();
        newUser.setName(registerRequest.getName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setContrasena(registerRequest.getContrasena()); // Debería estar encriptada
        newUser.setVerified(false);

        userRepository.save(newUser); // Guardamos el usuario

        // Enviar código de verificación
        String verificationCode = emailService.sendVerificationEmail(registerRequest.getEmail());

        // Almacenar código en memoria
        verificationCodes.put(registerRequest.getEmail(), verificationCode);

        return "Registro exitoso. Verifica tu correo con el código enviado.";
    }

    private boolean isEmailValidFormat(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public String verify(String email, String code) {
        System.out.println("Intentando verificar: " + email + " con código " + code);
        System.out.println("Códigos almacenados: " + verificationCodes);

        if (verificationCodes.containsKey(email)) {
            String storedCode = verificationCodes.get(email);
            if (storedCode.trim().equals(code.trim())) {
                // Activar la cuenta
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setVerified(true);
                    userRepository.save(user);
                    verificationCodes.remove(email);
                    return "Cuenta verificada con éxito.";
                }
            } else {
                return "Código incorrecto.";
            }
        }
        return "Código inválido o expirado.";
    }

}