package io.csra.wily.components.converter;

import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.MappingException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an extension of the DozerBeanMapper that adds the capability to map a List of objects,
 * rather than just a single object. This is currently configured to be brought in as the default
 * mapper.
 *
 * @author ndimola
 */
@Component
public class DozerMapperPlus {

    private Mapper mapper;
    private LocalDateTimeConverter customDateConverter;
    private static int JAVA_VERSION = getJavaVersion();

    public DozerMapperPlus(Mapper mapper) {
        this.mapper = mapper;
        this.customDateConverter = new LocalDateTimeConverter();
    }

    static int getJavaVersion() {
        final String[] versionElements = System.getProperty("java.version").split("\\.");
        int discard = Integer.parseInt(versionElements[0]);
        if (discard == 1) {
            return Integer.parseInt(versionElements[1]);
        } else {
            return discard;
        }
    }

    /**
     * Map a list of objects of one type to a list of objects of another type. Uses an ArrayList as the
     * list implementation under the hood.
     *
     * @param source
     * @param clazz
     * @param <S>
     * @param <D>
     * @return
     */
    public <S, D> List<D> mapList(List<S> source, Class<D> clazz) {
        List<D> destination = new ArrayList<>();

        if (source != null) {
            for (S s : source) {
                destination.add(map(s, clazz));
            }
        }

        return destination;
    }


    public <T> T map(Object source, Class<T> destinationClass, String mapId) throws MappingException {
        if (source == null) {
            return null;
        }

        // this is necessary to handle conversions involving java.time objects for JSE16+
        if (JAVA_VERSION >= 16 && LocalDateTimeConverter.isApplicable(destinationClass, source)) {
            return (T) customDateConverter.convert(destinationClass, source);
        }

        return mapper.map(source, destinationClass, mapId);
    }

    public <T> T map(Object source, Class<T> destinationClass) throws MappingException {
        if (source == null) {
            return null;
        }

        // this is necessary to handle conversions involving java.time objects for JSE16+
        if (JAVA_VERSION >= 16 && LocalDateTimeConverter.isApplicable(destinationClass, source)) {
            return (T) customDateConverter.convert(destinationClass, source);
        }

        return mapper.map(source, destinationClass);
    }

}
