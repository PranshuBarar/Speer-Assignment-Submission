package com.example.speer.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomQuery {
    private Float timeTook;
    private Integer numberOfResults;
    private String elements;
}
