package com.ferrumlog.controller;

import com.ferrumlog.domain.entity.Routine;
import com.ferrumlog.dto.WorkoutSummaryDto;
import com.ferrumlog.repository.RoutineRepository;
import com.ferrumlog.repository.WorkoutLogRepository;
import com.ferrumlog.repository.WorkoutSetRepository;
import com.ferrumlog.security.CustomUserDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para páginas estáticas y navegación general.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final WorkoutLogRepository workoutLogRepository;
    private final RoutineRepository routineRepository;
    private final WorkoutSetRepository workoutSetRepository;

    /**
     * Página de inicio (pública).
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Dashboard del usuario autenticado.
     * Aquí verá resumen de sus entrenamientos, rutinas, etc.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Long userId = currentUser.getUserId();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1)
                .atStartOfDay().minusNanos(1);

        long monthlyWorkouts = workoutLogRepository
                .countByUserIdAndWorkoutDateBetween(userId, startOfMonth, endOfMonth);
        long routinesCount = routineRepository.countByUserId(userId);
        long totalSets = workoutSetRepository.countByWorkoutLog_UserId(userId);

        List<WorkoutSummaryDto> recentWorkouts = workoutLogRepository
            .findWorkoutSummariesByUserIdOrderByWorkoutDateDesc(userId)
            .stream()
            .limit(5)
            .toList();
        List<Routine> recentRoutines = routineRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);

        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("monthlyWorkouts", monthlyWorkouts);
        model.addAttribute("routinesCount", routinesCount);
        model.addAttribute("totalSets", totalSets);
        model.addAttribute("recentWorkouts", recentWorkouts);
        model.addAttribute("recentRoutines", recentRoutines);

        return "dashboard";
    }
}
