package ro.nicolaemariusghergu.queryitdata.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Promotion extends BaseEntity {

    private String description;

    private Long expireDate;

    private Integer quantityNeeded;
}
