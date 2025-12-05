package app.nepaliapp.padhaighar.service;

import java.util.List;

import app.nepaliapp.padhaighar.model.CategoryModel;

public interface CategoryService {

    CategoryModel saveCategory(CategoryModel category);
    List<CategoryModel> getAll();
    CategoryModel getById(Long id);
    void deleteCategory(Long id);
    CategoryModel toggleEnable(Long id);
	
}
