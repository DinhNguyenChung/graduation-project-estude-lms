package org.example.estudebackendspring.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CreateClazzRequest {
    @NotBlank
    private String name;
    private String term;
    @Min(0)
    private Integer classSize;
    @NotNull
    private Long schoolId; // thêm trường này
    private Date beginDate;
    private Date endDate;

}