package com.celarli.commons.vfs.provider.google;

public enum ClientType {

    APPLICATION(1),
    STORAGE_ACCOUNT(2),
    COMPUTE_ENGINE(3);

    private Integer type;


    ClientType(Integer type) {

        this.type = type;
    }


    public static ClientType getByType(Integer type) {

        if (type != null) {
            for (ClientType clientType : values()) {
                if (clientType.type == type) {
                    return clientType;
                }
            }
        }

        return null;
    }
}
