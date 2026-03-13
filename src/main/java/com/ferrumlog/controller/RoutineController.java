package com.ferrumlog.controller;

import com.ferrumlog.dto.RoutineDto;
import com.ferrumlog.security.CustomUserDetails;
import com.ferrumlog.service.ExerciseService;
import com.ferrumlog.service.RoutineService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la gestión de rutinas de entrenamiento.
 * ASIGNA AUTOMÁTICAMENTE el usuario autenticado usando SecurityContextHolder.
 */
@Controller
@RequestMapping("/routines")
@RequiredArgsConstructor
@Slf4j
public class RoutineController {

    private final RoutineService routineService;
    private final ExerciseService exerciseService;

    /**
     * Lista todas las rutinas del usuario autenticado.
     */
    @GetMapping
    public String listRoutines(Model model) {
        Long userId = getCurrentUserId();
        model.addAttribute("routines", routineService.getUserRoutines(userId));
        return "routines/list";
    }

    /**
     * Muestra el formulario para crear una nueva rutina.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("routineForm", buildCreateForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/routines");
        return "routines/form";
    }

    /**
     * Procesa la creación de una nueva rutina.
     * ASIGNA AUTOMÁTICAMENTE el usuario autenticado mediante SecurityContextHolder.
     */
    @PostMapping
    public String createRoutine(
            @ModelAttribute("routineForm") RoutineForm routineForm,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Compatibilidad defensiva: si llega routineId por POST /routines,
        // tratamos el request como una actualización.
        if (routineForm != null && routineForm.getRoutineId() != null) {
            return updateRoutine(routineForm.getRoutineId(), routineForm, redirectAttributes, model);
        }

        if (!isRoutineFormValid(routineForm, model)) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/routines");
            return "routines/form";
        }

        try {
            // OBTENER USUARIO AUTENTICADO automáticamente
            Long userId = getCurrentUserId();

            RoutineDto routineDto = toRoutineDto(routineForm, null);
            routineService.createRoutine(routineDto, userId);
            log.info("Rutina creada por usuario ID {}: {}", userId, routineDto.name());
            redirectAttributes.addFlashAttribute("successMessage",
                "Rutina creada exitosamente");
            
            return "redirect:/routines";

        } catch (Exception e) {
            log.error("Error al crear rutina", e);
            model.addAttribute("errorMessage", "Error al crear rutina: " + e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/routines");
            return "routines/form";
        }
    }

    /**
     * Muestra el formulario para editar una rutina existente.
     * Solo el propietario puede editar su rutina.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Long userId = getCurrentUserId();
            RoutineDto routine = routineService.getRoutineById(id, userId);

            model.addAttribute("routineForm", buildEditForm(routine));
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/routines/" + id);
            return "routines/form";

        } catch (SecurityException e) {
            log.warn("Usuario sin permisos intentando editar rutina ID: {}", id);
            return "redirect:/routines";
        } catch (Exception e) {
            log.error("Error al cargar rutina para editar", e);
            return "redirect:/routines";
        }
    }

    /**
     * Procesa la actualización de una rutina.
     * Solo el propietario puede actualizar su rutina.
     */
    @PostMapping("/{id}")
    public String updateRoutine(
            @PathVariable Long id,
            @ModelAttribute("routineForm") RoutineForm routineForm,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!isRoutineFormValid(routineForm, model)) {
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/routines/" + id);
            return "routines/form";
        }

