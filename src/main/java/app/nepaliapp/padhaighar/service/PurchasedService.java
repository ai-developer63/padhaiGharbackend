package app.nepaliapp.padhaighar.service;
import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import java.util.List;

public interface PurchasedService {
    PurchasedUserModel savePurchase(PurchasedUserModel purchase);
    List<PurchasedUserModel> getPurchasesByUserId(Long userId);
}

