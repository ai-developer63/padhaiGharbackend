package app.nepaliapp.padhaighar.serviceimp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.nepaliapp.padhaighar.enums.SaleType;
import app.nepaliapp.padhaighar.enums.SellerType;
import app.nepaliapp.padhaighar.enums.TransactionType;
import app.nepaliapp.padhaighar.model.CommissionRuleModel;
import app.nepaliapp.padhaighar.model.CouponModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import app.nepaliapp.padhaighar.model.SalesRecordModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.WalletTransactionModel;
import app.nepaliapp.padhaighar.repository.CommissionRuleRepository;
import app.nepaliapp.padhaighar.repository.CouponRepository;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.repository.WalletTransactionRepository;
import app.nepaliapp.padhaighar.service.CourseService;
import app.nepaliapp.padhaighar.service.PurchasedService;
import app.nepaliapp.padhaighar.service.SalesService;
import app.nepaliapp.padhaighar.service.UserService;

@Service
public class SalesServiceImp implements SalesService {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private PurchasedService purchasedService;

    @Autowired
    private CommissionRuleRepository commissionRuleRepo;

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private WalletTransactionRepository walletTxRepo;

    @Autowired
    private CouponRepository couponRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reverseSale(Long salesRecordId) throws Exception {
        SalesRecordModel sale = salesRecordRepo.findById(salesRecordId)
                .orElseThrow(() -> new Exception("Sales record not found."));

        if (sale.getIsReversed()) throw new Exception("This sale is already reversed.");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        if (sale.getCreatedAt().isBefore(cutoffDate)) {
            throw new Exception("Sales cannot be reversed after 7 days.");
        }

        if (sale.getSaleType() == SaleType.PREPAID_SELLER && sale.getSellerId() != null && sale.getSellerId() > 0) {
            UserModel seller = userService.getUserById(sale.getSellerId());
            if (seller != null) {
                double amountToRefund = sale.getTotalAmountPaid() - sale.getSellerCut();
                seller.setWalletBalance(seller.getWalletBalance() + amountToRefund);
                userService.updateUser(seller);

                WalletTransactionModel refundTx = new WalletTransactionModel();
                refundTx.setUserId(seller.getId());
                refundTx.setAmount(amountToRefund);
                refundTx.setTransactionType(TransactionType.CREDIT);
                refundTx.setDescription("Refund for reversed sale: " + sale.getTransactionId());
                walletTxRepo.save(refundTx);
            }
        }

        purchasedService.revokeAccessBySalesId(sale.getId());
        sale.setIsReversed(true);
        salesRecordRepo.save(sale);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesRecordModel processSale(Long buyerId, Long courseId, Long sellerId, SaleType saleType, int days, String whoActivated) throws Exception {
        return processSale(buyerId, courseId, sellerId, saleType, days, whoActivated, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesRecordModel processSale(Long buyerId, Long courseId, Long sellerId, SaleType saleType, int days, String whoActivated, String couponCode) throws Exception {
        
        UserModel buyer = userService.getUserById(buyerId);
        if (buyer == null) throw new Exception("Buyer not found.");

        CourseModel course = courseService.getById(courseId);
        if (course == null) throw new Exception("Subject not found.");

        double coursePrice = 0.0;
        try {
            if (course.getPrice() != null && !course.getPrice().trim().isEmpty() && !course.getPrice().equalsIgnoreCase("Free")) {
                String cleanPrice = course.getPrice().replaceAll("[^\\d.]", "");
                if (!cleanPrice.isEmpty()) coursePrice = Double.parseDouble(cleanPrice);
            }
        } catch (Exception e) { coursePrice = 0.0; }

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            CouponModel coupon = couponRepo.findByCode(couponCode.toUpperCase())
                    .orElseThrow(() -> new Exception("Invalid coupon code."));
            
            if (!coupon.getIsActive()) throw new Exception("This coupon is inactive.");
            if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) throw new Exception("This coupon has expired.");
            if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) throw new Exception("Coupon usage limit reached.");
            if (coupon.getSpecificCourseId() != null && !coupon.getSpecificCourseId().equals(courseId)) throw new Exception("This coupon is not valid for this specific subject.");

            double discountAmount = (coursePrice * coupon.getDiscountPercentage()) / 100.0;
            coursePrice = coursePrice - discountAmount;

            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepo.save(coupon);

            if (saleType == SaleType.ORGANIC) {
                saleType = SaleType.COUPON_CODE; 
            }
        }

        CommissionRuleModel rule = commissionRuleRepo.findBySubjectId(courseId).orElseGet(() -> {
            CommissionRuleModel defaultRule = new CommissionRuleModel();
            defaultRule.setAppPercentage(100.0);
            defaultRule.setTeacherPercentage(0.0);
            defaultRule.setAffiliatePercentage(0.0);
            return defaultRule;
        });

        double appCut = (coursePrice * rule.getAppPercentage()) / 100.0;
        double teacherCut = (coursePrice * rule.getTeacherPercentage()) / 100.0;
        double sellerCut = (saleType == SaleType.PREPAID_SELLER || saleType == SaleType.AFFILIATOR) ? (coursePrice * rule.getAffiliatePercentage()) / 100.0 : 0.0;

        UserModel seller = (sellerId != null && sellerId > 0) ? userService.getUserById(sellerId) : null;

        // 1. PREPAID SELLER LOGIC (Wallet Deduction)
        if (saleType == SaleType.PREPAID_SELLER) {
            if (seller == null || seller.getSellerType() != SellerType.PREPAID) throw new Exception("Invalid seller or unauthorized.");
            
            double netDeduction = coursePrice - sellerCut;
            if (seller.getWalletBalance() < netDeduction) throw new Exception("Insufficient wallet balance. You need Rs. " + netDeduction);

            seller.setWalletBalance(seller.getWalletBalance() - netDeduction);
            userService.updateUser(seller);

            WalletTransactionModel walletTx = new WalletTransactionModel();
            walletTx.setUserId(seller.getId());
            walletTx.setAmount(netDeduction);
            walletTx.setTransactionType(TransactionType.DEBIT);
            walletTx.setDescription("Sold subject: " + course.getName() + " to " + buyer.getEmailId());
            walletTxRepo.save(walletTx);
        }

        // 2. POSTPAID / AFFILIATOR LOGIC (Credit & Pending Status)
        boolean isPending = false;
        if (saleType == SaleType.AFFILIATOR) {
            if (seller == null || seller.getSellerType() != SellerType.POSTPAID) throw new Exception("Invalid Postpaid agent.");
            isPending = true; // Flag for Admin Approval
        }

        SalesRecordModel salesRecord = new SalesRecordModel();
        salesRecord.setTransactionId("TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        salesRecord.setPurchasedSubjectId(courseId);
        salesRecord.setSubjectName(course.getName());
        salesRecord.setBuyerId(buyerId);
        salesRecord.setBuyerName(buyer.getName());
        salesRecord.setSellerId(seller != null ? seller.getId() : 0L);
        salesRecord.setSellerName(seller != null ? seller.getName() : "System / Organic");
        salesRecord.setSaleType(saleType);
        salesRecord.setTotalAmountPaid(coursePrice);
        salesRecord.setAppCut(appCut);
        salesRecord.setTeacherCut(teacherCut);
        salesRecord.setSellerCut(sellerCut);
        
        // --- NEW: FINANCIAL SETTLEMENT FIELDS ---
        salesRecord.setIsCreditPending(isPending); 
        salesRecord.setTeacherName(course.getTeacherName());
        salesRecord.setIsTeacherPaid(false);
        salesRecord.setIsAffiliateSettled(false);
        // ----------------------------------------

        salesRecord = salesRecordRepo.save(salesRecord);

        // 3. TRIGGER PUSH NOTIFICATION
        if (isPending) {
            sendAffiliateSaleNotification(salesRecord);
        }

        PurchasedUserModel purchase = new PurchasedUserModel();
        purchase.setUserId(buyerId);
        purchase.setUserName(buyer.getName());
        purchase.setPurchasedSubjectId(courseId);
        purchase.setPurchasedSubjectName(course.getName());
        purchase.setWhoActivated(whoActivated);
        purchase.setSalesId(salesRecord.getId()); 
        purchase.setSalesAccountId(seller != null ? seller.getId() : 0L);
        purchase.setIsLive(false);
        purchase.setPurchaseDate(LocalDate.now().toString());
        purchase.setPurchaseUpto(LocalDate.now().plusDays(days).toString());
        purchasedService.savePurchase(purchase);

        return salesRecord;
    }
    
    // --- NEW: TEACHER & AFFILIATE SETTLEMENT METHODS ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean settleTeacherDues(String teacherName) throws Exception {
        List<SalesRecordModel> unpaid = salesRecordRepo.findByTeacherNameAndIsTeacherPaidFalseAndIsReversedFalse(teacherName);
        if (unpaid.isEmpty()) throw new Exception("No pending dues for " + teacherName);
        
        LocalDateTime now = LocalDateTime.now();
        unpaid.forEach(s -> {
            s.setIsTeacherPaid(true);
            s.setTeacherPaidDate(now);
        });
        salesRecordRepo.saveAll(unpaid);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean settleAffiliateDues(Long sellerId) throws Exception {
        // Collects cash from Postpaid Affiliates for all approved sales
        List<SalesRecordModel> unpaid = salesRecordRepo.findBySellerIdAndSaleTypeAndIsCreditPendingFalseAndIsAffiliateSettledFalseAndIsReversedFalse(sellerId, SaleType.AFFILIATOR);
        if (unpaid.isEmpty()) throw new Exception("No pending receivables for this agent.");
        
        unpaid.forEach(s -> s.setIsAffiliateSettled(true));
        salesRecordRepo.saveAll(unpaid);
        return true;
    }

    // ========================================================================
    // PUSH NOTIFICATION HOOK
    // ========================================================================
    public void sendAffiliateSaleNotification(SalesRecordModel sale) {
        // TODO: Add your Firebase/FCM or WebSocket Push Notification logic here!
        System.out.println("ALERT: New Credit Sale Pending! Subject: " + sale.getSubjectName() + " by Agent: " + sale.getSellerName());
    }

    // ========================================================================
    // ADMIN ACCEPTS THE CREDIT SALE
    // ========================================================================
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptCreditSale(Long salesRecordId) throws Exception {
        SalesRecordModel sale = salesRecordRepo.findById(salesRecordId)
                .orElseThrow(() -> new Exception("Sales record not found."));

        if (!sale.getIsCreditPending()) throw new Exception("This sale is not pending.");
        
        sale.setIsCreditPending(false); // Lock it in as a permanent sale!
        salesRecordRepo.save(sale);
        return true;
    }
}