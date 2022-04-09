package ro.nicolaemariusghergu.queryitdata.model;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class BaseEntity {

    private Long id;

    private String name;
}
