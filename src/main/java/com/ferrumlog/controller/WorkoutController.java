package com.ferrumlog.controller;

import com.ferrumlog.dto.WorkoutSetDto;
import com.ferrumlog.dto.WorkoutSummaryDto;
import com.ferrumlog.repository.RoutineExerciseRepository;
import com.ferrumlog.repository.WorkoutLogRepository;
import com.ferrumlog.repository.RoutineRepository;
import com.ferrumlog.repository.WorkoutSetRepository;
import com.ferrumlog.security.CustomUserDetails;
import com.ferrumlog.service.RoutineService;
import com.ferrumlog.service.WorkoutService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador para registrar entrenamientos y consultar el historial.
 * Implementa la funcionalidad CRÍTICA de recuperar el último WorkoutSet.
 */
@Controller
@RequestMapping("/workouts")
@RequiredArgsConstructor
@Slf4j
public class WorkoutController {

    private final WorkoutService workoutService;
    private final RoutineService routineService;
    private final RoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSetRepository workoutSetRepository;

    /**
     * Muestra el formulario para registrar una nueva serie.
     */
    @GetMapping("/record")
    public String showRecordForm(
            @RequestParam(required = false) Long routineId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = getCurrentUserId();
        var routines = routineService.getUserRoutines(userId);
        WorkoutRoutineForm workoutRoutineForm = new WorkoutRoutineForm();

        model.addAttribute("routines", routines);

        if (routines.isEmpty()) {
            model.addAttribute("selectedRoutineId", null);
            model.addAttribute("routineExercises", java.util.List.of());
            model.addAttribute("workoutRoutineForm", workoutRoutineForm);
            return "workouts/record";
        }

        Long selectedRoutineId = routineId != null ? routineId : routines.get(0).id();
        if (!routineRepository.existsByIdAndUserId(selectedRoutineId, userId)) {
            selectedRoutineId = routines.get(0).id();
        }

        var routineExercises = routineExerciseRepository.findDetailedByRoutineId(selectedRoutineId);
        workoutRoutineForm.setRoutineId(selectedRoutineId);

        List<WorkoutEntry> entries = new ArrayList<>();
        for (var routineExercise : routineExercises) {
            int totalSets = Math.max(1, routineExercise.getSets());
            for (int setNumber = 1; setNumber <= totalSets; setNumber++) {
                WorkoutEntry entry = new WorkoutEntry();
                entry.setExerciseId(routineExercise.getExercise().getId());
                entry.setExerciseName(routineExercise.getExercise().getName());
                entry.setMuscleGroup(routineExercise.getExercise().getMuscleGroup());
                entry.setSetNumber(setNumber);
                entries.add(entry);
            }
        }
        workoutRoutineForm.setEntries(entries);

        model.addAttribute("selectedRoutineId", selectedRoutineId);
        model.addAttribute("routineExercises", routineExercises);
        model.addAttribute("workoutRoutineForm", workoutRoutineForm);

        return "workouts/record";
    }

    /**
     * Registra una nueva serie de entrenamiento.
     * ASIGNA AUTOMÁTICAMENTE el usuario autenticado.
     */
    @PostMapping("/record")
    public String recordWorkoutSet(
            @ModelAttribute("workoutRoutineForm") WorkoutRoutineForm workoutRoutineForm,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            Long userId = getCurrentUserId();
            if (workoutRoutineForm == null || workoutRoutineForm.getRoutineId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Selecciona una rutina para entrenar");
                return "redirect:/workouts/record";
            }

            if (!routineRepository.existsByIdAndUserId(workoutRoutineForm.getRoutineId(), userId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Rutina invalida");
                return "redirect:/workouts/record";
            }

            List<WorkoutSetDto> setsToRecord = new ArrayList<>();
            if (workoutRoutineForm.getEntries() != null) {
                for (WorkoutEntry entry : workoutRoutineForm.getEntries()) {
                    if (entry.getExerciseId() == null || entry.getWeight() == null || entry.getReps() == null) {
                        continue;
                    }
                    if (entry.getReps() < 1 || entry.getWeight() < 0) {
                        continue;
                    }

                        setsToRecord.add(new WorkoutSetDto(
                            null,
                            entry.getExerciseId(),
                            "",
                            entry.getWeight(),
                            entry.getReps(),
                            entry.getRpe()));
                }
            }

                    if (setsToRecord.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Rellena al menos una fila con peso y repeticiones");
                return "redirect:/workouts/record?routineId=" + workoutRoutineForm.getRoutineId();
            }

                    Long workoutLogId = workoutService.recordWorkoutSession(setsToRecord, userId);
                    int recordedSets = setsToRecord.size();

            log.info("Series registradas - Usuario: {}, Cantidad: {}", userId, recordedSets);
            
            redirectAttributes.addFlashAttribute("successMessage",
                        "Entrenamiento guardado. Series registradas: " + recordedSets);
                    return "redirect:/workouts/" + workoutLogId;

        } catch (Exception e) {
            log.error("Error al registrar serie", e);
            model.addAttribute("errorMessage", "Error al registrar serie: " + e.getMessage());
            Long routineId = workoutRoutineForm != null ? workoutRoutineForm.getRoutineId() : null;
            return showRecordForm(routineId, model, redirectAttributes);
        }
    }

