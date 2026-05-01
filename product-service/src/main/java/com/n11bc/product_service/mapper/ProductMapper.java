package com.n11bc.product_service.mapper;

import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.request.ProductUpdateRequest;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;
import com.n11bc.product_service.entity.Product;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = CategoryMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "name", expression = "java(localeContext.productName(product))")
    @Mapping(target = "description", expression = "java(localeContext.productDescription(product))")
    @Mapping(target = "locale", expression = "java(localeContext.requestedLocale())")
    ProductResponse toResponse(Product product, @Context LocaleContext localeContext);

    @Mapping(target = "name", expression = "java(localeContext.productName(product))")
    @Mapping(target = "locale", expression = "java(localeContext.requestedLocale())")
    ProductSummaryResponse toSummaryResponse(Product product, @Context LocaleContext localeContext);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);
}
