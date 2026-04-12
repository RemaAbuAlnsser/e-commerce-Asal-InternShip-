package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.dto.SettingsUpdateRequest;
import com.asal.ecommerce.model.Settings;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SettingsMapper {

    SettingsResponse toResponse(Settings settings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "siteLogo", ignore = true)
    @Mapping(target = "siteFavicon", ignore = true)
    @Mapping(target = "siteImage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Settings settings, SettingsUpdateRequest request);
}
