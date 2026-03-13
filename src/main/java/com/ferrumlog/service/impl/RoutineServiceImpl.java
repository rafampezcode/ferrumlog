package com.ferrumlog.service.impl;

import com.ferrumlog.domain.entity.Routine;
import com.ferrumlog.domain.entity.RoutineExercise;
import com.ferrumlog.domain.entity.User;
import com.ferrumlog.domain.entity.Exercise;
import com.ferrumlog.dto.RoutineDto;
import com.ferrumlog.repository.ExerciseRepository;
import com.ferrumlog.repository.RoutineRepository;
import com.ferrumlog.repository.UserRepository;
import com.ferrumlog.service.RoutineService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de rutinas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineServiceImpl implements RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional
    public RoutineDto createRoutine(RoutineDto routineDto, Long userId) {
        log.info("Creando rutina '{}' para usuario ID: {}", routineDto.name(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Routine routine = Routine.builder()
                .name(routineDto.name())
                .description(routineDto.description())
                .user(user)
                .build();

        applyRoutineExercises(routine, routineDto.exerciseIds(), routineDto.exerciseSets());

        Routine savedRoutine = routineRepository.save(routine);
        log.info("Rutina creada: {} (ID: {})", savedRoutine.getName(), savedRoutine.getId());

        return mapToDto(savedRoutine);
    }

    @Override
    @Transactional
    public RoutineDto updateRoutine(Long id, RoutineDto routineDto, Long userId) {
        log.info("Actualizando rutina ID: {} por usuario ID: {}", id, userId);

        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + id));

        // Verificar que el usuario es el propietario
        if (!routine.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permisos para actualizar esta rutina");
        }

        routine.setName(routineDto.name());
        routine.setDescription(routineDto.description());
        applyRoutineExercises(routine, routineDto.exerciseIds(), routineDto.exerciseSets());

        Routine updatedRoutine = routineRepository.save(routine);
        log.info("Rutina actualizada: {}", updatedRoutine.getName());

        return mapToDto(updatedRoutine);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineDto getRoutineById(Long id, Long userId) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + id));

        // Verificar que el usuario es el propietario
        if (!routine.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permisos para ver esta rutina");
        }

        return mapToDto(routine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineDto> getUserRoutines(Long userId) {
        log.info("Obteniendo rutinas del usuario ID: {}", userId);
        return routineRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRoutine(Long id, Long userId) {
        log.info("Eliminando rutina ID: {} por usuario ID: {}", id, userId);

        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + id));

        // Verificar que el usuario es el propietario
        if (!routine.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permisos para eliminar esta rutina");
        }

        routineRepository.delete(routine);
        log.info("Rutina eliminada ID: {}", id);
    }

    private RoutineDto mapToDto(Routine routine) {
        return new RoutineDto(
                routine.getId(),
                routine.getName(),
                routine.getDescription(),
                routine.getRoutineExercises().stream()
                        .sorted(java.util.Comparator.comparingInt(RoutineExercise::getOrderIndex))
                        .map(re -> re.getExercise().getId())
                        .toList(),
                routine.getRoutineExercises().stream()
                        .sorted(java.util.Comparator.comparingInt(RoutineExercise::getOrderIndex))
                        .map(RoutineExercise::getSets)
                        .toList()
        );
    }

    private void applyRoutineExercises(Routine routine,
                                       java.util.List<Long> exerciseIds,
                                       java.util.List<Integer> exerciseSets) {
        routine.getRoutineExercises().clear();

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return;
        }

        java.util.List<RoutineExercise> items = new ArrayList<>();
        int order = 1;
        for (int i = 0; i < exerciseIds.size(); i++) {
            Long exerciseId = exerciseIds.get(i);
            Exercise exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + exerciseId));

            int sets = 3;
            if (exerciseSets != null && i < exerciseSets.size() && exerciseSets.get(i) != null
                && exerciseSets.get(i) > 0) {
            sets = exerciseSets.get(i);
            }

            items.add(RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(order++)
                .sets(sets)
                    .reps(10)
                    .build());
        }

        routine.getRoutineExercises().addAll(items);
    }
}
