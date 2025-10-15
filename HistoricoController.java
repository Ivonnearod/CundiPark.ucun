package com.grupo0.cundipark.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;

@Controller
public class HistoricoController {

    @Autowired
    BloqueService bloqueService;
    @Autowired
    RegistroService registroService;

    @GetMapping("/historico")
    public String historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long bloqueId,
            @RequestParam(required = false) String placa,
            Model model
    ) {

        List<Registro> registros = registroService.buscarConFiltros(desde, hasta, bloqueId, placa, activo);
        List<Bloque> bloques = bloqueService.findAll();

        model.addAttribute("registros", registros);
        model.addAttribute("bloques", bloques);

        return "historico";
    }

}
