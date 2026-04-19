package com.grupo0.cundipark.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo0.cundipark.dtos.ApiResponse;
import com.grupo0.cundipark.dtos.RegistroDTO;
import com.grupo0.cundipark.validators.ValidadorPlaca;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.utils.MapperUtil;

@RestController
@RequestMapping("/api/historico")
public class RestHistoricoController {

    @Autowired
    private RegistroService registroService;

    /**
     * GET /api/historico - Obtener registros históricos con filtros
     * @param desde Fecha desde
     * @param hasta Fecha hasta
     * @param activo Si está activo
     * @param bloqueId ID del bloque
     * @param placa Placa del vehículo
     * @return Lista de registros
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RegistroDTO>>> getHistorico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long bloqueId,
            @RequestParam(required = false) String placa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Normalizamos la placa para la búsqueda en la base de datos (sin guion y en mayúsculas)
        if (placa != null) {
            try {
                placa = ValidadorPlaca.formatear(placa);
            } catch (Exception e) {
                placa = placa.trim().toUpperCase();
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<com.grupo0.cundipark.models.Registro> registrosPage = registroService.buscarConFiltros(desde, hasta, bloqueId, placa, activo, pageable);
        Page<RegistroDTO> registroDTOPage = registrosPage.map(MapperUtil::toRegistroDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(registroDTOPage, "Registros históricos obtenidos exitosamente")
        );
    }
}