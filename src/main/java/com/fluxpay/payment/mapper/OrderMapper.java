package com.fluxpay.payment.mapper;

import com.fluxpay.payment.dto.response.OrderResponse;
import com.fluxpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord order);

}
