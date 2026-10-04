package edu.cibertec.products.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("genre")
public class Genre {

    @Id
    @Column("idgenre")
    private Long idGenre;

    @Column("name")
    private String name;

    @Column("active")
    private boolean active;
}
