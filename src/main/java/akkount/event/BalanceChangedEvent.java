/*
 * Copyright (c) 2015 akkount
 */

package akkount.event;

import org.springframework.context.ApplicationEvent;

public class BalanceChangedEvent extends ApplicationEvent {

    public BalanceChangedEvent(Object source) {
        super(source);
    }
}
