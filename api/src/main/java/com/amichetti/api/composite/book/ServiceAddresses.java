package com.amichetti.api.composite.book;

import lombok.*;

public class ServiceAddresses{

    private final String cmp;
    private final String pro;
    private final String rev;
    private final String rec;

    public ServiceAddresses() {
        cmp = null;
        pro = null;
        rev = null;
        rec = null;
    }

    public ServiceAddresses(
            String compositeAddress,
            String bookAddress,
            String reviewAddress,
            String recommendationAddress) {

        this.cmp = compositeAddress;
        this.pro = bookAddress;
        this.rev = reviewAddress;
        this.rec = recommendationAddress;
    }
}
