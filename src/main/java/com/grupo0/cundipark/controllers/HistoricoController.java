package com.grupo0.cundipark.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HistoricoController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistroService registroService;

    @GetMapping("/historico")
    public String historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String placa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session, 
            Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        
        // Validaciones básicas de parámetros
        if (page < 0) page = 0;
        if (placa != null && placa.trim().isEmpty()) placa = null;
        else if (placa != null) placa = placa.trim();

        // Creamos un objeto Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size);

        // El servicio ahora devuelve un objeto Page, que contiene los registros y la información de paginación
        Page<Registro> paginaRegistros = registroService.buscarConFiltros(desde, hasta, null, placa, null, pageable); 

        model.addAttribute("user", user);
        model.addAttribute("paginaRegistros", paginaRegistros);

        // Devolvemos los filtros a la vista para que se mantengan en los campos de búsqueda
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("placa", placa);
        
        return "historicoPage";
    }
}