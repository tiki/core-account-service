/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.event.ao;

import java.util.List;

public class EventAOSubEstimateRsp extends EventAOBase {
    private Long count;
    private List<List<EventAOSubEstimateRspCol>> sample;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<List<EventAOSubEstimateRspCol>> getSample() {
        return sample;
    }

    public void setSample(List<List<EventAOSubEstimateRspCol>> sample) {
        this.sample = sample;
    }
}
