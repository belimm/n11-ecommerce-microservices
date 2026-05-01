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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Category> categories = seedCategories();
        seedProducts(categories);
    }

    private Map<String, Category> seedCategories() {
        Category pantry = category("herbal-pantry", "Herbal Pantry", "Natural pantry staples and daily cooking essentials",
                "Bitkisel Kiler", "Gunluk mutfak icin dogal temel urunler");
        Category care = category("personal-care", "Personal Care", "Plant-based care products for everyday rituals",
                "Kisisel Bakim", "Gunluk ritueler icin bitki bazli bakim urunleri");
        Category home = category("home-living", "Home Living", "Calm home goods with organic textures",
                "Ev ve Yasam", "Organik dokulara sahip sakin ev urunleri");
        Category wellness = category("wellness", "Wellness", "Thoughtful wellness goods for balanced routines",
                "Iyi Yasam", "Dengeli rutinler icin ozenli iyi yasam urunleri");
        return Map.of(
                pantry.getSlug(), pantry,
                care.getSlug(), care,
                home.getSlug(), home,
                wellness.getSlug(), wellness
        );
    }

    private Category category(String slug, String name, String description, String trName, String trDescription) {
        return categoryRepository.findBySlug(slug).orElseGet(() -> {
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

    private void seedProducts(Map<String, Category> categories) {
        product("cold-pressed-olive-oil", "Cold-Pressed Olive Oil", "Single-origin extra virgin olive oil with a bright, grassy finish.", "Soguk Sikim Zeytinyagi", "Tek bahceden gelen, canli ve otsu bitise sahip naturel sizma zeytinyagi.", "249.90", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5", categories.get("herbal-pantry"));
        product("wildflower-honey", "Wildflower Honey", "Raw wildflower honey harvested in small seasonal batches.", "Yaban Cicegi Bali", "Kucuk sezonluk partiler halinde hasat edilen ham yaban cicegi bali.", "189.90", "https://images.unsplash.com/photo-1587049352851-8d4e89133924", categories.get("herbal-pantry"));
        product("stoneground-tahini", "Stoneground Tahini", "Velvety sesame tahini stoneground for a deep nutty flavor.", "Tas Degirmen Tahin", "Yogun susam aromasi icin tas degirmende cekilmis ipeksi tahin.", "129.90", "https://images.unsplash.com/photo-1609501676725-7186f017a4b7", categories.get("herbal-pantry"));
        product("botanical-hand-cream", "Botanical Hand Cream", "Fast-absorbing hand cream with shea butter and calendula extract.", "Botanik El Kremi", "Shea yagi ve aynisafa ozlu hizli emilen el kremi.", "119.90", "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b", categories.get("personal-care"));
        product("cedar-soap-bar", "Cedar Soap Bar", "Cold-process soap bar with cedarwood scent and olive oil base.", "Sedir Sabunu", "Zeytinyagi bazli, sedir agaci kokulu soguk proses sabun.", "74.90", "https://images.unsplash.com/photo-1600857544200-b2f666a9a2ec", categories.get("personal-care"));
        product("rosewater-face-mist", "Rosewater Face Mist", "Gentle rosewater mist for refreshing skin through the day.", "Gul Suyu Yuz Spreyi", "Gun boyunca cildi tazelemek icin nazik gul suyu spreyi.", "94.90", "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908", categories.get("personal-care"));
        product("linen-table-runner", "Linen Table Runner", "Washed linen runner with a soft natural drape.", "Keten Masa Runneri", "Yumusak ve dogal dokulu yikanmis keten masa runneri.", "349.90", "https://images.unsplash.com/photo-1517705008128-361805f42e86", categories.get("home-living"));
        product("ceramic-storage-jar", "Ceramic Storage Jar", "Hand-finished ceramic jar for tea, coffee, or dry goods.", "Seramik Saklama Kavanozu", "Cay, kahve veya kuru gida icin elde finislenmis seramik kavanoz.", "219.90", "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61", categories.get("home-living"));
        product("woven-market-basket", "Woven Market Basket", "Durable handwoven basket for shopping and home storage.", "Orgulu Pazar Sepeti", "Alisveris ve ev duzeni icin dayanikli el orgusu sepet.", "289.90", "https://images.unsplash.com/photo-1524758631624-e2822e304c36", categories.get("home-living"));
        product("calm-herbal-tea", "Calm Herbal Tea", "Caffeine-free blend of lemon balm, lavender, and chamomile.", "Sakin Bitki Cayi", "Melisa, lavanta ve papatya karisimli kafeinsiz bitki cayi.", "99.90", "https://images.unsplash.com/photo-1544787219-7f47ccb76574", categories.get("wellness"));
        product("magnesium-bath-salts", "Magnesium Bath Salts", "Mineral-rich bath salts with eucalyptus and sea salt.", "Magnezyum Banyo Tuzu", "Okaliptus ve deniz tuzu iceren mineral zengini banyo tuzu.", "159.90", "https://images.unsplash.com/photo-1515377905703-c4788e51af15", categories.get("wellness"));
        product("cotton-yoga-strap", "Cotton Yoga Strap", "Soft cotton yoga strap with brass buckle for mindful stretching.", "Pamuk Yoga Kemeri", "Farkindalikla esneme icin pirinc tokali yumusak pamuk yoga kemeri.", "139.90", "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0", categories.get("wellness"));
    }

    private void product(String slug, String name, String description, String trName, String trDescription, String price, String imageUrl, Category category) {
        if (productRepository.existsBySlug(slug)) {
            return;
        }
        Product product = Product.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .price(new BigDecimal(price))
                .imageUrl(imageUrl)
                .active(true)
                .category(category)
                .build();
        product.addTranslation(ProductTranslation.builder()
                .locale("en")
                .name(name)
                .description(description)
                .searchText(name + " " + description)
                .build());
        product.addTranslation(ProductTranslation.builder()
                .locale("tr")
                .name(trName)
                .description(trDescription)
                .searchText(trName + " " + trDescription)
                .build());
        Product saved = productRepository.save(product);
        log.info("Seeded product: {}", saved.getSlug());
    }
}
