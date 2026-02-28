package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.nepaliapp.padhaighar.model.CouponModel;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponModel, Long> {
    Optional<CouponModel> findByCode(String code);
}