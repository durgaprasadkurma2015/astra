package com.astra.common;

public final class Constants {

    private Constants() {
        // Utility class
    }

    public static final String API_PREFIX = "/api/v1";

    public static final String AUTH_API = API_PREFIX + "/auth";
    public static final String PRODUCTS_API = API_PREFIX + "/products";
    public static final String ORDERS_API = API_PREFIX + "/orders";
    public static final String USERS_API = API_PREFIX + "/users";

    public static final String ADMIN_API = API_PREFIX + "/admin";

    public static final String ADMIN_PRODUCTS_API =
            ADMIN_API + "/products";

    public static final String ADMIN_ORDERS_API =
            ADMIN_API + "/orders";

    public static final String ADMIN_USERS_API =
            ADMIN_API + "/users";

    public static final String ADMIN_SELLERS_API =
            ADMIN_API + "/sellers";

    public static final String BEARER_PREFIX = "Bearer ";

    public static final String AUTHORIZATION_HEADER =
            "Authorization";

    public static final int DEFAULT_PAGE = 0;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;
}
