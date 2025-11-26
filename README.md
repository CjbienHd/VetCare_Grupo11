# VetCare - Sistema de Gestión Veterinaria

Proyecto desarrollado para la asignatura DSY1105 - Desarrollo de Aplicaciones Móviles (Duoc UC).

VetCare es una solución creada para apoyar la gestión diaria de una clínica veterinaria. Incluye una aplicación móvil Android y un backend REST en Spring Boot, ambos desarrollados con una arquitectura clara y enfocada en buenas prácticas.

---

## 1. Descripción General

VetCare permite administrar de forma ordenada y eficiente la información esencial de una clínica veterinaria. Sus principales funciones incluyen:

- Registro y administración de pacientes (mascotas).
- Gestión de citas: motivo, fecha, hora, estado, notas.
- Consulta de clima mediante una API externa.
- Recordatorios automáticos de citas a través de notificaciones en Android.

La aplicación móvil se comunica con un backend REST que expone microservicios orientados a pacientes y citas, utilizando una base de datos Oracle como sistema de persistencia.

---

## 2. Tecnologías Utilizadas

### Backend
- Java  
- Spring Boot  
- Spring Web (MVC)  
- Spring Data JPA  
- Oracle Autonomous Database  
- Maven  
- JUnit 5  
- Mockito  

### Aplicación Android
- Kotlin  
- Android Studio  
- Jetpack Compose  
- Arquitectura MVVM  
- Retrofit (microservicios y API externa)  
- StateFlow y coroutines  
- AlarmManager y BroadcastReceiver (notificaciones)  
- SharedPreferences  
- MockK  
- Kotest  
- coroutines-test  

---

## 3. Arquitectura del Backend

El backend está estructurado por capas, lo que facilita su mantenimiento y escalabilidad:

- Entity  
  - Patient  
  - Appointment  

- Repository  
  - PatientRepository  
  - AppointmentRepository  

- Service  
  - PatientService  
  - AppointmentService  

- Controller  
  - PatientController  
  - AppointmentController  

Los controladores exponen endpoints REST bajo el prefijo `/api`.

---

## 4. Endpoints Principales

### Pacientes – `/api/patients`

#### GET `/api/patients`
Retorna la lista completa de pacientes.

#### POST `/api/patients`
Crea un nuevo paciente.

Ejemplo JSON:
```json
{
  "nombre": "Luna",
  "especie": "Perro",
  "raza": "Labrador",
  "tutor": "Juan Pérez",
  "edad": 3
}
