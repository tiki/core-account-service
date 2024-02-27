/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.cleanroom;

import com.mytiki.account.features.latest.event.EventService;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.utilities.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(CleanroomConfig.PACKAGE_PATH)
@EntityScan(CleanroomConfig.PACKAGE_PATH)
public class CleanroomConfig {
    public static final String PACKAGE_PATH = Constants.PKG_FEAT_LATEST_DOT_PATH + ".cleanroom";

    @Bean
    public CleanroomController cleanroomController(@Autowired CleanroomService service){
        return new CleanroomController(service);
    }

    @Bean
    public CleanroomService cleanroomService(
            @Autowired CleanroomRepository repository,
            @Autowired ProfileService profileService,
            @Autowired EventService eventService){
        return new CleanroomService(repository, profileService, eventService);
    }
}
