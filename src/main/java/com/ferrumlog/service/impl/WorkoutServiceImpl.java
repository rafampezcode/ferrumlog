package com.ferrumlog.service.impl;

import com.ferrumlog.domain.entity.Exercise;
import com.ferrumlog.domain.entity.User;
import com.ferrumlog.domain.entity.WorkoutLog;
import com.ferrumlog.domain.entity.WorkoutSet;
import com.ferrumlog.dto.WorkoutSetDto;
import com.ferrumlog.repository.ExerciseRepository;
import com.ferrumlog.repository.UserRepository;
import com.ferrumlog.repository.WorkoutLogRepository;
import com.ferrumlog.repository.WorkoutSetRepository;
import com.ferrumlog.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de entrenamientos.
 * Incluye el MÉTODO CRÍTICO para recuperar el último WorkoutSet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * MÉTODO CRÍTICO: Recupera el último WorkoutSet registrado para un ejercicio específico.
     * Implementa el principio de sobrecarga progresiva mostrando al usuario su último registro.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<WorkoutSetDto> getLastWorkoutSet(Long userId, Long exerciseId) {
        log.debug("Buscando último WorkoutSet para usuario {} en ejercicio {}", userId, exerciseId);

        Optional<WorkoutSet> lastSet = workoutSetRepository
                .findFirstByWorkoutLog_UserIdAndExerciseIdOrderByWorkoutLog_WorkoutDateDescIdDesc(
                        userId, exerciseId);

        if (lastSet.isPresent()) {
            WorkoutSet workoutSet = lastSet.get();
            log.info("Último registro encontrado - Ejercicio: {}, Peso: {}kg, Reps: {}, RPE: {}",
                    workoutSet.getExercise().getName(),
                    workoutSet.getWeight(),
                    workoutSet.getReps(),
                    workoutSet.getRpe());

            return Optional.of(mapToDto(workoutSet));
        }

        log.debug("No se encontró registro previo para este ejercicio y usuario");
        return Optional.empty();
    }

    @Override
    @Transactional
    public WorkoutSetDto recordWorkoutSet(WorkoutSetDto workoutSetDto, Long userId) {
        log.info("Registrando nueva serie - Usuario: {}, Ejercicio: {}, Peso: {}kg, Reps: {}",
                userId, workoutSetDto.exerciseId(), workoutSetDto.weight(), workoutSetDto.reps());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Exercise exercise = exerciseRepository.findById(workoutSetDto.exerciseId())
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + workoutSetDto.exerciseId()));

        // Este endpoint guarda una serie aislada en su propia sesion.
        WorkoutLog workoutLog = workoutLogRepository.save(
                WorkoutLog.builder()
                        .user(user)
                        .workoutDate(LocalDateTime.now())
                        .build());

        // Crear WorkoutSet
        WorkoutSet workoutSet = WorkoutSet.builder()
                .workoutLog(workoutLog)
                .exercise(exercise)
                .weight(BigDecimal.valueOf(workoutSetDto.weight()))
                .reps(workoutSetDto.reps())
                .rpe(workoutSetDto.rpe() != null ? BigDecimal.valueOf(workoutSetDto.rpe()) : null)
                .build();

        WorkoutSet savedSet = workoutSetRepository.save(workoutSet);
        log.info("Serie registrada exitosamente (ID: {})", savedSet.getId());

        return mapToDto(savedSet);
    }

    @Override
    @Transactional
    public Long recordWorkoutSession(List<WorkoutSetDto> workoutSets, Long userId) {
        if (workoutSets == null || workoutSets.isEmpty()) {
            throw new IllegalArgumentException("No hay series para guardar en la sesion");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        WorkoutLog workoutLog = workoutLogRepository.save(
                WorkoutLog.builder()
                        .user(user)
                        .workoutDate(LocalDateTime.now())
                        .build());

        for (WorkoutSetDto workoutSetDto : workoutSets) {
            Exercise exercise = exerciseRepository.findById(workoutSetDto.exerciseId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ejercicio no encontrado: " + workoutSetDto.exerciseId()));

            WorkoutSet workoutSet = WorkoutSet.builder()
                    .workoutLog(workoutLog)
                    .exercise(exercise)
                    .weight(BigDecimal.valueOf(workoutSetDto.weight()))
                    .reps(workoutSetDto.reps())
                    .rpe(workoutSetDto.rpe() != null ? BigDecimal.valueOf(workoutSetDto.rpe()) : null)
                    .build();
            workoutSetRepository.save(workoutSet);
        }

        log.info("Sesion registrada - Usuario: {}, WorkoutLog: {}, Series: {}",
                userId, workoutLog.getId(), workoutSets.size());
        return workoutLog.getId();
    }

    private WorkoutSetDto mapToDto(WorkoutSet workoutSet) {
        return new WorkoutSetDto(
                workoutSet.getId(),
                workoutSet.getExercise().getId(),
                workoutSet.getExercise().getName(),
                workoutSet.getWeight().doubleValue(),
                workoutSet.getReps(),
                workoutSet.getRpe() != null ? workoutSet.getRpe().doubleValue() : null
        );
    }
}
