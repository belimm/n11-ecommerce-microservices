package com.n11bc.product_service.mapper;

import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.entity.CategoryTranslation;
import com.n11bc.product_service.entity.Product;
import com.n11bc.product_service.entity.ProductTranslation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LocaleContext {

    private static final String DEFAULT_LOCALE = "en";

    private final String requestedLocale;
    private final String language;

    private LocaleContext(String requestedLocale) {
        this.requestedLocale = normalize(requestedLocale).orElse(DEFAULT_LOCALE);
        this.language = this.requestedLocale.split("-")[0];
    }

    public static LocaleContext fromAcceptLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return new LocaleContext(DEFAULT_LOCALE);
        }
        try {
            Locale.LanguageRange range = Locale.LanguageRange.parse(acceptLanguage).stream()
                    .findFirst()
                    .orElse(new Locale.LanguageRange(DEFAULT_LOCALE));
            return new LocaleContext(range.getRange());
        } catch (IllegalArgumentException ex) {
            return new LocaleContext(DEFAULT_LOCALE);
        }
    }

    public String requestedLocale() {
        return requestedLocale;
    }

    public String productName(Product product) {
        return productTranslation(product).map(ProductTranslation::getName).orElse(product.getName());
    }

    public String productDescription(Product product) {
        return productTranslation(product).map(ProductTranslation::getDescription).orElse(product.getDescription());
    }

    public String categoryName(Category category) {
        return categoryTranslation(category).map(CategoryTranslation::getName).orElse(category.getName());
    }

    public String categoryDescription(Category category) {
        return categoryTranslation(category).map(CategoryTranslation::getDescription).orElse(category.getDescription());
    }

    private Optional<ProductTranslation> productTranslation(Product product) {
        if (product == null) {
            return Optional.empty();
        }
        return findTranslation(product.getTranslations(), ProductTranslation::getLocale);
    }

    private Optional<CategoryTranslation> categoryTranslation(Category category) {
        if (category == null) {
            return Optional.empty();
        }
        return findTranslation(category.getTranslations(), CategoryTranslation::getLocale);
    }

    private <T> Optional<T> findTranslation(List<T> translations, java.util.function.Function<T, String> localeReader) {
        if (translations == null || translations.isEmpty()) {
            return Optional.empty();
        }
        Optional<T> exact = translations.stream()
                .filter(translation -> requestedLocale.equals(normalize(localeReader.apply(translation)).orElse("")))
                .findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        Optional<T> languageMatch = translations.stream()
                .filter(translation -> language.equals(normalize(localeReader.apply(translation)).orElse("")))
                .findFirst();
        if (languageMatch.isPresent()) {
            return languageMatch;
        }
        return translations.stream()
                .filter(translation -> DEFAULT_LOCALE.equals(normalize(localeReader.apply(translation)).orElse("")))
                .findFirst();
    }

    private static Optional<String> normalize(String locale) {
        if (locale == null || locale.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(locale.trim().replace('_', '-').toLowerCase(Locale.ROOT));
    }
}
