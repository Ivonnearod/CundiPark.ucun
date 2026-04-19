package com.grupo0.cundipark.services;

import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()))
        );
    }

    public boolean authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrProgramaContainingIgnoreCaseOrTipoVinculacionContainingIgnoreCase(
                query, query, query, query, query);
    }

    public long countActiveUsers() {
        return userRepository.countByActivo(true);
    }

    public long countUsersByRole(RolUsuario rol) {
        return userRepository.countByRol(rol);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findUsersByRole(RolUsuario rol) {
        return userRepository.findByRol(rol);
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRol() == null) {
            user.setRol(RolUsuario.USER);
        }
        user.setActivo(true);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}