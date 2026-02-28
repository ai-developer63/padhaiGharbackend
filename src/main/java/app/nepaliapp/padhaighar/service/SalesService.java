package app.nepaliapp.padhaighar.service;

import app.nepaliapp.padhaighar.enums.SaleType;
import app.nepaliapp.padhaighar.model.SalesRecordModel;

public interface SalesService {
    
	// Original method (No coupon)
    SalesRecordModel processSale(Long buyerId, Long courseId, Long sellerId, SaleType saleType, int days, String whoActivated) throws Exception;

    // New Master method (With coupon)
    SalesRecordModel processSale(Long buyerId, Long courseId, Long sellerId, SaleType saleType, int days, String whoActivated, String couponCode) throws Exception;
    
    boolean reverseSale(Long salesRecordId) throws Exception;

    public boolean settleTeacherDues(String teacherName) throws Exception;
}