package app.nepaliapp.padhaighar.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.nepaliapp.padhaighar.model.UserModel;
import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
	UserModel findByEmailId(String emailId);
	UserModel findByPhoneNumber(String phoneNumber);
	Boolean existsByEmailId(String email);
	Boolean existsByPhoneNumber(String phoneNumber);
	@Transactional
	@Modifying
	@Query("UPDATE UserModel u SET u.deviceID = :deviceID WHERE u.id = :userId")
	int updateDeviceIdByUserId(@Param("userId") Long userId, @Param("deviceID") String deviceID);
	long countBycountry(String string);
	
	List<UserModel> findByRole(String role);
	
	@Query("""
			SELECT u FROM UserModel u
			WHERE (:lastActive IS NULL OR u.lastActive LIKE %:lastActive%)
			AND (:country IS NULL OR u.country = :country)
			AND (:refer IS NULL OR u.refer = :refer)
			""")
			Page<UserModel> getFilteredUsers(
			        @Param("lastActive") String lastActive,
			        @Param("country") String country,
			        @Param("refer") String refer,
			        Pageable pageable
			);
}
