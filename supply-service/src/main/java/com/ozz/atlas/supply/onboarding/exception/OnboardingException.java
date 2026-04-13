package com.ozz.atlas.supply.onboarding.exception;

import com.ozz.atlas.common.exception.BaseException;

public class OnboardingException extends BaseException {

    public OnboardingException(OnboardingErrorCode errorCode) {
        super(errorCode);
    }
}
