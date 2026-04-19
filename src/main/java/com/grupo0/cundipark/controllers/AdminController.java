package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
            String email = auth.getName();
            if (email != null) {
                return userService.getUserByEmail(email.toLowerCase().trim());
            }
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getAuthenticatedUser();

        // Verificación de seguridad: Si no es admin, redirigir al home de usuario
        if (user == null || (user.getRol() != RolUsuario.ADMIN && user.getRol() != RolUsuario.SUPERADMIN)) {
            return "redirect:/home";
        }

        // Obtener datos
        List<User> users = userService.getAllUsers(); // Podría optimizarse si solo se necesita el conteo
        List<Bloque> bloques = bloqueService.getAllBloques();

        // Cálculos para tarjetas
        long totalUsuarios = users.size();
        long vehiculosActivos = registroService.countActiveRegistros(); // Optimizado
        
        int capacidadTotal = bloques.stream().mapToInt(Bloque::getCapacidad).sum();
        double ocupacionGeneral = capacidadTotal > 0 ? (double) vehiculosActivos / capacidadTotal * 100 : 0;
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        long entradas24h = registroService.countEntradasLast24Hours(oneDayAgo); // Optimizado

        // OPTIMIZACIÓN: Los cálculos pesados ahora se delegan al Service con queries nativas o agregaciones
        double promedioEstadiaMinutos = registroService.calcularPromedioEstadiaMinutos();
        
        // Obtener distribución de entradas por hora (solo de las últimas 24h, no de todo el histórico)
        Map<Integer, Long> entradasPorHora = registroService.getEntradasDistribucionHora(oneDayAgo);
        
        // Identificación de la "Hora Pico" sin procesar miles de objetos en memoria
        Integer horaPico = entradasPorHora.isEmpty() ? 0 : entradasPorHora.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0);

        // Añadir al modelo
        model.addAttribute("user", user);
        model.addAttribute("isSuperAdmin", user.getRol() == RolUsuario.SUPERADMIN);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("vehiculosActivos", vehiculosActivos);
        model.addAttribute("ocupacionGeneral", String.format("%.1f", ocupacionGeneral));
        model.addAttribute("ocupacionStatus", ocupacionGeneral > 90 ? "Crítica" : ocupacionGeneral > 70 ? "Alta" : "Normal");
        model.addAttribute("statusColor", ocupacionGeneral > 90 ? "danger" : ocupacionGeneral > 70 ? "warning" : "success");
        model.addAttribute("entradas24h", entradas24h);
        model.addAttribute("horaPico", horaPico + ":00");
        model.addAttribute("bloques", bloques);
        model.addAttribute("promedioEstadia", String.format("%.1f", promedioEstadiaMinutos));
        model.addAttribute("entradasPorHora", entradasPorHora);
            // Métrica de Eficiencia Operativa (Ciencia de Datos)
        // Ayuda a reducir las "búsquedas estocásticas" mencionadas en el planteamiento
        Map<String, Double> ocupacionPorBloque = bloques.stream()
                .collect(Collectors.toMap(Bloque::getNombre, 
                    b -> (double) (b.getCapacidad() - b.getDisponibles()) / b.getCapacidad() * 100));
        model.addAttribute("mapaOcupacion", ocupacionPorBloque);

        // Últimos registros (tabla)
        List<Registro> ultimosRegistros = registroService.findTopNByCreatedAtDesc(10);
        model.addAttribute("registros", ultimosRegistros);
        
        // Vehículos activos para gestión directa
        List<Registro> activos = registroService.findByActivoTrue(); // Optimizado

        model.addAttribute("vehiculosActivosList", activos);

        return "adminDashboard";
    }

    @PostMapping("/delete-registro/{id}")
    public String deleteRegistro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registroService.eliminarRegistro(id);
            redirectAttributes.addFlashAttribute("success", "Registro eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el registro.");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/backup")
    public String backupPage(Model model) {
        User user = getAuthenticatedUser();
        if (user == null || (user.getRol() != RolUsuario.ADMIN && user.getRol() != RolUsuario.SUPERADMIN)) {
            return "redirect:/home";
        }
        model.addAttribute("user", user);
        return "backup";
    }

    @GetMapping("/backup/csv")
    public ResponseEntity<byte[]> descargarBackup() {
        User user = getAuthenticatedUser();
        if (user == null || (user.getRol() != RolUsuario.ADMIN && user.getRol() != RolUsuario.SUPERADMIN)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Registro> registros = registroService.getAllRegistros();
        StringBuilder csv = new StringBuilder();
        
        // Añadir BOM (Byte Order Mark) para que Excel reconozca UTF-8 correctamente al abrir el archivo
        csv.append('\ufeff');
        
        // Usamos punto y coma (;) como separador para mejor compatibilidad con Excel en español
        csv.append("ID;Placa;Usuario;Email;Bloque;Fecha Entrada;Fecha Salida;Estado;SOAT Vigente;Tecno Vigente\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Registro r : registros) {
            csv.append(r.getId()).append(";")
               .append(r.getVehiculo() != null ? r.getVehiculo().getPlaca() : "N/A").append(";")
               .append(r.getUser() != null ? r.getUser().getNombre() : "Usuario desconocido").append(";")
               .append(r.getUser() != null ? r.getUser().getEmail() : "N/A").append(";")
               .append(r.getBloque() != null ? r.getBloque().getNombre() : "Sin Bloque").append(";")
               .append(r.getFechaEntrada() != null ? r.getFechaEntrada().format(formatter) : "N/A").append(";")
               .append(r.getFechaSalida() != null ? r.getFechaSalida().format(formatter) : "N/A").append(";")
               .append(Boolean.TRUE.equals(r.getActivo()) ? "ACTIVO" : "FINALIZADO").append(";");
            
            // Obtener la vigencia de los documentos del vehículo asociado
            if (r.getVehiculo() != null) {
                csv.append(Boolean.TRUE.equals(r.getVehiculo().isSoatVigente()) ? "SÍ" : "NO").append(";")
                   .append(Boolean.TRUE.equals(r.getVehiculo().isTecnomecanicaVigente()) ? "SÍ" : "NO");
            } else {
                csv.append("N/A;N/A");
            }
            csv.append("\n");
        }

        // Convertir a bytes asegurando UTF-8
        byte[] out = csv.toString().getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders responseHeaders = new HttpHeaders();
        String filename = "backup_cundipark_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
        
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        responseHeaders.setContentLength(out.length);

        return new ResponseEntity<>(out, responseHeaders, HttpStatus.OK);
    }
    
    // Redirecciones para módulos en construcción
    @GetMapping({"/bloques", "/registros", "/usuarios", "/reportes"})
    public String modulosEnConstruccion() {
        return "redirect:/admin/dashboard";
    }
}