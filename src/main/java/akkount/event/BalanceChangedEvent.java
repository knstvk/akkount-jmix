/*
 * Copyright (c) 2015 akkount
 */

package akkount.event;

import com.haulmont.cuba.gui.events.UiEvent;
import org.springframework.context.ApplicationEvent;

public class BalanceChangedEvent extends ApplicationEvent implements UiEvent {

    public BalanceChangedEvent(Object source) {
        super(source);
    }
}
