package com.grupo0.cundipark.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Vehiculo;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import com.grupo0.cundipark.services.VehiculoService;
import com.grupo0.cundipark.validators.ValidadorContrasena;
import com.grupo0.cundipark.validators.ValidadorEmail;
import com.grupo0.cundipark.validators.ValidadorPlaca;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BloqueService bloqueService;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private VehiculoService vehiculoService;

    /**
     * Método de ayuda para obtener el usuario autenticado actualmente.
     * Centraliza la lógica de autenticación para evitar duplicación de código.
     * @return El objeto User si está autenticado, o null en caso contrario.
     */
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

    @GetMapping("/")
    public String index() {
        if (getAuthenticatedUser() != null) {
            return "redirect:/home";
        }
        return "index";
    }

    @GetMapping("/home")
    public String home(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/login";
        }

        // Redirección basada en rol para que cada usuario vea su dashboard correcto
         if (user.getRol() == RolUsuario.SUPERADMIN) {
            return "redirect:/superuser/dashboard";
        }
        if (user.getRol() == RolUsuario.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        // Lógica para el dashboard de usuario normal (USER)
        model.addAttribute("user", user);
        
        int hora = LocalDateTime.now().getHour();
        String saludo = (hora < 12) ? "¡Buenos días!" : (hora < 18) ? "¡Buenas tardes!" : "¡Buenas noches!";
        model.addAttribute("saludo", saludo);
        
        model.addAttribute("vehiculoActual", registroService.findVehiculoActivoPorUsuario(user));
        // Filtrar solo bloques activos para el formulario
        List<Bloque> bloquesActivos = bloqueService.getAllBloques().stream()
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .collect(Collectors.toList());
        model.addAttribute("bloques", bloquesActivos);
        // El historial se consulta ahora en su propia página /historico

        return "dashboard";
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if (getAuthenticatedUser() != null) {
            return "redirect:/home";
        }

        // Calcular ocupación para mostrar en la página de login
        try {
            List<Bloque> todosBloques = bloqueService.getAllBloques();
            long vehiculosActivos = registroService.findByActivoTrue().size();
            int capacidadTotal = todosBloques.stream().mapToInt(Bloque::getCapacidad).sum();
            double ocupacion = (capacidadTotal > 0) ? ((double) vehiculosActivos / capacidadTotal) * 100 : 0;
            
            model.addAttribute("ocupacion", String.format("%.1f", ocupacion));
            String claseOcupacion = (ocupacion < 60) ? "progress-low" : (ocupacion < 90) ? "progress-medium" : "progress-high";
            model.addAttribute("claseOcupacion", claseOcupacion);
        } catch (Exception e) {
            model.addAttribute("ocupacion", "0.0");
        }

        return "loginPage";
    }

    @GetMapping("/registration")
    public String registerForm(@ModelAttribute("user") User user, HttpSession session) {
        if (getAuthenticatedUser() != null) {
            return "redirect:/home";
        }
        return "registrationPage";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        // Normalizar email
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().toLowerCase().trim());
        }

        if (user.getPassword() != null && !user.getPassword().equals(user.getPasswordConfirmation())) {
            result.rejectValue("passwordConfirmation", "error.user", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "registrationPage";
        }

        try {
            // El servicio se encarga de encriptar la contraseña
            userService.registerUser(user);
            
            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Por favor inicia sesión con tus credenciales.");
            return "redirect:/login";
        } catch (DataIntegrityViolationException ex) {
            result.rejectValue("email", "error.user", "El email ya está registrado");
            return "registrationPage";
        }
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@RequestParam("vehiculoId") Long vehiculoId, 
                                 @RequestParam("bloqueId") Long bloqueId, 
                                 RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Su sesión ha expirado. Por favor, inicie sesión de nuevo.");
            return "redirect:/login";
        }

        if (registroService.findVehiculoActivoPorUsuario(user) != null) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes un vehículo registrado dentro del parqueadero.");
            return "redirect:/home";
        }

        // Obtener el vehículo y validar su existencia y vigencia de documentos
        Vehiculo vehiculo = vehiculoService.getVehiculoById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", vehiculoId));

        if (Boolean.FALSE.equals(vehiculo.isSoatVigente()) || Boolean.FALSE.equals(vehiculo.isTecnomecanicaVigente())) {
            redirectAttributes.addFlashAttribute("error", "No se permite el ingreso: El SOAT y la Revisión Tecnomecánica del vehículo deben estar vigentes.");
            return "redirect:/home";
        }

        Bloque bloque = bloqueService.getBloqueById(bloqueId);
        if (bloque == null || bloque.getDisponibles() <= 0) {
            redirectAttributes.addFlashAttribute("error", "El bloque seleccionado no tiene cupos disponibles."); // Recargar bloque para asegurar disponibilidad
            return "redirect:/home";
        }

        try {
            registroService.registrarEntrada(vehiculo, user, bloque);
            redirectAttributes.addFlashAttribute("success", "Entrada registrada exitosamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al procesar la entrada.");
        }
        
        return "redirect:/home";
    }

    @PostMapping("/salida/{id}")
    public String registrarSalida(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Su sesión ha expirado. Por favor, inicie sesión de nuevo.");
            return "redirect:/login";
        }

        Registro registro = registroService.getRegistroById(id);
        
        // Validación de seguridad: el usuario solo puede sacar su propio vehículo
        // O si el usuario es administrador
        boolean esAdmin = user.getRol() == RolUsuario.ADMIN || user.getRol() == RolUsuario.SUPERADMIN;
        boolean esPropio = registro != null && registro.getUser().getId().equals(user.getId());

        if (registro == null || (!esPropio && !esAdmin)) {
            redirectAttributes.addFlashAttribute("error", "Operación no autorizada.");
            return "redirect:/home";
        }

        try {
            // Llamada al método transaccional del servicio
            registroService.registrarSalida(id);
            redirectAttributes.addFlashAttribute("success", "Salida registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la salida: " + e.getMessage());
        }
        
        // Redirección inteligente tras la salida según el rol
        if (user.getRol() == RolUsuario.SUPERADMIN) {
            return "redirect:/superuser/dashboard";
        } else if (user.getRol() == RolUsuario.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/home";
    }

    @GetMapping("/disponibilidad")
    public String verDisponibilidad(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("bloques", bloqueService.getAllBloques());

        return "disponibilidadPage"; // Vista para mostrar la disponibilidad de los bloques
    }
}
