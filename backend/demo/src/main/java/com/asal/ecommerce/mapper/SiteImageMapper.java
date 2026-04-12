package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.model.SiteImage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SiteImageMapper {

    SiteImageResponse toResponse(SiteImage siteImage);
}
