package edu.cibertec.products.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class StockDeductionRepository {
    private final JdbcClient jdbc;

    public StockDeductionRepository(DataSource dataSource) {
        jdbc = JdbcClient.create(dataSource);
    }

    public void lock(UUID id) {
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtextextended(CAST(:id AS text), 0))")
                .param("id", id).query((rs, rowNum) -> true).single();
    }

    public Optional<Deduction> find(UUID id) {
        return jdbc.sql("""
                SELECT deduction_id, id_products, quantity, status
                FROM products.purchase_stock_deduction WHERE deduction_id = :id FOR UPDATE
                """).param("id", id)
                .query((rs, rowNum) -> new Deduction(rs.getObject("deduction_id", UUID.class),
                        rs.getLong("id_products"), rs.getInt("quantity"), rs.getString("status")))
                .optional();
    }

    public void insert(UUID id, Long product, Integer quantity, String status) {
        jdbc.sql("""
                INSERT INTO products.purchase_stock_deduction (deduction_id, id_products, quantity, status)
                VALUES (:id, :product, :quantity, :status)
                """).param("id", id).param("product", product).param("quantity", quantity).param("status", status).update();
    }

    public void markReturned(UUID id) {
        jdbc.sql("""
                UPDATE products.purchase_stock_deduction
                SET status = 'RETURNED', returned_at = CURRENT_TIMESTAMP WHERE deduction_id = :id
                """).param("id", id).update();
    }

    public record Deduction(UUID id, Long product, Integer quantity, String status) {}
}
