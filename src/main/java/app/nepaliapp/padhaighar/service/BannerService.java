package app.nepaliapp.padhaighar.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.nepaliapp.padhaighar.model.BannerModel;

public interface BannerService {
	public List<BannerModel> getAllBanners();
	public BannerModel saveBanner(BannerModel banner);
	 public void deleteBanner(Long id);
	 public BannerModel toggleVisibility(Long id);
	 public Page<BannerModel> getBannersPage(Pageable pageable);
	 BannerModel getBannerById(Long id);
}
