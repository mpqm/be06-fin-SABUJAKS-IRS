package com.service.common.dto.request.resume;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePersonalHistoryReq {
    private String companyName;
    private String deptName;
    private String enteredAt;
    private String quitAt;
    private Boolean empStatus;
    private String position;
    private String job;
    private Integer salary;
    private String work;
}
