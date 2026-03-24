package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
            return userService.getUserByEmail(auth.getName());
        }
        return null;
    }

    private boolean isSuperAdmin(User user) {
        return user != null && user.getRol() == RolUsuario.SUPERADMIN;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperAdmin(currentUser)) {
            return "redirect:/home";
        }

        List<User> todosLosUsuarios = userService.getAllUsers();
        model.addAttribute("listausuarios", todosLosUsuarios);
        model.addAttribute("currentUser", currentUser);
        
        return "superuserDashboard";
    }

    @PostMapping("/changerole/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam("rol") String rolStr, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperAdmin(currentUser)) {
            return "redirect:/home";
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

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (!isSuperAdmin(currentUser)) {
            return "redirect:/home";
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
        if (!isSuperAdmin(currentUser)) {
            return "redirect:/home";
        }
        model.addAttribute("currentUser", currentUser);

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
        if (!isSuperAdmin(currentUser)) {
            return "redirect:/home";
        }
        model.addAttribute("currentUser", currentUser);
        // Aquí se agregaría la lógica para obtener los logs de auditoría

        return "superuserAuditoria";
    }
}