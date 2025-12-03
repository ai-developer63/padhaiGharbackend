package app.nepaliapp.padhaighar.serviceimp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.BannerModel;
import app.nepaliapp.padhaighar.repository.BannerRepository;
import app.nepaliapp.padhaighar.service.BannerService;
import jakarta.transaction.Transactional;

@Service
public class BannerServiceImp implements BannerService {

	  @Autowired
	    BannerRepository bannerRepository;

	    public List<BannerModel> getAllBanners() {
	        return bannerRepository.findAll();
	    }

	    @Transactional
	    public BannerModel saveBanner(BannerModel banner) {
	        return bannerRepository.save(banner);
	    }

	    @Transactional
	    public void deleteBanner(Long id) {
	        bannerRepository.deleteById(id);
	    }

	    @Transactional
	    public BannerModel toggleVisibility(Long id) {
	        BannerModel banner = bannerRepository.findById(id).orElseThrow(() -> new RuntimeException("Banner not found"));
	        banner.setIsVisible(!Boolean.TRUE.equals(banner.getIsVisible()));
	        return bannerRepository.save(banner);
	    }

		@Override
		public Page<BannerModel> getBannersPage(Pageable pageable) {
			 return bannerRepository.findAll(pageable);
		}

		@Override
		public BannerModel getBannerById(Long id) {
			
			    return bannerRepository.findById(id)
			            .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
			}

		

}
