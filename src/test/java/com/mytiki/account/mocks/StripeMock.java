/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.mocks;

import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.mockito.Mockito;

public class StripeMock {

    public static StripeF facade() throws StripeException {
        StripeF stripe = Mockito.mock(StripeF.class);
        Mockito.doReturn("dummy_billing_id").when(stripe).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(stripe).isValid(Mockito.any());
        Mockito.doReturn("dummy_url").when(stripe).portal(Mockito.any());
        Mockito.doNothing().when(stripe).usage(Mockito.any(), Mockito.any(long.class));
        return stripe;
    }
}
