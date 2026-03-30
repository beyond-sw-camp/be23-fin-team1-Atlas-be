package com.ozz.atlas.common.id;

import com.github.f4b6a3.ulid.UlidCreator;

public final class PublicIdGenerator {

    private PublicIdGenerator() {
    }

    public static String next() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
