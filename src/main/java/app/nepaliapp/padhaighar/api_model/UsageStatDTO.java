package app.nepaliapp.padhaighar.api_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageStatDTO {
    private Long userId;
    private Long totalBytes;
    private String userName; // We will fill this later
    
    // Constructor for JPQL Query (without name)
    public UsageStatDTO(Long userId, Long totalBytes) {
        this.userId = userId;
        this.totalBytes = totalBytes;
    }
    
    // Helper to display readable data size
    public String getFormattedSize() {
        if (totalBytes == null) return "0 MB";
        double mb = totalBytes / (1024.0 * 1024.0);
        if (mb > 1024) {
            return String.format("%.2f GB", mb / 1024.0);
        }
        return String.format("%.2f MB", mb);
    }
}