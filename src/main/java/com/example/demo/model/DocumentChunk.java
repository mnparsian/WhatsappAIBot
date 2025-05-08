package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Document document;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Lob // Utilizzato per salvare stringhe molto lunghe nel database (es. array di embedding)
    private String embeddingJson; //L'elenco di embedding è memorizzato in formato JSON.
}
