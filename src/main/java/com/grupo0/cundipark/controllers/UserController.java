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
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import com.grupo0.cundipark.validators.ValidadorContrasena;
import com.grupo0.cundipark.validators.ValidadorEmail;
import com.grupo0.cundipark.validators.ValidadorPlaca;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

    /**
     * Método de ayuda para obtener el usuario autenticado actualmente.
     * Centraliza la lógica de autenticación para evitar duplicación de código.
     * @return El objeto User si está autenticado, o null en caso contrario.
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
            return userService.getUserByEmail(auth.getName());
        }
        return null;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/login";
        }

        // Redirección basada en rol para que cada usuario vea su dashboard correcto
        if (user.getRol() == RolUsuario.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        if (user.getRol() == RolUsuario.SUPERADMIN) {
            return "redirect:/superuser/dashboard";
        }

        // Lógica para el dashboard de usuario normal (USER)
        model.addAttribute("user", user);
        model.addAttribute("vehiculoActual", registroService.findVehiculoActivoPorUsuario(user));
        // Filtrar solo bloques activos para el formulario
        List<Bloque> bloquesActivos = bloqueService.getAllBloques().stream()
                .filter(Bloque::getActivo)
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .collect(Collectors.toList());
        model.addAttribute("bloques", bloquesActivos);
        // El historial se consulta ahora en su propia página /historico

        return "dashboard";
        return "home";
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
        // 1. Validar formato de email
        if (user.getEmail() != null && !ValidadorEmail.esValido(user.getEmail())) {
            result.rejectValue("email", "error.user", "El formato del email es inválido.");
        } else if (user.getEmail() != null) {
            // Normalizar email antes de cualquier operación
            user.setEmail(user.getEmail().toLowerCase().trim());
        }

        // 2. Validar fortaleza de la contraseña
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            java.util.List<String> erroresContrasena = ValidadorContrasena.obtenerErrores(user.getPassword());
            if (!erroresContrasena.isEmpty()) {
                for (String error : erroresContrasena) {
                    result.rejectValue("password", "error.user", error);
                }
            }
        }

        // 3. Validar que las contraseñas coincidan
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
    public String registrarEntrada(@RequestParam("placa") String placa, @RequestParam("bloqueId") Long bloqueId, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Su sesión ha expirado. Por favor, inicie sesión de nuevo.");
            return "redirect:/login";
        }

        if (!ValidadorPlaca.esValida(placa)) {
            redirectAttributes.addFlashAttribute("error", "Placa inválida. Use formato ABC-123 o ABC123");
            return "redirect:/home";
        }

        if (registroService.findVehiculoActivoPorUsuario(user) != null) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes un vehículo registrado dentro del parqueadero.");
            return "redirect:/home";
        }

        Bloque bloque = bloqueService.getBloqueById(bloqueId);
        if (bloque == null || bloque.getDisponibles() <= 0) {
            redirectAttributes.addFlashAttribute("error", "El bloque seleccionado no tiene cupos disponibles.");
            return "redirect:/home";
        }

        try {
            Registro registro = new Registro();
            // Guardamos la placa sin guiones para respetar el límite de 6 caracteres de la base de datos
            registro.setPlaca(ValidadorPlaca.formatear(placa).replace("-", ""));
            registro.setUser(user);
            registro.setBloque(bloque);
            registro.setActivo(true);
            
            registroService.saveRegistro(registro);
            redirectAttributes.addFlashAttribute("success", "Entrada registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la entrada: " + e.getMessage());
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
        if (registro == null || !registro.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Operación no autorizada.");
            return "redirect:/home";
        }

        try {
            registro.setActivo(false);
            registro.setFechaSalida(LocalDateTime.now());
            registroService.saveRegistro(registro);
            redirectAttributes.addFlashAttribute("success", "Salida registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la salida: " + e.getMessage());
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
