/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities;

import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.spring.aop.AbstractXRayInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Map;

@Aspect
public class XRayInspector extends AbstractXRayInterceptor {
    @Override
    protected Map<String, Map<String, Object>> generateMetadata(
            ProceedingJoinPoint proceedingJoinPoint,
            Subsegment subsegment) {
        return super.generateMetadata(proceedingJoinPoint, subsegment);
    }

    @Override
    @Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled) && bean(*)")
    public void xrayEnabledClasses() {}
}
