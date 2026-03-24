package com.grupo0.cundipark.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo0.cundipark.dtos.ApiResponse;
import com.grupo0.cundipark.dtos.BloqueDTO;
import com.grupo0.cundipark.exceptions.DuplicateResourceException;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.utils.MapperUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bloques")
public class RestBloqueController {

    @Autowired
    private BloqueService bloqueService;

    /**
     * GET /api/bloques - Obtener todos los bloques
     * @return Lista de bloques con HTTP 200
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BloqueDTO>>> getAllBloques() {
        List<Bloque> bloques = bloqueService.getAllBloques();
        List<BloqueDTO> bloqueDTOs = bloques.stream()
                .map(MapperUtil::toBloqueDTO)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(bloqueDTOs, "Bloques obtenidos exitosamente")
        );
    }

    /**
     * GET /api/bloques/{id} - Obtener bloque por ID
     * @param id ID del bloque
     * @return Bloque encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BloqueDTO>> getBloqueById(@PathVariable Long id) {
        Bloque bloque = bloqueService.getBloqueById(id);
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toBloqueDTO(bloque), "Bloque obtenido")
        );
    }

    /**
     * POST /api/bloques - Crear nuevo bloque
     * @param bloque Datos del bloque
     * @return Bloque creado con HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BloqueDTO>> createBloque(@Valid @RequestBody Bloque bloque) {
        // Validar que el nombre no esté vacío
        if (bloque.getNombre() == null || bloque.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del bloque es requerido");
        }

        // Validar que no exista un bloque con el mismo nombre
        List<Bloque> bloques = bloqueService.getAllBloques();
        if (bloques.stream().anyMatch(b -> b.getNombre().equalsIgnoreCase(bloque.getNombre()))) {
            throw new DuplicateResourceException("Bloque", "nombre", bloque.getNombre());
        }

        Bloque bloqueGuardado = bloqueService.saveBloque(bloque);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(MapperUtil.toBloqueDTO(bloqueGuardado), "Bloque creado exitosamente")
        );
    }

    /**
     * PUT /api/bloques/{id} - Actualizar bloque
     * @param id ID del bloque
     * @param bloque Datos actualizados
     * @return Bloque actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BloqueDTO>> updateBloque(
            @PathVariable Long id,
            @Valid @RequestBody Bloque bloque) {
        Bloque existente = bloqueService.getBloqueById(id);

        // Validar nombre si cambió
        if (bloque.getNombre() != null && !existente.getNombre().equals(bloque.getNombre())) {
            if (bloque.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del bloque no puede estar vacío");
            }

            List<Bloque> bloques = bloqueService.getAllBloques();
            if (bloques.stream()
                    .filter(b -> !b.getId().equals(id))
                    .anyMatch(b -> b.getNombre().equalsIgnoreCase(bloque.getNombre()))) {
                throw new DuplicateResourceException("Bloque", "nombre", bloque.getNombre());
            }

            existente.setNombre(bloque.getNombre());
        }

        if (bloque.getCapacidad() != null) {
            existente.setCapacidad(bloque.getCapacidad());
        }
        if (bloque.getDisponibles() != null) {
            existente.setDisponibles(bloque.getDisponibles());
        }
        if (bloque.getActivo() != null) {
            existente.setActivo(bloque.getActivo());
        }

        Bloque bloqueActualizado = bloqueService.saveBloque(existente);
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toBloqueDTO(bloqueActualizado), "Bloque actualizado")
        );
    }

    /**
     * DELETE /api/bloques/{id} - Eliminar bloque
     * @param id ID del bloque a eliminar
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBloque(@PathVariable Long id) {
        Bloque bloque = bloqueService.getBloqueById(id);
        bloqueService.deleteBloque(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Bloque eliminado exitosamente")
        );
    }

    /**
     * GET /api/bloques/{id}/disponibilidad - Obtener disponibilidad de un bloque
     * @param id ID del bloque
     * @return Información de disponibilidad
     */
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadDTO>> getDisponibilidad(@PathVariable Long id) {
        Bloque bloque = bloqueService.getBloqueById(id);

        DisponibilidadDTO disponibilidad = new DisponibilidadDTO(
                bloque.getId(),
                bloque.getNombre(),
                bloque.getCapacidad(),
                bloque.getDisponibles(),
                bloque.getCapacidad() - bloque.getDisponibles()
        );

        return ResponseEntity.ok(
                ApiResponse.success(disponibilidad, "Información de disponibilidad")
        );
    }

    // Inner class para respuesta de disponibilidad
    public static class DisponibilidadDTO {
        private Long id;
        private String nombre;
        private Integer capacidadTotal;
        private Integer espaciosDisponibles;
        private Integer espaciosOcupados;

        public DisponibilidadDTO() {
        }

        public DisponibilidadDTO(Long id, String nombre, Integer capacidadTotal, Integer espaciosDisponibles, Integer espaciosOcupados) {
            this.id = id;
            this.nombre = nombre;
            this.capacidadTotal = capacidadTotal;
            this.espaciosDisponibles = espaciosDisponibles;
            this.espaciosOcupados = espaciosOcupados;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Integer getCapacidadTotal() {
            return capacidadTotal;
        }

        public void setCapacidadTotal(Integer capacidadTotal) {
            this.capacidadTotal = capacidadTotal;
        }

        public Integer getEspaciosDisponibles() {
            return espaciosDisponibles;
        }

        public void setEspaciosDisponibles(Integer espaciosDisponibles) {
            this.espaciosDisponibles = espaciosDisponibles;
        }

        public Integer getEspaciosOcupados() {
            return espaciosOcupados;
        }

        public void setEspaciosOcupados(Integer espaciosOcupados) {
            this.espaciosOcupados = espaciosOcupados;
        }
    }
}
