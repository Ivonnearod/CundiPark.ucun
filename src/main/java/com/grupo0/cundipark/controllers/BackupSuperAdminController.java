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
public class BackupSuperAdminController {

    @Autowired private UserService userService;
    @Value("${spring.datasource.username}") private String dbUser;
    @Value("${spring.datasource.password}") private String dbPass;
    @Value("${spring.datasource.url}") private String dbUrl;

    @GetMapping("/superadmin")
    public ResponseEntity<String> createSuperAdminBackup() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (user.getRol() != RolUsuario.SUPERADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String zipName = "superadmin_" + ts + ".zip";
        Path path = Paths.get("backups");
        File tempSql = new File("temp_super_" + ts + ".sql");

        try {
            if (!Files.exists(path)) Files.createDirectories(path);
            File zipFile = path.resolve(zipName).toFile();

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                // 1. DB
                String dbName = extractDbName(dbUrl);
                ProcessBuilder pb = new ProcessBuilder("mysqldump", "-u" + dbUser, "-p" + dbPass, dbName, "--result-file=" + tempSql.getAbsolutePath());
                Process p = pb.start();
                
                if (p.waitFor() == 0 && tempSql.exists()) {
                    addFileToZip(tempSql, "database_full.sql", zos);
                }
                
                // 2. Copia completa de carpetas críticas
                addDirToZip(new File("src/main/resources"), "configuration", zos);
                addDirToZip(new File("logs"), "system_logs", zos);
                addDirToZip(new File("uploads"), "user_uploads", zos);
            }
            return ResponseEntity.ok("Backup Completo creado: " + zipName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear ZIP: " + e.getMessage());
        } finally {
            if (tempSql.exists()) tempSql.delete();
        }
    }

    private String extractDbName(String url) {
        String cleanUrl = url.split("\\?")[0];
        return cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);
    }

    private void addFileToZip(File file, String name, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(file.toPath(), zos);
        zos.closeEntry();
    }

    private void addDirToZip(File dir, String base, ZipOutputStream zos) throws IOException {
        if (!dir.exists()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) addDirToZip(f, base + "/" + f.getName(), zos);
            else addFileToZip(f, base + "/" + f.getName(), zos);
        }
    }
}