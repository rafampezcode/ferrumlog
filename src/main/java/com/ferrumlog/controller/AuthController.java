package com.ferrumlog.controller;

import com.ferrumlog.dto.UserRegistrationDto;
import com.ferrumlog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para autenticación y registro de usuarios.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Muestra el formulario de registro.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto("", "", ""));
        return "register";
    }

    /**
     * Procesa el registro de un nuevo usuario.
     * Aplica validaciones con @Valid y muestra errores en el formulario.
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Si hay errores de validación, volver al formulario
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Validaciones de negocio
            if (userService.existsByUsername(registrationDto.username())) {
                bindingResult.rejectValue("username", "error.user",
                        "El nombre de usuario ya está en uso");
                return "register";
            }

            if (userService.existsByEmail(registrationDto.email())) {
                bindingResult.rejectValue("email", "error.user",
                        "El email ya está registrado");
                return "register";
            }

            // Registrar usuario
            userService.registerUser(registrationDto);
            log.info("Usuario registrado exitosamente: {}", registrationDto.username());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Registro exitoso. Por favor inicia sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Error al registrar usuario", e);
            model.addAttribute("errorMessage", "Error al registrar usuario: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Muestra el formulario de login.
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
