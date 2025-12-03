package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.nepaliapp.padhaighar.model.CategoryModel;

public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {

}
