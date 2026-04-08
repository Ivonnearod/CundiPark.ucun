package com.grupo0.cundipark.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Vehiculo;
import com.grupo0.cundipark.repositories.RegistroRepository;
import com.grupo0.cundipark.repositories.BloqueRepository;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class RegistroService {

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private BloqueRepository bloqueRepository;

    @Autowired
    private VehiculoService vehiculoService; // Necesitarás un VehiculoService

    public List<Registro> getAllRegistros() {
        return registroRepository.findAll();
    }

    public Registro getRegistroById(Long id) {
        return registroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Registro", id));
    }

    public Registro saveRegistro(Registro registro) {
        return registroRepository.save(registro);
    }

    /**
     * Transacción para eliminar un registro:
     * Si el registro estaba activo, se debe devolver el cupo al bloque antes de borrarlo.
     */
    public void eliminarRegistro(Long id) {
        Registro registro = getRegistroById(id);
        
        // Si el registro estaba activo, restaurar el cupo antes de eliminar para mantener coherencia
        if (Boolean.TRUE.equals(registro.getActivo()) && registro.getBloque() != null) {
            Bloque bloque = registro.getBloque();
            if (bloque.getDisponibles() < bloque.getCapacidad()) {
                bloque.setDisponibles(bloque.getDisponibles() + 1);
                bloque.setUpdatedAt(LocalDateTime.now());
                bloqueRepository.save(bloque);
            }
        }
        
        registroRepository.delete(registro);
    }

    // NOTA: Este método fue optimizado para no cargar todos los registros en memoria.
    // Ahora delega el filtrado directamente a la base de datos.
    // Asegúrate de agregar el método 'findByActivo(boolean activo)' a tu interface RegistroRepository.
    public List<Registro> findByActivoTrue() {
        return registroRepository.findByActivo(true);
    }

    /**
     * Transacción atómica para registrar entrada:
     * Crea el registro y actualiza la disponibilidad del bloque en un solo paso.
     */
    public Registro registrarEntrada(Vehiculo vehiculo, User user, Bloque bloque) {
        // Validación de integridad: No permitir que el mismo vehículo entre dos veces si ya está activo

        // Recargar el bloque desde la DB para asegurar que el contador de cupos es el real en este instante
        Bloque bloqueActualizado = bloqueRepository.findById(bloque.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloque", bloque.getId()));

        if (bloqueActualizado.getDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles en este bloque.");
        }
        
        if (registroRepository.existsByVehiculoPlacaAndActivo(vehiculo.getPlaca(), true)) {
            throw new IllegalStateException("El vehículo con placa " + vehiculo.getPlaca() + " ya se encuentra dentro del parqueadero.");
        }

        // Validar vigencia de documentos del vehículo
        if (Boolean.FALSE.equals(vehiculo.isSoatVigente()) || Boolean.FALSE.equals(vehiculo.isTecnomecanicaVigente())) {
            throw new IllegalStateException("No se permite el ingreso: El SOAT y la Revisión Tecnomecánica del vehículo deben estar vigentes.");
        }

        Registro registro = new Registro();
        registro.setVehiculo(vehiculo);
        registro.setUser(user);
        registro.setBloque(bloqueActualizado);
        registro.setActivo(true);
        registro.setFechaEntrada(LocalDateTime.now());

        // Capa de Asociación: Notas de auditoría
        registro.setObservaciones("Ingreso validado para Vehículo: " + vehiculo.getPlaca());

        // Operación atómica de actualización de estado del recurso (Bloque)
        bloqueActualizado.setDisponibles(bloqueActualizado.getDisponibles() - 1);
        bloqueActualizado.setUpdatedAt(LocalDateTime.now());
        bloqueRepository.save(bloqueActualizado);

        return registroRepository.save(registro);
    }

    /**
     * Transacción atómica para registrar salida:
     * Finaliza el registro y libera el cupo del bloque.
     */
    public Registro registrarSalida(Long registroId) {
        Registro registro = getRegistroById(registroId);
        
        // Solo procesar si el registro está actualmente activo
        if (Boolean.TRUE.equals(registro.getActivo())) {
            // Lógica de Seguridad: Aquí se podría disparar una alerta si el usuario que solicita
            // la salida difiere del que registró la entrada (Data Association Layer)
            
            // En un entorno de producción, aquí se compararía el ID escaneado al salir 
            // con registro.getUser().getId()

            registro.setActivo(false);
            registro.setFechaSalida(LocalDateTime.now());

            // Liberar cupo en el bloque correspondiente
            Bloque bloque = registro.getBloque();
            if (bloque != null) {
                // Recargar el bloque para asegurar que liberamos sobre el dato real de la base de datos
                Bloque bloqueActualizado = bloqueRepository.findById(bloque.getId())
                        .orElse(bloque);
                if (bloqueActualizado.getDisponibles() < bloqueActualizado.getCapacidad()) {
                    bloqueActualizado.setDisponibles(bloqueActualizado.getDisponibles() + 1);
                    bloqueActualizado.setUpdatedAt(LocalDateTime.now());
                    bloqueRepository.save(bloqueActualizado);
                } else {
                    // Sincronizar al tope si había inconsistencia
                    bloqueActualizado.setDisponibles(bloqueActualizado.getCapacidad());
                    bloqueRepository.save(bloqueActualizado);
                }
            }

            return registroRepository.save(registro);
        }
        
        return registro; // Si ya estaba inactivo, retornar sin cambios
    }

    /**
     * Transacción compleja para actualizar un registro existente.
     * Maneja el movimiento de cupos entre bloques si el usuario cambia de zona
     * o si el estado del vehículo cambia de activo a inactivo.
     */
    public Registro actualizarRegistro(Long id, String nuevaPlaca, Boolean nuevoEstado, Long nuevoBloqueId) {
        Registro existente = getRegistroById(id);
        boolean estabaActivo = existente.getActivo();
        
        // Validación de placa al actualizar
        // Si se permite cambiar el vehículo asociado a un registro, se haría aquí:
        // if (nuevoVehiculoId != null && !existente.getVehiculo().getId().equals(nuevoVehiculoId)) {
        //     Vehiculo nuevoVehiculo = vehiculoService.getVehiculoById(nuevoVehiculoId)
        //             .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", nuevoVehiculoId));
        //     if (registroRepository.existsByVehiculoPlacaAndActivo(nuevoVehiculo.getPlaca(), true) && Boolean.TRUE.equals(nuevoEstado)) {
        //         throw new IllegalStateException("El vehículo con placa " + nuevoVehiculo.getPlaca() + " ya tiene un registro activo.");
        //     }
        //     existente.setVehiculo(nuevoVehiculo);
        // }
        if (nuevaPlaca != null && !nuevaPlaca.trim().isEmpty() && !nuevaPlaca.equalsIgnoreCase(existente.getVehiculo().getPlaca())) {
            throw new IllegalArgumentException("La placa del vehículo debe ser actualizada directamente en el vehículo, no en el registro.");
        }
        if (nuevoEstado != null && estabaActivo != nuevoEstado) {
            existente.setActivo(nuevoEstado);
            // Si el vehículo pasa de activo a inactivo, liberar cupo
            if (!nuevoEstado) {
                Bloque b = bloqueRepository.findById(existente.getBloque().getId())
                        .orElse(existente.getBloque());
                if (b.getDisponibles() < b.getCapacidad()) {
                    b.setDisponibles(b.getDisponibles() + 1);
                    b.setUpdatedAt(LocalDateTime.now());
                    bloqueRepository.save(b);
                }
            }
            // Si el vehículo pasa de inactivo a activo (Re-activación manual por admin)
            else if (nuevoEstado) {
                Bloque b = bloqueRepository.findById(existente.getBloque().getId())
                        .orElse(existente.getBloque());
                if (b.getDisponibles() <= 0) {
                    throw new IllegalStateException("No hay cupos disponibles para reactivar este registro.");
                }
                b.setDisponibles(b.getDisponibles() - 1);
                b.setUpdatedAt(LocalDateTime.now());
                bloqueRepository.save(b);
            }
        }

        // Lógica para cambiar de bloque (Transferencia de cupo)
        if (nuevoBloqueId != null && !existente.getBloque().getId().equals(nuevoBloqueId)) {
            Bloque bloqueNuevo = bloqueRepository.findById(nuevoBloqueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bloque", nuevoBloqueId));
            
            // Solo transferir cupos si el registro está actualmente ocupando un lugar
            if (Boolean.TRUE.equals(existente.getActivo())) {
                Bloque bNuevo = bloqueRepository.findById(nuevoBloqueId)
                        .orElseThrow(() -> new ResourceNotFoundException("Bloque", nuevoBloqueId));
                
                if (bNuevo.getDisponibles() <= 0) {
                    throw new IllegalStateException("El nuevo bloque no tiene cupos disponibles");
                }
                
                Bloque bAnterior = bloqueRepository.findById(existente.getBloque().getId())
                        .orElse(existente.getBloque());
                
                if (bAnterior.getDisponibles() < bAnterior.getCapacidad()) {
                    bAnterior.setDisponibles(bAnterior.getDisponibles() + 1);
                    bloqueRepository.save(bAnterior);
                }
                bNuevo.setDisponibles(bNuevo.getDisponibles() - 1);
                bloqueRepository.save(bNuevo);
                existente.setBloque(bNuevo);
            } else {
                existente.setBloque(bloqueNuevo);
            }
        }

        return registroRepository.save(existente);
    }

    // Método sobrecargado para compatibilidad con controladores que no envían userId
    public Page<Registro> buscarConFiltros(LocalDateTime desde, LocalDateTime hasta,
                                           Long bloqueId, String placa, Boolean activo, Pageable pageable) {
        return buscarConFiltros(null, desde, hasta, bloqueId, placa, activo, pageable);
    }

    public Page<Registro> buscarConFiltros(Long userId, LocalDateTime desde, LocalDateTime hasta,
                                           Long bloqueId, String placa, Boolean activo, Pageable pageable) {
        return registroRepository.findAll((Specification<Registro>) (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (desde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), desde));
            }
            if (hasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), hasta));
            }
            if (placa != null && !placa.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vehiculo").get("placa")), "%" + placa.toLowerCase() + "%"));
            }
            if (bloqueId != null) {
                predicates.add(criteriaBuilder.equal(root.get("bloque").get("id"), bloqueId));
            }
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public Registro findVehiculoActivoPorUsuario(User user) {
        return registroRepository.findByUserAndActivo(user, true);
    }

    public List<Registro> findUltimos5RegistrosPorUsuario(User user) {
        return registroRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Genera datos precisos para el gráfico de anillo (doughnut chart).
     * Calcula la ocupación basándose en el conteo real de registros activos,
     * lo que garantiza que el gráfico siempre sea veraz aunque los contadores de bloque fallen.
     */
    public Map<String, Object> obtenerEstadisticasOcupacionReal() {
        List<Bloque> bloques = bloqueRepository.findAll();
        int capacidadTotal = bloques.stream().mapToInt(Bloque::getCapacidad).sum();
        long ocupados = registroRepository.findByActivo(true).size();
        int disponibles = Math.max(0, capacidadTotal - (int) ocupados);

        Map<String, Object> stats = new HashMap<>();
        stats.put("ocupados", ocupados);
        stats.put("disponibles", disponibles);
        stats.put("total", capacidadTotal);
        return stats;
    }
}
