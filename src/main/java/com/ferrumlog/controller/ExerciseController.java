package com.ferrumlog.controller;

import com.ferrumlog.dto.ExerciseDto;
import com.ferrumlog.repository.ExerciseRepository;
import com.ferrumlog.repository.WorkoutSetRepository;
import com.ferrumlog.security.CustomUserDetails;
import com.ferrumlog.service.ExerciseService;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la gestión del banco de ejercicios.
 * Permite CRUD completo de ejercicios del catálogo global.
 */
@Controller
@RequestMapping("/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSetRepository workoutSetRepository;

    /**
     * Lista todos los ejercicios con paginación.
     */
    @GetMapping
    public String listExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<ExerciseDto> exercisePage = exerciseService.getExercises(PageRequest.of(page, size));
        
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", exercisePage.getTotalPages());
        model.addAttribute("totalItems", exercisePage.getTotalElements());

        Long userId = getCurrentUserId();
        List<ExerciseStatsView> myRoutineExercises = buildUserExerciseStats(userId);
        model.addAttribute("myRoutineExercises", myRoutineExercises);

        return "exercises/list";
    }

    /**
     * Muestra el formulario para crear un nuevo ejercicio.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("exercise", new ExerciseDto(null, "", "", ""));
        model.addAttribute("isEdit", false);
        return "exercises/form";
    }

    /**
     * Procesa la creación de un nuevo ejercicio.
     * Aplica validaciones Jakarta con @Valid.
     */
    @PostMapping
    public String createExercise(
            @Valid @ModelAttribute("exercise") ExerciseDto exerciseDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "exercises/form";
        }

        try {
            exerciseService.createExercise(exerciseDto);
            log.info("Ejercicio creado: {}", exerciseDto.name());
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ejercicio creado exitosamente");
            return "redirect:/exercises";

        } catch (Exception e) {
            log.error("Error al crear ejercicio", e);
            model.addAttribute("errorMessage", "Error al crear ejercicio: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "exercises/form";
        }
    }

    /**
     * Muestra el formulario para editar un ejercicio existente.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ExerciseDto exercise = exerciseService.getExerciseById(id);
            model.addAttribute("exercise", exercise);
            model.addAttribute("isEdit", true);
            return "exercises/form";

        } catch (Exception e) {
            log.error("Error al cargar ejercicio para editar", e);
            return "redirect:/exercises";
        }
    }

    /**
     * Procesa la actualización de un ejercicio.
     */
    @PostMapping("/{id}")
    public String updateExercise(
            @PathVariable Long id,
            @Valid @ModelAttribute("exercise") ExerciseDto exerciseDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "exercises/form";
        }

        try {
            exerciseService.updateExercise(id, exerciseDto);
            log.info("Ejercicio actualizado: {}", exerciseDto.name());
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ejercicio actualizado exitosamente");
            return "redirect:/exercises";

        } catch (Exception e) {
            log.error("Error al actualizar ejercicio", e);
            model.addAttribute("errorMessage", "Error al actualizar ejercicio: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "exercises/form";
        }
    }

    /**
     * Elimina un ejercicio.
     */
    @PostMapping("/{id}/delete")
    public String deleteExercise(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            exerciseService.deleteExercise(id);
            log.info("Ejercicio eliminado ID: {}", id);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ejercicio eliminado exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar ejercicio", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al eliminar ejercicio: " + e.getMessage());
        }

        return "redirect:/exercises";
    }

    /**
     * Busca ejercicios por término de búsqueda.
     */
    @GetMapping("/search")
    public String searchExercises(
            @RequestParam String query,
            Model model) {

        model.addAttribute("exercises", exerciseService.searchExercises(query));
        model.addAttribute("searchQuery", query);
        Long userId = getCurrentUserId();
        model.addAttribute("myRoutineExercises", buildUserExerciseStats(userId));
        return "exercises/list";
    }

    private List<ExerciseStatsView> buildUserExerciseStats(Long userId) {
        List<com.ferrumlog.domain.entity.Exercise> routineExercises =
                exerciseRepository.findDistinctByRoutineUserId(userId);

        List<com.ferrumlog.domain.entity.WorkoutSet> allSets =
                workoutSetRepository.findAllByUserIdWithExercise(userId);

        Map<Long, com.ferrumlog.domain.entity.WorkoutSet> bestSetByExercise = new HashMap<>();
        for (com.ferrumlog.domain.entity.WorkoutSet set : allSets) {
            Long exerciseId = set.getExercise().getId();
            com.ferrumlog.domain.entity.WorkoutSet currentBest = bestSetByExercise.get(exerciseId);
            if (currentBest == null || compareSet(set, currentBest) > 0) {
                bestSetByExercise.put(exerciseId, set);
            }
        }

        return routineExercises.stream()
                .map(exercise -> {
                    com.ferrumlog.domain.entity.WorkoutSet bestSet = bestSetByExercise.get(exercise.getId());
                    String bestSetLabel = bestSet == null
                            ? "Aun sin registros"
                            : bestSet.getWeight() + " kg x " + bestSet.getReps();

                    long totalSets = allSets.stream()
                            .filter(set -> set.getExercise().getId().equals(exercise.getId()))
                            .count();

                    return new ExerciseStatsView(
                            exercise.getId(),
                            exercise.getName(),
                            exercise.getMuscleGroup(),
                            bestSetLabel,
                            totalSets);
                })
                .sorted(Comparator.comparing(ExerciseStatsView::name))
                .toList();
    }

    private int compareSet(com.ferrumlog.domain.entity.WorkoutSet a,
                           com.ferrumlog.domain.entity.WorkoutSet b) {
        int byWeight = a.getWeight().compareTo(b.getWeight());
        if (byWeight != 0) {
            return byWeight;
        }
        return Integer.compare(a.getReps(), b.getReps());
    }

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

    public record ExerciseStatsView(
            Long exerciseId,
            String name,
            String muscleGroup,
            String bestSet,
            Long totalSets
    ) {
    }
}
