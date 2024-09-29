package com.sabujaks.irs.domain.data_init.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryRes {
    private String code; // 소분류 코드
    private String description; // 소분류 설명
}