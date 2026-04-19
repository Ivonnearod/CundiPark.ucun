# 🚀 REST API ENDPOINTS - CUNDIPARK

Documentación completa de todos los endpoints REST disponibles en la aplicación CUNDIPARK.

---

## 📡 Base URL
```
http://localhost:8080/api
```

---

## 👤 USUARIOS - `/api/users`

### 1. Obtener todos los usuarios
```http
GET /api/users
```
**Respuesta 200:**
```json
{
  "success": true,
  "status": 200,
  "message": "Usuarios obtenidos exitosamente",
  "data": [
    {
      "id": 1,
      "email": "user@example.com",
      "nombre": "Juan",
      "rol": "USUARIO",
      "activo": true
    }
  ],
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 2. Obtener usuario por ID
```http
GET /api/users/{id}
```
**Ejemplo:**
```http
GET /api/users/1
```
**Respuesta 200:**
```json
{
  "success": true,
  "status": 200,
  "message": "Usuario obtenido",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nombre": "Juan",
    "rol": "USUARIO",
    "activo": true
  },
  "timestamp": "2026-02-16T22:30:00"
}
```

**Respuesta 404:**
```json
{
  "success": false,
  "status": 404,
  "message": "Usuario con id 999 no encontrado",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 3. Registrar nuevo usuario
```http
POST /api/users
Content-Type: application/json
```
**Request:**
```json
{
  "email": "nuevo@example.com",
  "nombre": "Carlos",
  "password": "Contraseña123"
}
```
**Validaciones:**
- Email válido y no duplicado
- Contraseña mínimo 6 caracteres
- Contraseña debe tener mayúscula, minúscula y número

**Respuesta 201:**
```json
{
  "success": true,
  "status": 201,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 2,
    "email": "nuevo@example.com",
    "nombre": "Carlos",
    "rol": "USUARIO",
    "activo": true
  },
  "timestamp": "2026-02-16T22:30:00"
}
```

**Respuesta 400 - Email inválido:**
```json
{
  "success": false,
  "status": 400,
  "message": "Email inválido",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

**Respuesta 400 - Contraseña débil:**
```json
{
  "success": false,
  "status": 400,
  "message": "Contraseña débil: Debe contener mayúscula",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

**Respuesta 409 - Email duplicado:**
```json
{
  "success": false,
  "status": 409,
  "message": "Email 'nuevo@example.com' ya existe",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 4. Autenticar usuario (Login)
```http
POST /api/users/login
Content-Type: application/json
```
**Request:**
```json
{
  "email": "user@example.com",
  "password": "Contraseña123"
}
```
**Respuesta 200 (token JWT):**
```json
{
  "success": true,
  "status": 200,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "message": "Autenticación exitosa"
  },
  "timestamp": "2026-02-16T22:30:00"
}
```

**Respuesta 401 - Credenciales inválidas:**
```json
{
  "success": false,
  "status": 401,
  "message": "Credenciales inválidas",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 5. Actualizar usuario
```http
PUT /api/users/{id}
Content-Type: application/json
```
**Request:**
```json
{
  "email": "newemail@example.com",
  "nombre": "Juan Actualizado"
}
```
**Respuesta 200:**
```json
{
  "success": true,
  "status": 200,
  "message": "Usuario actualizado",
  "data": {
    "id": 1,
    "email": "newemail@example.com",
    "nombre": "Juan Actualizado",
    "rol": "USUARIO",
    "activo": true
  },
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 6. Eliminar usuario
```http
DELETE /api/users/{id}
```
**Ejemplo:**
```http
DELETE /api/users/1
```
**Respuesta 200:**
```json
{
  "success": true,
  "status": 200,
  "message": "Usuario eliminado exitosamente",
  "data": null,
  "timestamp": "2026-02-16T22:30:00"
}
```

---

## 📋 REGISTROS - `/api/registros`

### 1. Obtener todos los registros
```http
GET /api/registros
```
**Respuesta:**
```json
{
  "success": true,
  "status": 200,
  "message": "Registros obtenidos exitosamente",
  "data": [
    {
      "id": 1,
      "placa": "ABC-1234",
      "activo": true,
      "estado": "ACTIVO",
      "userId": 1,
      "bloqueId": 1,
      "bloqueNombre": "A",
      "createdAt": "2026-02-16T22:30:00",
      "updatedAt": "2026-02-16T22:30:00"
    }
  ],
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 2. Obtener registro por ID
```http
GET /api/registros/{id}
```
**Ejemplo:**
```http
GET /api/registros/1
```

---

### 3. Búsqueda avanzada con filtros
```http
GET /api/registros/filtro/avanzado?desde=2026-02-01&hasta=2026-02-28&bloqueId=1&placa=ABC-1234&activo=true
```
**Parámetros opcionales:**
- `desde`: Fecha inicio (ISO 8601: YYYY-MM-DDTHH:mm:ss)
- `hasta`: Fecha fin (ISO 8601)
- `bloqueId`: ID del bloque
- `placa`: Número de placa
- `activo`: true/false

**Respuesta:**
```json
{
  "success": true,
  "status": 200,
  "message": "Búsqueda completada: 5 registros encontrados",
  "data": [ ... ],
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 4. Obtener registros activos
```http
GET /api/registros/activos/listado
```
**Respuesta:** Lista de registros con `activo: true`

---

### 5. Crear nuevo registro
```http
POST /api/registros
Content-Type: application/json
```
**Request:**
```json
{
  "vehiculoPlaca": "ABC-1234",
  "userId": 1,
  "bloqueId": 1,
  "vehiculoMarca": "Toyota",
  "vehiculoModelo": "2023",
  "vehiculoColor": "Gris",
  "soatVencimiento": "2026-12-31",
  "tecnomecanicaVencimiento": "2026-12-31"
}
```
**Validaciones:**
- Placa debe ser válida: ABC-1234 o ABC1234 (formato colombiano)

**Respuesta 201:**
```json
{
  "success": true,
  "status": 201,
  "message": "Registro creado exitosamente",
  "data": { ... },
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 6. Actualizar registro
```http
PUT /api/registros/{id}
Content-Type: application/json
```
**Request:**
```json
{
  "placa": "XYZ-5678",
  "activo": false
}
```

---

### 7. Eliminar registro
```http
DELETE /api/registros/{id}
```

---

## 🏢 BLOQUES - `/api/bloques`

### 1. Obtener todos los bloques
```http
GET /api/bloques
```
**Respuesta:**
```json
{
  "success": true,
  "status": 200,
  "message": "Bloques obtenidos exitosamente",
  "data": [
    {
      "id": 1,
      "nombre": "Bloque A",
      "capacidad": 50,
      "disponibles": 25,
      "activo": true
    }
  ],
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 2. Obtener bloque por ID
```http
GET /api/bloques/{id}
```
**Ejemplo:**
```http
GET /api/bloques/1
```

---

### 3. Crear nuevo bloque
```http
POST /api/bloques
Content-Type: application/json
```
**Request:**
```json
{
  "nombre": "Bloque B",
  "capacidad": 60,
  "disponibles": 60,
  "activo": true
}
```
**Validaciones:**
- Nombre no puede estar vacío
- Nombre debe ser único

**Respuesta 201:**
```json
{
  "success": true,
  "status": 201,
  "message": "Bloque creado exitosamente",
  "data": { ... },
  "timestamp": "2026-02-16T22:30:00"
}
```

---

### 4. Actualizar bloque
```http
PUT /api/bloques/{id}
Content-Type: application/json
```
**Request:**
```json
{
  "nombre": "Bloque A Actualizado",
  "capacidad": 100,
  "disponibles": 50,
  "activo": true
}
```

---

### 5. Eliminar bloque
```http
DELETE /api/bloques/{id}
```

---

### 6. Obtener disponibilidad de bloque
```http
GET /api/bloques/{id}/disponibilidad
```
**Ejemplo:**
```http
GET /api/bloques/1/disponibilidad
```
**Respuesta:**
```json
{
  "success": true,
  "status": 200,
  "message": "Información de disponibilidad",
  "data": {
    "id": 1,
    "nombre": "Bloque A",
    "capacidadTotal": 50,
    "espaciosDisponibles": 25,
    "espaciosOcupados": 25
  },
  "timestamp": "2026-02-16T22:30:00"
}
```

---

## ⚠️ Códigos de Error

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado |
| 400 | Bad Request - Datos inválidos |
| 401 | Unauthorized - No autenticado |
| 404 | Not Found - Recurso no encontrado |
| 409 | Conflict - Recurso duplicado |
| 500 | Internal Server Error - Error del servidor |

---

## 🔐 Seguridad

- ✅ Validación de entrada en todos los endpoints
- ✅ Encriptación BCrypt para contraseñas
- ✅ CORS habilitado para `/api/**`
- ✅ Manejo centralizado de excepciones
- ✅ Validación de placas colombianas

---

## 📝 Ejemplos cURL

### Obtener todos los usuarios
```bash
curl -X GET http://localhost:8080/api/users
```

### Registrar nuevo usuario
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "nombre": "Carlos",
    "password": "Contraseña123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Contraseña123"
  }'
```

### Crear registro
```bash
curl -X POST http://localhost:8080/api/registros \
  -H "Content-Type: application/json" \
  -d '{
    "vehiculoPlaca": "ABC-123",
    "userId": 1,
    "bloqueId": 1,
    "vehiculoMarca": "Mazda",
    "vehiculoModelo": "2024",
    "soatVencimiento": "2026-01-01",
    "tecnomecanicaVencimiento": "2026-01-01"
  }'
```

### Búsqueda avanzada de registros
```bash
curl -X GET "http://localhost:8080/api/registros/filtro/avanzado?desde=2026-02-01T00:00:00&hasta=2026-02-28T23:59:59&bloqueId=1"
```

### Obtener disponibilidad de bloque
```bash
curl -X GET http://localhost:8080/api/bloques/1/disponibilidad
```

---

## 🧪 Testing con Postman

Para probar estos endpoints usando Postman:

1. Crea una colección llamada "CUNDIPARK API"
2. Crea carpetas: Users, Registros, Bloques
3. Importa los ejemplos anteriores
4. Configura variables de entorno:
   - `{{baseUrl}}` = `http://localhost:8080/api`
   - `{{userId}}` = ID del usuario actual
   - `{{bloqueId}}` = ID del bloque actual

---

## 📚 Próximas Mejoras

- [ ] Autenticación JWT
- [ ] Roles y permisos RBAC
- [ ] Paginación en listados
- [ ] Búsqueda full-text
- [ ] Rate limiting
- [ ] Swagger/OpenAPI
- [ ] WebSocket para actualizaciones en tiempo real
