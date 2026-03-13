package com.ferrumlog.service.impl;

import com.ferrumlog.domain.entity.Exercise;
import com.ferrumlog.dto.ExerciseDto;
import com.ferrumlog.repository.ExerciseRepository;
import com.ferrumlog.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de ejercicios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional
    public ExerciseDto createExercise(ExerciseDto exerciseDto) {
        log.info("Creando nuevo ejercicio: {}", exerciseDto.name());

        Exercise exercise = Exercise.builder()
                .name(exerciseDto.name())
                .muscleGroup(exerciseDto.muscleGroup())
                .description(exerciseDto.description())
                .build();

        Exercise savedExercise = exerciseRepository.save(exercise);
        log.info("Ejercicio creado: {} (ID: {})", savedExercise.getName(), savedExercise.getId());

        return mapToDto(savedExercise);
    }

    @Override
    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto exerciseDto) {
        log.info("Actualizando ejercicio ID: {}", id);

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + id));

        exercise.setName(exerciseDto.name());
        exercise.setMuscleGroup(exerciseDto.muscleGroup());
        exercise.setDescription(exerciseDto.description());

        Exercise updatedExercise = exerciseRepository.save(exercise);
        log.info("Ejercicio actualizado: {}", updatedExercise.getName());

        return mapToDto(updatedExercise);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseDto getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + id));
        return mapToDto(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDto> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseDto> getExercises(Pageable pageable) {
        return exerciseRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDto> searchExercises(String query) {
        log.info("Buscando ejercicios con query: {}", query);
        return exerciseRepository.findByNameContainingIgnoreCaseOrMuscleGroupContainingIgnoreCase(
                        query, query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        log.info("Eliminando ejercicio ID: {}", id);

        if (!exerciseRepository.existsById(id)) {
            throw new RuntimeException("Ejercicio no encontrado: " + id);
        }

        exerciseRepository.deleteById(id);
        log.info("Ejercicio eliminado ID: {}", id);
    }

    private ExerciseDto mapToDto(Exercise exercise) {
        return new ExerciseDto(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getDescription()
        );
    }
}
