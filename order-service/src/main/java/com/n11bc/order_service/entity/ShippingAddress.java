package com.n11bc.order_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    @Column(name = "shipping_source_address_id", nullable = false, length = 80)
    private String sourceAddressId;

    @Column(name = "shipping_title", length = 80)
    private String title;

    @Column(name = "shipping_street", nullable = false, length = 220)
    private String street;

    @Column(name = "shipping_city", nullable = false, length = 80)
    private String city;

    @Column(name = "shipping_country", nullable = false, length = 80)
    private String country;

    @Column(name = "shipping_zip_code", nullable = false, length = 24)
    private String zipCode;
}
