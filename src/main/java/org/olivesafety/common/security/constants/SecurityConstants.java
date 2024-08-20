package org.olivesafety.common.security.constants;

/**
 * HTTP
 * headers : {
 * Authorization : Bearer ${jwt}
 * }
 */
public class SecurityConstants {

    //JWT 토큰을 담을 HTTP 요청 헤더 이름
    public static final String TOKEN_HEADER = "Authorization";
    //헤더의 접두사
    public static final String TOKEN_PREFIX = "Bearer";
    // 토큰 타입
    public static final String TOKEN_TYPE = "JWT";
}
