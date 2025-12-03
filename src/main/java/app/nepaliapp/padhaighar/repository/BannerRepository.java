package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.nepaliapp.padhaighar.model.BannerModel;

public interface BannerRepository extends JpaRepository<BannerModel, Long> {
}
