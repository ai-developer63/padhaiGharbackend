package app.nepaliapp.padhaighar.serviceimp;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import app.nepaliapp.padhaighar.repository.PurchasedRepository;
import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import app.nepaliapp.padhaighar.service.PurchasedService;

import java.util.List;

@Service
public class PurchasedServiceImp implements PurchasedService {

    @Autowired
    private PurchasedRepository purchasedRepository;

    @Override
    public PurchasedUserModel savePurchase(PurchasedUserModel purchase) {
        return purchasedRepository.save(purchase);
    }

    @Override
    public List<PurchasedUserModel> getPurchasesByUserId(Long userId) {
        return purchasedRepository.findByUserId(userId);
    }
}