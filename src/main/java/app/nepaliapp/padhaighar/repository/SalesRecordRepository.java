package app.nepaliapp.padhaighar.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.nepaliapp.padhaighar.enums.SaleType;
import app.nepaliapp.padhaighar.model.SalesRecordModel;

public interface SalesRecordRepository extends JpaRepository<SalesRecordModel, Long> {

    // 1. Pagination Queries (For UI Tables)
    Page<SalesRecordModel> findBySellerId(Long sellerId, Pageable pageable);
    Page<SalesRecordModel> findByPurchasedSubjectIdIn(List<Long> subjectIds, Pageable pageable);

    // 2. High-Performance Math Queries (For Affiliate Dashboard)
    @Query("SELECT SUM(s.sellerCut) FROM SalesRecordModel s WHERE s.sellerId = :sellerId AND s.isReversed = false")
    Double sumSellerCutBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerIdAndIsReversedFalse(Long sellerId);

    // 3. High-Performance Math Queries (For Teacher Dashboard)
    @Query("SELECT SUM(s.teacherCut) FROM SalesRecordModel s WHERE s.purchasedSubjectId IN :subjectIds AND s.isReversed = false")
    Double sumTeacherCutBySubjectIds(@Param("subjectIds") List<Long> subjectIds);

    long countByPurchasedSubjectIdInAndIsReversedFalse(List<Long> subjectIds);
    
 // NEW: Search across Transaction ID, Buyer Name, or Subject Name
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SalesRecordModel s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<SalesRecordModel> searchAdminSales(@org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
    
    //Sales analysis
    
 // 1. The Main Filter Query (Handles Keyword + Date Ranges)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SalesRecordModel s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.sellerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR s.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR s.createdAt <= :endDate)")
    org.springframework.data.domain.Page<SalesRecordModel> searchAndFilterSales(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate, 
            org.springframework.data.domain.Pageable pageable);

    // 2. Sum Total Sales Volume
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.totalAmountPaid), 0) FROM SalesRecordModel s WHERE s.isReversed = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.sellerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR s.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR s.createdAt <= :endDate)")
    Double sumFilteredTotalVolume(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    // 3. Sum App Net Earnings
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.appCut), 0) FROM SalesRecordModel s WHERE s.isReversed = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.sellerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR s.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR s.createdAt <= :endDate)")
    Double sumFilteredAppEarnings(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    // 4. Count Active Sales
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM SalesRecordModel s WHERE s.isReversed = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.sellerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR s.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR s.createdAt <= :endDate)")
    Long countFilteredActiveSales(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
    
 // Find all pending credit sales for the Admin Inbox
    org.springframework.data.domain.Page<SalesRecordModel> findByIsCreditPendingTrue(org.springframework.data.domain.Pageable pageable);

    // Find expired credit sales for the Auto-Reversal Robot
    java.util.List<SalesRecordModel> findByIsCreditPendingTrueAndCreatedAtBefore(java.time.LocalDateTime cutoffDate);
    
 // Counts how many credit sales are waiting for Admin approval
    long countByIsCreditPendingTrue();
    
 // PHASE 1: TEACHER LEDGER
    @Query("SELECT COALESCE(SUM(s.teacherCut), 0.0) FROM SalesRecordModel s WHERE s.isReversed = false AND s.isTeacherPaid = false AND s.isCreditPending = false")
    Double sumUnpaidTeacherCuts();

    public interface TeacherPayoutProjection {
        String getTeacherName();
        Double getTotalOwed();
        Long getUnpaidSalesCount();
    }

    @Query("SELECT s.teacherName AS teacherName, SUM(s.teacherCut) AS totalOwed, COUNT(s.id) AS unpaidSalesCount " +
           "FROM SalesRecordModel s WHERE s.isReversed = false AND s.isTeacherPaid = false AND s.isCreditPending = false AND s.teacherName IS NOT NULL " +
           "GROUP BY s.teacherName")
    List<TeacherPayoutProjection> getUnpaidTeacherPayouts();

    List<SalesRecordModel> findByTeacherNameAndIsTeacherPaidFalseAndIsReversedFalse(String teacherName);

    // PHASE 2: AFFILIATE RECEIVABLE
    // Simplified query to prevent "UnknownPathException" during boot
    public interface AffiliateReceivableProjection {
        Long getSellerId();
        String getSellerName();
        Double getTotalOwedToAdmin();
        Long getUnsettledSalesCount();
    }
    @Query("SELECT COALESCE(SUM(s.totalAmountPaid - s.sellerCut), 0.0) FROM SalesRecordModel s " +
           "WHERE s.saleType = app.nepaliapp.padhaighar.enums.SaleType.AFFILIATOR " +
           "AND s.isCreditPending = false AND s.isAffiliateSettled = false AND s.isReversed = false")
    Double sumTotalAffiliateReceivables();
    
    @Query("SELECT s.sellerId AS sellerId, s.sellerName AS sellerName, " +
            "SUM(s.totalAmountPaid - s.sellerCut) AS totalOwedToAdmin, COUNT(s.id) AS unsettledSalesCount " +
            "FROM SalesRecordModel s WHERE s.saleType = 'AFFILIATOR' AND s.isCreditPending = false " +
            "AND s.isAffiliateSettled = false AND s.isReversed = false GROUP BY s.sellerId, s.sellerName")
     List<AffiliateReceivableProjection> getUnsettledAffiliateReceivables();
	List<SalesRecordModel> findBySellerIdAndSaleTypeAndIsCreditPendingFalseAndIsAffiliateSettledFalseAndIsReversedFalse(
			Long sellerId, SaleType affiliator);
}