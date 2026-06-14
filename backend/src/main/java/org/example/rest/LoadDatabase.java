package org.example.rest;

import org.example.rest.amenity.Amenity;
import org.example.rest.amenity.AmenityCategory;
import org.example.rest.amenity.AmenityRepository;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingRepository;
import org.example.rest.booking.BookingStatus;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.example.rest.contact.*;
import org.example.rest.event.*;
import org.example.rest.eventreservation.*;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.hotel.HotelStatus;
import org.example.rest.loyalty.*;
import org.example.rest.payment.*;
import org.example.rest.payment.refund.*;
import org.example.rest.review.*;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.tablereservation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    @Order(1)
    CommandLineRunner initAmenities(AmenityRepository amenityRepository) {
        return args -> {
            if (amenityRepository.count() > 0) {
                log.info("Database already initialized (amenities exist), skipping amenities load");
                return;
            }

            List<Amenity> amenities = new ArrayList<>();

            amenities.add(Amenity.builder().name("WiFi")
                    .description("High-speed wireless internet access throughout the hotel")
                    .iconCode("wifi").category(AmenityCategory.WIFI).build());
            amenities.add(Amenity.builder().name("Parking")
                    .description("Free or paid parking available for guests")
                    .iconCode("parking").category(AmenityCategory.PARKING).build());
            amenities.add(Amenity.builder().name("Swimming Pool")
                    .description("Outdoor or indoor swimming pool for guests")
                    .iconCode("pool").category(AmenityCategory.POOL).build());
            amenities.add(Amenity.builder().name("Fitness Center")
                    .description("Fully equipped gym and fitness facilities")
                    .iconCode("gym").category(AmenityCategory.GYM).build());
            amenities.add(Amenity.builder().name("Restaurant")
                    .description("On-site restaurant serving breakfast, lunch, and dinner")
                    .iconCode("restaurant").category(AmenityCategory.RESTAURANT).build());
            amenities.add(Amenity.builder().name("Bar & Lounge")
                    .description("Full-service bar with cocktails and beverages")
                    .iconCode("bar").category(AmenityCategory.BAR).build());
            amenities.add(Amenity.builder().name("Spa")
                    .description("Spa and wellness center with massage and beauty treatments")
                    .iconCode("spa").category(AmenityCategory.SPA).build());
            amenities.add(Amenity.builder().name("Conference Room")
                    .description("Meeting and conference facilities for business events")
                    .iconCode("conference").category(AmenityCategory.CONFERENCE_ROOM).build());
            amenities.add(Amenity.builder().name("Laundry Service")
                    .description("Laundry and dry cleaning services available")
                    .iconCode("laundry").category(AmenityCategory.LAUNDRY).build());
            amenities.add(Amenity.builder().name("Room Service")
                    .description("24-hour room service for meals and beverages")
                    .iconCode("room-service").category(AmenityCategory.ROOM_SERVICE).build());
            amenities.add(Amenity.builder().name("Concierge")
                    .description("Concierge service for reservations and recommendations")
                    .iconCode("concierge").category(AmenityCategory.CONCIERGE).build());
            amenities.add(Amenity.builder().name("Business Center")
                    .description("Business center with computers, printers, and workstations")
                    .iconCode("business").category(AmenityCategory.BUSINESS_CENTER).build());
            amenities.add(Amenity.builder().name("Pet Friendly")
                    .description("Pets are allowed in the hotel")
                    .iconCode("pets").category(AmenityCategory.PETS_ALLOWED).build());
            amenities.add(Amenity.builder().name("Accessible Rooms")
                    .description("Wheelchair accessible rooms and facilities")
                    .iconCode("accessible").category(AmenityCategory.ACCESSIBLE).build());
            amenities.add(Amenity.builder().name("Other Amenities")
                    .description("Additional amenities available upon request")
                    .iconCode("other").category(AmenityCategory.OTHER).build());

            amenityRepository.saveAll(amenities);
            log.info("Loaded {} amenities", amenities.size());
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner initHotels(HotelRepository hotelRepository, AmenityRepository amenityRepository) {
        return args -> {
            if (hotelRepository.count() > 0) {
                log.info("Database already initialized (hotels exist), skipping hotels load");
                return;
            }

            Amenity wifi       = amenityRepository.findByName("WiFi").orElse(null);
            Amenity parking    = amenityRepository.findByName("Parking").orElse(null);
            Amenity pool       = amenityRepository.findByName("Swimming Pool").orElse(null);
            Amenity gym        = amenityRepository.findByName("Fitness Center").orElse(null);
            Amenity restaurant = amenityRepository.findByName("Restaurant").orElse(null);
            Amenity spa        = amenityRepository.findByName("Spa").orElse(null);
            Amenity concierge  = amenityRepository.findByName("Concierge").orElse(null);
            Amenity business   = amenityRepository.findByName("Business Center").orElse(null);

            List<Hotel> hotels = new ArrayList<>();

            Hotel grandPalace = Hotel.builder()
                    .name("Grand Palace Nablus")
                    .description("A luxury hotel in the heart of Nablus with stunning mountain views.")
                    .historicalBackground("Built on the site of an ancient caravanserai dating back to the Ottoman era.")
                    .address("1 Al-Najah Street, Nablus").city("Nablus").region("West Bank").starRating(5)
                    .phoneNumber("+970-9-2341234").email("info@grandpalacenablus.ps")
                    .websiteUrl("https://grandpalacenablus.ps")
                    .coverImageUrl("https://media.assettype.com/gulfnews%2Fimport%2F2019%2F02%2F12%2F20190212_HouseofPalestine_resources1_16a4a15e9c8_large.jpg?w=640&auto=format%2Ccompress&fit=max")
                    .galleryImageUrls(List.of("https://media.assettype.com/gulfnews%2Fimport%2F2019%2F02%2F12%2F20190212_HouseofPalestine_resources1_16a4a15e9c8_large.jpg?w=640&auto=format%2Ccompress&fit=max"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(grandPalace);

            Hotel ramallah = Hotel.builder()
                    .name("Ramallah City Hotel")
                    .description("A modern hotel in central Ramallah, perfect for business and leisure travelers.")
                    .historicalBackground("Located in the modern downtown district, close to cultural landmarks.")
                    .address("15 Al-Manara Square, Ramallah").city("Ramallah").region("West Bank").starRating(4)
                    .phoneNumber("+970-2-2981234").email("stay@ramallahcityhotel.ps")
                    .coverImageUrl("https://images.trvl-media.com/lodging/12000000/11810000/11801600/11801592/3b7ab331.jpg?impolicy=resizecrop&rw=1200&ra=fit")
                    .galleryImageUrls(List.of(
                            "https://images.trvl-media.com/lodging/12000000/11810000/11801600/11801592/3b7ab331.jpg?impolicy=resizecrop&rw=1200&ra=fit",
                            "https://images.trvl-media.com/lodging/12000000/11810000/11801600/11801592/0fe49c4d.jpg?impolicy=resizecrop&rw=1200&ra=fit"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(ramallah);

            Hotel bethlehem = Hotel.builder()
                    .name("Bethlehem Star Inn")
                    .description("A charming boutique hotel steps away from the Church of the Nativity.")
                    .historicalBackground("The building dates back to the early 1900s, originally a merchant's house.")
                    .address("Manger Square, Bethlehem").city("Bethlehem").region("West Bank").starRating(3)
                    .phoneNumber("+970-2-2741234").email("info@bethlehemstarinn.ps")
                    .coverImageUrl("https://media-cdn.tripadvisor.com/media/photo-s/06/9c/97/e2/bethlehem-star-hotel.jpg")
                    .galleryImageUrls(List.of("https://media-cdn.tripadvisor.com/media/photo-s/06/9c/97/e2/bethlehem-star-hotel.jpg"))
                    .managerId(3L).status(HotelStatus.ACTIVE).build();
            hotels.add(bethlehem);

            Hotel jerusalem = Hotel.builder()
                    .name("Jerusalem Heritage Hotel")
                    .description("Experience the magic of Jerusalem with a stay in our heritage property.")
                    .historicalBackground("A restored 19th-century Ottoman-era building in the heart of the Old City.")
                    .address("12 Christian Quarter Road, Jerusalem").city("Jerusalem").region("West Bank").starRating(4)
                    .phoneNumber("+970-2-6271234").email("heritage@jerusalemhotel.ps")
                    .coverImageUrl("https://www.jerusalemstory.com/sites/default/files/styles/width_1000/public/2024-11/IMG_2667-rsfw.jpg?itok=2NXj6jBU")
                    .galleryImageUrls(List.of("https://www.jerusalemstory.com/sites/default/files/styles/width_1000/public/2024-11/IMG_2667-rsfw.jpg?itok=2NXj6jBU"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(jerusalem);

            Hotel hebron = Hotel.builder()
                    .name("Hebron Hills Resort")
                    .description("Escape to the tranquil hills of Hebron — ideal for nature lovers.")
                    .historicalBackground("Surrounded by centuries-old olive groves with breathtaking valley views.")
                    .address("Old City Road, Hebron").city("Hebron").region("West Bank").starRating(3)
                    .phoneNumber("+970-2-2221234").email("info@hebronhillsresort.ps")
                    .coverImageUrl("https://content.skyscnr.com/available/1256654155/1256654155_WxH.jpg")
                    .galleryImageUrls(List.of("https://content.skyscnr.com/available/1256654155/1256654155_WxH.jpg"))
                    .managerId(3L).status(HotelStatus.ACTIVE).build();
            hotels.add(hebron);

            Hotel jericho = Hotel.builder()
                    .name("Jericho Oasis Hotel")
                    .description("A serene retreat in the world's oldest city, featuring lush gardens and a large pool.")
                    .historicalBackground("Jericho has been continuously inhabited for over 10,000 years.")
                    .address("Ein Al-Sultan Road, Jericho").city("Jericho").region("West Bank").starRating(4)
                    .phoneNumber("+970-2-2321234").email("oasis@jerichohotel.ps")
                    .coverImageUrl("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/02/56/dc/65/hotel-swimming-facilities.jpg?w=700&h=-1&s=1")
                    .galleryImageUrls(List.of("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/02/56/dc/65/hotel-swimming-facilities.jpg?w=700&h=-1&s=1"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(jericho);

            Hotel jenin = Hotel.builder()
                    .name("Jenin Cultural Lodge")
                    .description("A cozy lodge celebrating Jenin's rich cultural and artistic heritage.")
                    .historicalBackground("Jenin is known for its famous Freedom Theatre and vibrant arts scene.")
                    .address("22 Freedom Theatre Street, Jenin").city("Jenin").region("West Bank").starRating(2)
                    .phoneNumber("+970-4-2501234").email("lodge@jenincultural.ps")
                    .coverImageUrl("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/14/32/b4/ae/haddad-tourism-village.jpg?w=900&h=500&s=1")
                    .galleryImageUrls(List.of("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/14/32/b4/ae/haddad-tourism-village.jpg?w=900&h=500&s=1"))
                    .managerId(3L).status(HotelStatus.ACTIVE).build();
            hotels.add(jenin);

            Hotel tulkarm = Hotel.builder()
                    .name("Tulkarm Garden Hotel")
                    .description("A comfortable mid-range hotel surrounded by citrus groves.")
                    .historicalBackground("Tulkarm is historically known as the Bride of the West Bank for its greenery.")
                    .address("5 Garden Street, Tulkarm").city("Tulkarm").region("West Bank").starRating(3)
                    .phoneNumber("+970-9-2671234").email("info@tulkarmgarden.ps")
                    .coverImageUrl("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/05/ec/40/11/the-main-swimming-pool.jpg?w=1400&h=1400&s=1")
                    .galleryImageUrls(List.of("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/05/ec/40/11/the-main-swimming-pool.jpg?w=1400&h=1400&s=1"))
                    .managerId(3L).status(HotelStatus.ACTIVE).build();
            hotels.add(tulkarm);

            Hotel qalqilya = Hotel.builder()
                    .name("Qalqilya Frontier Hotel")
                    .description("A welcoming hotel serving the agricultural heartland of Palestine.")
                    .historicalBackground("A gateway city with deep agricultural roots and a famous zoo.")
                    .address("1 Main Road, Qalqilya").city("Qalqilya").region("West Bank").starRating(2)
                    .phoneNumber("+970-9-2941234").email("frontier@qalqilyahotel.ps")
                    .coverImageUrl("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/29/01/fc/28/caption.jpg?w=1200&h=-1&s=1")
                    .galleryImageUrls(List.of("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/29/01/fc/28/caption.jpg?w=1200&h=-1&s=1"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(qalqilya);

            Hotel salfit = Hotel.builder()
                    .name("Salfit Eco Retreat")
                    .description("A sustainable eco-retreat nestled in the olive-tree-covered hills of Salfit.")
                    .historicalBackground("Salfit sits amid one of Palestine's oldest olive groves, some trees over 4000 years old.")
                    .address("Olive Grove Road, Salfit").city("Salfit").region("West Bank").starRating(3)
                    .phoneNumber("+970-9-2511234").email("stay@salfitecoreteat.ps")
                    .coverImageUrl("https://scontent.fjrs25-1.fna.fbcdn.net/v/t39.30808-6/518456849_1186943310113801_838564493441982595_n.jpg?stp=cp6_dst-jpg_tt6&_nc_cat=110&ccb=1-7&_nc_sid=1d70fc&_nc_ohc=s7nvoWoMrswQ7kNvwF0f-9r&_nc_oc=AdpgUT6CYLQYlMPHeA00izCV355aEmtQ4-GsZw_Gg15wpMC8Xt9KxrcDZ17sgERMjsA&_nc_zt=23&_nc_ht=scontent.fjrs25-1.fna&_nc_gid=CvoLedeahQM5VWIds5b10w&_nc_ss=7a3a8&oh=00_Af2Dc2b9Kx1E9Kkz0LDSQkvtQw3aQjcSQF37aTqOoIbCNw&oe=69D5A4ED")
                    .galleryImageUrls(List.of("https://scontent.fjrs25-1.fna.fbcdn.net/v/t39.30808-6/518456849_1186943310113801_838564493441982595_n.jpg?stp=cp6_dst-jpg_tt6&_nc_cat=110&ccb=1-7&_nc_sid=1d70fc&_nc_ohc=s7nvoWoMrswQ7kNvwF0f-9r&_nc_oc=AdpgUT6CYLQYlMPHeA00izCV355aEmtQ4-GsZw_Gg15wpMC8Xt9KxrcDZ17sgERMjsA&_nc_zt=23&_nc_ht=scontent.fjrs25-1.fna&_nc_gid=CvoLedeahQM5VWIds5b10w&_nc_ss=7a3a8&oh=00_Af2Dc2b9Kx1E9Kkz0LDSQkvtQw3aQjcSQF37aTqOoIbCNw&oe=69D5A4ED"))
                    .managerId(2L).status(HotelStatus.ACTIVE).build();
            hotels.add(salfit);

            hotelRepository.saveAll(hotels);

            if (wifi != null) grandPalace.getAmenities().add(wifi);
            if (parking != null) grandPalace.getAmenities().add(parking);
            if (pool != null) grandPalace.getAmenities().add(pool);
            if (gym != null) grandPalace.getAmenities().add(gym);
            if (restaurant != null) grandPalace.getAmenities().add(restaurant);
            if (spa != null) grandPalace.getAmenities().add(spa);
            if (concierge != null) grandPalace.getAmenities().add(concierge);
            if (business != null) grandPalace.getAmenities().add(business);

            if (wifi != null) ramallah.getAmenities().add(wifi);
            if (parking != null) ramallah.getAmenities().add(parking);
            if (gym != null) ramallah.getAmenities().add(gym);
            if (business != null) ramallah.getAmenities().add(business);
            if (concierge != null) ramallah.getAmenities().add(concierge);

            if (wifi != null) bethlehem.getAmenities().add(wifi);
            if (restaurant != null) bethlehem.getAmenities().add(restaurant);
            if (concierge != null) bethlehem.getAmenities().add(concierge);

            if (wifi != null) jerusalem.getAmenities().add(wifi);
            if (concierge != null) jerusalem.getAmenities().add(concierge);
            if (restaurant != null) jerusalem.getAmenities().add(restaurant);
            if (business != null) jerusalem.getAmenities().add(business);

            if (wifi != null) hebron.getAmenities().add(wifi);
            if (pool != null) hebron.getAmenities().add(pool);
            if (parking != null) hebron.getAmenities().add(parking);

            if (wifi != null) jericho.getAmenities().add(wifi);
            if (pool != null) jericho.getAmenities().add(pool);
            if (restaurant != null) jericho.getAmenities().add(restaurant);
            if (spa != null) jericho.getAmenities().add(spa);

            if (wifi != null) jenin.getAmenities().add(wifi);
            if (concierge != null) jenin.getAmenities().add(concierge);

            if (wifi != null) tulkarm.getAmenities().add(wifi);
            if (parking != null) tulkarm.getAmenities().add(parking);
            if (restaurant != null) tulkarm.getAmenities().add(restaurant);

            if (wifi != null) qalqilya.getAmenities().add(wifi);
            if (parking != null) qalqilya.getAmenities().add(parking);

            if (wifi != null) salfit.getAmenities().add(wifi);
            if (parking != null) salfit.getAmenities().add(parking);
            if (gym != null) salfit.getAmenities().add(gym);

            hotelRepository.saveAll(hotels);
            log.info("Loaded {} hotels", hotels.size());
        };
    }

    @Bean
    @Order(3)
    CommandLineRunner initRoomsAndBookings(
            org.example.rest.room.RoomRepository roomRepository,
            org.example.rest.hotel.HotelRepository hotelRepository,
            org.example.rest.amenity.AmenityRepository amenityRepository,
            org.example.rest.cancellationpolicy.CancellationPolicyRepository cancellationPolicyRepository,
            BookingRepository bookingRepository,
            org.example.rest.pricingrule.PricingRuleRepository pricingRuleRepository,
            org.example.rest.inventory.InventoryRepository inventoryRepository) {
        return args -> {

            if (roomRepository.count() > 0) {
                log.info("Database already initialized (rooms exist), skipping rooms and bookings load");
                return;
            }

            Hotel grandPalace = hotelRepository.findAll().stream()
                    .filter(h -> h.getName().equals("Grand Palace Nablus"))
                    .findFirst().orElse(null);

            Hotel ramallahCity = hotelRepository.findAll().stream()
                    .filter(h -> h.getName().equals("Ramallah City Hotel"))
                    .findFirst().orElse(null);

            if (grandPalace == null || ramallahCity == null) {
                log.warn("Hotels not found — skipping rooms and bookings seed");
                return;
            }

            java.util.function.BiFunction<String, org.example.rest.amenity.Amenity, org.example.rest.amenity.Amenity> findOrCreate =
                    (amenityName, newAmenity) -> amenityRepository.findByName(amenityName)
                            .orElseGet(() -> amenityRepository.save(newAmenity));

            org.example.rest.amenity.Amenity aWifi = findOrCreate.apply("WiFi",
                    org.example.rest.amenity.Amenity.builder()
                            .name("WiFi").description("High-speed wireless internet")
                            .iconCode("wifi").category(org.example.rest.amenity.AmenityCategory.WIFI).build());

            org.example.rest.amenity.Amenity aTv = findOrCreate.apply("Smart TV",
                    org.example.rest.amenity.Amenity.builder()
                            .name("Smart TV").description("55-inch smart TV with streaming apps")
                            .iconCode("tv").category(org.example.rest.amenity.AmenityCategory.OTHER).build());

            org.example.rest.amenity.Amenity aMinibar = findOrCreate.apply("Minibar",
                    org.example.rest.amenity.Amenity.builder()
                            .name("Minibar").description("Stocked minibar with beverages and snacks")
                            .iconCode("minibar").category(org.example.rest.amenity.AmenityCategory.ROOM_SERVICE).build());

            org.example.rest.amenity.Amenity aAc = findOrCreate.apply("Air Conditioning",
                    org.example.rest.amenity.Amenity.builder()
                            .name("Air Conditioning").description("Climate-controlled room")
                            .iconCode("ac").category(org.example.rest.amenity.AmenityCategory.OTHER).build());

            org.example.rest.amenity.Amenity aBalcony = findOrCreate.apply("Balcony",
                    org.example.rest.amenity.Amenity.builder()
                            .name("Balcony").description("Private balcony with mountain views")
                            .iconCode("balcony").category(org.example.rest.amenity.AmenityCategory.OTHER).build());

            org.example.rest.amenity.Amenity aBathtub = findOrCreate.apply("Bathtub",
                    org.example.rest.amenity.Amenity.builder()
                            .name("Bathtub").description("Luxury soaking bathtub")
                            .iconCode("bathtub").category(org.example.rest.amenity.AmenityCategory.OTHER).build());

            log.info("Room amenities ready (created or reused)");

            org.example.rest.cancellationpolicy.CancellationPolicy policyFlexible =
                    cancellationPolicyRepository.findByHotel_IdAndName(
                                    grandPalace.getId(), org.example.rest.cancellationpolicy.CancellationPolicyName.FLEXIBLE)
                            .orElseGet(() -> {
                                org.example.rest.cancellationpolicy.CancellationPolicy p =
                                        new org.example.rest.cancellationpolicy.CancellationPolicy();
                                p.setHotel(grandPalace);
                                p.setName(org.example.rest.cancellationpolicy.CancellationPolicyName.FLEXIBLE);
                                p.setDescription("Free cancellation if cancelled 7 or more days before check-in.");
                                p.setDaysBeforeCheckin(7);
                                p.setRefundPercentage(new BigDecimal("100.00"));
                                return cancellationPolicyRepository.save(p);
                            });

            org.example.rest.cancellationpolicy.CancellationPolicy policyStrict =
                    cancellationPolicyRepository.findByHotel_IdAndName(
                                    grandPalace.getId(), org.example.rest.cancellationpolicy.CancellationPolicyName.STRICT)
                            .orElseGet(() -> {
                                org.example.rest.cancellationpolicy.CancellationPolicy p =
                                        new org.example.rest.cancellationpolicy.CancellationPolicy();
                                p.setHotel(grandPalace);
                                p.setName(org.example.rest.cancellationpolicy.CancellationPolicyName.STRICT);
                                p.setDescription("No refund if cancelled fewer than 10 days before check-in.");
                                p.setDaysBeforeCheckin(10);
                                p.setRefundPercentage(new BigDecimal("0.00"));
                                return cancellationPolicyRepository.save(p);
                            });

            org.example.rest.cancellationpolicy.CancellationPolicy policyModerate =
                    cancellationPolicyRepository.findByHotel_IdAndName(
                                    ramallahCity.getId(), org.example.rest.cancellationpolicy.CancellationPolicyName.MODERATE)
                            .orElseGet(() -> {
                                org.example.rest.cancellationpolicy.CancellationPolicy p =
                                        new org.example.rest.cancellationpolicy.CancellationPolicy();
                                p.setHotel(ramallahCity);
                                p.setName(org.example.rest.cancellationpolicy.CancellationPolicyName.MODERATE);
                                p.setDescription("50% refund if cancelled 5 or more days before check-in.");
                                p.setDaysBeforeCheckin(5);
                                p.setRefundPercentage(new BigDecimal("50.00"));
                                return cancellationPolicyRepository.save(p);
                            });

            log.info("Cancellation policies ready (created or reused)");

            org.example.rest.room.Room deluxeKing = org.example.rest.room.Room.builder()
                    .hotel(grandPalace).name("Deluxe King")
                    .roomType(org.example.rest.room.RoomType.DELUXE)
                    .description("Spacious king room with mountain views and luxury amenities.")
                    .maxCapacity(2).totalRooms(10).basePrice(new BigDecimal("150.00"))
                    .bedType("King").active(true).cancellationPolicyId(policyFlexible.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aTv, aMinibar, aAc, aBalcony)))
                    .build();

            org.example.rest.room.Room standardTwin = org.example.rest.room.Room.builder()
                    .hotel(grandPalace).name("Standard Twin")
                    .roomType(org.example.rest.room.RoomType.TWIN)
                    .description("Comfortable twin room with garden views.")
                    .maxCapacity(2).totalRooms(15).basePrice(new BigDecimal("90.00"))
                    .bedType("Twin").active(true).cancellationPolicyId(policyStrict.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aTv, aAc)))
                    .build();

            org.example.rest.room.Room suite = org.example.rest.room.Room.builder()
                    .hotel(ramallahCity).name("Executive Suite")
                    .roomType(org.example.rest.room.RoomType.SUITE)
                    .description("Luxury suite with separate living area, bathtub, and city views.")
                    .maxCapacity(3).totalRooms(5).basePrice(new BigDecimal("250.00"))
                    .bedType("King").active(true).cancellationPolicyId(policyModerate.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aTv, aMinibar, aAc, aBathtub)))
                    .build();

            org.example.rest.room.Room standardDouble = org.example.rest.room.Room.builder()
                    .hotel(ramallahCity).name("Standard Double")
                    .roomType(org.example.rest.room.RoomType.DOUBLE)
                    .description("Modern double room in the heart of Ramallah.")
                    .maxCapacity(2).totalRooms(20).basePrice(new BigDecimal("80.00"))
                    .bedType("Double").active(true).cancellationPolicyId(policyModerate.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aTv, aAc)))
                    .build();

            org.example.rest.room.Room inactiveRoom1 = org.example.rest.room.Room.builder()
                    .hotel(grandPalace)
                    .name("Inactive Deluxe")
                    .roomType(org.example.rest.room.RoomType.DELUXE)
                    .description("Inactive room for testing booking restrictions.")
                    .maxCapacity(2)
                    .totalRooms(5)
                    .basePrice(new BigDecimal("120.00"))
                    .bedType("Queen")
                    .active(false)
                    .cancellationPolicyId(policyFlexible.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aTv, aAc)))
                    .build();

            org.example.rest.room.Room inactiveRoom2 = org.example.rest.room.Room.builder()
                    .hotel(ramallahCity)
                    .name("Inactive Double")
                    .roomType(org.example.rest.room.RoomType.DOUBLE)
                    .description("Inactive double room for testing.")
                    .maxCapacity(2)
                    .totalRooms(3)
                    .basePrice(new BigDecimal("80.00"))
                    .bedType("Twin")
                    .active(false)
                    .cancellationPolicyId(policyModerate.getId())
                    .amenities(new java.util.HashSet<>(List.of(aWifi, aAc)))
                    .build();

            List<org.example.rest.room.Room> savedRooms = roomRepository.saveAll(
                    List.of(deluxeKing, standardTwin, suite, standardDouble, inactiveRoom1, inactiveRoom2));
            log.info("Loaded {} rooms", savedRooms.size());

            org.example.rest.room.Room roomDeluxe = savedRooms.get(0);
            org.example.rest.room.Room roomTwin   = savedRooms.get(1);
            org.example.rest.room.Room roomSuite  = savedRooms.get(2);
            org.example.rest.room.Room roomDouble = savedRooms.get(3);

            LocalDate today = LocalDate.now();
            LocalDate fourHundredDaysAhead = today.plusDays(400);

            List<org.example.rest.pricingrule.PricingRule> pricingRules = List.of(
                    org.example.rest.pricingrule.PricingRule.builder()
                            .room(roomDeluxe).name("Standard Rate")
                            .description("Standard nightly rate for Deluxe King")
                            .startDate(today).endDate(fourHundredDaysAhead)
                            .pricePerNight(new BigDecimal("150.00")).priority(1).active(true).build(),

                    org.example.rest.pricingrule.PricingRule.builder()
                            .room(roomTwin).name("Standard Rate")
                            .description("Standard nightly rate for Standard Twin")
                            .startDate(today).endDate(fourHundredDaysAhead)
                            .pricePerNight(new BigDecimal("90.00")).priority(1).active(true).build(),

                    org.example.rest.pricingrule.PricingRule.builder()
                            .room(roomSuite).name("Standard Rate")
                            .description("Standard nightly rate for Executive Suite")
                            .startDate(today).endDate(fourHundredDaysAhead)
                            .pricePerNight(new BigDecimal("250.00")).priority(1).active(true).build(),

                    org.example.rest.pricingrule.PricingRule.builder()
                            .room(roomDouble).name("Standard Rate")
                            .description("Standard nightly rate for Standard Double")
                            .startDate(today).endDate(fourHundredDaysAhead)
                            .pricePerNight(new BigDecimal("80.00")).priority(1).active(true).build()
            );
            pricingRuleRepository.saveAll(pricingRules);
            log.info("Loaded {} pricing rules", pricingRules.size());

            List<org.example.rest.inventory.Inventory> inventories = new ArrayList<>();
            List<org.example.rest.room.Room> roomsToInventory = List.of(roomDeluxe, roomTwin, roomSuite, roomDouble);

            for (org.example.rest.room.Room room : roomsToInventory) {
                for (int i = 0; i < 400; i++) {
                    inventories.add(org.example.rest.inventory.Inventory.builder()
                            .room(room)
                            .date(today.plusDays(i))
                            .totalRooms(room.getTotalRooms())
                            .availableRooms(room.getTotalRooms())
                            .build());
                }
            }
            inventoryRepository.saveAll(inventories);
            log.info("Loaded {} inventory records ({} rooms x 400 days)", inventories.size(), roomsToInventory.size());

            if (bookingRepository.count() == 0) {

                // regular users only in booking seed: 4L and 5L
                Booking b1 = new Booking();
                b1.setUserId(5L);
                b1.setRoom(roomDeluxe);
                b1.setNumberOfGuests(2);
                b1.setTotalPrice(450.00);
                b1.setCheckInDate(today.plusDays(14));
                b1.setCheckOutDate(today.plusDays(17));
                b1.setBookingDate(LocalDateTime.now().minusDays(2));
                b1.setStatus(BookingStatus.CONFIRMED);
                b1.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b1.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b1.setCancellationDaysBeforeCheckin(7);
                b1.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b2 = new Booking();
                b2.setUserId(4L);
                b2.setRoom(roomSuite);
                b2.setNumberOfGuests(2);
                b2.setTotalPrice(500.00);
                b2.setCheckInDate(today.plusDays(30));
                b2.setCheckOutDate(today.plusDays(32));
                b2.setBookingDate(LocalDateTime.now().minusDays(1));
                b2.setStatus(BookingStatus.PENDING);
                b2.setCancellationPolicyName(CancellationPolicyName.MODERATE);
                b2.setCancellationPolicyDescription("50% refund if cancelled 5 or more days before check-in.");
                b2.setCancellationDaysBeforeCheckin(5);
                b2.setCancellationRefundPercentage(new BigDecimal("50.00"));

                Booking b3 = new Booking();
                b3.setUserId(5L);
                b3.setRoom(roomTwin);
                b3.setNumberOfGuests(2);
                b3.setTotalPrice(270.00);
                b3.setCheckInDate(today.plusDays(40));
                b3.setCheckOutDate(today.plusDays(43));
                b3.setBookingDate(LocalDateTime.now().minusDays(5));
                b3.setStatus(BookingStatus.CANCELLED);
                b3.setCancellationPolicyName(CancellationPolicyName.STRICT);
                b3.setCancellationPolicyDescription("No refund if cancelled fewer than 10 days before check-in.");
                b3.setCancellationDaysBeforeCheckin(10);
                b3.setCancellationRefundPercentage(new BigDecimal("0.00"));
                b3.setRefundAmount(new BigDecimal("0.00"));

                Booking b4 = new Booking();
                b4.setUserId(5L);
                b4.setRoom(roomDouble);
                b4.setNumberOfGuests(2);
                b4.setTotalPrice(160.00);
                b4.setCheckInDate(today.plusDays(50));
                b4.setCheckOutDate(today.plusDays(52));
                b4.setBookingDate(LocalDateTime.now().minusHours(3));
                b4.setStatus(BookingStatus.CONFIRMED);
                b4.setCancellationPolicyName(CancellationPolicyName.MODERATE);
                b4.setCancellationPolicyDescription("50% refund if cancelled 5 or more days before check-in.");
                b4.setCancellationDaysBeforeCheckin(5);
                b4.setCancellationRefundPercentage(new BigDecimal("50.00"));

                Booking b5 = new Booking();
                b5.setUserId(4L);
                b5.setRoom(roomDouble);
                b5.setNumberOfGuests(2);
                b5.setTotalPrice(160.00);
                b5.setCheckInDate(today.plusDays(50));
                b5.setCheckOutDate(today.plusDays(55));
                b5.setBookingDate(LocalDateTime.now().minusHours(6));
                b5.setStatus(BookingStatus.CONFIRMED);
                b5.setCancellationPolicyName(CancellationPolicyName.MODERATE);
                b5.setCancellationPolicyDescription("50% refund if cancelled 5 or more days before check-in.");
                b5.setCancellationDaysBeforeCheckin(5);
                b5.setCancellationRefundPercentage(new BigDecimal("50.00"));

                Booking b6 = new Booking();
                b6.setUserId(4L);
                b6.setRoom(roomDeluxe);
                b6.setNumberOfGuests(2);
                b6.setTotalPrice(450.00);
                b6.setCheckInDate(today.plusDays(14));
                b6.setCheckOutDate(today.plusDays(17));
                b6.setBookingDate(LocalDateTime.now().minusHours(10));
                b6.setStatus(BookingStatus.PENDING);
                b6.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b6.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b6.setCancellationDaysBeforeCheckin(7);
                b6.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b7 = new Booking();
                b7.setUserId(5L);
                b7.setRoom(roomDeluxe);
                b7.setNumberOfGuests(2);
                b7.setTotalPrice(450.00);
                b7.setCheckInDate(today.plusDays(14));
                b7.setCheckOutDate(today.plusDays(17));
                b7.setBookingDate(LocalDateTime.now().minusHours(11));
                b7.setStatus(BookingStatus.PENDING);
                b7.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b7.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b7.setCancellationDaysBeforeCheckin(7);
                b7.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b8 = new Booking();
                b8.setUserId(4L);
                b8.setRoom(roomDeluxe);
                b8.setNumberOfGuests(2);
                b8.setTotalPrice(450.00);
                b8.setCheckInDate(today.plusDays(14));
                b8.setCheckOutDate(today.plusDays(17));
                b8.setBookingDate(LocalDateTime.now().minusHours(12));
                b8.setStatus(BookingStatus.PENDING);
                b8.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b8.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b8.setCancellationDaysBeforeCheckin(7);
                b8.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b9 = new Booking();
                b9.setUserId(5L);
                b9.setRoom(roomDeluxe);
                b9.setNumberOfGuests(2);
                b9.setTotalPrice(450.00);
                b9.setCheckInDate(today.plusDays(14));
                b9.setCheckOutDate(today.plusDays(17));
                b9.setBookingDate(LocalDateTime.now().minusHours(13));
                b9.setStatus(BookingStatus.PENDING);
                b9.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b9.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b9.setCancellationDaysBeforeCheckin(7);
                b9.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b10 = new Booking();
                b10.setUserId(4L);
                b10.setRoom(roomDeluxe);
                b10.setNumberOfGuests(2);
                b10.setTotalPrice(450.00);
                b10.setCheckInDate(today.plusDays(14));
                b10.setCheckOutDate(today.plusDays(17));
                b10.setBookingDate(LocalDateTime.now().minusHours(14));
                b10.setStatus(BookingStatus.PENDING);
                b10.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b10.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b10.setCancellationDaysBeforeCheckin(7);
                b10.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b11 = new Booking();
                b11.setUserId(5L);
                b11.setRoom(roomDeluxe);
                b11.setNumberOfGuests(2);
                b11.setTotalPrice(450.00);
                b11.setCheckInDate(today.plusDays(14));
                b11.setCheckOutDate(today.plusDays(17));
                b11.setBookingDate(LocalDateTime.now().minusHours(15));
                b11.setStatus(BookingStatus.PENDING);
                b11.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b11.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b11.setCancellationDaysBeforeCheckin(7);
                b11.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b12 = new Booking();
                b12.setUserId(4L);
                b12.setRoom(roomDeluxe);
                b12.setNumberOfGuests(2);
                b12.setTotalPrice(450.00);
                b12.setCheckInDate(today.plusDays(14));
                b12.setCheckOutDate(today.plusDays(17));
                b12.setBookingDate(LocalDateTime.now().minusHours(16));
                b12.setStatus(BookingStatus.PENDING);
                b12.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b12.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b12.setCancellationDaysBeforeCheckin(7);
                b12.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b13 = new Booking();
                b13.setUserId(5L);
                b13.setRoom(roomDeluxe);
                b13.setNumberOfGuests(2);
                b13.setTotalPrice(450.00);
                b13.setCheckInDate(today.plusDays(14));
                b13.setCheckOutDate(today.plusDays(17));
                b13.setBookingDate(LocalDateTime.now().minusHours(17));
                b13.setStatus(BookingStatus.PENDING);
                b13.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b13.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b13.setCancellationDaysBeforeCheckin(7);
                b13.setCancellationRefundPercentage(new BigDecimal("100.00"));

                Booking b14 = new Booking();
                b14.setUserId(4L);
                b14.setRoom(roomDeluxe);
                b14.setNumberOfGuests(2);
                b14.setTotalPrice(450.00);
                b14.setCheckInDate(today.plusDays(14));
                b14.setCheckOutDate(today.plusDays(17));
                b14.setBookingDate(LocalDateTime.now().minusHours(18));
                b14.setStatus(BookingStatus.PENDING);
                b14.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
                b14.setCancellationPolicyDescription("Free cancellation if cancelled 7 or more days before check-in.");
                b14.setCancellationDaysBeforeCheckin(7);
                b14.setCancellationRefundPercentage(new BigDecimal("100.00"));

                bookingRepository.saveAll(List.of(
                        b1, b2, b3, b4, b5,
                        b6, b7, b8, b9, b10, b11, b12, b13, b14
                ));
                log.info("Loaded 14 bookings");

                for (int i = 14; i < 17; i++) {
                    inventoryRepository.findByRoomIdAndDate(roomDeluxe.getId(), today.plusDays(i))
                            .ifPresent(inv -> {
                                for (int j = 0; j < 10; j++) {
                                    inv.reserve(1);
                                }
                                inventoryRepository.save(inv);
                            });
                }

                for (int i = 30; i < 32; i++) {
                    inventoryRepository.findByRoomIdAndDate(roomSuite.getId(), today.plusDays(i))
                            .ifPresent(inv -> { inv.reserve(1); inventoryRepository.save(inv); });
                }

                for (int i = 50; i < 52; i++) {
                    inventoryRepository.findByRoomIdAndDate(roomDouble.getId(), today.plusDays(i))
                            .ifPresent(inv -> { inv.reserve(1); inventoryRepository.save(inv); });
                }

                for (int i = 50; i < 55; i++) {
                    inventoryRepository.findByRoomIdAndDate(roomDouble.getId(), today.plusDays(i))
                            .ifPresent(inv -> { inv.reserve(1); inventoryRepository.save(inv); });
                }

                log.info("Inventory adjusted for active bookings");

            } else {
                log.info("Bookings already exist, skipping booking seed");
            }
        };
    }

    @Bean
    @Order(4)
    CommandLineRunner initEvents(
            EventRepository eventRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository) {
        return args -> {
            if (eventRepository.count() > 0) { log.info("Events already exist, skipping"); return; }

            Hotel grandPalace   = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Grand Palace Nablus")).findFirst().orElse(null);
            Hotel ramallahCity  = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Ramallah City Hotel")).findFirst().orElse(null);
            Hotel bethlehemStar = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Bethlehem Star Inn")).findFirst().orElse(null);
            Hotel jenin         = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Jenin Cultural Lodge")).findFirst().orElse(null);
            Hotel hebron        = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Hebron Hills Resort")).findFirst().orElse(null);

            AppUser creator1 = userRepository.findByEmail("waddler.info@gmail.com").orElse(null);
            AppUser creator2 = userRepository.findByEmail("salmamahmoudao@gmail.com").orElse(null);

            if (grandPalace == null || ramallahCity == null || creator1 == null || creator2 == null) {
                log.warn("initEvents: hotels or users not found — skipping"); return;
            }

            List<Event> events = new ArrayList<>();

            // ─── e1: Grand Palace Nablus - PUBLISHED ───────────────────────────────────
            Event e1 = new Event(); e1.setHotel(grandPalace); e1.setCreatedBy(creator1);
            e1.setTitle("Nablus Cultural Night");
            e1.setDescription("An enchanting evening celebrating the rich cultural heritage of Nablus. Guests will enjoy live dabke performances, traditional Palestinian music played on the oud and tabla, and a curated spread of authentic local dishes including knafeh, mansaf, and musakhan. The event takes place in the grand ballroom of Grand Palace Nablus and is open to all ages.");
            e1.setCategory(EventCategory.CULTURE); e1.getTags().addAll(List.of("culture", "music", "food", "nablus", "dabke", "heritage"));
            e1.setAddress("Grand Palace Nablus, 1 Al-Najah Street, Nablus"); e1.setCity("Nablus"); e1.setLocationType(EventLocationType.HOTEL);
            e1.setStartDateTime(LocalDateTime.now().plusDays(5)); e1.setEndDateTime(LocalDateTime.now().plusDays(5).plusHours(3));
            e1.setPrice(15L); e1.setCurrency("USD"); e1.setCapacityTotal(100); e1.setMaxPerUser(5); e1.setBookingCutoffMinutes(120);
            e1.setStatus(EventStatus.PUBLISHED); e1.setRefundEnabled(true); e1.setRefundPercent(80);
            e1.setBannerImageUrl("https://www.newarab.com/sites/default/files/styles/new_arab_overlay_medium_16_9/public/2023-03/MM2.jpg?itok=7mX3Va9j");
            e1.getPhotos().add("https://www.newarab.com/sites/default/files/styles/new_arab_overlay_medium_16_9/public/2023-03/MM2.jpg?itok=7mX3Va9j");
            events.add(e1);

            // ─── e2: Ramallah City Hotel (Salma's) - PUBLISHED ────────────────────────
            Event e2 = new Event(); e2.setHotel(ramallahCity); e2.setCreatedBy(creator2);
            e2.setTitle("Ramallah Kids Art Workshop");
            e2.setDescription("A fun and creative workshop designed for children aged 5 to 12. Kids will explore Palestinian art, traditional embroidery patterns, and storytelling through hands-on activities. All materials are provided. Parents are welcome to attend. The workshop is hosted in the garden courtyard of Ramallah City Hotel.");
            e2.setCategory(EventCategory.KIDS); e2.getTags().addAll(List.of("kids", "family", "art", "workshop", "ramallah", "creative"));
            e2.setAddress("Ramallah City Hotel, 15 Al-Manara Square, Ramallah"); e2.setCity("Ramallah"); e2.setLocationType(EventLocationType.HOTEL);
            e2.setStartDateTime(LocalDateTime.now().plusDays(10)); e2.setEndDateTime(LocalDateTime.now().plusDays(10).plusHours(3));
            e2.setPrice(0L); e2.setCurrency("USD"); e2.setCapacityTotal(40); e2.setMaxPerUser(3); e2.setBookingCutoffMinutes(60);
            e2.setStatus(EventStatus.PUBLISHED); e2.setRefundEnabled(false);
            e2.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e2.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e2);

            // ─── e3: Bethlehem Star Inn - PUBLISHED ───────────────────────────────────
            Event e3 = new Event(); e3.setHotel(bethlehemStar != null ? bethlehemStar : ramallahCity); e3.setCreatedBy(creator1);
            e3.setTitle("Bethlehem Christmas Street Festival");
            e3.setDescription("Experience the magic of Bethlehem during the festive season. Stroll through beautifully decorated streets near Manger Square, enjoy carol performances by local choirs, and browse artisan stalls selling handcrafted olive wood nativity sets, pottery, and embroidered textiles. A warm celebration of faith, culture, and community.");
            e3.setCategory(EventCategory.CULTURE); e3.getTags().addAll(List.of("christmas", "bethlehem", "festival", "culture", "street", "artisan"));
            e3.setAddress("Manger Square, Bethlehem"); e3.setCity("Bethlehem"); e3.setLocationType(EventLocationType.OUTSIDE);
            e3.setStartDateTime(LocalDateTime.now().plusDays(15)); e3.setEndDateTime(LocalDateTime.now().plusDays(15).plusHours(5));
            e3.setPrice(0L); e3.setCurrency("USD"); e3.setCapacityTotal(500); e3.setMaxPerUser(10); e3.setBookingCutoffMinutes(30);
            e3.setStatus(EventStatus.PUBLISHED); e3.setRefundEnabled(false);
            e3.setBannerImageUrl("https://www.travelpalestine.ps/public/files/image/festivals/bethlehem%20street.jpeg");
            e3.getPhotos().add("https://www.travelpalestine.ps/public/files/image/festivals/bethlehem%20street.jpeg");
            events.add(e3);

            // ─── e4: Jenin Cultural Lodge - PUBLISHED ─────────────────────────────────
            Event e4 = new Event(); e4.setHotel(jenin != null ? jenin : grandPalace); e4.setCreatedBy(creator1);
            e4.setTitle("The Horse of Jenin Theatre Night");
            e4.setDescription("A powerful theatrical performance by the acclaimed Freedom Theatre of Jenin. The Horse of Jenin is an emotionally stirring production that explores themes of resilience, identity, and hope through the eyes of a Palestinian family. The performance is conducted in Arabic with English subtitles and is recommended for audiences aged 14 and above.");
            e4.setCategory(EventCategory.CULTURE); e4.getTags().addAll(List.of("theatre", "jenin", "culture", "drama", "arabic"));
            e4.setAddress("Jenin Cultural Lodge, 22 Freedom Theatre Street, Jenin"); e4.setCity("Jenin"); e4.setLocationType(EventLocationType.HOTEL);
            e4.setStartDateTime(LocalDateTime.now().plusDays(20)); e4.setEndDateTime(LocalDateTime.now().plusDays(20).plusHours(2));
            e4.setPrice(10L); e4.setCurrency("USD"); e4.setCapacityTotal(80); e4.setMaxPerUser(4); e4.setBookingCutoffMinutes(120);
            e4.setStatus(EventStatus.PUBLISHED); e4.setRefundEnabled(true); e4.setRefundPercent(100);
            e4.setBannerImageUrl("https://officiallondontheatre.com/app/uploads/2025/03/THEhorseofjenin-agentassets-nodirector_1200-x-600.jpg");
            e4.getPhotos().add("https://officiallondontheatre.com/app/uploads/2025/03/THEhorseofjenin-agentassets-nodirector_1200-x-600.jpg");
            events.add(e4);

            // ─── e5: Hebron Hills Resort - PUBLISHED ──────────────────────────────────
            Event e5 = new Event(); e5.setHotel(hebron != null ? hebron : grandPalace); e5.setCreatedBy(creator2);
            e5.setTitle("Hebron Olive Harvest Festival");
            e5.setDescription("Join us for a full-day celebration of one of Palestine oldest traditions — the olive harvest. Guests will participate in picking olives from groves that are hundreds of years old, learn about the cold-press oil production process, and enjoy a traditional lunch in the fields. The festival concludes with a communal dinner featuring dishes cooked entirely with fresh-pressed olive oil.");
            e5.setCategory(EventCategory.ADVENTURE); e5.getTags().addAll(List.of("olive", "harvest", "hebron", "tradition", "food", "outdoor"));
            e5.setAddress("Old City Road, Hebron Hills"); e5.setCity("Hebron"); e5.setLocationType(EventLocationType.OUTSIDE);
            e5.setStartDateTime(LocalDateTime.now().plusDays(25)); e5.setEndDateTime(LocalDateTime.now().plusDays(25).plusHours(8));
            e5.setPrice(20L); e5.setCurrency("USD"); e5.setCapacityTotal(60); e5.setMaxPerUser(6); e5.setBookingCutoffMinutes(180);
            e5.setStatus(EventStatus.PUBLISHED); e5.setRefundEnabled(true); e5.setRefundPercent(50);
            e5.setBannerImageUrl("https://www.jerichocci.org/public/storage/files/resized/500x320/image/2025/11/1-1763649415.jpeg");
            e5.getPhotos().add("https://www.jerichocci.org/public/storage/files/resized/500x320/image/2025/11/1-1763649415.jpeg");
            events.add(e5);

            // ─── e6: Ramallah City Hotel (Salma's) - PUBLISHED ────────────────────────
            Event e6 = new Event(); e6.setHotel(ramallahCity); e6.setCreatedBy(creator1);
            e6.setTitle("Ramallah Business Networking Conference");
            e6.setDescription("A premier business networking event bringing together entrepreneurs, investors, and professionals from across Palestine and the diaspora. The conference features keynote speakers, panel discussions on economic development, and structured networking sessions. Lunch and refreshments are included. Business attire required.");
            e6.setCategory(EventCategory.CONFERENCE); e6.getTags().addAll(List.of("business", "networking", "conference", "ramallah", "economy", "professional"));
            e6.setAddress("Ramallah City Hotel, Conference Hall, 15 Al-Manara Square"); e6.setCity("Ramallah"); e6.setLocationType(EventLocationType.HOTEL);
            e6.setStartDateTime(LocalDateTime.now().plusDays(30)); e6.setEndDateTime(LocalDateTime.now().plusDays(30).plusHours(6));
            e6.setPrice(35L); e6.setCurrency("USD"); e6.setCapacityTotal(120); e6.setMaxPerUser(2); e6.setBookingCutoffMinutes(1440);
            e6.setStatus(EventStatus.PUBLISHED); e6.setRefundEnabled(true); e6.setRefundPercent(60); e6.setRequiresApproval(true);
            e6.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e6.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e6);

            // ─── e7: Grand Palace Nablus - PUBLISHED ──────────────────────────────────
            Event e7 = new Event(); e7.setHotel(grandPalace); e7.setCreatedBy(creator2);
            e7.setTitle("Nablus Adventure Hiking Trip");
            e7.setDescription("A guided hiking experience through the scenic mountains surrounding Nablus. Explore ancient olive groves, Roman ruins, and breathtaking panoramic views of the West Bank. The hike is moderate level and suitable for adults and teenagers. Comfortable shoes and water are required. Lunch and snacks are provided at the summit.");
            e7.setCategory(EventCategory.ADVENTURE); e7.getTags().addAll(List.of("hiking", "adventure", "nablus", "nature", "outdoor", "mountains"));
            e7.setAddress("Grand Palace Nablus, 1 Al-Najah Street, Nablus"); e7.setCity("Nablus"); e7.setLocationType(EventLocationType.OUTSIDE);
            e7.setStartDateTime(LocalDateTime.now().plusDays(7)); e7.setEndDateTime(LocalDateTime.now().plusDays(7).plusHours(6));
            e7.setPrice(25L); e7.setCurrency("USD"); e7.setCapacityTotal(30); e7.setMaxPerUser(4); e7.setBookingCutoffMinutes(240);
            e7.setStatus(EventStatus.PUBLISHED); e7.setRefundEnabled(true); e7.setRefundPercent(70);
            e7.setBannerImageUrl("https://www.jerichocci.org/public/storage/files/resized/500x320/image/2025/11/1-1763649415.jpeg");
            e7.getPhotos().add("https://www.jerichocci.org/public/storage/files/resized/500x320/image/2025/11/1-1763649415.jpeg");
            events.add(e7);

            // ─── e8: Grand Palace Nablus - DRAFT ──────────────────────────────────────
            Event e8 = new Event(); e8.setHotel(grandPalace); e8.setCreatedBy(creator1);
            e8.setTitle("Nablus Business & Investment Forum");
            e8.setDescription("A high-level conference bringing together local business leaders, international investors, and government officials to discuss economic opportunities in the Nablus region. Topics include real estate, tourism, agriculture, and tech startups. Keynote speakers, panel sessions, and one-on-one networking slots available. Business attire required. Lunch included.");
            e8.setCategory(EventCategory.CONFERENCE); e8.getTags().addAll(List.of("business", "investment", "nablus", "conference", "economy", "networking"));
            e8.setAddress("Grand Palace Nablus, Conference Hall, 1 Al-Najah Street"); e8.setCity("Nablus"); e8.setLocationType(EventLocationType.HOTEL);
            e8.setStartDateTime(LocalDateTime.now().plusDays(12)); e8.setEndDateTime(LocalDateTime.now().plusDays(12).plusHours(5));
            e8.setPrice(40L); e8.setCurrency("USD"); e8.setCapacityTotal(90); e8.setMaxPerUser(2); e8.setBookingCutoffMinutes(1440);
            e8.setStatus(EventStatus.DRAFT); e8.setRefundEnabled(true); e8.setRefundPercent(100); e8.setRequiresApproval(true);
            e8.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e8.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e8);

            // ─── e9: Ramallah City Hotel (Salma's) - DRAFT ────────────────────────────
            Event e9 = new Event(); e9.setHotel(ramallahCity); e9.setCreatedBy(creator2);
            e9.setTitle("Ramallah Art Exhibition");
            e9.setDescription("A showcase of emerging Palestinian artists from Ramallah and the surrounding region. The exhibition features paintings, sculptures, and mixed-media works exploring themes of identity, memory, and resilience. Free entry. Refreshments provided.");
            e9.setCategory(EventCategory.CULTURE); e9.getTags().addAll(List.of("art", "exhibition", "ramallah", "culture", "palestinian"));
            e9.setAddress("Ramallah City Hotel, 15 Al-Manara Square, Ramallah"); e9.setCity("Ramallah"); e9.setLocationType(EventLocationType.HOTEL);
            e9.setStartDateTime(LocalDateTime.now().plusDays(35)); e9.setEndDateTime(LocalDateTime.now().plusDays(35).plusHours(4));
            e9.setPrice(0L); e9.setCurrency("USD"); e9.setCapacityTotal(80); e9.setMaxPerUser(5); e9.setBookingCutoffMinutes(60);
            e9.setStatus(EventStatus.DRAFT); e9.setRefundEnabled(false);
            e9.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e9.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e9);

            // ─── e10: Ramallah City Hotel (Salma's) - DRAFT ───────────────────────────
            Event e10 = new Event(); e10.setHotel(ramallahCity); e10.setCreatedBy(creator2);
            e10.setTitle("Ramallah Food Festival");
            e10.setDescription("Celebrating the rich culinary heritage of Ramallah and the West Bank. Local chefs and home cooks will present traditional Palestinian dishes, street food, and modern fusion cuisine. Live cooking demonstrations, food stalls, and a knafeh-making competition.");
            e10.setCategory(EventCategory.CULTURE); e10.getTags().addAll(List.of("food", "festival", "ramallah", "culinary", "palestinian"));
            e10.setAddress("Ramallah City Hotel, 15 Al-Manara Square, Ramallah"); e10.setCity("Ramallah"); e10.setLocationType(EventLocationType.HOTEL);
            e10.setStartDateTime(LocalDateTime.now().plusDays(40)); e10.setEndDateTime(LocalDateTime.now().plusDays(40).plusHours(6));
            e10.setPrice(10L); e10.setCurrency("USD"); e10.setCapacityTotal(150); e10.setMaxPerUser(5); e10.setBookingCutoffMinutes(30);
            e10.setStatus(EventStatus.DRAFT); e10.setRefundEnabled(true); e10.setRefundPercent(100);
            e10.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e10.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e10);

            // ─── e11: Ramallah City Hotel (Salma's) - DRAFT ───────────────────────────
            Event e11 = new Event(); e11.setHotel(ramallahCity); e11.setCreatedBy(creator2);
            e11.setTitle("Ramallah Startup Summit");
            e11.setDescription("Connecting entrepreneurs, investors, and innovators in Palestine. The summit features pitch competitions, panel discussions on tech and social entrepreneurship, and structured networking sessions. Lunch included. Business attire required.");
            e11.setCategory(EventCategory.CONFERENCE); e11.getTags().addAll(List.of("startup", "business", "ramallah", "tech", "networking"));
            e11.setAddress("Ramallah City Hotel, Conference Hall, 15 Al-Manara Square"); e11.setCity("Ramallah"); e11.setLocationType(EventLocationType.HOTEL);
            e11.setStartDateTime(LocalDateTime.now().plusDays(45)); e11.setEndDateTime(LocalDateTime.now().plusDays(45).plusHours(7));
            e11.setPrice(50L); e11.setCurrency("USD"); e11.setCapacityTotal(100); e11.setMaxPerUser(2); e11.setBookingCutoffMinutes(1440);
            e11.setStatus(EventStatus.DRAFT); e11.setRefundEnabled(true); e11.setRefundPercent(50); e11.setRequiresApproval(true);
            e11.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e11.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e11);

            // ─── e12: Ramallah City Hotel (Salma's) - PUBLISHED (for CancelEvent testing) ──
            Event e12 = new Event(); e12.setHotel(ramallahCity); e12.setCreatedBy(creator2);
            e12.setTitle("Ramallah Photography Walk");
            e12.setDescription("A guided photography walk through the streets and landmarks of Ramallah. Participants will explore the city through the lens of their cameras or phones, guided by a professional photographer. Suitable for all skill levels. Water provided.");
            e12.setCategory(EventCategory.ADVENTURE); e12.getTags().addAll(List.of("photography", "ramallah", "outdoor", "art", "guided"));
            e12.setAddress("Ramallah City Hotel, 15 Al-Manara Square, Ramallah"); e12.setCity("Ramallah"); e12.setLocationType(EventLocationType.OUTSIDE);
            e12.setStartDateTime(LocalDateTime.now().plusDays(50)); e12.setEndDateTime(LocalDateTime.now().plusDays(50).plusHours(3));
            e12.setPrice(5L); e12.setCurrency("USD"); e12.setCapacityTotal(25); e12.setMaxPerUser(2); e12.setBookingCutoffMinutes(60);
            e12.setStatus(EventStatus.PUBLISHED); e12.setRefundEnabled(true); e12.setRefundPercent(100);
            e12.setBannerImageUrl("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            e12.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg");
            events.add(e12);

            eventRepository.saveAll(events);
            log.info("Loaded {} events", events.size());
        };
    }
    @Bean
    @Order(5)
    CommandLineRunner initReviews(
            ReviewRepository reviewRepository,
            EventRepository eventRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository) {
        return args -> {
            if (reviewRepository.count() > 0) { log.info("Reviews already exist, skipping"); return; }

            AppUser user    = userRepository.findByEmail("202302878@bethlehem.edu").orElse(null);
            AppUser admin   = userRepository.findByEmail("waddler.info@gmail.com").orElse(null);
            AppUser manager = userRepository.findByEmail("salmamahmoudao@gmail.com").orElse(null);

            if (user == null || admin == null || manager == null) { log.warn("initReviews: users not found — skipping"); return; }

            List<Hotel> hotels = hotelRepository.findAll();
            List<Event> events = eventRepository.findAll();

            Hotel grandPalace  = hotels.stream().filter(h -> h.getName().equals("Grand Palace Nablus")).findFirst().orElse(null);
            Hotel ramallahCity = hotels.stream().filter(h -> h.getName().equals("Ramallah City Hotel")).findFirst().orElse(null);
            Hotel bethlehem    = hotels.stream().filter(h -> h.getName().equals("Bethlehem Star Inn")).findFirst().orElse(null);
            Hotel jericho      = hotels.stream().filter(h -> h.getName().equals("Jericho Oasis Hotel")).findFirst().orElse(null);
            Hotel jenin        = hotels.stream().filter(h -> h.getName().equals("Jenin Cultural Lodge")).findFirst().orElse(null);

            Event nabulsCultural = events.stream().filter(e -> e.getTitle().contains("Nablus")).findFirst().orElse(null);
            Event kidsWorkshop   = events.stream().filter(e -> e.getTitle().contains("Kids")).findFirst().orElse(null);
            Event bethlehemFest  = events.stream().filter(e -> e.getTitle().contains("Bethlehem")).findFirst().orElse(null);
            Event theatreNight   = events.stream().filter(e -> e.getTitle().contains("Horse")).findFirst().orElse(null);
            Event oliveFest      = events.stream().filter(e -> e.getTitle().contains("Olive")).findFirst().orElse(null);

            List<Review> reviews = new ArrayList<>();

            if (grandPalace != null) {
                Review r1 = new Review(); r1.setReviewer(user); r1.setTargetType(ReviewTargetType.HOTEL); r1.setTargetId(grandPalace.getId()); r1.setRating(5); r1.setStatus(ReviewStatus.PUBLISHED);
                r1.setComment("Absolutely stunning hotel in the heart of Nablus. The mountain views from our room were breathtaking, the staff were incredibly welcoming, and the breakfast spread featured the best knafeh I have ever had. The spa was world-class. Will definitely return.");
                r1.getPhotos().add("https://www.newarab.com/sites/default/files/styles/new_arab_overlay_medium_16_9/public/2023-03/MM2.jpg?itok=7mX3Va9j"); reviews.add(r1);

                Review r2 = new Review(); r2.setReviewer(admin); r2.setTargetType(ReviewTargetType.HOTEL); r2.setTargetId(grandPalace.getId()); r2.setRating(4); r2.setStatus(ReviewStatus.PUBLISHED);
                r2.setComment("Grand Palace lives up to its name. Elegant rooms, attentive concierge, and a great location for exploring Nablus Old City. The pool area could be slightly larger for a 5-star property, but overall an excellent experience."); reviews.add(r2);
            }

            if (ramallahCity != null) {
                Review r3 = new Review(); r3.setReviewer(user); r3.setTargetType(ReviewTargetType.HOTEL); r3.setTargetId(ramallahCity.getId()); r3.setRating(4); r3.setStatus(ReviewStatus.PUBLISHED);
                r3.setComment("Perfect location right on Al-Manara Square. The rooms are modern and clean, WiFi is fast, and the business center was exactly what I needed for my work trip. The restaurant serves good food though the menu could be more varied. Highly recommend for business travelers.");
                r3.getPhotos().add("https://live.staticflickr.com/4164/33947511563_5dcf9b9a81_b.jpg"); reviews.add(r3);
            }

            if (bethlehem != null) {
                Review r4 = new Review(); r4.setReviewer(admin); r4.setTargetType(ReviewTargetType.HOTEL); r4.setTargetId(bethlehem.getId()); r4.setRating(5); r4.setStatus(ReviewStatus.PUBLISHED);
                r4.setComment("Staying at Bethlehem Star Inn was a spiritual and cultural experience unlike any other. Waking up steps away from the Church of the Nativity is something I will never forget. The building itself is a piece of history. Highly recommended for anyone visiting Bethlehem.");
                r4.getPhotos().add("https://www.travelpalestine.ps/public/files/image/festivals/bethlehem%20street.jpeg"); reviews.add(r4);
            }

            if (jericho != null) {
                Review r5 = new Review(); r5.setReviewer(user); r5.setTargetType(ReviewTargetType.HOTEL); r5.setTargetId(jericho.getId()); r5.setRating(4); r5.setStatus(ReviewStatus.PUBLISHED);
                r5.setComment("A true oasis experience. The pool is enormous and the gardens are lush and peaceful. Perfect for relaxing after visiting the Dead Sea. The spa treatments were exceptional. The only minor issue was that the restaurant closes a bit early. Overall a lovely stay."); reviews.add(r5);
            }

            if (jenin != null) {
                Review r6 = new Review(); r6.setReviewer(manager); r6.setTargetType(ReviewTargetType.HOTEL); r6.setTargetId(jenin.getId()); r6.setRating(4); r6.setStatus(ReviewStatus.PUBLISHED);
                r6.setComment("The Jenin Cultural Lodge is a hidden gem. The atmosphere celebrates the vibrant arts scene of Jenin beautifully. Local artwork is displayed throughout and the staff are passionate about sharing the city story. Very cozy and affordable.");
                r6.getPhotos().add("https://officiallondontheatre.com/app/uploads/2025/03/THEhorseofjenin-agentassets-nodirector_1200-x-600.jpg"); reviews.add(r6);
            }

            if (nabulsCultural != null) {
                Review r7 = new Review(); r7.setReviewer(user); r7.setTargetType(ReviewTargetType.EVENT); r7.setTargetId(nabulsCultural.getId()); r7.setRating(5); r7.setStatus(ReviewStatus.PUBLISHED);
                r7.setComment("The Nablus Cultural Night was absolutely magical. The dabke performance was energetic and joyful, the traditional food was incredible, and the atmosphere was warm and welcoming. A perfect way to experience Palestinian culture in one evening.");
                r7.getPhotos().add("https://www.newarab.com/sites/default/files/styles/new_arab_overlay_medium_16_9/public/2023-03/MM2.jpg?itok=7mX3Va9j"); reviews.add(r7);
            }

            if (kidsWorkshop != null) {
                Review r8 = new Review(); r8.setReviewer(admin); r8.setTargetType(ReviewTargetType.EVENT); r8.setTargetId(kidsWorkshop.getId()); r8.setRating(5); r8.setStatus(ReviewStatus.PUBLISHED);
                r8.setComment("My kids absolutely loved the art workshop. They came home with beautiful embroidery pieces and spent the whole evening telling us stories they had learned. The organizers were patient and creative. A wonderful free event that connects children with their heritage."); reviews.add(r8);
            }

            if (bethlehemFest != null) {
                Review r9 = new Review(); r9.setReviewer(user); r9.setTargetType(ReviewTargetType.EVENT); r9.setTargetId(bethlehemFest.getId()); r9.setRating(5); r9.setStatus(ReviewStatus.PUBLISHED);
                r9.setComment("The Bethlehem Christmas Street Festival was breathtaking. Manger Square lit up at night is a sight I will carry forever. The carol performances were deeply moving and the artisan stalls had the most beautiful handcrafted items. Truly a once-in-a-lifetime experience.");
                r9.getPhotos().add("https://www.travelpalestine.ps/public/files/image/festivals/bethlehem%20street.jpeg"); reviews.add(r9);
            }

            if (theatreNight != null) {
                Review r10 = new Review(); r10.setReviewer(manager); r10.setTargetType(ReviewTargetType.EVENT); r10.setTargetId(theatreNight.getId()); r10.setRating(5); r10.setStatus(ReviewStatus.PUBLISHED);
                r10.setComment("The Horse of Jenin left me speechless. The Freedom Theatre delivers a performance that is raw, powerful, and deeply human. The cast was exceptional and the direction was masterful. If you are in Palestine and have the chance to see this production, do not miss it.");
                r10.getPhotos().add("https://officiallondontheatre.com/app/uploads/2025/03/THEhorseofjenin-agentassets-nodirector_1200-x-600.jpg"); reviews.add(r10);
            }

            if (oliveFest != null) {
                Review r11 = new Review(); r11.setReviewer(user); r11.setTargetType(ReviewTargetType.EVENT); r11.setTargetId(oliveFest.getId()); r11.setRating(4); r11.setStatus(ReviewStatus.PUBLISHED);
                r11.setComment("The Olive Harvest Festival in Hebron was a beautiful and grounding experience. Spending the morning picking olives from ancient trees felt like stepping back in time. The communal dinner was delicious. One note: bring comfortable shoes as the terrain is uneven.");
                r11.getPhotos().add("https://www.jerichocci.org/public/storage/files/resized/500x320/image/2025/11/1-1763649415.jpeg"); reviews.add(r11);
            }

            reviewRepository.saveAll(reviews);
            log.info("Loaded {} reviews", reviews.size());
        };
    }

    @Bean
    @Order(6)
    CommandLineRunner initTableReservations(
            TableReservationRepository tableReservationRepository,
            HotelRepository hotelRepository) {
        return args -> {
            if (tableReservationRepository.count() > 0) { log.info("Database already initialized (table reservations exist), skipping load"); return; }

            Hotel grandPalace   = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Grand Palace Nablus")).findFirst().orElse(null);
            Hotel ramallahCity  = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Ramallah City Hotel")).findFirst().orElse(null);
            Hotel bethlehemStar = hotelRepository.findAll().stream().filter(h -> h.getName().equals("Bethlehem Star Inn")).findFirst().orElse(null);

            if (grandPalace == null || ramallahCity == null || bethlehemStar == null) { log.warn("Hotels not found — skipping table reservations seed"); return; }

            List<TableReservation> reservations = new ArrayList<>();

            TableReservation tr1 = new TableReservation();
            tr1.setReservationCode("TR-A1B2C3D4"); tr1.setGuestCount(4);
            tr1.setReservationDateTime(LocalDateTime.now().plusDays(2).withHour(19).withMinute(0).withSecond(0).withNano(0));
            tr1.setDurationMinutes(120); tr1.setSpecialOccasion(SpecialOccasion.BIRTHDAY);
            tr1.setTableNumber("T5"); tr1.setTableType(TableType.PREMIUM);
            tr1.setPreOrderItems("Birthday cake, champagne"); tr1.setDietaryRestrictions("One guest is vegetarian");
            tr1.setStatus(TableReservationStatus.CONFIRMED); tr1.setHotel(grandPalace); reservations.add(tr1);

            TableReservation tr2 = new TableReservation();
            tr2.setReservationCode("TR-E5F6G7H8"); tr2.setGuestCount(2);
            tr2.setReservationDateTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0).withSecond(0).withNano(0));
            tr2.setDurationMinutes(90); tr2.setSpecialOccasion(SpecialOccasion.BUSINESS);
            tr2.setTableNumber("T2"); tr2.setTableType(TableType.STANDARD);
            tr2.setStatus(TableReservationStatus.PENDING); tr2.setHotel(ramallahCity); reservations.add(tr2);

            TableReservation tr3 = new TableReservation();
            tr3.setReservationCode("TR-I9J0K1L2"); tr3.setGuestCount(2);
            tr3.setReservationDateTime(LocalDateTime.now().plusDays(5).withHour(20).withMinute(30).withSecond(0).withNano(0));
            tr3.setDurationMinutes(150); tr3.setSpecialOccasion(SpecialOccasion.ANNIVERSARY);
            tr3.setTableNumber("T8"); tr3.setTableType(TableType.OUTDOOR); tr3.setPreOrderItems("Rose bouquet, wine");
            tr3.setStatus(TableReservationStatus.CONFIRMED); tr3.setHotel(bethlehemStar); reservations.add(tr3);

            TableReservation tr4 = new TableReservation();
            tr4.setReservationCode("TR-M3N4O5P6"); tr4.setGuestCount(6);
            tr4.setReservationDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
            tr4.setDurationMinutes(120); tr4.setSpecialOccasion(SpecialOccasion.FAMILY);
            tr4.setTableNumber("T10"); tr4.setTableType(TableType.STANDARD); tr4.setDietaryRestrictions("Two guests are gluten-free");
            tr4.setStatus(TableReservationStatus.CANCELLED); tr4.setCancelledAt(LocalDateTime.now().minusHours(5));
            tr4.setHotel(grandPalace); reservations.add(tr4);

            TableReservation tr5 = new TableReservation();
            tr5.setReservationCode("TR-Q7R8S9T0"); tr5.setGuestCount(3);
            tr5.setReservationDateTime(LocalDateTime.now().plusDays(4).withHour(12).withMinute(30).withSecond(0).withNano(0));
            tr5.setDurationMinutes(60); tr5.setTableNumber("T3"); tr5.setTableType(TableType.STANDARD);
            tr5.setStatus(TableReservationStatus.PENDING); tr5.setHotel(ramallahCity); reservations.add(tr5);

            tableReservationRepository.saveAll(reservations);
            log.info("Loaded {} table reservations", reservations.size());
        };
    }

    @Bean
    @Order(7)
    CommandLineRunner initEventReservations(
            EventReservationRepository eventReservationRepository,
            EventRepository eventRepository) {
        return args -> {
            if (eventReservationRepository.count() > 0) { log.info("Database already initialized (event reservations exist), skipping load"); return; }

            Event culturalNight = eventRepository.findAll().stream().filter(e -> e.getTitle().contains("Nablus Cultural")).findFirst().orElse(null);
            Event kidsWorkshop  = eventRepository.findAll().stream().filter(e -> e.getTitle().contains("Kids")).findFirst().orElse(null);

            if (culturalNight == null || kidsWorkshop == null) { log.warn("Events not found — skipping event reservations seed"); return; }

            List<EventReservation> reservations = new ArrayList<>();

            EventReservation er1 = new EventReservation();
            er1.setReservationCode("ER-A1B2C3D4"); er1.setEvent(culturalNight); er1.setParticipantsCount(2);
            er1.setTotalAmount(new BigDecimal("30.00")); er1.setCurrency("USD"); er1.setDiscountPercentage(BigDecimal.ZERO);
            er1.setAgeRestriction(AgeRestriction.ALL_AGES); er1.setMeetingPoint("Hotel Lobby, Bethlehem");
            er1.setSpecialRequests("Window seats preferred"); er1.setStatus(EventReservationStatus.CONFIRMED); reservations.add(er1);

            EventReservation er2 = new EventReservation();
            er2.setReservationCode("ER-E5F6G7H8"); er2.setEvent(culturalNight); er2.setParticipantsCount(4);
            er2.setTotalAmount(new BigDecimal("60.00")); er2.setCurrency("USD"); er2.setDiscountPercentage(new BigDecimal("10.00"));
            er2.setAgeRestriction(AgeRestriction.ALL_AGES); er2.setMeetingPoint("Hotel Lobby, Bethlehem");
            er2.setStatus(EventReservationStatus.PENDING); reservations.add(er2);

            EventReservation er3 = new EventReservation();
            er3.setReservationCode("ER-I9J0K1L2"); er3.setEvent(kidsWorkshop); er3.setParticipantsCount(3);
            er3.setTotalAmount(new BigDecimal("0.00")); er3.setCurrency("USD"); er3.setDiscountPercentage(BigDecimal.ZERO);
            er3.setAgeRestriction(AgeRestriction.KIDS_ONLY); er3.setMeetingPoint("Main Street, Ramallah");
            er3.setSpecialRequests("Two of the kids are siblings"); er3.setStatus(EventReservationStatus.CONFIRMED); reservations.add(er3);

            EventReservation er4 = new EventReservation();
            er4.setReservationCode("ER-M3N4O5P6"); er4.setEvent(culturalNight); er4.setParticipantsCount(1);
            er4.setTotalAmount(new BigDecimal("15.00")); er4.setCurrency("USD"); er4.setDiscountPercentage(BigDecimal.ZERO);
            er4.setAgeRestriction(AgeRestriction.ALL_AGES); er4.setCancellationReason("Change of plans");
            er4.setCancelledAt(LocalDateTime.now().minusHours(3)); er4.setStatus(EventReservationStatus.CANCELLED); reservations.add(er4);

            EventReservation er5 = new EventReservation();
            er5.setReservationCode("ER-Q7R8S9T0"); er5.setEvent(kidsWorkshop); er5.setParticipantsCount(2);
            er5.setTotalAmount(new BigDecimal("0.00")); er5.setCurrency("USD"); er5.setDiscountPercentage(BigDecimal.ZERO);
            er5.setDifficultyLevel(DifficultyLevel.EASY); er5.setAgeRestriction(AgeRestriction.KIDS_ONLY); er5.setMinAge(5);
            er5.setMeetingPoint("Main Street, Ramallah"); er5.setCheckedInAt(LocalDateTime.now().minusHours(1));
            er5.setStatus(EventReservationStatus.ATTENDED); reservations.add(er5);

            eventReservationRepository.saveAll(reservations);
            log.info("Loaded {} event reservations", reservations.size());
        };
    }

    @Bean
    @Order(8)
    CommandLineRunner initContacts(ContactRepository contactRepository, UserRepository userRepository) {
        return args -> {
            if (contactRepository.count() > 0) { log.info("Contacts already exist, skipping"); return; }

            Long userId1 = userRepository.findByEmail("202302878@bethlehem.edu").map(u -> u.getId()).orElse(null);
            Long userId2 = userRepository.findByEmail("waddler.info@gmail.com").map(u -> u.getId()).orElse(null);
            Long userId3 = userRepository.findByEmail("salmamahmoudao@gmail.com").map(u -> u.getId()).orElse(null);

            List<Contact> contacts = new ArrayList<>();

            Contact c1 = new Contact(); c1.setUserId(userId1); c1.setSenderName("Salma Abu Odeh"); c1.setSenderEmail("202302878@bethlehem.edu");
            c1.setSubject("Question about booking cancellation policy");
            c1.setMessage("Hello, I would like to know more about the cancellation policy for my upcoming booking at Grand Palace Nablus. Can I get a full refund if I cancel 10 days before check-in?");
            c1.setCategory(ContactCategory.BOOKING_SUPPORT); c1.setStatus(ContactStatus.RESOLVED); c1.setPriority(ContactPriority.HIGH);
            c1.setResolutionMessage("Hi Salma, yes you are eligible for a full refund under our Flexible policy if you cancel at least 7 days before check-in. Please proceed via your bookings page."); contacts.add(c1);

            Contact c2 = new Contact(); c2.setUserId(userId3); c2.setSenderName("Salma Mahmoud"); c2.setSenderEmail("salmamahmoudao@gmail.com");
            c2.setSubject("Partnership opportunity - travel agency");
            c2.setMessage("We are a travel agency based in Ramallah interested in a bulk booking partnership. Please contact us to discuss rates and availability.");
            c2.setCategory(ContactCategory.PARTNERSHIP); c2.setStatus(ContactStatus.IN_PROGRESS); c2.setPriority(ContactPriority.MEDIUM); contacts.add(c2);

            Contact c3 = new Contact(); c3.setUserId(null); c3.setSenderName("Fatima Hassan"); c3.setSenderEmail("fatima.h@outlook.com");
            c3.setSubject("App is not loading on my phone");
            c3.setMessage("I have been trying to access the booking page on my phone for the past two days but it keeps giving me an error. Please help.");
            c3.setCategory(ContactCategory.TECHNICAL_ISSUE); c3.setStatus(ContactStatus.NEW); c3.setPriority(ContactPriority.URGENT); contacts.add(c3);

            Contact c4 = new Contact(); c4.setUserId(userId2); c4.setSenderName("Admin Waddler"); c4.setSenderEmail("waddler.info@gmail.com");
            c4.setSubject("Great experience at Jericho Oasis Hotel");
            c4.setMessage("Just wanted to share that we had a wonderful stay at the Jericho Oasis Hotel. The pool and service were exceptional. Keep up the great work!");
            c4.setCategory(ContactCategory.FEEDBACK); c4.setStatus(ContactStatus.CLOSED); c4.setPriority(ContactPriority.LOW); contacts.add(c4);

            Contact c5 = new Contact(); c5.setUserId(null); c5.setSenderName("Nour Mansour"); c5.setSenderEmail("nour.mansour@gmail.com");
            c5.setSubject("General inquiry about available rooms");
            c5.setMessage("Hi, I am planning a family trip to Bethlehem in July and would like to know what family rooms are available. Do you have rooms that accommodate 4 people?");
            c5.setCategory(ContactCategory.GENERAL_INQUIRY); c5.setStatus(ContactStatus.NEW); c5.setPriority(ContactPriority.MEDIUM); contacts.add(c5);

            contactRepository.saveAll(contacts);
            log.info("Loaded {} contacts", contacts.size());
        };
    }

    @Bean
    @Order(9)
    CommandLineRunner initLoyalty(
            LoyaltyAccountRepository loyaltyAccountRepository,
            LoyaltyTransactionRepository loyaltyTransactionRepository,
            UserRepository userRepository) {
        return args -> {
            if (loyaltyAccountRepository.count() > 0) { log.info("Loyalty accounts already exist, skipping"); return; }

            Long userIdSalma   = userRepository.findByEmail("202302878@bethlehem.edu").map(u -> u.getId()).orElse(null);
            Long userIdAdmin   = userRepository.findByEmail("waddler.info@gmail.com").map(u -> u.getId()).orElse(null);
            Long userIdManager = userRepository.findByEmail("salmamahmoudao@gmail.com").map(u -> u.getId()).orElse(null);

            if (userIdSalma == null || userIdAdmin == null || userIdManager == null) { log.warn("initLoyalty: users not found — skipping"); return; }

            loyaltyAccountRepository.saveAll(List.of(
                    LoyaltyAccount.builder().userId(userIdSalma).pointsBalance(350L).lifetimePoints(350L).tier(LoyaltyTier.BRONZE).status(LoyaltyStatus.ACTIVE).build(),
                    LoyaltyAccount.builder().userId(userIdAdmin).pointsBalance(1200L).lifetimePoints(1500L).tier(LoyaltyTier.GOLD).status(LoyaltyStatus.ACTIVE).build(),
                    LoyaltyAccount.builder().userId(userIdManager).pointsBalance(0L).lifetimePoints(0L).tier(LoyaltyTier.NONE).status(LoyaltyStatus.ACTIVE).build()
            ));
            log.info("Loaded 3 loyalty accounts");

            List<LoyaltyTransaction> transactions = new ArrayList<>();

            LoyaltyTransaction t1 = new LoyaltyTransaction(); t1.setUserId(userIdSalma); t1.setPoints(200L); t1.setType(LoyaltyTransactionType.EARN); t1.setRewardType(LoyaltyRewardType.BOOKING_CONFIRMED); t1.setRelatedId(1L); t1.setNote("Points earned for confirmed booking #1"); t1.setBalanceAfter(200L); transactions.add(t1);
            LoyaltyTransaction t2 = new LoyaltyTransaction(); t2.setUserId(userIdSalma); t2.setPoints(150L); t2.setType(LoyaltyTransactionType.EARN); t2.setRewardType(LoyaltyRewardType.BOOKING_CONFIRMED); t2.setRelatedId(2L); t2.setNote("Points earned for confirmed booking #2"); t2.setBalanceAfter(350L); transactions.add(t2);
            LoyaltyTransaction t3 = new LoyaltyTransaction(); t3.setUserId(userIdAdmin); t3.setPoints(1500L); t3.setType(LoyaltyTransactionType.EARN); t3.setRewardType(LoyaltyRewardType.BOOKING_CONFIRMED); t3.setRelatedId(4L); t3.setNote("Points earned for confirmed booking #4"); t3.setBalanceAfter(1500L); transactions.add(t3);
            LoyaltyTransaction t4 = new LoyaltyTransaction(); t4.setUserId(userIdAdmin); t4.setPoints(300L); t4.setType(LoyaltyTransactionType.REDEEM); t4.setRewardType(LoyaltyRewardType.BOOKING_DISCOUNT); t4.setNote("Points redeemed for discount on booking"); t4.setBalanceAfter(1200L); transactions.add(t4);

            loyaltyTransactionRepository.saveAll(transactions);
            log.info("Loaded {} loyalty transactions", transactions.size());
        };
    }

    @Bean
    @Order(10)
    CommandLineRunner initPaymentsAndRefunds(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        return args -> {
            if (paymentRepository.count() > 0) { log.info("Payments already exist, skipping"); return; }

            List<Booking> bookings = bookingRepository.findAll();
            if (bookings.isEmpty()) { log.warn("No bookings found — skipping payments seed"); return; }

            Long salmaId    = userRepository.findByEmail("202302878@bethlehem.edu").map(u -> u.getId()).orElse(null);
            Long giovanniId = userRepository.findByEmail("202404659@bethlehem.edu").map(u -> u.getId()).orElse(null);

            if (salmaId == null || giovanniId == null) { log.warn("initPayments: users not found — skipping"); return; }

            Booking b1 = bookings.stream().filter(b -> b.getUserId().equals(salmaId) && b.getStatus() == BookingStatus.CONFIRMED).findFirst().orElse(null);
            Booking b2 = bookings.stream().filter(b -> b.getUserId().equals(salmaId) && b.getStatus() == BookingStatus.PENDING).findFirst().orElse(null);
            Booking b3 = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).findFirst().orElse(null);
            Booking b4 = bookings.stream().filter(b -> b.getUserId().equals(giovanniId) && b.getStatus() == BookingStatus.CONFIRMED).findFirst().orElse(null);

            List<Payment> payments = new ArrayList<>();

            if (b1 != null) {
                Payment p1 = new Payment(); p1.setPaymentCode("PAY-A1B2C3D4"); p1.setUserId(b1.getUserId()); p1.setBooking(b1);
                p1.setAmount(new BigDecimal("450.00")); p1.setCurrency("USD"); p1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
                p1.setPaymentStatus(PaymentStatus.SUCCEEDED); p1.setProcessedAt(LocalDateTime.now().minusDays(2));
                p1.setTransactionId("TXN-001-CONFIRMED"); p1.setFraudDetectionFlag(false); payments.add(p1);
            }
            if (b2 != null) {
                Payment p2 = new Payment(); p2.setPaymentCode("PAY-E5F6G7H8"); p2.setUserId(b2.getUserId()); p2.setBooking(b2);
                p2.setAmount(new BigDecimal("500.00")); p2.setCurrency("USD"); p2.setPaymentMethod(PaymentMethod.ONLINE_WALLET);
                p2.setPaymentStatus(PaymentStatus.PENDING); p2.setFraudDetectionFlag(false); payments.add(p2);
            }
            if (b3 != null) {
                Payment p3 = new Payment(); p3.setPaymentCode("PAY-I9J0K1L2"); p3.setUserId(b3.getUserId()); p3.setBooking(b3);
                p3.setAmount(new BigDecimal("270.00")); p3.setCurrency("USD"); p3.setPaymentMethod(PaymentMethod.DEBIT_CARD);
                p3.setPaymentStatus(PaymentStatus.REFUNDED); p3.setProcessedAt(LocalDateTime.now().minusDays(5));
                p3.setTransactionId("TXN-003-CANCELLED"); p3.setFraudDetectionFlag(false); payments.add(p3);
            }
            if (b4 != null) {
                Payment p4 = new Payment(); p4.setPaymentCode("PAY-M3N4O5P6"); p4.setUserId(b4.getUserId()); p4.setBooking(b4);
                p4.setAmount(new BigDecimal("160.00")); p4.setCurrency("USD"); p4.setPaymentMethod(PaymentMethod.APPLE_PAY);
                p4.setPaymentStatus(PaymentStatus.SUCCEEDED); p4.setProcessedAt(LocalDateTime.now().minusHours(3));
                p4.setTransactionId("TXN-004-CONFIRMED"); p4.setFraudDetectionFlag(false); payments.add(p4);
            }

            List<Payment> savedPayments = paymentRepository.saveAll(payments);
            log.info("Loaded {} payments", savedPayments.size());

            if (refundRepository.count() > 0) { log.info("Refunds already exist, skipping"); return; }

            savedPayments.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED).findFirst().ifPresent(cancelledPayment -> {
                Refund r1 = new Refund(); r1.setPayment(cancelledPayment); r1.setRefundCode("REF-A1B2C3D4");
                r1.setAmount(new BigDecimal("0.00")); r1.setCurrency("USD"); r1.setRefundPercentage(new BigDecimal("0.00"));
                r1.setReason("Cancelled under STRICT policy — no refund applicable");
                r1.setStatus(RefundStatus.COMPLETED); r1.setProcessedAt(LocalDateTime.now().minusDays(4));
                refundRepository.save(r1);
                log.info("Loaded 1 refund");
            });
        };
    }

}