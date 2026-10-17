package edu.cibertec.products.domain.entity;

import edu.cibertec.products.domain.entity.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("stock_movement")
public class StockMovement {

    @Id
    @Column("idmovement")
    private Long idMovement;

    @Column("idproduct")
    private Long idProduct;

    @Column("type")
    private StockMovementType type;

    @Column("quantity")
    private Integer quantity;

    @Column("movement_date")
    private LocalDateTime movementDate;

    @Column("resulting_stock")
    private Integer resultingStock;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private LocalDateTime createdAt;
}
