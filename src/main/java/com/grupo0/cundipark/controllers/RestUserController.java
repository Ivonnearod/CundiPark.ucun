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
import com.grupo0.cundipark.dtos.LoginRequest;
import com.grupo0.cundipark.dtos.LoginResponse;
import com.grupo0.cundipark.dtos.UserDTO;
import com.grupo0.cundipark.exceptions.DuplicateResourceException;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;
import com.grupo0.cundipark.exceptions.UnauthorizedException;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.UserService;
import com.grupo0.cundipark.utils.JwtUtil;
import com.grupo0.cundipark.utils.MapperUtil;
import com.grupo0.cundipark.validators.ValidadorContrasena;
import com.grupo0.cundipark.validators.ValidadorEmail;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class RestUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * GET /api/users - Obtener todos los usuarios
     * @return Lista de usuarios con HTTP 200
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(MapperUtil::toUserDTO)
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success(userDTOs, "Usuarios obtenidos exitosamente")
        );
    }

    /**
     * GET /api/users/{id} - Obtener usuario por ID
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario", id);
        }
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toUserDTO(user), "Usuario obtenido")
        );
    }

    /**
     * POST /api/users - Registrar nuevo usuario
     * @param userDTO Datos del usuario a registrar
     * @return Usuario creado con HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        // Validar email
        if (!ValidadorEmail.esValido(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Validar contraseña
        java.util.List<String> erroresContrasena = ValidadorContrasena.obtenerErrores(userDTO.getPassword());
        if (!erroresContrasena.isEmpty()) {
            throw new IllegalArgumentException("Contraseña débil: " + String.join(", ", erroresContrasena));
        }

        // Verificar si el email ya existe
        User existente = userService.findByEmail(userDTO.getEmail());
        if (existente != null) {
            throw new DuplicateResourceException("Usuario", "email", userDTO.getEmail());
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setNombre(userDTO.getNombre());
        user.setPassword(userDTO.getPassword());

        User userRegistrado = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(MapperUtil.toUserDTO(userRegistrado), "Usuario registrado exitosamente")
        );
    }

    /**
     * PUT /api/users/{id} - Actualizar usuario
     * @param id ID del usuario
     * @param userDTO Datos actualizados
     * @return Usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        User existente = userService.getUserById(id);
        if (existente == null) {
            throw new ResourceNotFoundException("Usuario", id);
        }

        // Validar nuevo email si cambió
        if (!existente.getEmail().equals(userDTO.getEmail())) {
            if (!ValidadorEmail.esValido(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email inválido");
            }
            User emailExistente = userService.findByEmail(userDTO.getEmail());
            if (emailExistente != null) {
                throw new DuplicateResourceException("Usuario", "email", userDTO.getEmail());
            }
        }

        existente.setEmail(userDTO.getEmail());
        if (userDTO.getNombre() != null) {
            existente.setNombre(userDTO.getNombre());
        }

        User userActualizado = userService.saveUser(existente);
        return ResponseEntity.ok(
                ApiResponse.success(MapperUtil.toUserDTO(userActualizado), "Usuario actualizado")
        );
    }

    /**
     * DELETE /api/users/{id} - Eliminar usuario
     * @param id ID del usuario a eliminar
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario", id);
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Usuario eliminado exitosamente")
        );
    }

    /**
     * POST /api/users/login - Autenticar usuario y obtener JWT
     * @param loginRequest credenciales del usuario
     * @return token JWT en caso de éxito
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        // validar email
        if (!ValidadorEmail.esValido(loginRequest.getEmail())) {
            throw new IllegalArgumentException("Email inválido");
        }

        boolean authenticated = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (!authenticated) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(loginRequest.getEmail());
        LoginResponse resp = new LoginResponse(token, "Autenticación exitosa");
        return ResponseEntity.ok(
                ApiResponse.success(resp, "Login exitoso")
        );
    }
}

