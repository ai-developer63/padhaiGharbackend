package app.nepaliapp.padhaighar.serviceimp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.repository.CategoryRepository;
import app.nepaliapp.padhaighar.service.CategoryService;

@Service
public class CategoryServiceImp implements CategoryService {

	  @Autowired
	    private CategoryRepository repo;

	    @Override
	    public CategoryModel saveCategory(CategoryModel category) {
	        if (category.getIsEnable() == null)
	            category.setIsEnable(true);

	        return repo.save(category);
	    }

	    @Override
	    public List<CategoryModel> getAll() {
	        return repo.findAll();
	    }

	    @Override
	    public CategoryModel getById(Long id) {
	        return repo.findById(id).orElse(null);
	    }

	    @Override
	    public void deleteCategory(Long id) {
	        repo.deleteById(id);
	    }

	    @Override
	    public CategoryModel toggleEnable(Long id) {
	        CategoryModel c = getById(id);
	        if (c == null) return null;
	        c.setIsEnable(!c.getIsEnable());
	        return repo.save(c);
	    }

}
