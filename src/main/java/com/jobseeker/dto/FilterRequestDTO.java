package com.jobseeker.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequestDTO {

    private String keywords;
    private String location;
    private String experienceLevel;
    private List<String> companies;
    private Double minMatchScore;
}
