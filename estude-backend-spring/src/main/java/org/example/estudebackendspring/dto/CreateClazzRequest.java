package org.example.estudebackendspring.dto;


import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.GradeLevel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClazzRequest {
    @NotBlank(message = "Tên lớp không được để trống")
    private String name;

    @NotNull(message = "Khối lớp không được để trống")
    private GradeLevel gradeLevel; // Thêm trường gradeLevel

    @Min(value = 0, message = "Sĩ số lớp phải lớn hơn hoặc bằng 0")
    private Integer classSize;

    @NotNull(message = "ID trường không được để trống")
    private Long schoolId;

    @NotNull(message = "Danh sách kỳ học không được để trống")
    @Size(min = 1, max = 2, message = "Danh sách kỳ học phải có từ 1 đến 2 kỳ")
    private List<TermInfo> terms;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TermInfo {
        @NotBlank(message = "Tên kỳ không được để trống")

        private String name;

        @NotNull(message = "Ngày bắt đầu không được để trống")
        private Date beginDate;

        @NotNull(message = "Ngày kết thúc không được để trống")
        private Date endDate;

        @AssertTrue(message = "Ngày bắt đầu phải trước ngày kết thúc")
        private boolean isValidDateRange() {
            return !beginDate.after(endDate);
        }
    }
}