    /**
     * ENDPOINT CRÍTICO: Obtiene el último WorkoutSet del usuario para un ejercicio.
     * Útil para mostrar en la UI el último peso/reps/RPE registrado.
     * 
     * Respuesta JSON para consumo desde AJAX/Fetch en el frontend.
     * 
     * Ejemplo de uso en JavaScript:
     * fetch('/workouts/last-set?exerciseId=1')
     *   .then(res => res.json())
     *   .then(data => {
     *     document.getElementById('weight').value = data.weight;
     *     document.getElementById('reps').value = data.reps;
     *   });
     */
    @GetMapping("/last-set")
    @ResponseBody
    public ResponseEntity<?> getLastWorkoutSet(@RequestParam Long exerciseId) {
        try {
            Long userId = getCurrentUserId();
            Optional<WorkoutSetDto> lastSet = workoutService.getLastWorkoutSet(userId, exerciseId);

            if (lastSet.isPresent()) {
                log.debug("Último set encontrado para ejercicio {}: {}kg x {}",
                        exerciseId, lastSet.get().weight(), lastSet.get().reps());
                return ResponseEntity.ok(lastSet.get());
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al obtener último WorkoutSet", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

        @GetMapping("/history")
        public String history(
            @RequestParam(defaultValue = "desc") String sort,
            Model model) {
        Long userId = getCurrentUserId();
        String normalizedSort = "asc".equalsIgnoreCase(sort) ? "asc" : "desc";

        List<WorkoutSummaryDto> workouts = "asc".equals(normalizedSort)
            ? workoutLogRepository.findWorkoutSummariesByUserIdOrderByWorkoutDateAsc(userId)
            : workoutLogRepository.findWorkoutSummariesByUserIdOrderByWorkoutDateDesc(userId);

        model.addAttribute("workouts", workouts);
        model.addAttribute("sort", normalizedSort);
        return "workouts/history";
        }

        @GetMapping("/{workoutLogId}")
        public String workoutDetail(@PathVariable Long workoutLogId, Model model) {
        Long userId = getCurrentUserId();
        if (!workoutLogRepository.existsByIdAndUserId(workoutLogId, userId)) {
            return "redirect:/workouts/history";
        }

        List<WorkoutSetDto> workoutSets = workoutSetRepository
            .findDetailedByWorkoutLogIdAndUserId(workoutLogId, userId)
            .stream()
            .map(set -> new WorkoutSetDto(
                set.getId(),
                set.getExercise().getId(),
                set.getExercise().getName(),
                set.getWeight().doubleValue(),
                set.getReps(),
                set.getRpe() != null ? set.getRpe().doubleValue() : null
            ))
            .toList();

        var summary = workoutLogRepository.findWorkoutSummariesByUserIdOrderByWorkoutDateDesc(userId)
            .stream()
            .filter(workout -> workout.id().equals(workoutLogId))
            .findFirst()
            .orElse(null);

        model.addAttribute("workout", summary);
        model.addAttribute("workoutSets", workoutSets);
        return "workouts/detail";
        }

    /**
     * Método helper para obtener el ID del usuario autenticado.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }

        throw new RuntimeException("Principal no es una instancia de CustomUserDetails");
    }

    public static class WorkoutRoutineForm {
        private Long routineId;
        private List<WorkoutEntry> entries = new ArrayList<>();

        public Long getRoutineId() {
            return routineId;
        }

        public void setRoutineId(Long routineId) {
            this.routineId = routineId;
        }

        public List<WorkoutEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<WorkoutEntry> entries) {
            this.entries = entries;
        }
    }

    public static class WorkoutEntry {
        private Long exerciseId;
        private String exerciseName;
        private String muscleGroup;
        private Integer setNumber;
        private Double weight;
        private Integer reps;
        private Double rpe;

        public Long getExerciseId() {
            return exerciseId;
        }

        public void setExerciseId(Long exerciseId) {
            this.exerciseId = exerciseId;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public void setExerciseName(String exerciseName) {
            this.exerciseName = exerciseName;
        }

        public String getMuscleGroup() {
            return muscleGroup;
        }

        public void setMuscleGroup(String muscleGroup) {
            this.muscleGroup = muscleGroup;
        }

        public Integer getSetNumber() {
            return setNumber;
        }

        public void setSetNumber(Integer setNumber) {
            this.setNumber = setNumber;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Integer getReps() {
            return reps;
        }

        public void setReps(Integer reps) {
            this.reps = reps;
        }

        public Double getRpe() {
            return rpe;
        }

        public void setRpe(Double rpe) {
            this.rpe = rpe;
        }
    }
}
