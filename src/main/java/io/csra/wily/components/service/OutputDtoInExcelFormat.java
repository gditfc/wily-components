package io.csra.wily.components.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface OutputDtoInExcelFormat {

    /**
     * Feed in a list of objects, their class, and an output stream and this method will write Excel bytes to the provided
     * output stream.
     *
     * @param dtos - list of dtos to be written
     * @param clazz - object class of the dtos
     * @param out - output stream to receive excel bytes
     * @throws IOException - i/o error
     */
    <E> void write(List<E> dtos, Class<E> clazz, OutputStream out, String userName) throws IOException;

}
