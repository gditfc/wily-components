package io.csra.wily.components.service;

import org.springframework.context.MessageSource;

public interface CustomMessageSource extends MessageSource {

    String getMessage(String msg);

    String getMessage(String msg, String[] replacementValues);

}
