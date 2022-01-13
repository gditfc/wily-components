package io.csra.wily.components.converter;

import com.github.dozermapper.core.DozerConverter;
import org.springframework.stereotype.Component;

@Component("booleanStringConverter")
public class BooleanStringConverter extends DozerConverter<String, Boolean> {

    public BooleanStringConverter() {
        super(String.class, Boolean.class);
    }

    public BooleanStringConverter(Class<String> prototypeA, Class<Boolean> prototypeB) {
        super(prototypeA, prototypeB);
    }

    @Override
    public Boolean convertTo(String source, Boolean destination) {
        if (source != null) {
            if ("Yes".equals(source)) {
                return Boolean.TRUE;
            }

            if ("No".equals(source)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public String convertFrom(Boolean source, String destination) {
        return (source != null && (source)) ? "Yes" : "No";
    }
}
