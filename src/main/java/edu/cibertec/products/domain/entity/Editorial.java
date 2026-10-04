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
@Table("editorial")
public class Editorial {

    @Id
    @Column("ideditorial")
    private Long idEditorial;

    @Column("name")
    private String name;
}
