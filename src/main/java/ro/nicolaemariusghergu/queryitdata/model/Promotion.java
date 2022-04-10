package ro.nicolaemariusghergu.queryitdata.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Promotion extends BaseEntity {

    private String description;

    private Long expireDate;

    private Integer quantityNeeded;

    private Product productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Promotion promotion = (Promotion) o;

        return productId.equals(promotion.productId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + productId.hashCode();
        return result;
    }
}
