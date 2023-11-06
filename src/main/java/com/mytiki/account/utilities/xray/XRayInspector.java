/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.xray;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.spring.aop.AbstractXRayInterceptor;
import com.amazonaws.xray.spring.aop.XRayInterceptorUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Aspect
public class XRayInspector extends AbstractXRayInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected Object processXRayTrace(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Subsegment subsegment = AWSXRay.beginSubsegment(
                    pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());
            logger.trace("Begin aws xray subsegment");
            Optional.of(subsegment)
                    .ifPresent(s->s.setMetadata(generateMetadata(pjp, subsegment)));
            Object result = XRayInterceptorUtils.conditionalProceed(pjp);
            Optional.ofNullable(result)
                    .ifPresent(r->{
                        Map<String, Object> resultMeta = new HashMap<>();
                        resultMeta.put(result.getClass().getCanonicalName(), result);
                        subsegment.getMetadata().put("Result", resultMeta);
                    });
            return result;
        } catch (Exception e) {
            Objects.requireNonNull(AWSXRay.getCurrentSegment()).addException(e);
            throw e;
        } finally {
            logger.trace("Ending aws xray subsegment");
            AWSXRay.endSubsegment();
        }
    }

    @Override
    protected Map<String, Map<String, Object>> generateMetadata(
            ProceedingJoinPoint proceedingJoinPoint,
            Subsegment subsegment) {

        logger.trace("aws xray tracing method - {}.{}",
                proceedingJoinPoint.getSignature().getDeclaringTypeName(),
                proceedingJoinPoint.getSignature().getName());

        Map<String, Map<String, Object>> metadata = super.generateMetadata(proceedingJoinPoint, subsegment);
        metadata.get("ClassInfo").put("Class", proceedingJoinPoint.getSignature().getDeclaringTypeName());

        Map<String, Object> argumentsInfo = new HashMap<>();

        Arrays.stream(proceedingJoinPoint.getArgs())
                .forEach(arg->argumentsInfo.put(arg.getClass().getSimpleName(), arg));

        metadata.put("Arguments", argumentsInfo);
        metadata.get("ClassInfo").put("Package",
                proceedingJoinPoint.getSignature().getDeclaringType().getPackage().getName());
        metadata.get("ClassInfo").put("Method", proceedingJoinPoint.getSignature().getName());
        return metadata;
    }

    @Override
    @Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled) && bean(*Controller)")
    public void xrayEnabledClasses() {}
}
