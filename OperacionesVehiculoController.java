package com.grupo0.cundipark.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OperacionesVehiculoController {

    @Autowired
    UserService userService;
    @Autowired
    BloqueService bloqueService;
    @Autowired
    RegistroService registroService;

    @PostMapping("/registrar-vehiculo")
    public String entrada(@RequestParam("placa") String placa,
            @RequestParam("seccionId") Long bloqueId,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        Bloque bloque = bloqueService.findById(bloqueId);
        Optional<Registro> existe = registroService.findRegistroActivoPorPlaca(placa.toUpperCase());

        if (existe.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El vehículo con placa " + placa + " ya está registrado.");
            return "redirect:/home";
        }

        if (bloque.getPlazasDisponibles() <= 0) {
            redirectAttributes.addFlashAttribute("error", "No hay plazas disponibles en el " + bloque.getNombre());
            return "redirect:/home";
        }

        Registro registro = new Registro();
        registro.setPlaca(placa.toUpperCase());
        registro.setUser(user);
        registro.setBloque(bloque);

        registroService.save(registro);
        bloque.setPlazasDisponibles(bloque.getPlazasDisponibles() - 1);
        bloqueService.save(bloque);
        redirectAttributes.addFlashAttribute("success", "Vehículo " + placa + " registrado correctamente.");
        return "redirect:/home";
    }

    @PostMapping("/registrar-salida")
    public String salida(@RequestParam("placa") String placa, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }
        Optional<Registro> registroOpt = registroService.findRegistroActivoPorPlaca(placa.toUpperCase());

        if (registroOpt.isPresent()) {
            Registro registro = registroOpt.get();
            registro.setActivo(false);

            Bloque bloque = registro.getBloque();
            bloque.setPlazasDisponibles(bloque.getPlazasDisponibles() + 1);
            bloqueService.save(bloque);

            registroService.save(registro);
            redirectAttributes.addFlashAttribute("success", "Salida registrada exitosamente para la placa: " + placa);
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró un registro activo para la placa: " + placa);
        }

        return "redirect:/home";
    }

}
