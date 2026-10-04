package edu.cibertec.products.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product {

    @Id
    @Column("idproduct")
    private Long idProduct;

    @Column("name")
    private String name;

    @Column("isbn")
    private String isbn;

    @Column("author")
    private String author;

    @Column("ideditorial")
    private Long idEditorial;

    @Column("idgenre")
    private Long idGenre;

    @Column("publication_year")
    private Integer publicationYear;

    @Column("description")
    private String description;

    @Column("image_data")
    private byte[] image;

    @Column("image_content_type")
    private String imageContentType;

    @Column("purchase_price")
    private BigDecimal purchasePrice;

    @Column("profit_margin")
    private BigDecimal profitMargin;

    @Column("sale_price")
    private BigDecimal salePrice;

    @Column("active")
    private Boolean active;

    @Column("stock")
    private Integer stock;
}
