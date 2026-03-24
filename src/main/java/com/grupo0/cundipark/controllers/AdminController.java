package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private BloqueService bloqueService;
    @Autowired
    private RegistroService registroService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener datos
        List<User> users = userService.getAllUsers();
        List<Registro> registros = registroService.getAllRegistros(); 
        List<Bloque> bloques = bloqueService.getAllBloques();

        // Cálculos para tarjetas
        long totalUsuarios = users.size();
        long vehiculosActivos = registros.stream().filter(Registro::getActivo).count();
        
        int capacidadTotal = bloques.stream().mapToInt(Bloque::getCapacidad).sum();
        double ocupacionGeneral = capacidadTotal > 0 ? (double) vehiculosActivos / capacidadTotal * 100 : 0;
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        long entradas24h = registros.stream()
                .filter(r -> r.getCreatedAt().isAfter(oneDayAgo))
                .count();

        // Datos para Gráfica (Entradas por hora)
        Map<Integer, Long> entradasPorHora = registros.stream()
                .filter(r -> r.getCreatedAt().isAfter(oneDayAgo))
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().getHour(), Collectors.counting()));

        // Añadir al modelo
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("vehiculosActivos", vehiculosActivos);
        model.addAttribute("ocupacionGeneral", String.format("%.1f", ocupacionGeneral));
        model.addAttribute("entradas24h", entradas24h);
        model.addAttribute("bloques", bloques);
        model.addAttribute("entradasPorHora", entradasPorHora);
        
        // Últimos registros (tabla)
        List<Registro> ultimosRegistros = registros.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("registros", ultimosRegistros);

        return "adminDashboard";
    }
    
    // Redirecciones para módulos en construcción
    @GetMapping({"/bloques", "/registros", "/usuarios", "/reportes"})
    public String modulosEnConstruccion() {
        return "redirect:/admin/dashboard";
    }
}