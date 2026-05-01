package com.n11bc.product_service.mapper;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;
import com.n11bc.product_service.entity.Category;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "name", expression = "java(localeContext.categoryName(category))")
    @Mapping(target = "description", expression = "java(localeContext.categoryDescription(category))")
    @Mapping(target = "locale", expression = "java(localeContext.requestedLocale())")
    CategoryResponse toResponse(Category category, @Context LocaleContext localeContext);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CategoryRequest request, @MappingTarget Category category);
}
