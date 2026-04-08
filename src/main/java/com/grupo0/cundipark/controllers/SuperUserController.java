package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/superuser")
public class SuperUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private boolean isSuperUser(User user) {
        return user != null && (user.getRol() == RolUsuario.SUPERADMIN);
    }

    /**
     * Verifica si el usuario tiene un rol administrativo (ADMIN o SUPERADMIN).
     * Necesario para el conteo de estadísticas en el dashboard.
     */
    private boolean isAdministrative(User user) {
        return user != null && (user.getRol() == RolUsuario.ADMIN || 
                                user.getRol() == RolUsuario.SUPERADMIN);
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String search, Model model) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }

        List<User> todosLosUsuarios = userService.getAllUsers();
        List<User> usuariosAMostrar;

        if (search != null && !search.trim().isEmpty()) {
            usuariosAMostrar = todosLosUsuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(search.toLowerCase()) || 
                                 u.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                                 (u.getTelefono() != null && u.getTelefono().contains(search)) ||
                                 (u.getPrograma() != null && u.getPrograma().toLowerCase().contains(search.toLowerCase())) ||
                                 (u.getTipoVinculacion() != null && u.getTipoVinculacion().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
            model.addAttribute("search", search);
        } else {
            usuariosAMostrar = todosLosUsuarios;
        }

        // Estadísticas para el dashboard
        long activos = todosLosUsuarios.stream().filter(User::getActivo).count();
        long admins = todosLosUsuarios.stream().filter(this::isAdministrative).count();

        model.addAttribute("totalUsuarios", todosLosUsuarios.size());
        model.addAttribute("usuariosActivos", activos);
        model.addAttribute("totalAdmins", admins);
        model.addAttribute("listausuarios", usuariosAMostrar);
        model.addAttribute("user", currentUser); // Agregado para compatibilidad con navbar
        model.addAttribute("currentUser", currentUser);
        
        return "superuserDashboard";
    }

    @PostMapping("/changerole/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam("rol") String rolStr, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }

        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes cambiar tu propio rol.");
            return "redirect:/superuser/dashboard";
        }

        try {
            User userToUpdate = userService.getUserById(id);
            if (userToUpdate != null) {
                userToUpdate.setRol(RolUsuario.valueOf(rolStr));
                userService.updateUser(userToUpdate);
                redirectAttributes.addFlashAttribute("success", "Rol actualizado correctamente para el usuario: " + userToUpdate.getEmail());
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Rol inválido.");
        }

        return "redirect:/superuser/dashboard";
    }

    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }

        User user = userService.getUserById(id);
        if (user != null) {
            // Restablecer usando el codificador del sistema
            user.setPassword(passwordEncoder.encode("Admin123"));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Contraseña restablecida a 'Admin123' para el usuario: " + user.getEmail());
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
        }
        return "redirect:/superuser/dashboard";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }

        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes desactivar tu propia cuenta.");
            return "redirect:/superuser/dashboard";
        }

        User user = userService.getUserById(id);
        if (user != null) {
            user.setActivo(!user.getActivo());
            userService.updateUser(user);
            String accion = user.getActivo() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("success", "Usuario " + accion + " correctamente: " + user.getEmail());
        }
        return "redirect:/superuser/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }

        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta.");
            return "redirect:/superuser/dashboard";
        }

        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        return "redirect:/superuser/dashboard";
    }

    @GetMapping("/administradores")
    public String administradores(Model model) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser); // Agregado para consistencia

        // Filtrar para mostrar solo administradores
        List<User> admins = userService.getAllUsers().stream()
                .filter(u -> u.getRol() == RolUsuario.ADMIN)
                .collect(Collectors.toList());
        model.addAttribute("listaAdmins", admins);

        return "superuserAdministradores";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperUser(currentUser)) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser); // Agregado para consistencia
        // Aquí se agregaría la lógica para obtener los logs de auditoría

        return "superuserAuditoria";
    }
}