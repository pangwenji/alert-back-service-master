package com.welab.alertsystem.ulit;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"totalCount", "offset", "limit"})
public class PaginatedJsonResult extends JsonResult{
    Integer totalCount;
    Integer offset;
    Integer limit;

}
