package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class HistoricoController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private UserService userService;

    /**
     * Obtiene el usuario autenticado actualmente.
     * @return User o null si no está autenticado.
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

    /**
     * Normaliza una placa: elimina guiones y convierte a mayúsculas.
     */
    private String normalizarPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return null;
        }
        return placa.trim().replace("-", "").toUpperCase();
    }

    @GetMapping("/historico")
    public String historico(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lógica de segmentación: Si es Admin/SuperAdmin y se pasa un userId, filtramos por ese usuario.
        // De lo contrario, filtramos por el usuario actual.
        Long targetUserId = currentUser.getId();
        User targetUser = currentUser;

        // Lógica de segmentación administrativa unificada
        boolean esAdministrativo = currentUser.getRol() == RolUsuario.ADMIN || currentUser.getRol() == RolUsuario.SUPERADMIN;

        if (esAdministrativo && userId != null) {
            User userToView = userService.getUserById(userId);
            if (userToView != null) {
                targetUserId = userId;
                targetUser = userToView;
            }
        }

        // 1. Crear Pageable con ordenamiento descendente por fecha de creación
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());

        // 2. Normalizar placa para la búsqueda en la base de datos
        String placaNormalizada = normalizarPlaca(placa);

        // 3. Llamar al servicio optimizado que delega el filtrado y paginación a la base de datos
        Page<Registro> paginaRegistros = registroService.buscarConFiltros(
                targetUserId,
                desde,
                hasta,
                null, // bloqueId no se usa en el historial de usuario
                placaNormalizada,
                null, // 'activo' no se filtra, se muestran todos (activos y finalizados)
                pageable
        );

        // Agregar atributos al modelo
        model.addAttribute("paginaRegistros", paginaRegistros);
        model.addAttribute("user", currentUser);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("placa", placa); // valor original para el input
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        return "historicoPage";
    }
}