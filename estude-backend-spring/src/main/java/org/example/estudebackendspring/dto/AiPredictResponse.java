package org.example.estudebackendspring.dto;

import java.util.Map;

/**
 * Mô tả cấu trúc response từ AI service
 */
public class AiPredictResponse {
    public String student_id;
    public String du_doan_hoc_luc;
    public String thuc_te_xep_loai;
    public String goi_y_hanh_dong;
    public String[] goi_y_chi_tiet;
    
    // Sử dụng Map thay vì Object để dễ truy cập các thuộc tính
    public Map<String, Object> thong_ke;
    public Map<String, Object> phan_tich_chi_tiet;
}
