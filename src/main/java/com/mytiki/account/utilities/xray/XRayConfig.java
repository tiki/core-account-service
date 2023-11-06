/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.xray;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import com.amazonaws.xray.strategy.sampling.AllSamplingStrategy;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.invoke.MethodHandles;

@Import(XRayInspector.class)
@Configuration
public class XRayConfig {
//    @Value("${spring.application.name}")
//    private String AWSXRAY_SEGMENT_NAME;

//    private static final String SAMPLING_RULE_JSON = "classpath:xray/sampling-rules.json";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static {
//        URL ruleFile = null;
//        try {
//            ruleFile = ResourceUtils.getURL(SAMPLING_RULE_JSON);
//        } catch (FileNotFoundException e) {
//            logger.error("sampling rule cannot load for aws xray - {}", e.getMessage());
//        }
        //logger.debug("sampling rule load from {} for aws xray", ruleFile.getPath());

        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withDefaultPlugins()
                //.withSamplingStrategy(new CentralizedSamplingStrategy(ruleFile))
                .withSamplingStrategy(new AllSamplingStrategy());
                //.withSegmentListener(new SLF4JSegmentListener());

        AWSXRay.setGlobalRecorder(builder.build());
        logger.debug("aws xray recorder was setted globally.");
    }

    @Bean
    public Filter TracingFilter() {
        logger.debug("The segment name for aws xray tracking has been set to {}.", /*AWSXRAY_SEGMENT_NAME*/ "CoreAccountService");
        return new AWSXRayServletFilter(SegmentNamingStrategy.dynamic(/*AWSXRAY_SEGMENT_NAME*/ "CoreAccountService"));
    }
}
