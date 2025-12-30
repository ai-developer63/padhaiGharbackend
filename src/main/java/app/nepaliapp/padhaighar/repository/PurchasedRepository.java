package app.nepaliapp.padhaighar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.nepaliapp.padhaighar.model.PurchasedUserModel;

public interface PurchasedRepository extends JpaRepository<PurchasedUserModel, Long> {
	List<PurchasedUserModel> findByUserId(Long userId);

}
