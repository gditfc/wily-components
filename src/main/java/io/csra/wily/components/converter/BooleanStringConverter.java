package io.csra.wily.components.converter;

import org.dozer.DozerConverter;
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
				return new Boolean(true);
			}

			if ("No".equals(source)) {
				return new Boolean(false);
			}
		}
		return new Boolean(true);
	}

	@Override
	public String convertFrom(Boolean source, String destination) {
		return (source != null && (source.booleanValue())) ? "Yes" : "No";
	}
}