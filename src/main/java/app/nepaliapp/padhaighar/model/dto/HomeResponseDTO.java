package app.nepaliapp.padhaighar.model.dto;
import java.util.List;

import lombok.Data;
@Data
public class HomeResponseDTO {
    private List<BannerDTO> banner;
    private List<CategoryDTO> category;
}
