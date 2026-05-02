package com.n11bc.product_service.config;

import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.entity.CategoryTranslation;
import com.n11bc.product_service.entity.Product;
import com.n11bc.product_service.entity.ProductTranslation;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final List<String> LEGACY_ORGANIC_SLUGS = List.of(
            "cold-pressed-olive-oil",
            "wildflower-honey",
            "stoneground-tahini",
            "botanical-hand-cream",
            "cedar-soap-bar",
            "rosewater-face-mist",
            "linen-table-runner",
            "ceramic-storage-jar",
            "woven-market-basket",
            "calm-herbal-tea",
            "magnesium-bath-salts",
            "cotton-yoga-strap"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Category> categories = seedCategories();
        deactivateLegacyOrganicProducts();
        seedProducts(categories);
    }

    private Map<String, Category> seedCategories() {
        Category electronics = category("electronics", "Electronics", "Phones, computers, wearables and smart devices",
                "Elektronik", "Telefon, bilgisayar, giyilebilir teknoloji ve akilli cihazlar");
        Category fashion = category("fashion", "Fashion", "Everyday apparel, sneakers and seasonal accessories",
                "Moda", "Gunluk giyim, sneaker ve sezonluk aksesuarlar");
        Category home = category("home-living", "Home & Living", "Small appliances, kitchen, decoration and home essentials",
                "Ev ve Yasam", "Kucuk ev aletleri, mutfak, dekorasyon ve ev ihtiyaclari");
        Category beauty = category("beauty-care", "Beauty & Care", "Skincare, grooming and personal care favorites",
                "Kozmetik ve Bakim", "Cilt bakimi, kisisel bakim ve populer kozmetik urunleri");
        Category sports = category("sports-outdoor", "Sports & Outdoor", "Training gear, outdoor equipment and active lifestyle products",
                "Spor ve Outdoor", "Antrenman ekipmani, outdoor urunleri ve aktif yasam urunleri");
        Category kids = category("kids-toys", "Kids & Toys", "Toys, learning kits and family essentials",
                "Anne, Bebek ve Oyuncak", "Oyuncaklar, egitici setler ve aile ihtiyaclari");
        return Map.of(
                electronics.getSlug(), electronics,
                fashion.getSlug(), fashion,
                home.getSlug(), home,
                beauty.getSlug(), beauty,
                sports.getSlug(), sports,
                kids.getSlug(), kids
        );
    }

    private Category category(String slug, String name, String description, String trName, String trDescription) {
        return categoryRepository.findBySlug(slug).map(existing -> {
            existing.setName(name);
            existing.setDescription(description);
            syncCategoryTranslation(existing, "en", name, description);
            syncCategoryTranslation(existing, "tr", trName, trDescription);
            return categoryRepository.save(existing);
        }).orElseGet(() -> {
            Category category = Category.builder()
                    .name(name)
                    .slug(slug)
                    .description(description)
                    .build();
            category.addTranslation(CategoryTranslation.builder()
                    .locale("en")
                    .name(name)
                    .description(description)
                    .build());
            category.addTranslation(CategoryTranslation.builder()
                    .locale("tr")
                    .name(trName)
                    .description(trDescription)
                    .build());
            Category saved = categoryRepository.save(category);
            log.info("Seeded category: {}", saved.getSlug());
            return saved;
        });
    }

    private void syncCategoryTranslation(Category category, String locale, String name, String description) {
        category.getTranslations().stream()
                .filter(translation -> locale.equals(translation.getLocale()))
                .findFirst()
                .ifPresentOrElse(translation -> {
                    translation.setName(name);
                    translation.setDescription(description);
                }, () -> category.addTranslation(CategoryTranslation.builder()
                        .locale(locale)
                        .name(name)
                        .description(description)
                        .build()));
    }

    private void deactivateLegacyOrganicProducts() {
        LEGACY_ORGANIC_SLUGS.forEach(slug -> productRepository.findBySlug(slug).ifPresent(product -> {
            if (product.isActive()) {
                product.setActive(false);
                productRepository.save(product);
                log.info("Deactivated legacy seed product: {}", slug);
            }
        }));
    }

    private void seedProducts(Map<String, Category> categories) {
        product("galaxy-s24-ultra-256gb", "Samsung Galaxy S24 Ultra 256 GB", "Titanium frame smartphone with AI camera tools, 6.8 inch AMOLED display and S Pen support.", "Samsung Galaxy S24 Ultra 256 GB", "Titanyum govde, yapay zeka destekli kamera, 6.8 inc AMOLED ekran ve S Pen destegi.", "64999.00", "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=1000&q=80", categories.get("electronics"));
        product("iphone-15-128gb", "Apple iPhone 15 128 GB", "A16 Bionic powered smartphone with Dynamic Island, advanced dual camera and USB-C charging.", "Apple iPhone 15 128 GB", "A16 Bionic islemci, Dynamic Island, gelismis cift kamera ve USB-C sarj destegi.", "52999.00", "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=1000&q=80", categories.get("electronics"));
        product("lenovo-ideapad-slim-5", "Lenovo IdeaPad Slim 5 Laptop", "Lightweight 14 inch laptop with Ryzen 7 processor, 16 GB RAM and 1 TB SSD for daily work.", "Lenovo IdeaPad Slim 5 Laptop", "Ryzen 7 islemci, 16 GB RAM ve 1 TB SSD ile gunluk isler icin hafif 14 inc laptop.", "32999.00", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1000&q=80", categories.get("electronics"));
        product("sony-wh-1000xm5", "Sony WH-1000XM5 Headphones", "Wireless noise cancelling headphones with long battery life and multipoint Bluetooth connection.", "Sony WH-1000XM5 Kulaklik", "Uzun pil omru ve coklu Bluetooth baglantiya sahip kablosuz gurultu engelleyici kulaklik.", "14999.00", "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=1000&q=80", categories.get("electronics"));

        product("regular-fit-denim-jacket", "Regular Fit Denim Jacket", "Mid-blue cotton denim jacket with relaxed fit, metal buttons and daily layering comfort.", "Regular Fit Denim Ceket", "Orta mavi pamuk denim, rahat kalip, metal dugmeler ve gunluk kombin konforu.", "1299.90", "https://images.unsplash.com/photo-1516257984-b1b4d707412e?auto=format&fit=crop&w=1000&q=80", categories.get("fashion"));
        product("running-sneaker-air-knit", "Air Knit Running Sneaker", "Breathable running sneaker with cushioned sole for city walks and light training.", "Air Knit Kosu Ayakkabisi", "Sehir yuruyusu ve hafif antrenman icin nefes alan ust yuzey ve yastiklamali taban.", "2199.90", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1000&q=80", categories.get("fashion"));
        product("minimal-backpack-22l", "Minimal Backpack 22L", "Water resistant daily backpack with laptop pocket, organizer panel and clean urban profile.", "Minimal Sirt Cantasi 22L", "Laptop gozu, organizer paneli ve sade sehir formuyla suya dayanikli gunluk sirt cantasi.", "899.90", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1000&q=80", categories.get("fashion"));

        product("philips-airfryer-xl", "Philips Airfryer XL", "Large capacity air fryer with rapid hot air technology and dishwasher safe basket.", "Philips Airfryer XL", "Hizli sicak hava teknolojisi ve bulasik makinesinde yikanabilir hazneye sahip genis kapasiteli airfryer.", "4999.90", "https://images.unsplash.com/photo-1626200419199-391ae4be7a41?auto=format&fit=crop&w=1000&q=80", categories.get("home-living"));
        product("dyson-v15-detect", "Dyson V15 Detect Cordless Vacuum", "Cordless vacuum cleaner with laser dust reveal, intelligent suction and multiple floor tools.", "Dyson V15 Detect Kablosuz Supurge", "Lazer toz gostergesi, akilli cekis gucu ve coklu zemin basliklari olan kablosuz supurge.", "24999.00", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=1000&q=80", categories.get("home-living"));
        product("karaca-84-piece-dinner-set", "Karaca 84 Piece Dinner Set", "Porcelain dinner set for twelve people with modern rim detail and dishwasher friendly finish.", "Karaca 84 Parca Yemek Takimi", "On iki kisilik, modern kenar detayli ve bulasik makinesine uygun porselen yemek takimi.", "6499.90", "https://images.unsplash.com/photo-1603199506016-b9a594b593c0?auto=format&fit=crop&w=1000&q=80", categories.get("home-living"));
        product("ikea-work-desk-120", "IKEA Work Desk 120 cm", "Compact work desk with cable shelf and durable matte surface for home office setups.", "IKEA Calisma Masasi 120 cm", "Ev ofis kurulumlari icin kablo rafli, dayanikli mat yuzeyli kompakt calisma masasi.", "2799.90", "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=1000&q=80", categories.get("home-living"));

        product("la-roche-posay-anthelios-spf50", "La Roche-Posay Anthelios SPF50", "Lightweight facial sunscreen for high protection with a non-greasy finish.", "La Roche-Posay Anthelios SPF50", "Yagli his birakmayan hafif yapili, yuksek korumali yuz gunes kremi.", "799.90", "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1000&q=80", categories.get("beauty-care"));
        product("oral-b-io-electric-toothbrush", "Oral-B iO Electric Toothbrush", "Smart electric toothbrush with pressure sensor, magnetic drive and travel case.", "Oral-B iO Elektrikli Dis Fircasi", "Basincl sensoru, manyetik surus ve seyahat kutusuna sahip akilli elektrikli dis fircasi.", "3499.90", "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?auto=format&fit=crop&w=1000&q=80", categories.get("beauty-care"));
        product("garnier-hyaluronic-serum", "Garnier Hyaluronic Aloe Serum", "Daily hydrating serum with hyaluronic acid and aloe for a fresh skin feel.", "Garnier Hyaluronik Aloe Serum", "Hyaluronik asit ve aloe iceren, ferah cilt hissi veren gunluk nem serumu.", "349.90", "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=1000&q=80", categories.get("beauty-care"));

        product("adidas-training-mat", "Adidas Training Mat", "Non-slip exercise mat with balanced cushioning for yoga, pilates and floor workouts.", "Adidas Antrenman Mati", "Yoga, pilates ve yer egzersizleri icin kaymaz yuzeyli, dengeli yastiklamali egzersiz mati.", "699.90", "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?auto=format&fit=crop&w=1000&q=80", categories.get("sports-outdoor"));
        product("decathlon-camping-tent-2p", "Decathlon 2 Person Camping Tent", "Easy pitch two person tent with waterproof flysheet for weekend camping trips.", "Decathlon 2 Kisilik Kamp Cadiri", "Hafta sonu kamplari icin su gecirmez dis tenteli, kolay kurulan iki kisilik cadir.", "2499.90", "https://images.unsplash.com/photo-1504851149312-7a075b496cc7?auto=format&fit=crop&w=1000&q=80", categories.get("sports-outdoor"));
        product("xiaomi-electric-scooter-4", "Xiaomi Electric Scooter 4", "Foldable electric scooter with city range, dual braking and dashboard display.", "Xiaomi Electric Scooter 4", "Sehir menzili, cift fren ve gosterge ekranina sahip katlanabilir elektrikli scooter.", "18999.00", "https://images.unsplash.com/photo-1591293835940-934a7c4f2d9b?auto=format&fit=crop&w=1000&q=80", categories.get("sports-outdoor"));

        product("lego-classic-creative-box", "LEGO Classic Creative Box", "Colorful brick set for open-ended building, STEM play and family creativity time.", "LEGO Classic Yaratici Kutu", "Serbest yapim, STEM oyunu ve ailece yaraticilik zamani icin renkli parca seti.", "999.90", "https://images.unsplash.com/photo-1587654780291-39c9404d746b?auto=format&fit=crop&w=1000&q=80", categories.get("kids-toys"));
        product("chicco-baby-stroller", "Chicco Baby Stroller", "Foldable baby stroller with reclining seat, sun canopy and large storage basket.", "Chicco Bebek Arabasi", "Yatabilir koltuk, guneslik ve genis saklama sepetine sahip katlanabilir bebek arabasi.", "5999.90", "https://images.unsplash.com/photo-1590649880765-91b1956b8276?auto=format&fit=crop&w=1000&q=80", categories.get("kids-toys"));
    }

    private void product(String slug, String name, String description, String trName, String trDescription, String price, String imageUrl, Category category) {
        Product product = productRepository.findBySlug(slug).orElseGet(() -> Product.builder()
                .slug(slug)
                .active(true)
                .build());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setImageUrl(imageUrl);
        product.setActive(true);
        product.setCategory(category);
        syncProductTranslation(product, "en", name, description);
        syncProductTranslation(product, "tr", trName, trDescription);
        Product saved = productRepository.save(product);
        log.info("Seeded product: {}", saved.getSlug());
    }

    private void syncProductTranslation(Product product, String locale, String name, String description) {
        product.getTranslations().stream()
                .filter(translation -> locale.equals(translation.getLocale()))
                .findFirst()
                .ifPresentOrElse(translation -> {
                    translation.setName(name);
                    translation.setDescription(description);
                    translation.setSearchText(name + " " + description);
                }, () -> product.addTranslation(ProductTranslation.builder()
                        .locale(locale)
                        .name(name)
                        .description(description)
                        .searchText(name + " " + description)
                        .build()));
    }
}