        try {
            Long userId = getCurrentUserId();
            RoutineDto routineDto = toRoutineDto(routineForm, id);
            routineService.updateRoutine(id, routineDto, userId);
            log.info("Rutina actualizada ID {}: {}", id, routineDto.name());
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Rutina actualizada exitosamente");
            return "redirect:/routines";

        } catch (SecurityException e) {
            log.warn("Usuario sin permisos intentando actualizar rutina ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No tienes permisos para modificar esta rutina");
            return "redirect:/routines";
        } catch (Exception e) {
            log.error("Error al actualizar rutina", e);
            model.addAttribute("errorMessage", "Error al actualizar rutina: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/routines/" + id);
            return "routines/form";
        }
    }

    /**
     * Elimina una rutina.
     * Solo el propietario puede eliminar su rutina.
     */
    @PostMapping("/{id}/delete")
    public String deleteRoutine(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Long userId = getCurrentUserId();
            routineService.deleteRoutine(id, userId);
            log.info("Rutina eliminada ID: {} por usuario ID: {}", id, userId);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Rutina eliminada exitosamente");

        } catch (SecurityException e) {
            log.warn("Usuario sin permisos intentando eliminar rutina ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No tienes permisos para eliminar esta rutina");
        } catch (Exception e) {
            log.error("Error al eliminar rutina", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al eliminar rutina: " + e.getMessage());
        }

        return "redirect:/routines";
    }

    /**
     * Método helper para obtener el ID del usuario autenticado.
     * Utiliza SecurityContextHolder para acceder al contexto de seguridad.
     * 
     * @return ID del usuario autenticado
     * @throws RuntimeException si no hay usuario autenticado
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

    private RoutineForm buildCreateForm() {
        RoutineForm form = new RoutineForm();
        form.setRoutineId(null);
        form.setName("");
        form.setDescription("");

        List<RoutineExerciseRow> rows = new ArrayList<>();
        exerciseService.getAllExercises().forEach(exercise -> {
            RoutineExerciseRow row = new RoutineExerciseRow();
            row.setExerciseId(exercise.id());
            row.setExerciseName(exercise.name());
            row.setMuscleGroup(exercise.muscleGroup());
            row.setSelected(false);
            row.setSets(3);
            rows.add(row);
        });

        form.setExercises(rows);
        return form;
    }

    private RoutineForm buildEditForm(RoutineDto routineDto) {
        RoutineForm form = buildCreateForm();
        form.setRoutineId(routineDto.id());
        form.setName(routineDto.name());
        form.setDescription(routineDto.description());

        Map<Long, Integer> setsByExercise = new HashMap<>();
        if (routineDto.exerciseIds() != null && routineDto.exerciseSets() != null) {
            for (int i = 0; i < routineDto.exerciseIds().size(); i++) {
                Integer sets = i < routineDto.exerciseSets().size() ? routineDto.exerciseSets().get(i) : 3;
                setsByExercise.put(routineDto.exerciseIds().get(i), sets != null && sets > 0 ? sets : 3);
            }
        }

        for (RoutineExerciseRow row : form.getExercises()) {
            if (setsByExercise.containsKey(row.getExerciseId())) {
                row.setSelected(true);
                row.setSets(setsByExercise.get(row.getExerciseId()));
            }
        }

        return form;
    }

    private boolean isRoutineFormValid(RoutineForm form, Model model) {
        if (form == null) {
            model.addAttribute("errorMessage", "Formulario invalido");
            model.addAttribute("routineForm", buildCreateForm());
            return false;
        }

        if (form.getName() == null || form.getName().isBlank()) {
            model.addAttribute("errorMessage", "El nombre de la rutina es obligatorio");
            return false;
        }

        String normalizedName = form.getName().trim();
        if (normalizedName.length() > 100) {
            model.addAttribute("errorMessage", "El nombre no puede superar 100 caracteres");
            return false;
        }

        if (form.getDescription() != null && form.getDescription().length() > 1000) {
            model.addAttribute("errorMessage", "La descripcion no puede superar 1000 caracteres");
            return false;
        }

        boolean hasSelectedExercise = form.getExercises() != null
                && form.getExercises().stream().anyMatch(RoutineExerciseRow::isSelected);
        if (!hasSelectedExercise) {
            model.addAttribute("errorMessage", "Selecciona al menos un ejercicio");
            return false;
        }

        boolean missingExerciseId = form.getExercises().stream()
                .filter(RoutineExerciseRow::isSelected)
                .anyMatch(row -> row.getExerciseId() == null);
        if (missingExerciseId) {
            model.addAttribute("errorMessage", "El formulario contiene ejercicios invalidos");
            return false;
        }

        boolean invalidSets = form.getExercises().stream()
                .filter(RoutineExerciseRow::isSelected)
                .anyMatch(row -> row.getSets() == null || row.getSets() < 1);
        if (invalidSets) {
            model.addAttribute("errorMessage", "Las series deben ser mayores que 0");
            return false;
        }

        return true;
    }

    private RoutineDto toRoutineDto(RoutineForm form, Long id) {
        List<Long> exerciseIds = new ArrayList<>();
        List<Integer> exerciseSets = new ArrayList<>();

        for (RoutineExerciseRow row : form.getExercises()) {
            if (row.isSelected()) {
                exerciseIds.add(row.getExerciseId());
                exerciseSets.add(row.getSets());
            }
        }

        return new RoutineDto(
                id,
                form.getName().trim(),
                form.getDescription(),
                exerciseIds,
                exerciseSets
        );
    }

    public static class RoutineForm {
        private Long routineId;
        private String name;
        private String description;
        private List<RoutineExerciseRow> exercises = new ArrayList<>();

        public Long getRoutineId() {
            return routineId;
        }

        public void setRoutineId(Long routineId) {
            this.routineId = routineId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<RoutineExerciseRow> getExercises() {
            return exercises;
        }

        public void setExercises(List<RoutineExerciseRow> exercises) {
            this.exercises = exercises;
        }
    }

    public static class RoutineExerciseRow {
        private Long exerciseId;
        private String exerciseName;
        private String muscleGroup;
        private boolean selected;
        private Integer sets;

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

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public Integer getSets() {
            return sets;
        }

        public void setSets(Integer sets) {
            this.sets = sets;
        }
    }
}
