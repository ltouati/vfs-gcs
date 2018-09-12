package com.celarli.commons.vfs.provider.google;

import java.util.Arrays;
import java.util.Optional;


public enum ClientType {

    APPLICATION(1),
    STORAGE_ACCOUNT(2),
    COMPUTE_ENGINE(3);

    private Integer type;


    ClientType(Integer type) {

        this.type = type;
    }


    public Integer getType() {

        return type;
    }


    public static Optional<ClientType> getByType(Integer type) {

        return Arrays.stream(values()).filter(ct -> ct.type == type).findFirst();
    }
}
