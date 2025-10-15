package com.grupo0.cundipark.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grupo0.cundipark.models.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

import com.grupo0.cundipark.services.UserService;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String login(HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId != null) {
            return "redirect:/home";
        } else {
            return "loginPage";
        }
    }

    @RequestMapping("/registration")
    public String registerForm(@ModelAttribute("user") User user, HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId != null) {
            return "redirect:/home";
        } else {
            return "registrationPage";
        }
    }

    @PostMapping("/registration")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registrationPage";
        } else {
            if (!user.getPassword().equals(user.getPasswordConfirmation())) {
                result.rejectValue("passwordConfirmation", "error.user", "Las contraseñas no coinciden");
                return "registrationPage";
            }
            try {
                user = userService.registerUser(user);

                session.setAttribute("userId", user.getId());

                redirectAttributes.addFlashAttribute("message", "Registro exitoso");
                return "redirect:/home";
            } catch (DataIntegrityViolationException ex) {
                result.rejectValue("email", "error.user", "El email ya está registrado");
                return "registrationPage";
            }
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (userService.authenticateUser(email, password)) {
            User user = userService.findByEmail(email);
            session.setAttribute("userId", user.getId());
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Credenciales inválidas");
            return "loginPage";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/login";

    }

}
