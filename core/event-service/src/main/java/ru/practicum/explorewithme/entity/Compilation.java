package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "compilations", schema = "public")
public class Compilation {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pinned")
    private boolean pinned;
    @Column(name = "title", length = 50)
    private String title;
    @OneToMany(mappedBy = "compilation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompilationEvent> events = new ArrayList<>();
}
