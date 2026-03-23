INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (1, 'Bloque A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (2, 'Bloque B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (3, 'Bloque C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Inserción de Bloques (Usamos IGNORE para evitar errores si ya existen al reiniciar)
-- En H2 no es necesario IGNORE ya que la DB se recrea al inicio.
-- INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (1, 'Bloque A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (2, 'Bloque B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- INSERT INTO bloques (id, nombre, created_at, updated_at) VALUES (3, 'Bloque C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserción de Usuario Administrador (Password: 'admin')
-- El password hash corresponde a 'admin'. Genera uno nuevo para producción.
INSERT INTO users (id, email, password, nombre, rol, activo, created_at, updated_at) VALUES (1, 'admin@cundipark.com', '$2a$10$GRLdNGh7PAeZc87b.b0g.eTNGj9.1', 'Administrador Principal', 'ADMIN', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);