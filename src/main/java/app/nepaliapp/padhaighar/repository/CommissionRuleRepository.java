package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.nepaliapp.padhaighar.model.CommissionRuleModel;
import java.util.Optional;

public interface CommissionRuleRepository extends JpaRepository<CommissionRuleModel, Long> {
    Optional<CommissionRuleModel> findBySubjectId(Long subjectId);
    java.util.List<app.nepaliapp.padhaighar.model.CommissionRuleModel> findBySubjectIdIn(java.util.List<Long> subjectIds);
}