package app.nepaliapp.padhaighar.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import app.nepaliapp.padhaighar.model.SalesRecordModel;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.SalesService;

@Configuration
@EnableScheduling
public class CreditSaleScheduler {

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private SalesService salesService;

    // Runs every hour to check for expired credit sales
    @Scheduled(cron = "0 0 * * * *") 
    public void autoReverseExpiredCreditSales() {
        
        // Find the exact cutoff time (7 days ago)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        
        // Fetch all sales that are STILL pending and older than 7 days
        List<SalesRecordModel> expiredSales = salesRecordRepo.findByIsCreditPendingTrueAndCreatedAtBefore(cutoffDate);

        for (SalesRecordModel sale : expiredSales) {
            try {
                // We reuse your bulletproof reverse logic! (It revokes access and marks it reversed)
                salesService.reverseSale(sale.getId());
                System.out.println("AUTO-REVERSED Expired Credit Sale: " + sale.getTransactionId());
            } catch (Exception e) {
                System.out.println("Failed to auto-reverse sale " + sale.getId() + ": " + e.getMessage());
            }
        }
    }
}