package io.csra.wily.components.service;

import com.smartystreets.api.exceptions.SmartyException;
import io.csra.wily.components.model.AddressInputDTO;
import io.csra.wily.components.model.AddressResultDTO;
import io.csra.wily.components.model.CoordinatesDTO;

import java.io.IOException;
import java.util.List;

public interface AddressService {

    AddressResultDTO checkAddress(AddressInputDTO dto)
            throws IOException, InterruptedException, SmartyException;

    List<AddressResultDTO> checkAddresses(List<AddressInputDTO> dtos)
            throws IOException, InterruptedException, SmartyException;

    double distanceBetween(CoordinatesDTO coordinates1, CoordinatesDTO coordinates2);

}
