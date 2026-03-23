package com.grupo0.cundipark.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/home";
        }
        return "loginPage";
    }

    @GetMapping("/registration")
    public String registerForm(@ModelAttribute("user") User user, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/home";
        }
        return "registrationPage";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registrationPage";
        }

        if (user.getPassword() != null && !user.getPassword().equals(user.getPasswordConfirmation())) {
            result.rejectValue("passwordConfirmation", "error.user", "Las contraseñas no coinciden");
            return "registrationPage";
        }
        try {
            User registeredUser = userService.registerUser(user);
            session.setAttribute("userId", registeredUser.getId());
            redirectAttributes.addFlashAttribute("message", "Registro exitoso. ¡Bienvenido!");
            return "redirect:/home";
        } catch (DataIntegrityViolationException ex) {
            result.rejectValue("email", "error.user", "El email ya está registrado");
            return "registrationPage";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session) {
        String emailTrimmed = email != null ? email.trim() : "";
        
        if (userService.authenticateUser(emailTrimmed, password)) {
            User user = userService.findByEmail(emailTrimmed);
            if (user != null) {
                session.setAttribute("userId", user.getId());
                return "redirect:/home";
            }
        }
        
        model.addAttribute("error", "Credenciales inválidas. Por favor, intente de nuevo.");
        return "loginPage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
