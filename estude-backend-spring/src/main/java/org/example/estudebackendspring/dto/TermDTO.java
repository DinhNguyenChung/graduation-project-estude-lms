package org.example.estudebackendspring.dto;

import lombok.*;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TermDTO {
    private Long termId;
    private String name;
    private Date beginDate;
    private Date endDate;

}
