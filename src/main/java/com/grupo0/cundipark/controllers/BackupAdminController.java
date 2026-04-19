package com.grupo0.cundipark.controllers;

import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/backup")
public class BackupAdminController {

    @Autowired private UserService userService;
    @Value("${spring.datasource.username}") private String dbUser;
    @Value("${spring.datasource.password}") private String dbPass;
    @Value("${spring.datasource.url}") private String dbUrl;

    @GetMapping("/admin")
    public ResponseEntity<String> createAdminBackup() {
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        if (user.getRol() != RolUsuario.ADMIN && user.getRol() != RolUsuario.SUPERADMIN) 
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String zipName = "admin_" + ts + ".zip";
        Path path = Paths.get("backups");

        try {
            if (!Files.exists(path)) Files.createDirectories(path);
            File zipFile = path.resolve(zipName).toFile();
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                // 1. DB (Cambiado a pg_dump para PostgreSQL en Render)
                File sql = new File("temp_admin.sql");
                ProcessBuilder pb = new ProcessBuilder("pg_dump", "-U", dbUser, "-d", "cundipark", "-f", sql.getAbsolutePath());
                pb.environment().put("PGPASSWORD", dbPass);
                Process p = pb.start();
                
                if (p.waitFor() == 0) {
                    addFileToZip(sql, "database.sql", zos);
                    sql.delete();
                }
                // 2. Config
                File cfg = new File("src/main/resources/application.properties");
                if (cfg.exists()) addFileToZip(cfg, "application.properties", zos);
            }
            return ResponseEntity.ok("Backup Admin creado: " + zipName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    private void addFileToZip(File file, String name, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(file.toPath(), zos);
        zos.closeEntry();
    }
}