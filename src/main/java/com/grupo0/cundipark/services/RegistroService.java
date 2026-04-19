package com.grupo0.cundipark.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Vehiculo;
import com.grupo0.cundipark.repositories.RegistroRepository;
import com.grupo0.cundipark.validators.ValidadorPlaca;
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
    private VehiculoService vehiculoService;

    @Autowired
    private UserService userService;

    @Autowired
    private BloqueService bloqueService;

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

    public List<Registro> findByActivoTrue() {
        return registroRepository.findByActivo(true);
    }

    /**
     * Orquesta el proceso completo de entrada: gestiona el vehículo y valida los requisitos.
     * Esto centraliza la lógica que antes estaba repetida en los controladores.
     */
    public Registro procesarEntradaCompleta(String placa, String marca, String modelo, String color, 
                                          LocalDate soat, LocalDate tecno, Long userId, Long bloqueId) {
        User user = userService.getUserById(userId);
        if (user == null) throw new ResourceNotFoundException("Usuario", userId);

        // La placa ya fue validada y formateada en el controlador (ABC-123)
        // Solo hacer una validación rápida
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        
        // ASIGNAR BLOQUE POR DEFECTO SI NO VIENE
        if (bloqueId == null) {
            List<Bloque> activos = bloqueService.getBloquesActivos();
            if (activos.isEmpty()) {
                throw new IllegalStateException("No hay bloques disponibles en el sistema.");
            }
            bloqueId = activos.get(0).getId();
            System.out.println("[DEBUG] BloqueId nulo, asignando por defecto: " + bloqueId);
        }

        // Usamos el formateador oficial (AAA-123)
        String placaFormateada = ValidadorPlaca.formatear(placa);

        Bloque bloque = bloqueService.getBloqueById(bloqueId);
        if (bloque == null) throw new ResourceNotFoundException("Bloque", bloqueId);

        // 1. Buscar o Crear Vehículo
        Vehiculo vehiculo = vehiculoService.findByPlaca(placaFormateada).orElse(null);
        if (vehiculo == null) {
            System.out.println("[DEBUG] Creando nuevo vehículo con placa: " + placaFormateada);
            vehiculo = new Vehiculo();
            vehiculo.setPlaca(placaFormateada);
            vehiculo.setUser(user);
        }
        
        // 2. Actualizar datos técnicos (aseguramos que siempre estén al día)
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setColor(color);
        vehiculo.setSoatVencimiento(soat);
        vehiculo.setTecnomecanicaVencimiento(tecno);
        
        vehiculo = vehiculoService.saveVehiculo(vehiculo);

        // 3. Delegar al registro de entrada (donde se valida SOAT y Disponibilidad)
        return registrarEntrada(vehiculo, user, bloque);
    }

    /**
     * Transacción atómica para registrar entrada:
     * Crea el registro y actualiza la disponibilidad del bloque en un solo paso.
     */
    public Registro registrarEntrada(Vehiculo vehiculo, User user, Bloque bloque) {
        Bloque bloqueActualizado = bloqueRepository.findById(bloque.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloque", bloque.getId()));

        // Validar cupos disponibles
        if (bloqueActualizado.getDisponibles() <= 0) {
            System.out.println("[ERROR REGISTRO] Bloque sin cupos: " + bloqueActualizado.getNombre());
            throw new IllegalStateException("No hay cupos disponibles en este bloque.");
        }
        
        // Validar que el vehículo no esté ya adentro
        if (registroRepository.countByVehiculo_PlacaAndActivo(vehiculo.getPlaca(), true) > 0) {
            System.out.println("[ERROR REGISTRO] Vehículo ya está dentro: " + vehiculo.getPlaca());
            throw new IllegalStateException("El vehículo con placa " + vehiculo.getPlaca() + " ya se encuentra dentro del parqueadero.");
        }

        // Validar vigencia de documentos del vehículo
        LocalDate hoy = LocalDate.now();
        boolean soatVigente = vehiculo.getSoatVencimiento() != null && !vehiculo.getSoatVencimiento().isBefore(hoy);
        boolean tecnoVigente = vehiculo.getTecnomecanicaVencimiento() != null && !vehiculo.getTecnomecanicaVencimiento().isBefore(hoy);

        // Log preventivo para depuración rápida en consola
        if (!soatVigente) System.out.println("[ALERTA] SOAT Vencido o Nulo para: " + vehiculo.getPlaca());
        if (!tecnoVigente) System.out.println("[ALERTA] Tecnomecánica Vencida o Nula para: " + vehiculo.getPlaca());

        if (!soatVigente || !tecnoVigente) {
            System.out.println("[ERROR REGISTRO] Documentos vencidos para placa: " + vehiculo.getPlaca());
            throw new IllegalStateException("No se permite el ingreso: El SOAT y la Revisión Tecnomecánica del vehículo deben estar vigentes.");
        }

        Registro registro = new Registro();
        registro.setVehiculo(vehiculo);

        registro.setPlaca(vehiculo.getPlaca()); // Asignamos la placa para cumplir con la restricción de la DB
        registro.setUser(user);
        registro.setBloque(bloqueActualizado);
        registro.setActivo(true);
        registro.setSoatVigente(soatVigente);
        registro.setTecnomecanicaVigente(tecnoVigente);
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
        return registroRepository.findAll((root, query, criteriaBuilder) -> {
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
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasOcupacionReal() {
        List<Bloque> bloques = bloqueRepository.findAll();
        int capacidadTotal = bloques.stream().mapToInt(Bloque::getCapacidad).sum();
        long ocupados = countActiveRegistros(); // Usar el método optimizado
        int disponibles = Math.max(0, capacidadTotal - (int) ocupados);

        Map<String, Object> stats = new HashMap<>();
        stats.put("ocupados", ocupados);
        stats.put("disponibles", disponibles);
        stats.put("total", capacidadTotal);
        return stats;
    }

    @Transactional(readOnly = true)
    public long countActiveRegistros() {
        return registroRepository.countByActivo(true);
    }

    @Transactional(readOnly = true)
    public long countEntradasLast24Hours(LocalDateTime oneDayAgo) {
        return registroRepository.countByCreatedAtAfter(oneDayAgo);
    }

    @Transactional(readOnly = true)
    public List<Registro> findTopNByCreatedAtDesc(int limit) {
        // Usar PageRequest para que la base de datos limite los resultados (SELECT ... LIMIT X)
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return registroRepository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public double calcularPromedioEstadiaMinutos() {
        // En un entorno real, usaríamos una query JPQL: "SELECT AVG(TIMESTAMPDIFF(MINUTE, r.fechaEntrada, r.fechaSalida)) FROM Registro r WHERE r.activo = false"
        // Por ahora, limitamos el cálculo a los últimos 500 registros para evitar congelamiento
        List<Registro> recientes = findTopNByCreatedAtDesc(500);
        if (recientes.isEmpty()) return 0.0;

        return recientes.stream()
                .filter(r -> r != null && Boolean.FALSE.equals(r.getActivo()) 
                        && r.getFechaEntrada() != null && r.getFechaSalida() != null)
                .mapToLong(r -> java.time.Duration.between(r.getFechaEntrada(), r.getFechaSalida()).toMinutes())
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getEntradasDistribucionHora(LocalDateTime since) {
        // CORRECCIÓN: Filtrar directamente desde los últimos 1000 registros
        // En producción se debe usar: registroRepository.countByHourSince(since)
        return findTopNByCreatedAtDesc(1000).stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since))
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getCreatedAt().getHour(), java.util.stream.Collectors.counting()));
    }
}
