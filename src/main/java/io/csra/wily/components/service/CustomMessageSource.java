package io.csra.wily.components.service;

import org.springframework.context.MessageSource;

public interface CustomMessageSource extends MessageSource {

    public String getMessage(String msg);

    public String getMessage(String msg, String[] replacementValues);

}
