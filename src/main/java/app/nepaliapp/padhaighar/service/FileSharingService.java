package app.nepaliapp.padhaighar.service;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface FileSharingService {

	Resource getBannerImage(String filename) throws IOException;

	Resource getCategoryIcon(String filename) throws IOException;;

}
