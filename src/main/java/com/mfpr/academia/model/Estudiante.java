package com.mfpr.academia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 25, nullable = false)
    private String nombres;

    @Column(length = 25, nullable = false)
    private String apellidos;

    @Column(length = 8, unique = true, nullable = false)
    private String dni;

    @Column(length = 2, nullable = false)
    private int edad;
}
