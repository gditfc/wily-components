package io.csra.wily.components.converter;

import com.github.dozermapper.core.CustomConverter;
import com.github.dozermapper.core.converters.ConversionException;
import com.github.dozermapper.core.converters.DateConverter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

public class LocalDateTimeConverter extends DateConverter implements CustomConverter {

    public LocalDateTimeConverter() {
        this(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public LocalDateTimeConverter(DateFormat dateFormat) {
        super(dateFormat);
    }

    @Override
    public Object convert(Object destination, Object source, Class<?> destinationClass, Class<?> sourceClass) {
        if (source == null) return null;
        return convert(destinationClass, source);
    }

    @Override
    public Object convert(Class destClass, Object srcObject) {
        if (LocalDateTime.class.isAssignableFrom(srcObject.getClass())) {
            return fromInstant(toInstant((LocalDateTime) srcObject), destClass);
        } else if (LocalDate.class.isAssignableFrom(srcObject.getClass())) {
            return fromInstant(toInstant((LocalDate) srcObject), destClass);
        } else if (OffsetDateTime.class.isAssignableFrom(srcObject.getClass())) {
            return fromInstant(((OffsetDateTime) srcObject).toInstant(), destClass, ((OffsetDateTime)srcObject).getOffset());
        } else if (ZonedDateTime.class.isAssignableFrom(srcObject.getClass())) {
            return fromInstant(((ZonedDateTime) srcObject).toInstant(), destClass, ((ZonedDateTime)srcObject).getZone());
        } else if (isJavaTimeObject(destClass)) {
            return fromInstant(((Date) super.convert(Date.class, srcObject)).toInstant(), destClass);
        } else {
            return super.convert(destClass, srcObject);
        }
    }

    public static boolean isApplicable(Class destinationClass, Object source) {
        return isJavaTimeObject(destinationClass) || (source != null && isJavaTimeObject(source.getClass()));
    }

    private static boolean isJavaTimeObject(Class clazz) {
        return clazz != null && (LocalDateTime.class.isAssignableFrom(clazz) || LocalDate.class.isAssignableFrom(clazz)
                || ZonedDateTime.class.isAssignableFrom(clazz) || OffsetDateTime.class.isAssignableFrom(clazz));
    }

    private Instant toInstant(final LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }
    private Instant toInstant(final LocalDate ld) {
        return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Object fromInstant(final Instant instant, final Class dest) {
        return fromInstant(instant, dest, ZoneId.systemDefault());
    }

    private Object fromInstant(final Instant instant, final Class dest, final ZoneId zoneId) {
        if (LocalDateTime.class.isAssignableFrom(dest)) {
            return LocalDateTime.ofInstant(instant, zoneId);
        } else if (OffsetDateTime.class.isAssignableFrom(dest)) {
            return OffsetDateTime.ofInstant(instant, zoneId);
        } else if (ZonedDateTime.class.isAssignableFrom(dest)) {
            return ZonedDateTime.ofInstant(instant, zoneId);
        } else if (LocalDate.class.isAssignableFrom(dest)) {
            return LocalDate.ofInstant(instant, zoneId);
        } else if (Timestamp.class.isAssignableFrom(dest)) {
            return Timestamp.from(instant);
        } else if (Date.class.isAssignableFrom(dest)) {
            final Date date = Date.from(instant);
            if (java.sql.Date.class.isAssignableFrom(dest)) {
                return new java.sql.Date(date.getTime());
            }
            return date;
        } else if (String.class.isAssignableFrom(dest)) {
            return super.convert(String.class, instant.toEpochMilli());
        }
        throw new ConversionException("Destination class is not date friendly", new IllegalArgumentException());
    }
}
