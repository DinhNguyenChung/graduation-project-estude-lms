package org.example.estudebackendspring.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.estudebackendspring.enums.GradeLevel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClazzRequest {
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
    @UniqueTermNames
    private List<TermInfo> terms;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TermInfo {
        private Long termId; // ID kỳ học, dùng khi cập nhật kỳ hiện có

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

    // Custom validator để kiểm tra trùng lặp tên kỳ
    @Constraint(validatedBy = UniqueTermNamesValidator.class)
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UniqueTermNames {
        String message() default "Danh sách kỳ học không được chứa tên kỳ trùng lặp";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class UniqueTermNamesValidator implements ConstraintValidator<UniqueTermNames, List<TermInfo>> {
        @Override
        public boolean isValid(List<TermInfo> terms, ConstraintValidatorContext context) {
            if (terms == null) return true;
            Set<String> termNames = terms.stream().map(TermInfo::getName).collect(Collectors.toSet());
            return termNames.size() == terms.size();
        }
    }
}