package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.nepaliapp.padhaighar.model.WalletTransactionModel;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionModel, Long> {
    List<WalletTransactionModel> findByUserIdOrderByCreatedAtDesc(Long userId);
}