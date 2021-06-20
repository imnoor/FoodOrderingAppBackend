package com.upgrad.FoodOrderingApp.service.common;

import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

public class FoodOrderingUtils {

    public static String getBearerAuthToken(String headerParam) {
        if (!headerParam.startsWith(FoodOrderingConstants.PREFIX_BEARER)) {
            throw new UnexpectedException(ATHR_005);
        } else {
            String bearerToken = StringUtils.substringAfter(headerParam, FoodOrderingConstants.PREFIX_BEARER);
            if (bearerToken == null || bearerToken.isEmpty()) {
                throw new UnexpectedException(GEN_001);
            } else {
                return bearerToken;
            }
        }
    }
}
