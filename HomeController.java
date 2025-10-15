package com.grupo0.cundipark.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    BloqueService bloqueService;

    @Autowired
    RegistroService registroService;

    @Autowired
    UserService userService;
    
    @GetMapping("")
    public String home(HttpSession httpSession, Model model){
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("secciones", bloqueService.findAll());
            model.addAttribute("vehiculosActivos", registroService.findByActivoTrue());
            model.addAttribute("user", userService.findById(userId).getEmail());
            return "index";
        }
        return "redirect:/login";
    }    
}