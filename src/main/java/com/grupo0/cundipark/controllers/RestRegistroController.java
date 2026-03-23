package com.grupo0.cundipark.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo0.cundipark.dtos.ApiResponse;
import com.grupo0.cundipark.dtos.RegistroDTO;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import com.grupo0.cundipark.utils.MapperUtil;
import com.grupo0.cundipark.validators.ValidadorPlaca;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registros")
public class RestRegistroController {

    @Autowired
    private RegistroService registroService;

    @Autowired
    private UserService userService;

    @Autowired
    private BloqueService bloqueService;

    /**
     * GET /api/registros - Obtener todos los registros
     * @return Lista de registros con HTTP 200
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistroDTO>>> getAllRegistros() {
        List<Registro> registros = registroService.getAllRegistros();
        List<RegistroDTO> registroDTOs = registros.stream()
                .map(MapperUtil::toRegistroDTO)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(registroDTOs, "Registros obtenidos exitosamente")
        );
    }

    /**
     * GET /api/registros/{id} - Obtener registro por ID
     * @param id ID del registro
     * @return Registro encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistroDTO>> getRegistroById(@PathVariable Long id) {
        Registro registro = registroService.getRegistroById(id);
        if (registro == null) {
            throw new ResourceNotFoundException("Registro", id);
        }
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toRegistroDTO(registro), "Registro obtenido")
        );
    }

    /**
     * GET /api/registros/filtro - Buscar registros con filtros
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @param bloqueId ID del bloque
     * @param placa Número de placa del vehículo
     * @param activo Estado del registro
     * @return Lista de registros filtrados
     */
    @GetMapping("/filtro/avanzado")
    public ResponseEntity<ApiResponse<Page<RegistroDTO>>> buscarConFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long bloqueId,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Registro> registrosPage = registroService.buscarConFiltros(desde, hasta, bloqueId, placa, activo, pageable);
        Page<RegistroDTO> registroDTOPage = registrosPage.map(MapperUtil::toRegistroDTO);

        return ResponseEntity.ok(
                ApiResponse.success(registroDTOPage, "Búsqueda completada: " + registroDTOPage.getTotalElements() + " registros encontrados")
        );
    }

    /**
     * GET /api/registros/activos - Obtener solo registros activos
     * @return Lista de registros activos
     */
    @GetMapping("/activos/listado")
    public ResponseEntity<ApiResponse<List<RegistroDTO>>> getRegistrosActivos() {
        List<Registro> registros = registroService.findByActivoTrue();
        List<RegistroDTO> registroDTOs = registros.stream()
                .map(MapperUtil::toRegistroDTO)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(registroDTOs, "Registros activos obtenidos")
        );
    }


    /**
     * POST /api/registros - Crear nuevo registro
     * @param registroDTO Datos del registro
     * @return Registro creado con HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RegistroDTO>> createRegistro(@Valid @RequestBody RegistroDTO registroDTO) {
        // Validar placa
        if (!ValidadorPlaca.esValida(registroDTO.getPlaca())) {
            throw new IllegalArgumentException("Número de placa inválido. Formato: ABC-1234");
        }

        // La lógica de negocio (como buscar entidades) debería estar idealmente en la capa de servicio para asegurar atomicidad.
        // Sin embargo, hacer la validación aquí es una mejora crucial sobre el código anterior.
        User user = null;
        if (registroDTO.getUserId() != null) {
            user = userService.getUserById(registroDTO.getUserId());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario", registroDTO.getUserId());
            }
        }

        Bloque bloque = null;
        if (registroDTO.getBloqueId() != null) {
            bloque = bloqueService.findById(registroDTO.getBloqueId());
            if (bloque == null) {
                throw new ResourceNotFoundException("Bloque", registroDTO.getBloqueId());
            }
        }

        Registro registro = new Registro();
        registro.setPlaca(ValidadorPlaca.formatear(registroDTO.getPlaca()));
        registro.setActivo(registroDTO.isActivo());
        registro.setUser(user);
        registro.setBloque(bloque);
        // El campo 'estado' podría tener lógica de negocio asociada, que debería manejarse en el servicio.

        Registro registroGuardado = registroService.saveRegistro(registro);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(MapperUtil.toRegistroDTO(registroGuardado), "Registro creado exitosamente")
        );
    }

    /**
     * PUT /api/registros/{id} - Actualizar registro
     * @param id ID del registro
     * @param registroDTO Datos actualizados
     * @return Registro actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistroDTO>> updateRegistro(
            @PathVariable Long id,
            @Valid @RequestBody RegistroDTO registroDTO) {
        Registro existente = registroService.getRegistroById(id);
        if (existente == null) {
            throw new ResourceNotFoundException("Registro", id);
        }

        // Validar y formatear placa si cambió
        if (registroDTO.getPlaca() != null && !existente.getPlaca().equals(registroDTO.getPlaca())) {
            if (!ValidadorPlaca.esValida(registroDTO.getPlaca())) {
                throw new IllegalArgumentException("Número de placa inválido. Formato: ABC-1234");
            }
            existente.setPlaca(ValidadorPlaca.formatear(registroDTO.getPlaca()));
        }

        if (registroDTO.isActivo() != existente.getActivo()) {
            existente.setActivo(registroDTO.isActivo());
        }
        if (registroDTO.getFechaSalida() != null) {
            existente.setFechaSalida(registroDTO.getFechaSalida());
        }
        
        // Actualiza correctamente las asociaciones solo si el ID ha cambiado
        if (registroDTO.getUserId() != null && (existente.getUser() == null || !registroDTO.getUserId().equals(existente.getUser().getId()))) {
            User user = userService.getUserById(registroDTO.getUserId());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario", registroDTO.getUserId());
            }
            existente.setUser(user);
        }

        if (registroDTO.getBloqueId() != null && (existente.getBloque() == null || !registroDTO.getBloqueId().equals(existente.getBloque().getId()))) {
            Bloque bloque = bloqueService.findById(registroDTO.getBloqueId());
            if (bloque == null) {
                throw new ResourceNotFoundException("Bloque", registroDTO.getBloqueId());
            }
            existente.setBloque(bloque);
        }

        Registro registroActualizado = registroService.saveRegistro(existente);
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toRegistroDTO(registroActualizado), "Registro actualizado")
        );
    }

    /**
     * PUT /api/registros/{id}/salida - Registrar salida de vehículo
     * @param id ID del registro
     * @return Registro actualizado con salida
     */
    @PutMapping("/{id}/salida")
    public ResponseEntity<ApiResponse<RegistroDTO>> registrarSalida(@PathVariable Long id) {
        Registro existente = registroService.getRegistroById(id);
        if (existente == null) {
            throw new ResourceNotFoundException("Registro", id);
        }
        if (!existente.getActivo()) {
            throw new IllegalArgumentException("El vehículo ya ha salido");
        }

        existente.setActivo(false);
        existente.setFechaSalida(LocalDateTime.now());

        Registro registroActualizado = registroService.saveRegistro(existente);
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toRegistroDTO(registroActualizado), "Salida registrada exitosamente")
        );
    }

    /**
     * DELETE /api/registros/{id} - Eliminar registro
     * @param id ID del registro a eliminar
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRegistro(@PathVariable Long id) {
        Registro registro = registroService.getRegistroById(id);
        if (registro == null) {
            throw new ResourceNotFoundException("Registro", id);
        }
        registroService.deleteRegistro(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Registro eliminado exitosamente")
        );
    }
}
