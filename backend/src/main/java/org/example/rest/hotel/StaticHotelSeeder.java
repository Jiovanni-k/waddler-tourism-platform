package org.example.rest.hotel;

import lombok.extern.slf4j.Slf4j;
import org.example.rest.amenity.Amenity;
import org.example.rest.amenity.AmenityRepository;
import org.example.rest.inventory.Inventory;
import org.example.rest.inventory.InventoryRepository;
import org.example.rest.room.Room;
import org.example.rest.room.RoomRepository;
import org.example.rest.room.RoomType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class StaticHotelSeeder {

    private static final String[] PHOTOS = {
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80",
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&q=80",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&q=80",
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800&q=80",
        "https://images.unsplash.com/photo-1455587734955-081b22074882?w=800&q=80",
        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&q=80",
        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=80",
        "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?w=800&q=80",
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80",
        "https://images.unsplash.com/photo-1629140727571-9b5c6f6267b4?w=800&q=80",
        "https://images.unsplash.com/photo-1615460549969-36fa19521a4f?w=800&q=80"
    };

    record HotelSeed(String name, String description, String address, String city,
                     String region, int stars, double lat, double lng,
                     String phone, String website, int photoIdx, String photoUrl) {}

    private static HotelSeed h(String name, String desc, String addr, String city,
                                String region, int stars, double lat, double lng,
                                String phone, String website, int photo) {
        return new HotelSeed(name, desc, addr, city, region, stars, lat, lng, phone, website, photo, null);
    }

    private static HotelSeed hp(String name, String desc, String addr, String city,
                                String region, int stars, double lat, double lng,
                                String phone, String website, String photoUrl) {
        return new HotelSeed(name, desc, addr, city, region, stars, lat, lng, phone, website, 0, photoUrl);
    }

    private static final List<String> FEATURED_REGION_ORDER = List.of(
            "United States", "Egypt", "United Kingdom", "France",
            "Saudi Arabia", "Qatar", "Jordan", "Lebanon", "Oman", "Morocco",
            "UAE", "Turkey", "Italy", "Spain", "Netherlands", "Austria",
            "Czech Republic", "Hungary", "Germany", "Switzerland"
    );

    private static final List<HotelSeed> HOTELS = List.of(
        // ── DUBAI ──
        h("Burj Al Arab Jumeirah","Iconic sail-shaped tower on its own island, the world's most photographed hotel.","Jumeirah Beach Road","Dubai","UAE",5,25.1412,55.1853,"+971 4 301 7777","https://www.jumeirah.com",10),
        h("Atlantis The Palm","Sprawling resort on the Palm with an aquapark and 17 world-class restaurants.","Crescent Road, The Palm","Dubai","UAE",5,25.1304,55.1174,"+971 4 426 0000","https://www.atlantis.com/dubai",8),
        h("Four Seasons Resort Dubai at Jumeirah Beach","Beachfront sanctuary with impeccable personalised service and three pools.","Jumeirah Road","Dubai","UAE",5,25.2048,55.2381,"+971 4 270 7777","https://www.fourseasons.com/dubai",0),
        h("Armani Hotel Dubai","Giorgio Armani's first hotel, embedded inside the iconic Burj Khalifa.","Burj Khalifa, Downtown Dubai","Dubai","UAE",5,25.1972,55.2742,"+971 4 888 3888","https://www.armanihotels.com",4),
        h("Address Downtown Dubai","Sleek tower with spectacular Burj Khalifa views and an outdoor terrace pool.","Sheikh Mohammed Bin Rashid Blvd","Dubai","UAE",5,25.1939,55.2791,"+971 4 436 8888","https://www.addresshotels.com",7),

        // ── ABU DHABI ──
        h("Emirates Palace Mandarin Oriental","Palace-style mega-resort with gold-leaf interiors and a private beach.","West Corniche Road","Abu Dhabi","UAE",5,24.4622,54.3171,"+971 2 690 9000","https://www.mandarinoriental.com/abu-dhabi",6),
        h("Qasr Al Watan","Majestic cultural hotel inside a presidential palace setting on the Corniche.","Al Ras Al Akhdar","Abu Dhabi","UAE",5,24.4665,54.3092,"+971 2 698 2222","https://www.qasralwatan.ae",5),
        h("Four Seasons Hotel Abu Dhabi","Refined tower on Al Maryah Island with a rooftop infinity pool over the skyline.","Al Maryah Island","Abu Dhabi","UAE",5,24.5089,54.3894,"+971 2 333 2222","https://www.fourseasons.com/abudhabi",3),
        h("St. Regis Abu Dhabi","Timeless butler-service hotel in the Nation Towers with Gulf views.","Nation Towers, Corniche","Abu Dhabi","UAE",5,24.4855,54.3375,"+971 2 694 4444","https://www.marriott.com/stregis",9),
        h("Jumeirah at Etihad Towers","Contemporary tower with a panoramic sky lounge and full-service spa.","Etihad Towers, Corniche","Abu Dhabi","UAE",5,24.4632,54.3229,"+971 2 811 5555","https://www.jumeirah.com",1),

        // ── DOHA ──
        h("Marsa Malaz Kempinski The Pearl","Palatial resort on The Pearl island with a private beach and outdoor pools.","The Pearl, West Bay Lagoon","Doha","Qatar",5,25.3684,51.5493,"+974 4035 5555","https://www.kempinski.com/doha",8),
        h("Four Seasons Hotel Doha","Elegant beachfront hotel with a private cove and refined Middle Eastern touches.","Diplomatic Area, West Bay","Doha","Qatar",5,25.3347,51.5310,"+974 4494 8888","https://www.fourseasons.com/doha",0),
        h("Mondrian Doha","Bold art-inspired tower in West Bay with a rooftop pool and vibrant nightlife.","Al Corniche, West Bay","Doha","Qatar",5,25.3312,51.5264,"+974 4045 0000","https://www.sbe.com/mondrian",7),
        h("W Doha Hotel & Residences","Cutting-edge design hotel with a lively pool deck and stellar city views.","West Bay, Al Matar Street","Doha","Qatar",5,25.3290,51.5256,"+974 4453 5000","https://www.marriott.com/w-hotels",1),
        h("Banana Island Resort Doha by Anantara","Private island getaway with overwater bungalows and dolphin encounters.","Banana Island","Doha","Qatar",5,25.2756,51.5564,"+974 4040 5050","https://www.anantara.com/doha",2),

        // ── AMMAN ──
        h("Four Seasons Hotel Amman","Commanding hilltop hotel with panoramic city views and an acclaimed spa.","5th Circle, Al Kindi Street","Amman","Jordan",5,31.9784,35.8795,"+962 6 550 5555","https://www.fourseasons.com/amman",6),
        h("Kempinski Hotel Amman","Elegant luxury on the 7th Circle with a lavish pool and international cuisine.","Abdul Hamid Sharaf Street","Amman","Jordan",5,31.9527,35.8714,"+962 6 520 0200","https://www.kempinski.com/amman",5),
        h("Grand Hyatt Amman","Landmark hotel near the city centre with sweeping Jordan valley views.","Hussein Bin Ali Street","Amman","Jordan",5,31.9539,35.9106,"+962 6 465 1234","https://www.hyatt.com",4),
        h("InterContinental Amman","Classic five-star on the 3rd Circle with an outdoor pool and lush gardens.","Islamic College Street","Amman","Jordan",5,31.9651,35.8972,"+962 6 464 1361","https://www.ihg.com/intercontinental",9),
        h("The Ritz-Carlton Amman","Contemporary luxury on the prestigious Jabal Amman with a sky-terrace pool.","Al Hijaz Street, Jabal Amman","Amman","Jordan",5,31.9563,35.8941,"+962 6 500 8888","https://www.ritzcarlton.com",7),

        // ── ISTANBUL ──
        h("Four Seasons Hotel Istanbul at Sultanahmet","Neoclassical former prison facing Hagia Sophia — perhaps the world's best location.","Tevkifhane Sokak 1, Sultanahmet","Istanbul","Turkey",5,41.0054,28.9768,"+90 212 402 3000","https://www.fourseasons.com/istanbul",6),
        h("Çırağan Palace Kempinski","19th-century Ottoman palace on the Bosphorus with its own infinity pool on the water.","Çırağan Caddesi 32, Beşiktaş","Istanbul","Turkey",5,41.0477,29.0038,"+90 212 326 4646","https://www.kempinski.com/istanbul",3),
        h("Shangri-La Bosphorus Istanbul","Refined tower with floor-to-ceiling Bosphorus panoramas and celebrated dining.","Hayrettin İskelesi Sokak 1, Beşiktaş","Istanbul","Turkey",5,41.0426,29.0054,"+90 212 275 8888","https://www.shangri-la.com",1),
        h("The Ritz-Carlton Istanbul","Grand contemporary hotel in Şişli with a rooftop pool above the city skyline.","Suzer Plaza, Elmadağ, Şişli","Istanbul","Turkey",5,41.0481,28.9923,"+90 212 334 4444","https://www.ritzcarlton.com",7),
        h("Park Hyatt Istanbul","Restored historic building in Maçka with a signature rooftop bar and wine cellar.","Bronz Sokak 4, Maçka","Istanbul","Turkey",5,41.0440,28.9993,"+90 212 315 1234","https://www.hyatt.com",5),

        // ── CAIRO ──
        h("Four Seasons Hotel Cairo at Nile Plaza","Soaring Nile-front tower with an outdoor infinity pool and stellar views of the river.","1089 Corniche El Nil, Garden City","Cairo","Egypt",5,30.0378,31.2254,"+20 2 2791 7000","https://www.fourseasons.com/cairo",0),
        h("Marriott Mena House Cairo","Legendary 19th-century palace hotel with direct views of the Giza Pyramids.","6 Pyramids Road, Giza","Cairo","Egypt",5,29.9851,31.1347,"+20 2 3377 3222","https://www.marriott.com/mena-house",5),
        h("Kempinski Nile Hotel Cairo","Sleek glass tower on the Nile Corniche with a rooftop pool and skyline panoramas.","12 Ahmed Ragheb Street, Garden City","Cairo","Egypt",5,30.0386,31.2267,"+20 2 2798 0000","https://www.kempinski.com/cairo",7),
        h("Conrad Cairo Hotel","Contemporary five-star on the Nile with a sprawling pool deck and live entertainment.","1191 Corniche El Nil, Bulaq","Cairo","Egypt",5,30.0595,31.2295,"+20 2 2580 8000","https://www.conradhotels.com",4),
        h("Sofitel Cairo Nile El Gezirah","Circular tower on Zamalek Island with panoramic Nile-and-city views.","El Thawra Street, Zamalek","Cairo","Egypt",5,30.0620,31.2218,"+20 2 2737 3737","https://www.sofitel.com",2),

        // ── MARRAKECH ──
        h("La Mamounia","Legendary Art Deco palace surrounded by 17 acres of gardens; open since 1923.","Avenue Bab Jdid","Marrakech","Morocco",5,31.6239,-7.9986,"+212 524 388 600","https://www.mamounia.com",6),
        h("Royal Mansour Marrakech","Private riad city built by the King of Morocco with individual pools and butler service.","Rue Abou Abbas El Sebti","Marrakech","Morocco",5,31.6268,-8.0029,"+212 529 808 080","https://www.royalmansour.com",0),
        h("Mandarin Oriental Marrakech","Sprawling villa resort in a palm grove with 31 pools and an award-winning spa.","Route du Golf Royal","Marrakech","Morocco",5,31.6115,-8.0498,"+212 529 888 888","https://www.mandarinoriental.com/marrakech",3),
        h("Amanjena","Moorish pavilions and pool maisons set among rose gardens near the Menara.","Route de Ouarzazate km12","Marrakech","Morocco",5,31.5865,-8.0365,"+212 524 399 000","https://www.aman.com/resorts/amanjena",2),
        h("Selman Marrakech","Equestrian-inspired palace hotel with a 100-metre pool and resident Arabian horses.","Route d'Amizmiz km5","Marrakech","Morocco",5,31.6071,-8.0241,"+212 524 459 000","https://www.selman-marrakech.com",1),

        // ── RIYADH ──
        h("The Ritz-Carlton Riyadh","Palatial fortress resort with 52 acres of grounds, multiple pools and 16 restaurants.","Al Hada Area, Makkah Road","Riyadh","Saudi Arabia",5,24.6697,46.6711,"+966 11 802 8080","https://www.ritzcarlton.com/riyadh",6),
        h("Four Seasons Hotel Riyadh","Dual-tower icon at Kingdom Centre with a sky bridge pool and butler service.","Kingdom Centre Tower","Riyadh","Saudi Arabia",5,24.6877,46.6855,"+966 11 211 5000","https://www.fourseasons.com/riyadh",7),
        h("Waldorf Astoria Riyadh","Art Deco grandeur in the heart of Riyadh with a celebrated spa and roof pool.","Al Urubah Road, Al Woroud","Riyadh","Saudi Arabia",5,24.7008,46.6863,"+966 11 200 6000","https://www.waldorfastoria.com",4),
        h("Rosewood Riyadh","Architectural showpiece in King Abdullah Financial District with infinity pools.","KAFD, Al Nakheel District","Riyadh","Saudi Arabia",5,24.7724,46.6378,"+966 11 283 8888","https://www.rosewoodhotels.com",9),
        h("Mandarin Oriental Riyadh","Sleek urban retreat in King Abdullah Financial District with a rooftop infinity pool.","KAFD, Northern Riyadh","Riyadh","Saudi Arabia",5,24.7747,46.6358,"+966 11 273 7777","https://www.mandarinoriental.com/riyadh",3),

        // ── BEIRUT ──
        h("Four Seasons Hotel Beirut","Beachfront landmark in the heart of the city with a private beach club.","1418 Minet El Hosn, Zeitounay Bay","Beirut","Lebanon",5,33.8938,35.4975,"+961 1 761 000","https://www.fourseasons.com/beirut",8),
        h("InterContinental Le Vendôme Beirut","Elegant 1950s building on the Corniche with spectacular Mediterranean vistas.","Ain Mreisseh, Ras Beirut","Beirut","Lebanon",5,33.8993,35.4820,"+961 1 369 280","https://www.ihg.com/intercontinental",5),
        h("Le Gray Beirut","Chic contemporary hotel in Martyrs Square with a rooftop pool and cocktail bar.","Martyrs Square, Downtown","Beirut","Lebanon",5,33.8961,35.5018,"+961 1 971 111","https://www.campbellgrayhotels.com",7),
        h("Albergo Hotel Beirut","Boutique 1930s Art Deco mansion in Achrafieh with individually designed rooms.","137 Abdel Wahab El Inglizi","Beirut","Lebanon",5,33.8898,35.5185,"+961 1 339 797","https://www.albergobeirut.com",9),
        h("Mövenpick Hotel Beirut","Modern business-and-leisure hotel near the port with city and sea views.","Antelias, Beirut","Beirut","Lebanon",4,33.9005,35.5710,"+961 4 714 000","https://www.movenpick.com/beirut",1),

        // ── MUSCAT ──
        h("The Chedi Muscat","Minimalist Zen resort stretching 9 hectares to the sea; longest pool in Oman.","18th November Street, Al Ghubrah","Muscat","Oman",5,23.6145,58.5421,"+968 2452 4400","https://www.ghmhotels.com/chedi-muscat",3),
        h("Al Bustan Palace A Ritz-Carlton Hotel","Grand palace hotel nestled between mountains and sea with a private beach.","Al Bustan Street, Qantab","Muscat","Oman",5,23.5627,58.6168,"+968 2479 9666","https://www.ritzcarlton.com/muscat",6),
        h("Grand Hyatt Muscat","Palatial Omani-inspired resort with a lagoon pool and private beach.","Shatti Al Qurum","Muscat","Oman",5,23.6041,58.5560,"+968 2464 1234","https://www.hyatt.com",0),
        h("Anantara Al Jabal Al Akhdar Resort","Dramatic clifftop retreat at 2000 m in the Green Mountain with infinity pool over the canyon.","Al Jabal Al Akhdar, Nizwa","Muscat","Oman",5,23.0494,57.6521,"+968 2521 8000","https://www.anantara.com/al-jabal-al-akhdar",11),
        h("Kempinski Hotel Muscat","Contemporary seaside resort with an amphitheatre pool and panoramic Gulf views.","Ghala Gardens, Ghala","Muscat","Oman",5,23.6367,58.5124,"+968 2453 9999","https://www.kempinski.com/muscat",8),

        // ── PARIS ──
        h("Hôtel Ritz Paris","The original grand luxury hotel, opened 1898, facing Place Vendôme.","15 Place Vendôme","Paris","France",5,48.8694,2.3294,"+33 1 43 16 30 30","https://www.ritzparis.com",6),
        h("Four Seasons Hotel George V","Palatial 1928 Art Deco landmark two blocks from the Champs-Élysées.","31 Avenue George V","Paris","France",5,48.8678,2.3022,"+33 1 49 52 70 00","https://www.fourseasons.com/paris",5),
        h("Le Meurice","Versailles-inspired palace facing the Tuileries with Alain Ducasse dining.","228 Rue de Rivoli","Paris","France",5,48.8653,2.3278,"+33 1 44 58 10 10","https://www.dorchestercollection.com/le-meurice",4),
        h("The Peninsula Paris","Belle Époque landmark near the Arc de Triomphe with a stunning rooftop pool.","19 Avenue Kléber","Paris","France",5,48.8733,2.2965,"+33 1 58 12 28 88","https://www.peninsula.com/paris",7),
        h("Mandarin Oriental Paris","Contemporary jewel on Rue Saint-Honoré with a tranquil spa garden and chic bar.","251 Rue Saint-Honoré","Paris","France",5,48.8658,2.3217,"+33 1 70 98 78 88","https://www.mandarinoriental.com/paris",9),

        // ── LONDON ──
        h("The Savoy","Legendary Thames-side grande dame open since 1889, recently restored to glory.","Strand, Covent Garden","London","United Kingdom",5,51.5104,-0.1204,"+44 20 7836 4343","https://www.thesavoy.com",6),
        h("Claridge's","Quintessential Mayfair institution beloved by royalty and celebrities since 1856.","Brook Street, Mayfair","London","United Kingdom",5,51.5122,-0.1492,"+44 20 7629 8860","https://www.claridges.co.uk",5),
        h("The Dorchester","Park Lane icon with gold-and-black marble lobby and the celebrated Spa at The Dorchester.","Park Lane, Mayfair","London","United Kingdom",5,51.5054,-0.1519,"+44 20 7629 8888","https://www.dorchestercollection.com/the-dorchester",4),
        h("Four Seasons Hotel London at Park Lane","Contemporary tower above Hyde Park Corner with a renowned spa and rooftop infinity pool.","Hamilton Place, Park Lane","London","United Kingdom",5,51.5055,-0.1495,"+44 20 7499 0888","https://www.fourseasons.com/london",3),
        h("Mandarin Oriental Hyde Park","Edwardian palace overlooking Hyde Park with a stunning spa and destination restaurants.","66 Knightsbridge","London","United Kingdom",5,51.5020,-0.1599,"+44 20 7235 2000","https://www.mandarinoriental.com/london",9),

        // ── ROME ──
        h("Hotel de Russie","Via Veneto rival that wins on gardens: a tiered terraced garden steps from Piazza del Popolo.","Via del Babuino 9","Rome","Italy",5,41.9093,12.4778,"+39 06 328 881","https://www.roccofortehotels.com/hotel-de-russie",2),
        h("Rome Cavalieri Waldorf Astoria","Hilltop villa resort above the city with three pools and a Michelin three-star restaurant.","Via Alberto Cadlolo 101, Monte Mario","Rome","Italy",5,41.9183,12.4418,"+39 06 35091","https://www.waldorfastoria.com/rome",0),
        h("Hotel Eden Rome","Intimate Dolce Vita landmark on Via Veneto with a roof-terrace restaurant and panoramic pool.","Via Ludovisi 49","Rome","Italy",5,41.9082,12.4889,"+39 06 478 121","https://www.dorchestercollection.com/hotel-eden",7),
        h("Hassler Roma","Storied family-owned palazzo at the top of the Spanish Steps with unmatched old-Rome views.","Trinità dei Monti 6","Rome","Italy",5,41.9063,12.4833,"+39 06 699 340","https://www.hotelhassler.com",5),
        h("Gran Meliá Rome","Striking contemporary hotel near the Colosseum with a rooftop pool and private gardens.","Via Vittorio Veneto 119","Rome","Italy",5,41.9076,12.4908,"+39 06 4700 1234","https://www.melia.com",1),

        // ── BARCELONA ──
        h("Hotel Arts Barcelona","40-storey Frank Gehry fish-sculpture landmark on Barceloneta beach.","Carrer de la Marina 19-21","Barcelona","Spain",5,41.3876,2.1965,"+34 93 221 1000","https://www.hotelartsbarcelona.com",8),
        h("W Barcelona","Sail-shaped skyscraper at the tip of La Barceloneta with a swim-up pool bar.","Pl. de la Rosa dels Vents 1","Barcelona","Spain",5,41.3776,2.1877,"+34 93 295 2800","https://www.marriott.com/w-hotels",3),
        h("Mandarin Oriental Barcelona","Former bank on Passeig de Gràcia with a rooftop infinity pool and celebrated spa.","Passeig de Gràcia 38-40","Barcelona","Spain",5,41.3934,2.1642,"+34 93 151 8888","https://www.mandarinoriental.com/barcelona",7),
        h("Hotel Majestic Barcelona","Classic grand hotel on Passeig de Gràcia open since 1918 with a roof pool.","Passeig de Gràcia 68","Barcelona","Spain",5,41.3956,2.1618,"+34 93 488 1717","https://www.hotelmajestic.es",6),
        h("The Barcelona Edition","Sleek modern tower in the Gothic Quarter with a celebrated rooftop bar and pool.","Avinguda de Francesc Cambó 14","Barcelona","Spain",5,41.3851,2.1736,"+34 93 626 8000","https://www.marriott.com/edition",9),

        // ── AMSTERDAM ──
        h("Waldorf Astoria Amsterdam","Six 17th-century canal houses fused into one ultra-luxurious property.","Herengracht 542-556","Amsterdam","Netherlands",5,52.3659,4.9015,"+31 20 718 4600","https://www.waldorfastoria.com/amsterdam",5),
        h("InterContinental Amstel Amsterdam","Grand 1867 palace on the Amstel river, visited by every crowned head in Europe.","Professor Tulpplein 1","Amsterdam","Netherlands",5,52.3583,4.9015,"+31 20 622 6060","https://www.ihg.com/intercontinental",6),
        h("Hotel Okura Amsterdam","Japanese-influenced skyscraper with a Michelin two-star restaurant and panoramic spa.","Ferdinand Bolstraat 333","Amsterdam","Netherlands",5,52.3494,4.8980,"+31 20 678 7111","https://www.okura.nl",4),
        h("W Amsterdam","Two historic buildings—a bank and a telephone exchange—reborn as a playful luxury hotel.","Spuistraat 175","Amsterdam","Netherlands",5,52.3730,4.8897,"+31 20 811 2100","https://www.marriott.com/w-hotels",1),
        h("Conservatorium Hotel","Neo-Gothic music conservatory converted into Amsterdam's most architecturally striking hotel.","Van Baerlestraat 27","Amsterdam","Netherlands",5,52.3578,4.8797,"+31 20 570 0000","https://www.conservatoriumhotel.com",9),

        // ── VIENNA ──
        h("Hotel Sacher Wien","Vienna's most celebrated hotel, home of the Original Sachertorte since 1876.","Philharmoniker Strasse 4","Vienna","Austria",5,48.2032,16.3700,"+43 1 51456","https://www.sacher.com",6),
        h("Hotel Imperial Vienna","Former palace of the Duke of Württemberg, now Austria's most imperial address.","Kärntner Ring 16","Vienna","Austria",5,48.2011,16.3727,"+43 1 501100","https://www.marriott.com/imperial",5),
        h("The Ritz-Carlton Vienna","Grand neoclassical property on the Ringstrasse with a rooftop bar and spa.","Schubertring 5-7","Vienna","Austria",5,48.2025,16.3762,"+43 1 311880","https://www.ritzcarlton.com/vienna",4),
        h("Palais Hansen Kempinski Vienna","Classicist landmark on the Ringstrasse with a celebrated rooftop pool and Viennese café.","Schottenring 24","Vienna","Austria",5,48.2168,16.3674,"+43 1 236100","https://www.kempinski.com/vienna",7),
        h("Grand Hotel Wien","Timeless Ringstrasse hotel with opulent interiors, open since 1870.","Kärntner Ring 9","Vienna","Austria",5,48.2021,16.3734,"+43 1 515800","https://www.grandhotelwien.com",9),

        // ── PRAGUE ──
        h("Four Seasons Hotel Prague","Riverside palazzo with Gothic, Baroque and neoclassical wings facing Charles Bridge.","Veleslavínova 2a","Prague","Czech Republic",5,50.0867,14.4148,"+420 221 427 000","https://www.fourseasons.com/prague",6),
        h("Mandarin Oriental Prague","Converted Dominican monastery in Malá Strana with a stunning spa in the former chapel.","Nebovidská 459/1","Prague","Czech Republic",5,50.0838,14.4047,"+420 233 088 888","https://www.mandarinoriental.com/prague",5),
        h("Hotel Aria Prague","Music-themed boutique hotel in Lesser Town with a rooftop garden and Vrtba Gardens access.","Tržiště 9","Prague","Czech Republic",5,50.0877,14.4022,"+420 225 334 111","https://www.ariahotel.net",9),
        h("The Emblem Hotel Prague","Design hotel steps from Old Town Square with individually themed rooms and a rooftop bar.","Platnéřská 110/19","Prague","Czech Republic",5,50.0879,14.4175,"+420 226 202 500","https://www.emblemprague.com",7),
        h("Alcron Hotel Prague","Restored Art Deco gem steps from Wenceslas Square with a celebrated seafood restaurant.","Štěpánská 40","Prague","Czech Republic",5,50.0793,14.4293,"+420 222 820 000","https://www.alcronhotel.cz",4),

        // ── ATHENS ──
        h("Hotel Grande Bretagne","Iconic 1874 palace facing the Acropolis and Syntagma Square, Athens's grandest address.","Vasileos Georgiou A 1","Athens","Greece",5,37.9754,23.7343,"+30 210 333 0000","https://www.grandebretagne.gr",6),
        h("King George Athens","Boutique palazzo next to Grande Bretagne with a celebrated rooftop Tudor Hall restaurant.","Vasileos Georgiou A 3","Athens","Greece",5,37.9752,23.7342,"+30 210 322 2210","https://www.marriott.com/king-george",5),
        h("Four Seasons Astir Palace Hotel Athens","Legendary seaside resort on the Athens Riviera with three private beaches and five pools.","40 Apollonos Street, Vouliagmeni","Athens","Greece",5,37.8097,23.7814,"+30 210 890 2000","https://www.fourseasons.com/athens",8),
        h("Electra Metropolis Athens","Rooftop-pool urban hotel with Acropolis views, steps from Monastiraki Square.","15 Mitropoleos Street","Athens","Greece",4,37.9745,23.7263,"+30 210 371 5000","https://www.electrahotels.gr",7),
        h("Erechtheus Hotel Athens","Charming boutique in Plaka with direct Acropolis views and a vine-covered courtyard.","Stratonos 1, Plaka","Athens","Greece",4,37.9718,23.7282,"+30 210 321 9805","https://www.hotelsathensgreece.com",2),

        // ── BUDAPEST ──
        h("Four Seasons Hotel Gresham Palace","Art Nouveau masterpiece on the Danube at the foot of Chain Bridge.","Széchenyi István tér 5-6","Budapest","Hungary",5,47.5004,19.0477,"+36 1 268 6000","https://www.fourseasons.com/budapest",6),
        h("Kempinski Hotel Corvinus Budapest","Elegant contemporary hotel on Erzsébet Square with a rooftop pool.","Erzsébet tér 7-8","Budapest","Hungary",5,47.4978,19.0554,"+36 1 429 3777","https://www.kempinski.com/budapest",4),
        h("Matild Palace Luxury Collection Hotel","Neo-Baroque palace on the Danube reborn as Budapest's newest five-star landmark.","Váci utca 36","Budapest","Hungary",5,47.4959,19.0536,"+36 1 550 5000","https://www.marriott.com/matild-palace",5),
        h("Corinthia Hotel Budapest","Grand 1896 palace with a spectacular marble atrium, restored to imperial splendour.","Erzsébet körút 43-49","Budapest","Hungary",5,47.5013,19.0604,"+36 1 479 4000","https://www.corinthia.com/budapest",9),
        h("Anantara New York Palace Budapest Hotel","Extravagant neo-Baroque New York Palace restored with a legendary gilded coffee house.","Erzsébet körút 9-11","Budapest","Hungary",5,47.4990,19.0630,"+36 1 886 6111","https://www.anantara.com/budapest",7),

        // ── LISBON ──
        h("Bairro Alto Hotel","Elegant boutique on Príncipe Real with a rooftop terrace and sweeping Tagus views.","Praça Luis de Camões 2","Lisbon","Portugal",5,38.7121,-9.1447,"+351 213 408 288","https://www.bairroaltohotel.com",7),
        h("Four Seasons Hotel Ritz Lisbon","Mid-century landmark on Eduardo VII Park with one of Lisbon's finest restaurants.","Rua Rodrigo da Fonseca 88","Lisbon","Portugal",5,38.7220,-9.1527,"+351 213 811 400","https://www.fourseasons.com/lisbon",6),
        h("Avenida Palace Hotel","Grand Victorian hotel at the end of Avenida da Liberdade, open since 1892.","Rua 1 de Dezembro 123","Lisbon","Portugal",5,38.7141,-9.1419,"+351 213 218 100","https://www.hotelavenidapalace.pt",5),
        h("Palácio do Governador","Stylish boutique in a 16th-century palace inside the historic Belém fortress.","Rua Bartolomeu Dias 117, Belém","Lisbon","Portugal",5,38.6970,-9.2059,"+351 213 624 900","https://www.palaciogovernador.com",9),
        h("Memmo Alfama Hotel","Whitewashed boutique carved into the hillside of the Alfama with a pool and Tagus panorama.","Travessa Merceeiras 27, Alfama","Lisbon","Portugal",4,38.7105,-9.1318,"+351 210 495 660","https://www.memmohotels.com",3),

        // ── SANTORINI ──
        h("Canaves Oia Epitome","Clifftop sanctuary in Oia with private plunge pools and celebrated caldera dining.","Oia","Santorini","Greece",5,36.4618,25.3754,"+30 22860 71453","https://www.canaves.com",3),
        h("Grace Santorini","Glamorous boutique perched above Imerovigli with an infinity pool and helipad.","Imerovigli","Santorini","Greece",5,36.4336,25.4224,"+30 22860 25090","https://www.gracehotels.com/santorini",0),
        h("Mystique Resort Santorini","Cliffside cave suites in Oia carved into the rock with a spectacular infinity pool.","Oia","Santorini","Greece",5,36.4619,25.3747,"+30 22860 71114","https://www.mystique.gr",2),
        h("Andronis Luxury Suites","Suites with private heated pools and unobstructed caldera views high above Oia.","Oia","Santorini","Greece",5,36.4601,25.3762,"+30 22860 72041","https://www.andronísluxury.com",9),
        h("Vedema Resort Santorini","Medieval village estate in Megalochori with a historic winery and two pools.","Megalochori","Santorini","Greece",5,36.3957,25.4250,"+30 22860 81796","https://www.vedema.gr",1),

        // ── FLORENCE ──
        h("Four Seasons Hotel Firenze","Former Medici chapel and palace set in 11 acres of private botanical gardens.","Borgo Pinti 99","Florence","Italy",5,43.7803,11.2659,"+39 055 26261","https://www.fourseasons.com/florence",2),
        h("Portrait Firenze","Sleek Ferragamo-owned boutique on the Arno with a rooftop bar and views of Ponte Vecchio.","Lungarno Acciaiuoli 4","Florence","Italy",5,43.7684,11.2535,"+39 055 27268000","https://www.lungarnocollection.com",7),
        h("Hotel Savoy Florence","Classic Piazza della Repubblica property with refined Florentine style and a rooftop pool.","Piazza della Repubblica 7","Florence","Italy",5,43.7710,11.2544,"+39 055 27351","https://www.roccofortehotels.com/hotel-savoy",5),
        h("Belmond Villa San Michele","15th-century monastery with a façade designed by Michelangelo above Florence in Fiesole.","Via Doccia 4, Fiesole","Florence","Italy",5,43.8050,11.2939,"+39 055 5678200","https://www.belmond.com/villa-san-michele",6),
        h("Villa Cora Florence","Neo-classical Belle Époque villa with terraced gardens and a rooftop pool above the city.","Viale Machiavelli 18","Florence","Italy",5,43.7616,11.2562,"+39 055 228790","https://www.villacora.it",9),

        // ── VENICE ──
        h("Belmond Hotel Cipriani","Island hotel on Giudecca with a 20-metre Olympic pool and a free shuttle to San Marco.","Giudecca 10","Venice","Italy",5,45.4264,12.3322,"+39 041 240801","https://www.belmond.com/hotel-cipriani",0),
        h("The Gritti Palace","Grand 16th-century doge's palace on the Grand Canal with sumptuous rooms.","Campo Santa Maria del Giglio 2467","Venice","Italy",5,45.4318,12.3320,"+39 041 794611","https://www.thegrittipalace.com",6),
        h("Hotel Danieli","Three linked palaces including the 14th-century Palazzo Dandolo, a Venetian icon.","Riva degli Schiavoni 4196","Venice","Italy",5,45.4337,12.3412,"+39 041 5226480","https://www.hoteldanieli.com",5),
        h("Aman Venice","Palazzo Papadopoli — a 16th-century palace with original frescoes and a Grand Canal garden.","Calle Tiepolo 1364","Venice","Italy",5,45.4370,12.3244,"+39 041 2707333","https://www.aman.com/resorts/aman-venice",9),
        h("Bauer Palazzo","Iconic palazzo on the Grand Canal with gondola dock, rooftop terrace and modern annex.","San Marco 1413","Venice","Italy",5,45.4323,12.3313,"+39 041 5207022","https://www.bauerhotels.com",4),

        // ── EDINBURGH ──
        h("The Balmoral Hotel","Iconic clock tower above Waverley station; Edinburgh's most recognisable grande dame.","1 Princes Street","Edinburgh","United Kingdom",5,55.9519,-3.1887,"+44 131 556 2414","https://www.roccofortehotels.com/the-balmoral",6),
        h("Waldorf Astoria Edinburgh – The Caledonian","Edwardian railway hotel at the west end of Princes Street with a celebrated spa.","Princes Street","Edinburgh","United Kingdom",5,55.9497,-3.2042,"+44 131 222 8888","https://www.waldorfastoria.com/edinburgh",5),
        h("Prestonfield House","Flamboyant 17th-century country house within the city, famous for its peacocks and opulence.","Priestfield Road","Edinburgh","United Kingdom",5,55.9393,-3.1697,"+44 131 225 7800","https://www.prestonfield.com",2),
        h("The Scotsman Hotel","Former offices of The Scotsman newspaper reborn as a boutique hotel with a stainless-steel pool.","20 North Bridge","Edinburgh","United Kingdom",4,55.9503,-3.1876,"+44 131 556 5565","https://www.thescotsmanhotel.co.uk",9),
        h("InterContinental Edinburgh The George","Georgian townhouses on George Street in the New Town, elegantly restored.","19-21 George Street","Edinburgh","United Kingdom",5,55.9537,-3.2019,"+44 131 225 1251","https://www.ihg.com/intercontinental",4),

        // ── BERLIN ──
        h("Hotel Adlon Kempinski Berlin","Berlin's most legendary hotel, reconstructed beside the Brandenburg Gate.","Unter den Linden 77","Berlin","Germany",5,52.5168,13.3806,"+49 30 22610","https://www.kempinski.com/berlin",6),
        h("Waldorf Astoria Berlin","Sleek glass high-rise inspired by the original NYC Waldorf, at the Kurfürstendamm.","Hardenbergstrasse 28","Berlin","Germany",5,52.5061,13.3343,"+49 30 814000","https://www.waldorfastoria.com/berlin",7),
        h("The Ritz-Carlton Berlin","Contemporary grandeur at Potsdamer Platz with a rooftop pool and landmark Lounge bar.","Potsdamer Platz 3","Berlin","Germany",5,52.5088,13.3758,"+49 30 33777","https://www.ritzcarlton.com/berlin",4),
        h("Regent Berlin","Quietly luxurious hotel on Gendarmenmarkt, Berlin's most beautiful square.","Charlottenstrasse 49","Berlin","Germany",5,52.5136,13.3912,"+49 30 20338","https://www.ihg.com/regent",5),
        h("Das Stue Berlin","Intimate design hotel in a 1930s embassy building overlooking the Berlin Zoo.","Drakestrasse 1","Berlin","Germany",5,52.5113,13.3352,"+49 30 31172220","https://www.das-stue.com",9),

        // ── ZURICH ──
        h("Baur au Lac Zurich","Timeless lakeside palace since 1844 with a private park and Michelin two-star dining.","Talstrasse 1","Zurich","Switzerland",5,47.3661,8.5388,"+41 44 220 5020","https://www.bauraulac.ch",6),
        h("The Dolder Grand","Belle Époque hilltop resort expanded by Norman Foster, with an ice rink and 4000 m² spa.","Kurhausstrasse 65","Zurich","Switzerland",5,47.3742,8.5802,"+41 44 456 6000","https://www.thedoldergrand.com",11),
        h("Park Hyatt Zurich","Contemporary urban hotel near Bahnhofstrasse with a signature rooftop bar.","Beethoven-strasse 21","Zurich","Switzerland",5,47.3686,8.5383,"+41 44 285 1234","https://www.hyatt.com",7),
        h("Hotel Widder Zurich","Nine medieval townhouses in the Old Town woven together into one design hotel.","Rennweg 7","Zurich","Switzerland",5,47.3734,8.5382,"+41 44 224 2526","https://www.widderhotel.com",9),
        h("Mandarin Oriental Zurich","Sleek new arrival on Bahnhofstrasse with an indoor rooftop pool and champagne bar.","Talstrasse 65","Zurich","Switzerland",5,47.3672,8.5395,"+41 44 818 8888","https://www.mandarinoriental.com/zurich",3),

        // ── COPENHAGEN ──
        h("Hotel d'Angleterre","Denmark's grandest hotel, open since 1755, facing Kongens Nytorv with a rooftop spa.","Kongens Nytorv 34","Copenhagen","Denmark",5,55.6789,12.5854,"+45 33 12 0095","https://www.dangleterre.com",6),
        h("Nimb Hotel Copenhagen","Moorish fantasy palace inside Tivoli Gardens with a heavenly spa and rooftop pool.","Bernstorffsgade 5","Copenhagen","Denmark",5,55.6737,12.5682,"+45 88 70 0000","https://www.nimb.dk",5),
        h("Villa Copenhagen","Grand Central Post Office converted into a striking luxury hotel with a stunning pool.","Tietgensgade 35","Copenhagen","Denmark",5,55.6724,12.5673,"+45 78 74 2000","https://www.villacopenhagen.com",0),
        h("Nobis Hotel Copenhagen","Neoclassical Royal Danish Music Conservatory reimagined as a sophisticated design hotel.","Niels Brocks Gade 1","Copenhagen","Denmark",5,55.6727,12.5697,"+45 70 22 8000","https://www.nobishotel.com",9),
        h("Radisson Collection Royal Hotel Copenhagen","Arne Jacobsen's 1960 design icon — every detail, including the famous Egg Chair, is original.","Hammerichsgade 1","Copenhagen","Denmark",4,55.6726,12.5700,"+45 33 42 6000","https://www.radissonhotels.com",4),

        // ── DUBLIN ──
        h("The Merrion Hotel","Four Georgian townhouses overlooking the garden of Number 29 in Georgian Dublin.","Upper Merrion Street","Dublin","Ireland",5,53.3399,-6.2542,"+353 1 603 0600","https://www.merrionhotel.com",6),
        h("Shelbourne Hotel Dublin","Landmark 1824 hotel where the Irish Constitution was drafted, facing St. Stephen's Green.","27 St. Stephen's Green","Dublin","Ireland",5,53.3382,-6.2579,"+353 1 663 4500","https://www.theshelbourne.com",5),
        h("The Westbury Hotel Dublin","Elegant contemporary hotel in the heart of Grafton Street's shopping district.","Grafton Street","Dublin","Ireland",5,53.3404,-6.2613,"+353 1 679 1122","https://www.doylecollection.com/westbury",4),
        h("InterContinental Dublin","Grand manor house in Ballsbridge with landscaped gardens, an indoor pool and full spa.","Simmonscourt Road, Ballsbridge","Dublin","Ireland",5,53.3256,-6.2343,"+353 1 665 4000","https://www.ihg.com/intercontinental",9),
        h("The Fitzwilliam Hotel Dublin","Sleek contemporary hotel on St. Stephen's Green with a rooftop garden and penthouse spa.","St. Stephen's Green","Dublin","Ireland",4,53.3393,-6.2589,"+353 1 478 7000","https://www.fitzwilliamhoteldublin.com",7),

        // ── PORTO ──
        h("Yeatman Hotel Porto","Quintessential wine hotel with panoramic Douro River terraces and a 70-treatment spa.","Rua do Choupelo 250, Vila Nova de Gaia","Porto","Portugal",5,41.1365,-8.6148,"+351 220 133 100","https://www.theyeatman.com",7),
        h("Hotel Infante Sagres Porto","1951 Art Deco palace in the city centre with handmade azulejo panels and carved wood.","Praça Dona Filipa de Lencastre 62","Porto","Portugal",5,41.1465,-8.6149,"+351 223 398 500","https://www.hotelinfantesagres.pt",5),
        h("Torel Avantgarde Porto","Cliffside boutique with an infinity pool over the city and individualistic design rooms.","Rua de Camões 36","Porto","Portugal",5,41.1413,-8.6065,"+351 220 109 420","https://www.torelavantgarde.com",3),
        h("Belmondo Porto","Restored 1930s building near Clérigos Tower with a rooftop pool and Douro views.","Rua Arquitecto Nicolau Nasoni 10","Porto","Portugal",5,41.1444,-8.6149,"+351 223 402 900","https://www.belmond.com/porto",9),
        h("The House of Sandeman Hostel & Suites","Heritage wine cellar suites in the Gaia waterfront with tasting room access.","Largo Miguel Bombarda 3","Porto","Portugal",4,41.1387,-8.6184,"+351 223 740 533","https://www.sandeman.com",2),

        // ── DUBROVNIK ──
        h("Villa Dubrovnik","Clifftop Adriatic villa with a private boat shuttle to the Old Town and spectacular sea vistas.","Vlaha Bukovca 6","Dubrovnik","Croatia",5,42.6442,18.1102,"+385 20 500 300","https://www.villa-dubrovnik.hr",8),
        h("Hotel Excelsior Dubrovnik","1913 Art Nouveau landmark with direct sea access and views of the illuminated Old Town walls.","Frana Supila 12","Dubrovnik","Croatia",5,42.6470,18.1127,"+385 20 353 353","https://www.adriaticluxuryhotels.com",0),
        h("Bellevue Hotel Dubrovnik","Modernist cliffside hotel carved into the rock with a private pebble beach.","Pera Čingrije 7","Dubrovnik","Croatia",5,42.6473,18.0885,"+385 20 330 000","https://www.adriaticluxuryhotels.com",2),
        h("Rixos Premium Dubrovnik","Contemporary clifftop all-inclusive with a cascading infinity pool over the Adriatic.","Liechtensteinov Put 3","Dubrovnik","Croatia",5,42.6369,18.0692,"+385 20 638 000","https://www.rixos.com",3),
        h("Hotel Kompas Dubrovnik","Mid-century classic in Lapad Bay with sea views, beach access and a family pool.","Šetalište kralja Zvonimira 56","Dubrovnik","Croatia",4,42.6565,18.0769,"+385 20 352 000","https://www.adriaticluxuryhotels.com",1),

        // ── TOKYO ──
        h("The Peninsula Tokyo","Refined Ginza landmark with butler service, helicopad and the best afternoon tea in Japan.","1-8-1 Yurakucho, Chiyoda","Tokyo","Japan",5,35.6746,139.7602,"+81 3 6270 2888","https://www.peninsula.com/tokyo",4),
        h("Mandarin Oriental Tokyo","Twin towers above Nihonbashi with a 38th-floor spa, two Michelin restaurants and Fuji views.","2-1-1 Nihonbashi Muromachi, Chuo","Tokyo","Japan",5,35.6860,139.7744,"+81 3 3270 8800","https://www.mandarinoriental.com/tokyo",7),
        h("Park Hyatt Tokyo","Lost in Translation icon above Shinjuku with a 47th-floor indoor pool and New York Grill.","3-7-1-2 Nishi Shinjuku","Tokyo","Japan",5,35.6869,139.6905,"+81 3 5322 1234","https://www.hyatt.com",3),
        h("Aman Tokyo","Minimalist urban sanctuary of stone, wood and paper towering over the Imperial Palace.","The Otemachi Tower, 1-5-6 Otemachi","Tokyo","Japan",5,35.6896,139.7636,"+81 3 5224 3333","https://www.aman.com/resorts/aman-tokyo",9),
        h("Four Seasons Hotel Tokyo at Marunouchi","Intimate 57-room boutique inside Pacific Century Place near Tokyo Station.","Pacific Century Place, 1-11-1 Marunouchi","Tokyo","Japan",5,35.6800,139.7671,"+81 3 5222 7222","https://www.fourseasons.com/tokyo",1),

        // ── KYOTO ──
        h("Aman Kyoto","Hidden forest retreat built around a stone garden first designed in 1000 AD.","Okitayama Washimine-cho, Kita","Kyoto","Japan",5,35.0723,135.7479,"+81 75 496 6666","https://www.aman.com/resorts/aman-kyoto",11),
        h("The Ritz-Carlton Kyoto","Inspired by traditional machiya townhouses, beside the Kamogawa River.","Kamogawa Nijo-Ohashi Hotori","Kyoto","Japan",5,35.0168,135.7743,"+81 75 746 5555","https://www.ritzcarlton.com/kyoto",5),
        h("Four Seasons Hotel Kyoto","Contemporary design in a walled garden estate in the Higashiyama hills.","445-3 Myohoin Maekawa-cho, Higashiyama","Kyoto","Japan",5,34.9993,135.7744,"+81 75 541 8288","https://www.fourseasons.com/kyoto",2),
        h("Suiran Kyoto","Luxury ryokan-influenced retreat in Sagano's bamboo grove beside Togetsukyo Bridge.","12 Susukinobaba-cho, Saga Tenryuji, Ukyo","Kyoto","Japan",5,35.0147,135.6776,"+81 75 872 0101","https://www.suirankyoto.com",6),
        h("Hoshinoya Kyoto","Ryokan accessible only by boat along the Oi River, deep in the Arashiyama mountains.","Genrokuzan, Nishikyo","Kyoto","Japan",5,35.0105,135.6718,"+81 50 3786 1144","https://www.hoshinoya.com/kyoto",11),

        // ── SINGAPORE ──
        h("Marina Bay Sands","Three-tower icon crowned by the SkyPark infinity pool 200 m above Singapore Bay.","10 Bayfront Avenue","Singapore","Singapore",5,1.2834,103.8607,"+65 6688 8868","https://www.marinabaysands.com",3),
        h("Raffles Hotel Singapore","Colonial grande dame since 1887, restored to perfection, birthplace of the Singapore Sling.","1 Beach Road","Singapore","Singapore",5,1.2951,103.8522,"+65 6337 1886","https://www.raffles.com/singapore",6),
        h("The Fullerton Hotel Singapore","Former General Post Office turned heritage hotel at the mouth of the Singapore River.","1 Fullerton Square","Singapore","Singapore",5,1.2865,103.8522,"+65 6733 8388","https://www.fullertonhotels.com",5),
        h("Capella Singapore","Restored colonial barracks in a rainforest on Sentosa Island with a 30-metre pool.","1 The Knolls, Sentosa Island","Singapore","Singapore",5,1.2479,103.8237,"+65 6591 5000","https://www.capellahotels.com/singapore",0),
        h("Four Seasons Hotel Singapore","Garden-city retreat in the Orchard Road belt with a celebrated pool terrace.","190 Orchard Boulevard","Singapore","Singapore",5,1.3014,103.8297,"+65 6734 1110","https://www.fourseasons.com/singapore",2),

        // ── BANGKOK ──
        h("Mandarin Oriental Bangkok","The oldest and most celebrated hotel in Southeast Asia, open since 1879 on the Chao Phraya.","48 Oriental Avenue, Bang Rak","Bangkok","Thailand",5,13.7226,100.5148,"+66 2 659 9000","https://www.mandarinoriental.com/bangkok",8),
        h("The Peninsula Bangkok","Thai-inspired tower on the west bank with riverside pool and private boats to the BTS.","333 Charoennakorn Road, Khlong San","Bangkok","Thailand",5,13.7249,100.5122,"+66 2 020 2888","https://www.peninsula.com/bangkok",3),
        h("Capella Bangkok","Stunning new riverside resort with 101 pool suites overlooking the Chao Phraya.","300-302 Charoenkrung Road, Bang Rak","Bangkok","Thailand",5,13.7190,100.5152,"+66 2 098 3888","https://www.capellahotels.com/bangkok",0),
        h("Four Seasons Hotel Bangkok at Chao Phraya River","Tropical resort on the Chao Phraya featuring seven pools and a private pier.","300/1 Charoenkrung Road, Bang Rak","Bangkok","Thailand",5,13.7197,100.5150,"+66 2 032 0888","https://www.fourseasons.com/bangkok",2),
        h("Rosewood Bangkok","Slender tower in Ploenchit with sky-high infinity pool, spa and panoramic Loy Krathong dining.","1041/38 Phloen Chit Road, Lumpini","Bangkok","Thailand",5,13.7437,100.5505,"+66 2 080 0088","https://www.rosewoodhotels.com/bangkok",7),

        // ── BALI ──
        h("Four Seasons Resort Bali at Sayan","Jungle lodge above the Ayung River gorge; approach on a suspension bridge.","Sayan, Ubud","Bali","Indonesia",5,-8.4869,115.2542,"+62 361 977577","https://www.fourseasons.com/balisayan",11),
        h("Amandari Bali","Original Aman resort in Ubud, built as a Balinese village above the Ayung gorge.","Kedewatan, Ubud","Bali","Indonesia",5,-8.4728,115.2482,"+62 361 975333","https://www.aman.com/resorts/amandari",2),
        h("COMO Shambhala Estate Bali","Holistic wellness retreat deep in the jungle above Ubud with spring-water pools.","Banjar Begawan, Ubud","Bali","Indonesia",5,-8.4520,115.2769,"+62 361 978888","https://www.comohotels.com/como-shambhala",11),
        h("Bulgari Resort Bali","Clifftop retreat above the Indian Ocean on the Uluwatu headland with a dramatic pool.","Jl. Goa Lempeh, Uluwatu","Bali","Indonesia",5,-8.8337,115.0872,"+62 361 847 1000","https://www.bulgarihotels.com/bali",3),
        h("Alila Villas Uluwatu","Honeycomb-architecture cliff-edge resort above the crashing waves of Uluwatu.","Jl. Belimbing Sari, Pecatu","Bali","Indonesia",5,-8.8290,115.0846,"+62 361 848 2166","https://www.alilahotels.com/uluwatu",0),

        // ── SEOUL ──
        h("The Shilla Seoul","Flagship of Korea's most celebrated luxury hotel brand, set in a pine-forested hillside.","249 Dongho-ro, Jung-gu","Seoul","South Korea",5,37.5537,127.0061,"+82 2 2233 3131","https://www.shilla.net",5),
        h("Lotte Hotel Seoul","Iconic twin tower in Myeongdong with a 38th-floor spa and South Korea's best shopping.","30 Eulji-ro, Jung-gu","Seoul","South Korea",5,37.5650,126.9815,"+82 2 771 1000","https://www.lottehotel.com",4),
        h("Four Seasons Hotel Seoul","Glass tower in Gwanghwamun with a rooftop infinity pool and celebrated Michelin dining.","97 Saemunan-ro, Jongno-gu","Seoul","South Korea",5,37.5716,126.9756,"+82 2 6388 5000","https://www.fourseasons.com/seoul",7),
        h("Park Hyatt Seoul","Chic tower in the Gangnam business district with floor-to-ceiling Han River views.","606 Teheran-ro, Gangnam-gu","Seoul","South Korea",5,37.5040,127.0508,"+82 2 2016 1234","https://www.hyatt.com",1),
        h("Banyan Tree Club & Spa Seoul","Mountain spa resort above the city with Korean-bath culture at its heart.","60 Jangchung-dong 2-ga, Jung-gu","Seoul","South Korea",5,37.5585,127.0107,"+82 2 2250 8100","https://www.banyantree.com/seoul",9),

        // ── MUMBAI ──
        h("The Taj Mahal Palace","India's most iconic hotel, open since 1903, with the Gateway of India as its backdrop.","Apollo Bunder, Colaba","Mumbai","India",5,18.9218,72.8331,"+91 22 6665 3366","https://www.tajhotels.com",6),
        h("Oberoi Mumbai","Glass tower on Marine Drive with a stunning infinity pool overlooking Back Bay.","Nariman Point","Mumbai","India",5,18.9321,72.8237,"+91 22 6632 5757","https://www.oberoihotels.com",3),
        h("Four Seasons Hotel Mumbai","Sky-reaching tower in Worli with a celebrated rooftop bar and panoramic sea views.","114 Dr E Moses Road, Worli","Mumbai","India",5,19.0057,72.8222,"+91 22 2481 8000","https://www.fourseasons.com/mumbai",7),
        h("The St. Regis Mumbai","India's tallest hotel in the Lower Parel business hub with signature butler service.","462 Senapati Bapat Marg","Mumbai","India",5,19.0009,72.8261,"+91 22 6162 8000","https://www.marriott.com/stregis",9),
        h("JW Marriott Mumbai Juhu","Beachfront resort on Juhu Beach with a luxurious pool, spa and celebrated dining.","Juhu Tara Road, Juhu","Mumbai","India",5,19.0944,72.8264,"+91 22 6693 3000","https://www.marriott.com/jw-marriott",8),

        // ── NEW DELHI ──
        h("The Oberoi New Delhi","Understated Lutyens Delhi luxury with meticulous service and a tranquil pool garden.","Dr Zakir Hussain Marg","New Delhi","India",5,28.5974,77.2277,"+91 11 2436 3030","https://www.oberoihotels.com",5),
        h("The Imperial New Delhi","Heritage 1931 hotel on Janpath lined with Kipling-era art and Mughal gardens.","Janpath","New Delhi","India",5,28.6285,77.2214,"+91 11 2334 1234","https://www.theimperialindia.com",6),
        h("Taj Mahal Hotel New Delhi","Flagship Taj property near India Gate with a famed pool terrace and spa.","1 Man Singh Road","New Delhi","India",5,28.6111,77.2288,"+91 11 6651 3131","https://www.tajhotels.com",4),
        h("The Leela Palace New Delhi","Neo-Lutyens palace in the Diplomatic Enclave with gold-leaf ceilings and a rooftop pool.","Diplomatic Enclave, Chanakyapuri","New Delhi","India",5,28.5999,77.2040,"+91 11 3933 1234","https://www.theleela.com",9),
        h("Aman New Delhi","Urban sanctuary inside a private haveli in the Lodhi colony with open-air spa villas.","Lodhi Road","New Delhi","India",5,28.5900,77.2258,"+91 11 4363 3333","https://www.aman.com/resorts/aman-new-delhi",2),

        // ── MALDIVES ──
        h("Soneva Fushi Maldives","Robinson Crusoe luxury — overwater and beach villas in a UNESCO biosphere island.","Kunfunadhoo Island, Baa Atoll","Maldives","Maldives",5,5.1079,72.9997,"+960 660 0304","https://www.soneva.com/soneva-fushi",8),
        h("Six Senses Laamu Maldives","Remote overwater villas in the least-visited atoll with an in-house marine biologist.","Olhuveli Island, Laamu Atoll","Maldives","Maldives",5,1.8667,73.5167,"+960 680 0600","https://www.sixsenses.com/laamu",0),
        h("One&Only Reethi Rah Maldives","Expansive private island with 130 villas, nine restaurants and 2.5 km of white beach.","North Malé Atoll","Maldives","Maldives",5,4.4167,73.3667,"+960 664 8800","https://www.oneandonlyresorts.com/reethi-rah",2),
        h("Gili Lankanfushi Maldives","Barefoot luxury with the world's longest overwater jetty and a 30-minute speedboat ride.","Lankanfushi Island, North Malé Atoll","Maldives","Maldives",5,4.3167,73.4167,"+960 664 0304","https://www.gili-lankanfushi.com",3),
        h("Cheval Blanc Randheli Maldives","LVMH's Maldives jewel with watercolour-palette villas and a Guerlain spa on the water.","Randheli Island, Noonu Atoll","Maldives","Maldives",5,5.9167,73.2667,"+960 656 1515","https://www.chevalblanc.com/randheli",9),

        // ── HONG KONG ──
        h("The Peninsula Hong Kong","The Grand Old Lady of the Far East since 1928; arrivals by helicopter or Rolls-Royce fleet.","Salisbury Road, Tsim Sha Tsui","Hong Kong","Hong Kong",5,22.2951,114.1720,"+852 2920 2888","https://www.peninsula.com/hong-kong",6),
        h("Four Seasons Hotel Hong Kong","Harbour-front tower at the International Finance Centre with a 55th-floor infinity pool.","8 Finance Street, Central","Hong Kong","Hong Kong",5,22.2872,114.1583,"+852 3196 8888","https://www.fourseasons.com/hongkong",7),
        h("Mandarin Oriental Hong Kong","Landmark on the Connaught Road harbour front, the original definition of Hong Kong luxury.","5 Connaught Road, Central","Hong Kong","Hong Kong",5,22.2808,114.1589,"+852 2522 0111","https://www.mandarinoriental.com/hong-kong",4),
        h("The Upper House Hong Kong","Minimalist sky sanctuary above Pacific Place with panoramic harbour views and free minibar.","88 Queensway, Admiralty","Hong Kong","Hong Kong",5,22.2784,114.1641,"+852 2918 1838","https://www.upperhouse.com",9),
        h("Rosewood Hong Kong","Dramatic curvilinear tower on the Kowloon waterfront with a rooftop pool and sky lounge.","18 Salisbury Road, Tsim Sha Tsui","Hong Kong","Hong Kong",5,22.2943,114.1730,"+852 3891 8888","https://www.rosewoodhotels.com/hong-kong",3),

        // ── NEW YORK ──
        h("The Plaza Hotel New York","Iconic Fifth Avenue château facing Central Park since 1907; America's most famous address.","768 Fifth Avenue","New York","United States",5,40.7646,-73.9743,"+1 212 759 3000","https://www.theplazany.com",6),
        h("The St. Regis New York","John Jacob Astor's 1904 masterpiece on Fifth Avenue with legendary butler service.","2 East 55th Street","New York","United States",5,40.7614,-73.9750,"+1 212 753 4500","https://www.marriott.com/stregis",5),
        h("Four Seasons Hotel New York","I.M. Pei–designed midtown tower with soaring ceilings and a celebrated spa.","57 East 57th Street","New York","United States",5,40.7620,-73.9699,"+1 212 758 5700","https://www.fourseasons.com/newyork",4),
        h("The Ritz-Carlton New York Central Park","Graceful tower overlooking Central Park South with afternoon tea and a full spa.","50 Central Park South","New York","United States",5,40.7657,-73.9783,"+1 212 308 9100","https://www.ritzcarlton.com/new-york",7),
        h("Mandarin Oriental New York","Rooms from the 35th floor up with wraparound Central Park and Hudson River views.","80 Columbus Circle","New York","United States",5,40.7689,-73.9831,"+1 212 805 8800","https://www.mandarinoriental.com/new-york",3),

        // ── MIAMI ──
        h("Faena Hotel Miami Beach","Lenny Kravitz–designed south-of-the-border fantasia with a gold Damien Hirst woolly mammoth.","3201 Collins Avenue, Miami Beach","Miami","United States",5,25.8076,-80.1218,"+1 305 534 8800","https://www.faena.com/miami-beach",1),
        h("Four Seasons Hotel at The Surf Club Miami","1930s Surf Club resurrected as a Richard Meier–designed masterpiece in Surfside.","9011 Collins Avenue, Surfside","Miami","United States",5,25.8810,-80.1218,"+1 305 381 3333","https://www.fourseasons.com/surfside",8),
        h("The Setai Miami Beach","Art Deco landmark combined with a contemporary Asian-minimalist tower and three pools.","2001 Collins Avenue","Miami","United States",5,25.7877,-80.1300,"+1 305 520 6000","https://www.thesetaihotel.com",0),
        h("1 Hotel South Beach","Eco-luxury on the beach with reclaimed wood, rooftop pools and a surf simulator.","2341 Collins Avenue","Miami","United States",5,25.7936,-80.1299,"+1 305 604 1000","https://www.1hotels.com/south-beach",2),
        h("Acqualina Resort & Residences","European-style grand resort on Sunny Isles Beach with three oceanfront pools.","17875 Collins Avenue, Sunny Isles","Miami","United States",5,25.9427,-80.1213,"+1 305 918 8000","https://www.acqualinaresort.com",3),

        // ── LAS VEGAS ──
        h("Bellagio Las Vegas","Iconic dancing fountain resort on the Strip with 8-acre lake and Cirque du Soleil.","3600 Las Vegas Blvd South","Las Vegas","United States",5,36.1126,-115.1767,"+1 702 693 7111","https://www.bellagio.com",6),
        h("Wynn Las Vegas","Steve Wynn's design masterpiece with a private golf course and over 18 restaurants.","3131 Las Vegas Blvd South","Las Vegas","United States",5,36.1270,-115.1681,"+1 702 770 7000","https://www.wynnlasvegas.com",5),
        h("ARIA Resort & Casino Las Vegas","Sleek LEED-Gold tower with floor-to-ceiling windows and a celebrated spa.","3730 Las Vegas Blvd South","Las Vegas","United States",5,36.1072,-115.1767,"+1 702 590 7757","https://www.aria.com",7),
        h("The Venetian Resort Las Vegas","Faithful recreation of Venice's Grand Canal with gondoliers and a 120,000 sq ft spa.","3355 Las Vegas Blvd South","Las Vegas","United States",5,36.1212,-115.1697,"+1 702 414 1000","https://www.venetianlasvegas.com",4),
        h("Four Seasons Hotel Las Vegas","Non-gaming oasis on the top floors of Mandalay Bay with a private pool terrace.","3960 Las Vegas Blvd South","Las Vegas","United States",5,36.0955,-115.1762,"+1 702 632 5000","https://www.fourseasons.com/lasvegas",9),

        // ── LOS ANGELES ──
        h("Shutters on the Beach","Cape Cod–style beachfront hotel with in-room fireplaces steps from the Santa Monica Pier.","1 Pico Blvd, Santa Monica","Los Angeles","United States",5,34.0100,-118.4966,"+1 310 458 0030","https://www.shuttersonthebeach.com",8),
        h("Beverly Hills Hotel","The Pink Palace — legendary bungalow resort surrounded by tropical gardens since 1912.","9641 Sunset Blvd, Beverly Hills","Los Angeles","United States",5,34.0817,-118.4148,"+1 310 276 2251","https://www.beverlyhillshotel.com",6),
        h("The Peninsula Beverly Hills","Understated perfection on Little Santa Monica with rooftop pool and the iconic Club Bar.","9882 Little Santa Monica Blvd","Los Angeles","United States",5,34.0694,-118.3997,"+1 310 551 2888","https://www.peninsula.com/beverly-hills",4),
        h("Chateau Marmont Los Angeles","Gothic castle above the Sunset Strip; legendary rock-and-roll hideaway since 1929.","8221 Sunset Blvd, West Hollywood","Los Angeles","United States",5,34.0930,-118.3795,"+1 323 656 1010","https://www.chateaumarmont.com",5),
        h("Hotel Bel-Air","Hidden canyon retreat in the hills with a swan lake, 12 acres of gardens and outdoor dining.","701 Stone Canyon Rd, Bel Air","Los Angeles","United States",5,34.0791,-118.4479,"+1 310 472 1211","https://www.dorchestercollection.com/hotel-bel-air",2),

        // ── TORONTO ──
        h("Four Seasons Hotel Toronto","Award-winning sky-garden tower in the upscale Yorkville neighbourhood.","60 Yorkville Avenue","Toronto","Canada",5,43.6712,-79.3927,"+1 416 964 0411","https://www.fourseasons.com/toronto",7),
        h("The Ritz-Carlton Toronto","Soaring tower on the entertainment district waterfront with a floor-high spa and pool.","181 Wellington Street West","Toronto","Canada",5,43.6466,-79.3862,"+1 416 585 2500","https://www.ritzcarlton.com/toronto",4),
        h("Hazelton Hotel Toronto","Intimate boutique in the heart of Yorkville with acclaimed ONE Restaurant and screening room.","118 Yorkville Avenue","Toronto","Canada",5,43.6707,-79.3932,"+1 416 963 6300","https://www.thehazeltonhotel.com",9),
        h("Shangri-La Hotel Toronto","Glass tower above the Financial District with a stunning indoor pool and Bosk restaurant.","188 University Avenue","Toronto","Canada",5,43.6519,-79.3866,"+1 647 788 8888","https://www.shangri-la.com/toronto",3),
        h("Fairmont Royal York Toronto","Grand railway hotel opened in 1929, once the tallest building in the British Empire.","100 Front Street West","Toronto","Canada",5,43.6466,-79.3818,"+1 416 368 2511","https://www.fairmont.com/royal-york",6),

        // ── RIO DE JANEIRO ──
        h("Belmond Copacabana Palace","Art Deco palace opened in 1923 by the Guinle family; Copacabana's crown jewel.","Avenida Atlântica 1702, Copacabana","Rio de Janeiro","Brazil",5,-22.9668,-43.1746,"+55 21 2548 7070","https://www.belmond.com/copacabana-palace",6),
        h("Hotel Santa Teresa Rio – MGallery","Colonial hillside estate with a mosaic pool and sweeping views over Guanabara Bay.","Rua Almirante Alexandrino 660, Santa Teresa","Rio de Janeiro","Brazil",5,-22.9221,-43.1744,"+55 21 3380 0200","https://www.hotelsantatereresort.com",5),
        h("Fasano Rio de Janeiro","Philippe Starck interior on Ipanema Beach with the city's most glamorous rooftop bar.","Avenida Vieira Souto 80, Ipanema","Rio de Janeiro","Brazil",5,-22.9855,-43.1993,"+55 21 3202 4000","https://www.fasano.com.br",7),
        h("Grand Hyatt Rio de Janeiro","Sleek tower in the Barra da Tijuca with multiple pools and a water park on site.","Avenida Lúcio Costa 9600, Barra da Tijuca","Rio de Janeiro","Brazil",5,-23.0110,-43.3658,"+55 21 2141 1234","https://www.hyatt.com",0),
        h("JW Marriott Hotel Rio de Janeiro","Towering seafront presence on Copacabana with a rooftop pool and panoramic Atlantic views.","Avenida Atlântica 2600, Copacabana","Rio de Janeiro","Brazil",5,-22.9737,-43.1821,"+55 21 2545 6500","https://www.marriott.com/jw-marriott",3),

        // ── BUENOS AIRES ──
        h("Alvear Palace Hotel Buenos Aires","French-palace landmark on Alvear Avenue, open since 1932 and ranked among Latin America's best.","Avenida Alvear 1891, Recoleta","Buenos Aires","Argentina",5,-34.5861,-58.3809,"+54 11 4808 2100","https://www.alvearpalace.com",6),
        h("Four Seasons Hotel Buenos Aires","Twin properties in Recoleta — a 1920s mansion and a modern tower sharing a pool garden.","Posadas 1086, Recoleta","Buenos Aires","Argentina",5,-34.5904,-58.3784,"+54 11 4321 1200","https://www.fourseasons.com/buenosaires",5),
        h("Palacio Duhau Park Hyatt Buenos Aires","Two Recoleta properties linked by a sunken garden: 1934 Belle Époque palace and modern tower.","Avenida Alvear 1661, Recoleta","Buenos Aires","Argentina",5,-34.5880,-58.3797,"+54 11 5171 1234","https://www.hyatt.com",4),
        h("Faena Hotel Buenos Aires","Donatella Versace–designed complex in the converted Puerto Madero warehouses.","Martha Salotti 445, Puerto Madero","Buenos Aires","Argentina",5,-34.6168,-58.3640,"+54 11 4010 9000","https://www.faena.com/buenos-aires",1),
        h("Belmond Casa Rosada Buenos Aires","Intimate boutique in the San Telmo arts neighbourhood with a rooftop pool and jazz nights.","Defensa 1338, San Telmo","Buenos Aires","Argentina",5,-34.6225,-58.3706,"+54 11 5252 5300","https://www.belmond.com",9),

        // ── MEXICO CITY ──
        h("Four Seasons Hotel Mexico City","Hacienda-style courtyard hotel on the Paseo de la Reforma with a celebrated spa.","Paseo de la Reforma 500, Juárez","Mexico City","Mexico",5,19.4299,-99.1663,"+52 55 5230 1818","https://www.fourseasons.com/mexico",2),
        h("Las Alcobas Mexico City","Intimate design hotel in Polanco with individually styled rooms and a rooftop pool.","Presidente Masaryk 390, Polanco","Mexico City","Mexico",5,19.4332,-99.1949,"+52 55 3300 3900","https://www.lasalcobas.com",9),
        h("St. Regis Mexico City","Slender tower above the Paseo de la Reforma with butler service and a panoramic rooftop.","Paseo de la Reforma 439","Mexico City","Mexico",5,19.4298,-99.1711,"+52 55 5228 1818","https://www.marriott.com/stregis",7),
        h("Camino Real Polanco Mexico City","Mid-century Legorreta–designed icon with primary-colour artworks and a private art museum.","Mariano Escobedo 700, Anzures","Mexico City","Mexico",5,19.4362,-99.1799,"+52 55 5263 8888","https://www.caminoreal.com",5),
        h("Habita Hotel Mexico City","Sleek Polanco rooftop-pool boutique, Mexico City's first design hotel since 2000.","Presidente Masaryk 201, Polanco","Mexico City","Mexico",5,19.4296,-99.1929,"+52 55 5282 3100","https://www.hotelhabita.com",4),

        // ── CANCUN ──
        h("Nizuc Resort & Spa Cancun","Secluded adults-only resort on its own peninsula with butler villas and six restaurants.","Carretera Cancun-Tulum km 4.5","Cancun","Mexico",5,21.0435,-86.7751,"+52 998 891 5800","https://www.nizuc.com",8),
        h("Ritz-Carlton Cancun","AAA Five Diamond resort with an infinity pool steps from the Caribbean and acclaimed dining.","Retorno del Rey 36, Zona Hotelera","Cancun","Mexico",5,21.1267,-86.7633,"+52 998 881 0808","https://www.ritzcarlton.com/cancun",0),
        h("Hyatt Ziva Cancun","All-inclusive on the northern tip of Hotel Zone with a rooftop pool overlooking two seas.","Boulevard Kukulcan km 4.5, Zona Hotelera","Cancun","Mexico",5,21.1521,-86.8302,"+52 998 848 7000","https://www.ziva.hyatt.com",3),
        h("Beloved Playa Mujeres","Adults-only all-butler resort north of Cancun with an overwater dock and swim-up suites.","Prolongacion Bonampak lote 1, Playa Mujeres","Cancun","Mexico",5,21.2613,-86.7836,"+52 998 872 8900","https://www.belovedhotels.com",2),
        h("Zoetry Villa Rolandi Isla Mujeres","Private island escape reachable only by the resort's catamaran, with butler villas.","Fracc Laguna Mar SM 75, Isla Mujeres","Cancun","Mexico",5,21.2398,-86.7298,"+52 998 999 2000","https://www.zoetryresorts.com",9),

        // ── VANCOUVER ──
        h("Fairmont Pacific Rim Vancouver","Coal Harbour waterfront flagship with a rooftop pool above the marina and mountains.","1038 Canada Place","Vancouver","Canada",5,49.2877,-123.1156,"+1 604 695 5300","https://www.fairmont.com/pacific-rim",3),
        h("Shangri-La Hotel Vancouver","Tallest hotel in British Columbia with a celebrated spa and a 24-hour butler suite.","1128 West Georgia Street","Vancouver","Canada",5,49.2849,-123.1218,"+1 604 689 1120","https://www.shangri-la.com/vancouver",7),
        h("Four Seasons Hotel Vancouver","Immaculate Yaletown hotel connected to the Pacific Centre mall with a rooftop pool.","791 West Georgia Street","Vancouver","Canada",5,49.2817,-123.1201,"+1 604 689 9333","https://www.fourseasons.com/vancouver",4),
        h("Hotel Georgia Vancouver","1927 Georgian-revival landmark restored with a buzzy rooftop pool bar above Robson Street.","801 West Georgia Street","Vancouver","Canada",5,49.2815,-123.1202,"+1 604 682 5566","https://www.hotelgeorgia.ca",6),
        h("Rosewood Hotel Georgia Vancouver","Heritage grande dame with a one-of-a-kind spa, rooftop pool and legendary Prohibition bar.","801 West Georgia Street","Vancouver","Canada",5,49.2813,-123.1199,"+1 604 673 7100","https://www.rosewoodhotels.com/vancouver",9),

        // ── CAPE TOWN ──
        h("Ellerman House Cape Town","Iconic Bantry Bay clifftop villa with private art collection, wine vault and plunge pools.","180 Kloof Road, Bantry Bay","Cape Town","South Africa",5,-33.9132,18.3820,"+27 21 430 3200","https://www.ellerman.co.za",8),
        h("One&Only Cape Town","Marina and island resort at the V&A Waterfront with direct mountain and harbour views.","Dock Road, V&A Waterfront","Cape Town","South Africa",5,-33.9023,18.4197,"+27 21 431 5888","https://www.oneandonlyresorts.com/cape-town",0),
        h("The Silo Hotel Cape Town","Grain silo converted into Cape Town's most dramatic boutique hotel above the Zeitz MOCAA.","Silo Square, V&A Waterfront","Cape Town","South Africa",5,-33.9043,18.4212,"+27 21 670 0500","https://www.theroyalportfolio.com/the-silo",7),
        h("Belmond Mount Nelson Hotel","Pink palace of the Cape — a colonial landmark in Gardens with nine acres of grounds.","76 Orange Street, Gardens","Cape Town","South Africa",5,-33.9296,18.4130,"+27 21 483 1000","https://www.belmond.com/mount-nelson",6),
        h("The Twelve Apostles Hotel and Spa","Clifftop retreat between the Apostles mountains and the Atlantic with a cinema spa.","Victoria Road, Camps Bay","Cape Town","South Africa",5,-33.9525,18.3723,"+27 21 437 9000","https://www.twelveapostles.co.za",11),

        // ── NAIROBI ──
        h("Giraffe Manor Nairobi","Boutique manor in Karen where Rothschild giraffes join you for breakfast.","Gogo Falls Road, Karen","Nairobi","Kenya",5,-1.3771,36.7253,"+254 20 891 0000","https://www.thesafaricollection.com/giraffe-manor",11),
        h("Hemingways Nairobi","Exclusive Karen estate with manicured gardens and an acclaimed spa set in Africa's garden suburb.","Mbagathi Ridge, Karen","Nairobi","Kenya",5,-1.3632,36.7247,"+254 709 074000","https://www.hemingways-nairobi.com",2),
        h("The Tribe Hotel Nairobi","Design hotel in Gigiri inspired by the 55 peoples of Kenya with an acclaimed pool terrace.","Village Market, Limuru Road","Nairobi","Kenya",5,-1.2317,36.8062,"+254 20 720 0000","https://www.tribe-hotel.com",1),
        h("Fairmont The Norfolk Nairobi","Nairobi's founding hotel since 1904 with safari-departure heritage and a celebrated spa.","Harry Thuku Road","Nairobi","Kenya",5,-1.2817,36.8183,"+254 20 226 5000","https://www.fairmont.com/norfolk-nairobi",5),
        h("Ole Sereni Hotel Nairobi","Nairobi National Park-facing boutique with a pool overlooking lions and giraffes from the city.","Mombasa Road","Nairobi","Kenya",4,-1.3095,36.8490,"+254 20 392 6000","https://www.olesereni.com",4),

        // ── ZANZIBAR ──
        h("Mnemba Island Lodge Zanzibar","Private coral island with 12 bandas, no shoes required and world-class house reef diving.","Mnemba Island, off Zanzibar North","Zanzibar","Tanzania",5,-5.8413,39.3727,"+255 24 223 3110","https://www.andbeyond.com/mnemba-island",8),
        h("Baraza Resort & Spa Zanzibar","All-suite Swahili palace on Bwejuu beach with a 75-metre pool and Frangipani Spa.","Bwejuu Beach, East Coast","Zanzibar","Tanzania",5,-6.2887,39.5400,"+255 24 224 0083","https://www.baraza-zanzibar.com",0),
        h("Zuri Zanzibar Hotel & Resort","Eco-luxury treetop villas in a mango grove on the north-west coast with a coral pool.","Kendwa Beach, Nungwi","Zanzibar","Tanzania",5,-5.7303,39.2817,"+255 24 223 0000","https://www.zurizanzibar.com",2),
        h("The Residence Zanzibar","Belle Époque villas on Kizimkazi in a protected marine park with butler service.","Kizimkazi, South West","Zanzibar","Tanzania",5,-6.4538,39.4629,"+255 24 223 2626","https://www.cenizaro.com/theresidencezanzibar",9),
        h("Matemwe Lodge Zanzibar","Clifftop bandas above a turquoise lagoon in unspoilt north-east Zanzibar.","Matemwe Beach, North East","Zanzibar","Tanzania",5,-5.9107,39.3993,"+255 24 2234 1066","https://www.asiliaafrica.com/matemwe",3),

        // ── SEYCHELLES ──
        h("Four Seasons Resort Seychelles","Hillside infinity-pool villas spilling through the jungle to Petite Anse beach on Mahé.","Petite Anse, Mahé","Seychelles","Seychelles",5,-4.7513,55.4827,"+248 4393 000","https://www.fourseasons.com/seychelles",0),
        h("North Island Seychelles","Eleven villas on a private island — the Robinson Crusoe escape perfected for celebrities.","North Island","Seychelles","Seychelles",5,-4.3971,55.2440,"+248 4293 100","https://www.north-island.com",8),
        h("Fregate Island Private Seychelles","Exclusive private island with 16 rock villas, giant tortoises and seven beaches.","Fregate Island","Seychelles","Seychelles",5,-4.5918,55.9449,"+248 4267 000","https://www.fregate.com",2),
        h("Constance Ephelia Seychelles","Twin bays on Port Launay — the largest resort in Seychelles with six pools.","Port Launay Marine National Park, Mahé","Seychelles","Seychelles",5,-4.6188,55.3907,"+248 4293 000","https://www.constancehotels.com/ephelia",3),
        h("Maia Luxury Resort & Spa Seychelles","Ultra-exclusive all-butler resort on Mahé with individual hilltop villas and plunge pools.","Anse Louis, Mahé","Seychelles","Seychelles",5,-4.6771,55.4216,"+248 4390 000","https://www.maia.com.sc",9),

        // ── SYDNEY ──
        h("Park Hyatt Sydney","Four-storey luxury directly under the Harbour Bridge with Opera House views from every room.","7 Hickson Road, The Rocks","Sydney","Australia",5,-33.8580,151.2086,"+61 2 9256 1234","https://www.hyatt.com",7),
        h("Four Seasons Hotel Sydney","Harbourside tower in The Rocks with a rooftop pool and panoramic bridge and bay vistas.","199 George Street","Sydney","Australia",5,-33.8618,151.2073,"+61 2 9238 0000","https://www.fourseasons.com/sydney",3),
        h("Quay Grand Suites Sydney","All-suite apartment-hotel on the East Circular Quay overlooking the Opera House and Harbour.","61 Macquarie Street, Circular Quay","Sydney","Australia",5,-33.8606,151.2118,"+61 2 9256 4000","https://www.quaywestsuites.com",0),
        h("Shangri-La Hotel Sydney","Landmark tower on Observatory Hill with the Altitude restaurant soaring above the harbour.","176 Cumberland Street, The Rocks","Sydney","Australia",5,-33.8622,151.2067,"+61 2 9250 6000","https://www.shangri-la.com/sydney",4),
        h("The Langham Sydney","Heritage bond store reborn as a five-star with a pool garden and celebrated Chuan Spa.","89-113 Kent Street","Sydney","Australia",5,-33.8676,151.2027,"+61 2 9256 2222","https://www.langhamhotels.com/sydney",6),

        // ── MELBOURNE ──
        h("The Langham Melbourne","Riverside landmark beside the Yarra with a pool, acclaimed Chuan Spa and eight restaurants.","1 Southgate Avenue, Southbank","Melbourne","Australia",5,-37.8220,144.9653,"+61 3 8696 8888","https://www.langhamhotels.com/melbourne",6),
        h("Park Hyatt Melbourne","Low-rise luxury in the Arts Precinct with suites overlooking St. Patrick's Cathedral.","1 Parliament Square, East Melbourne","Melbourne","Australia",5,-37.8126,144.9791,"+61 3 9224 1234","https://www.hyatt.com",5),
        h("Crown Towers Melbourne","Glamorous tower above Crown Casino with a full floor spa and the city's most lavish pool deck.","8 Whiteman Street, Southbank","Melbourne","Australia",5,-37.8239,144.9582,"+61 3 9292 6868","https://www.crowntowers.com.au",7),
        h("The Lyall Hotel Melbourne","Intimate all-suite boutique in South Yarra with a champagne bar and a rooftop garden retreat.","14 Murphy Street, South Yarra","Melbourne","Australia",5,-37.8384,144.9897,"+61 3 9868 8222","https://www.thelyall.com",9),
        h("Sofitel Melbourne on Collins","Blue-glass tower at the top of the Paris End with stunning arts district views.","25 Collins Street, Melbourne CBD","Melbourne","Australia",5,-37.8144,144.9711,"+61 3 9653 0000","https://www.sofitel.com/melbourne",1),

        // ── QUEENSTOWN ──
        h("Matakauri Lodge Queenstown","Mountain retreat on Lake Wakatipu with private hot tubs and Remarkables panoramas.","569 Glenorchy Road, Lake Wakatipu","Queenstown","New Zealand",5,-45.0186,168.6101,"+64 3 441 1008","https://www.matakauri.co.nz",11),
        h("Eichardt's Private Hotel Queenstown","Victorian lakefront manor in the heart of town with individually designed suites.","Marine Parade","Queenstown","New Zealand",5,-45.0328,168.6594,"+64 3 441 0450","https://www.eichardts.com",6),
        h("The Rees Hotel Queenstown","Lakefront apartment-hotel with access to a private beach on Lake Wakatipu.","377 Lake Esplanade","Queenstown","New Zealand",5,-45.0288,168.6537,"+64 3 450 1100","https://www.therees.co.nz",8),
        h("Blanket Bay Lodge Queenstown","Luxury lodge in a glacial valley at the head of Lake Wakatipu near Glenorchy.","Glenorchy, Lake Wakatipu","Queenstown","New Zealand",5,-44.8637,168.3897,"+64 3 442 9442","https://www.blanketbay.com",11),
        h("QT Queenstown","Playful design hotel in the heart of town with a vibrant rooftop bar and hot pools.","Marine Parade, Queenstown CBD","Queenstown","New Zealand",4,-45.0329,168.6602,"+64 3 450 0613","https://www.qthotels.com/queenstown",7),

        // ── MAURITIUS ──
        h("One&Only Le Saint Géran Mauritius","Iconic peninsula resort on Belle Mare Lagoon with an acclaimed spa and 36-hole golf.","Belle Mare, Flacq District","Mauritius","Mauritius",5,-20.1913,57.7786,"+230 401 1688","https://www.oneandonlyresorts.com/le-saint-geran",8),
        h("Four Seasons Resort Mauritius at Anahita","Private island retreat on a blue lagoon in the east coast with floating pavilions.","Beau Champ, East Coast","Mauritius","Mauritius",5,-20.2276,57.7914,"+230 402 3100","https://www.fourseasons.com/mauritius",0),
        h("Constance Prince Maurice Mauritius","Stilt villas over the lagoon in a protected mangrove reserve on the east coast.","Choisy Road, Poste de Flacq","Mauritius","Mauritius",5,-20.2000,57.7800,"+230 413 9100","https://www.constancehotels.com/prince-maurice",2),
        h("Royal Palm Beachcomber Luxury Mauritius","Legendary grand dame on Grand Baie with a private pier and a timeless Creole spa.","Grand Baie, Rivière du Rempart","Mauritius","Mauritius",5,-20.0161,57.5775,"+230 209 8300","https://www.beachcomber-hotels.com/royalpalm",3),
        h("LUX* Belle Mare Mauritius","Vibrant east-coast beachfront resort with a cool-chic restaurant scene and beach club.","Belle Mare, Flacq","Mauritius","Mauritius",5,-20.1854,57.7714,"+230 402 2000","https://www.luxresorts.com/belle-mare",9),

        // ── MOMBASA ──
        h("Alfajiri Cliff Villa Mombasa","Three exclusive clifftop villas above Diani Beach, each with a private pool and butler.","Diani Beach","Mombasa","Kenya",5,-4.3167,39.5833,"+254 40 320 2630","https://www.alfajirivillas.com",0),
        h("Hemingways Watamu Kenya","Intimate boutique on Watamu Marine National Park for diving and sportfishing aficionados.","Watamu Village, Watamu","Mombasa","Kenya",5,-3.3539,40.0014,"+254 42 232 0624","https://www.hemingways-watamu.com",8),
        h("The Majlis Mombasa","Swahili coast treasure on Manda Island — dhow arrivals and stilted teak villas in the mangroves.","Manda Island, Lamu Archipelago","Mombasa","Kenya",5,-2.2614,40.9261,"+254 42 463 3210","https://www.themajlisresorts.com",2),
        h("Diamonds Dream of Africa","Exclusive thatched resort on Malindi's white sand beach with a 70-metre pool.","Casuarina Road, Malindi","Mombasa","Kenya",5,-3.2244,40.1216,"+254 42 212 1234","https://www.diamantiresorts.com",3),
        h("Kinondo Kwetu Mombasa","Swedish-Kenyan eco-farm and beach house on Galu Beach with a lagoon pool and horses.","Galu Beach, Diani","Mombasa","Kenya",5,-4.3629,39.5721,"+254 40 320 3125","https://www.kinondo-kwetu.com",11),

        // ── PHUKET ──
        h("Amanpuri Phuket","Aman's original 1988 resort on Pansea Bay — Thai pavilions above a private beach.","Pansea Beach, Surin","Phuket","Thailand",5,7.9936,98.2711,"+66 76 324 333","https://www.aman.com/resorts/amanpuri",0),
        h("Trisara Phuket","Private-cove resort with 60 pool villas and a Marine Base conservation centre.","60/1 Moo 6 Srisoonthorn Road, Cherngtalay","Phuket","Thailand",5,8.0247,98.2978,"+66 76 310 100","https://www.trisara.com",2),
        h("Sri Panwa Phuket","Hilltop cape retreat with panoramic Andaman sunset views and a Cool Spa at the top.","88 Moo 8, Sakdidej Road, Cape Panwa","Phuket","Thailand",5,7.8005,98.3926,"+66 76 371 000","https://www.sripanwa.com",7),
        h("COMO Point Yamu Phuket","Italian-designed clifftop Andaman resort with a 25-metre pool and COMO Shambhala cuisine.","103/6 Moo 7 Paklok, Thalang","Phuket","Thailand",5,8.0438,98.4162,"+66 76 360 100","https://www.comohotels.com/pointyamu",3),
        h("Rosewood Phuket","Dramatic clifftop resort on Emerald Bay with three pools cascading to the beach.","88/28 Muen-Ngern Road, Patong","Phuket","Thailand",5,7.9014,98.2864,"+66 76 356 888","https://www.rosewoodhotels.com/phuket",8),

        // ── VERIFIED HOTEL PHOTOS FROM WIKIMEDIA COMMONS ──
        hp("Taj Mahal Palace Hotel","Mumbai's legendary waterfront palace hotel facing the Gateway of India.","Apollo Bandar, Colaba","Mumbai","India",5,18.92164,72.83317,"+91 22 6665 3366","https://taj.tajhotels.com/en-in/taj-mahal-palace-mumbai/","https://commons.wikimedia.org/wiki/Special:FilePath/Taj%20Mahal%20Palace%20Hotel.jpg"),
        hp("Castille Paris","Elegant Starhotels Collezione property near Place Vendome and Rue Saint-Honore.","33-37 Rue Cambon","Paris","France",5,48.8682694,2.3268049,"+33 1 44 58 44 58","https://collezione.starhotels.com/en/our-hotels/castille-paris/","https://commons.wikimedia.org/wiki/Special:FilePath/Exterior%20Castille%20Paris.jpg"),
        hp("L'Hotel Paris","Historic Left Bank boutique hotel on Rue des Beaux-Arts.","13 Rue des Beaux-Arts","Paris","France",5,48.85632497,2.335238699,"+33 1 44 41 99 00","http://www.l-hotel.com","https://commons.wikimedia.org/wiki/Special:FilePath/L%27H%C3%B4tel%2C%2013%20rue%20des%20Beaux-Arts%2C%20Paris%206e.jpg"),
        hp("Broadway Mansions Shanghai","Art Deco hotel tower near the Bund and Suzhou Creek.","20 North Suzhou Road","Shanghai","China",5,31.246338,121.485483,"+86 21 6324 6260","https://example.com/hotels/broadway-mansions-shanghai","https://commons.wikimedia.org/wiki/Special:FilePath/20112-Shanghai%2C%20Broadway%20Mansions.jpg"),
        hp("Park Hotel Shanghai","Historic landmark hotel facing People's Park in central Shanghai.","170 Nanjing West Road","Shanghai","China",5,31.235555555,121.467083333,"+86 21 6327 5225","https://example.com/hotels/park-hotel-shanghai","https://commons.wikimedia.org/wiki/Special:FilePath/Shanghai%20Park%20Hotel%202007.jpg"),
        hp("Bellagio Hotel and Casino","Lakefront Las Vegas resort famous for its fountains and conservatory.","3600 Las Vegas Blvd South","Las Vegas","United States",5,36.11201,-115.17534,"+1 702 693 7111","https://bellagio.mgmresorts.com/en.html","https://commons.wikimedia.org/wiki/Special:FilePath/Bellagio%20outside.jpg"),
        hp("Burj Al Arab Jumeirah","Sail-shaped Dubai icon on its own island off Jumeirah Beach.","Jumeirah Beach Road","Dubai","UAE",5,25.141388888,55.185277777,"+971 4 301 7777","https://www.jumeirah.com/en/stay/dubai/burj-al-arab-jumeirah","https://commons.wikimedia.org/wiki/Special:FilePath/Burj%20Al-Arab%20%2813996844503%29.jpg"),
        hp("Nobu Hotel Warsaw","Design-forward Nobu hotel in Warsaw's Srodmiescie district.","Wilcza 73","Warsaw","Poland",5,52.2231298,21.0081537,"+48 22 551 88 88","https://warsaw.nobuhotels.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20rialto%20z%20rozbudow%C4%85.jpg"),
        hp("The Rock Hotel Gibraltar","Classic hillside hotel overlooking Gibraltar and the bay.","3 Europa Road","Gibraltar","Gibraltar",4,36.1319,-5.35,"+350 200 73000","https://www.rockhotelgibraltar.com","https://commons.wikimedia.org/wiki/Special:FilePath/The%20Rock%20Hotel.jpg"),
        hp("Althoff Grandhotel Schloss Bensberg","Baroque castle hotel above Cologne with fine dining and palace gardens.","Kadettenstrasse","Bergisch Gladbach","Germany",5,50.9669,7.16222,"+49 2204 42 0","http://www.schlossbensberg.com/en/hotel-koeln","https://commons.wikimedia.org/wiki/Special:FilePath/Aerial%20image%20of%20the%20Bensberg%20Castle%20%28view%20from%20the%20southeast%29.jpg"),
        hp("Parador de Santiago de Compostela","Historic Hostal dos Reis Catolicos on Praza do Obradoiro.","Praza do Obradoiro 1","Santiago de Compostela","Spain",5,42.881388888,-8.545833333,"+34 981 58 22 00","https://www.parador.es","https://commons.wikimedia.org/wiki/Special:FilePath/Hostal%20dos%20Reis%20Cat%C3%B3licos.%20Praza%20do%20obradoiro.%20Santiago%20de%20Compostela.jpg"),
        hp("Tokyo Disneyland Hotel","Victorian-style Disney hotel at Tokyo Disney Resort.","29-1 Maihama","Urayasu","Japan",5,35.6368361,139.878159,"+81 47 305 3333","https://www.tokyodisneyresort.jp/en/hotel/dh.html","https://commons.wikimedia.org/wiki/Special:FilePath/Tokyo%20DisneyLand%20Hotel.jpg"),
        hp("Disney Ambassador Hotel","Art Deco Disney hotel beside Ikspiari at Tokyo Disney Resort.","2-11 Maihama","Urayasu","Japan",5,35.633,139.888,"+81 47 305 1111","http://www.disneyhotels.jp/en/dah/","https://commons.wikimedia.org/wiki/Special:FilePath/Disney%20ambassador%20hotel.jpg"),
        hp("Tokyo DisneySea Hotel MiraCosta","Luxury hotel built into Tokyo DisneySea's Mediterranean Harbor.","1-13 Maihama","Urayasu","Japan",5,35.6273756,139.8876269,"+81 47 305 2222","http://www.disneyhotels.jp/en/dhm/","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20MiraCosta%2C%20Porto%20Paradiso%20Side%2C%20Night.jpg"),
        hp("Hotel Elephant Weimar","Historic luxury hotel on Weimar's central market square.","Markt 19","Weimar","Germany",5,50.97888889,11.33027778,"+49 3643 8020","https://example.com/hotels/hotel-elephant-weimar","https://commons.wikimedia.org/wiki/Special:FilePath/Weimar%20Hotel%20Elephant.jpg"),
        hp("Hotel Polana Serena","Landmark colonial-era hotel overlooking Maputo Bay.","Avenida Julius Nyerere","Maputo","Mozambique",5,-25.969048,32.597245,"+258 21 241700","https://example.com/hotels/hotel-polana-serena","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20Polana.jpg"),
        hp("Waldorf Astoria Berlin","Luxury hotel in the Zoofenster tower near Kurfurstendamm.","Hardenbergstrasse 28","Berlin","Germany",5,52.505627,13.333392,"+49 30 8140000","https://www.hilton.com/en/hotels/berwawa-waldorf-astoria-berlin/","https://commons.wikimedia.org/wiki/Special:FilePath/2021-07-19%20Zoofenster%2001.jpg"),
        hp("Hotel Chelsea","Storied Manhattan hotel and arts landmark on West 23rd Street.","222 West 23rd Street","New York","United States",4,40.744444444,-73.996944444,"+1 212 483 1010","https://hotelchelsea.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Chelsea%20Manhattan%20Aug%202025%20102.jpg"),
        hp("Ocean Spray Hotel","Miami Beach Art Deco hotel on Collins Avenue.","4130 Collins Avenue","Miami Beach","United States",4,25.813671076,-80.123103393,"+1 305 535 5300","https://example.com/hotels/ocean-spray-hotel","https://commons.wikimedia.org/wiki/Special:FilePath/Miami%20Beach%20-%20Ocean%20Spray%20Hotel%2001.jpg"),
        hp("Fontainebleau Las Vegas","Modern luxury resort tower on the north Las Vegas Strip.","2777 Las Vegas Blvd South","Las Vegas","United States",5,36.1375,-115.158889,"+1 833 702 7272","https://www.fontainebleaulasvegas.com/","https://commons.wikimedia.org/wiki/Special:FilePath/South%20end%20of%20Fontainebleau%20Las%20Vegas%20%282024%29.png"),
        hp("Hotel Sacher Vienna","Historic luxury hotel beside the Vienna State Opera.","Philharmoniker Strasse 4","Vienna","Austria",5,48.203888888,16.369444444,"+43 1 51456","http://www.sacher.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20Sacher%20Vienna%20Sept%202006%20002.jpg"),
        hp("Vdara Hotel and Spa","Non-gaming all-suite hotel in the CityCenter complex.","2600 West Harmon Avenue","Las Vegas","United States",5,36.10943,-115.17808,"+1 866 745 7767","http://www.vdara.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Vdara%20-%20South%20-%202010-03-06.JPG"),
        hp("Hotel Hafnia","Central Torshavn hotel close to the harbor and old town.","Aarvegur 4-10","Torshavn","Faroe Islands",4,62.01,-6.77088889,"+298 313233","https://example.com/hotels/hotel-hafnia","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20Hafnia.JPG"),
        hp("Mnawi Basha Hotel","Prominent Basra hotel near the Shatt al-Arab waterfront.","Corniche Street","Basra","Iraq",4,30.506,47.8421,"+964 780 000 0000","https://example.com/hotels/mnawi-basha-hotel","https://commons.wikimedia.org/wiki/Special:FilePath/Mnawibashahotel.jpg"),
        hp("Abbasi Hotel","Historic caravanserai hotel in central Isfahan.","Amadegah Street","Isfahan","Iran",5,32.65152,51.670593,"+98 31 3222 6010","http://www.abbasihotel.ir/","https://commons.wikimedia.org/wiki/Special:FilePath/Abbasi%20Hotel.jpg"),
        hp("Schloss Elmau","Luxury mountain retreat in the Bavarian Alps.","In Elmau 2","Krün","Germany",5,47.462075414,11.186680031,"+49 8823 180","http://www.schloss-elmau.de/","https://commons.wikimedia.org/wiki/Special:FilePath/Schloss%20Elmau%20main%20entrance%20opt.jpg"),
        hp("The Adolphus Hotel","Historic Beaux-Arts luxury hotel in downtown Dallas.","1321 Commerce Street","Dallas","United States",5,32.779981,-96.799658,"+1 214 742 8200","http://www.adolphus.com","https://commons.wikimedia.org/wiki/Special:FilePath/Adolphus01.jpg"),
        hp("Westin Bonaventure Hotel","Iconic glass-tower hotel in downtown Los Angeles.","404 South Figueroa Street","Los Angeles","United States",4,34.05279,-118.25646,"+1 213 624 1000","https://bonaventurehotel.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Westin%20Bonaventure%20Hotel.jpg"),
        hp("Club Hotel Casapueblo","Cliffside hotel and art landmark near Punta Ballena.","Punta Ballena","Maldonado","Uruguay",4,-34.908869,-55.044906,"+598 4257 8611","https://www.clubhotelcasapueblo.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Casapueblo.JPG"),
        hp("Fairmont Peace Hotel","Historic Art Deco hotel on Shanghai's Bund.","20 Nanjing East Road","Shanghai","China",5,31.2411,121.4851,"+86 21 6138 6888","http://www.fairmont.com/peacehotel","https://commons.wikimedia.org/wiki/Special:FilePath/Sasson%20House%20The%20Bund.JPG"),
        hp("Many Glacier Hotel","Swiss chalet-style lodge inside Glacier National Park.","Many Glacier Road","Glacier County","United States",4,48.796758,-113.657802,"+1 855 733 4522","http://www.glaciernationalparklodges.com/lodging/many-glacier-hotel/","https://commons.wikimedia.org/wiki/Special:FilePath/Many%20glacier%20hotel.jpg"),
        hp("Ai-Ais Hot Springs Resort","Desert hot springs resort near Fish River Canyon.","Ai-Ais Richtersveld Transfrontier Park","Karas Region","Namibia",4,-27.92861,17.48333,"+264 63 683 606","http://www.nwr.com.na","https://commons.wikimedia.org/wiki/Special:FilePath/Ai%20Ais%20SPA.jpg"),
        hp("Gran Hotel Puente Colgante","Riverside hotel beside the Vizcaya Bridge in Portugalete.","Maria Diaz de Haro 2","Portugalete","Spain",4,43.322057,-3.017564,"+34 944 014 000","http://www.granhotelpuentecolgante.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Portugalete%20-%20Gran%20Hotel%20Puente%20Colgante%205.jpg"),
        hp("Metropol Palace Belgrade","Historic luxury hotel facing Tasmajdan Park.","Bulevar kralja Aleksandra 69","Belgrade","Serbia",5,44.806667,20.473889,"+381 11 3333 100","http://www.metropolpalace.com","https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20Metropol%20Belgrade%20Tasmajdan.JPG"),
        hp("Royal Hawaiian Hotel","The Pink Palace of the Pacific on Waikiki Beach.","2259 Kalakaua Avenue","Honolulu","United States",5,21.2774,-157.829,"+1 808 923 7311","https://www.royal-hawaiian.com/","https://commons.wikimedia.org/wiki/Special:FilePath/The%20Royal%20Hawaiian%20%282024%29-L1004805.jpg"),
        hp("Alvear Palace Hotel","French-style luxury landmark in Recoleta.","Avenida Alvear 1891","Buenos Aires","Argentina",5,-34.58769444,-58.38880556,"+54 11 4808 2100","http://www.alvearpalace.com","https://commons.wikimedia.org/wiki/Special:FilePath/Buenos%20Aires%20-%20Avenida%20Alvear%20-%2020090104-r.jpg"),
        hp("Hotel Nassauer Hof","Grand luxury hotel on Wiesbaden's Bowling Green.","Kaiser-Friedrich-Platz 3-4","Wiesbaden","Germany",5,50.08472,8.24367,"+49 611 1330","http://www.nassauer-hof.de/en/home","https://commons.wikimedia.org/wiki/Special:FilePath/Nassauer%20Hof%20from%20Bowling%20Green.JPG"),
        hp("The Greenbrier","Historic resort in the Allegheny Mountains of West Virginia.","101 Main Street West","White Sulphur Springs","United States",5,37.7854,-80.3083,"+1 855 453 4858","http://www.greenbrier.com/","https://commons.wikimedia.org/wiki/Special:FilePath/2008-0831-TheGreenbrier-North.jpg"),
        hp("InterContinental Amstel Amsterdam","Grand riverside hotel on the Amstel since 1867.","Professor Tulpplein 1","Amsterdam","Netherlands",5,52.3600068,4.9051643,"+31 20 622 6060","http://amsterdam.intercontinental.com","https://commons.wikimedia.org/wiki/Special:FilePath/Amstel%20Hotel%202034.jpg"),
        hp("Mandarin Oriental Singapore","Luxury marina hotel beside Marina Square and Marina Bay.","5 Raffles Avenue","Singapore","Singapore",5,1.290833,103.858056,"+65 6338 0066","https://www.mandarinoriental.com/en/singapore/marina-bay","https://commons.wikimedia.org/wiki/Special:FilePath/Singapore%20Mandarin-Oriental-01.jpg"),
        hp("Schlosshotel Kronberg","Castle hotel in the Taunus hills near Frankfurt.","Hainstrasse 25","Kronberg im Taunus","Germany",5,50.18888889,8.51,"+49 6173 70101","https://schlosshotel-kronberg.com/","https://commons.wikimedia.org/wiki/Special:FilePath/Schlosshotel-kronberg002.jpg"),
        hp("Hilton Buenos Aires","Puerto Madero hotel with a glass atrium and riverside setting.","Macacha Guemes 351","Buenos Aires","Argentina",5,-34.605833,-58.363611,"+54 11 4891 0000","http://www3.hilton.com/en/hotels/argentina/hilton-buenos-aires-BUEHIHH/index.html","https://commons.wikimedia.org/wiki/Special:FilePath/Buenos%20Aires%20-%20Puerto%20Madero%20-%20Hilton%20Hotel%20-%2020071208.jpg"),
        hp("MotorCity Casino Hotel","Casino hotel and entertainment complex in Detroit.","2901 Grand River Avenue","Detroit","United States",4,42.339,-83.069,"+1 866 782 9622","http://www.motorcitycasino.com/","https://commons.wikimedia.org/wiki/Special:FilePath/MC%20Casino%20Grand%20River%20Detroit1.jpg"),
        hp("Grand-Hotel du Cap-Ferrat","Riviera palace resort on the Saint-Jean-Cap-Ferrat peninsula.","71 Boulevard du General de Gaulle","Saint-Jean-Cap-Ferrat","France",5,43.67638889,7.33138889,"+33 4 93 76 50 50","http://www.fourseasons.com/capferrat/","https://commons.wikimedia.org/wiki/Special:FilePath/Cap-Ferrat%20-%20Avenue%20de%20la%20Corniche%20-%20View%20NNW%20on%20Grand%20Hotel%20du%20Cap%20Ferrat.jpg"),
        hp("AC Hotel Bella Sky Copenhagen","Twin-tower hotel beside Bella Center and Copenhagen Airport.","Center Boulevard 5","Copenhagen","Denmark",4,55.6396,12.5782,"+45 32 47 30 00","http://www.acbellaskycopenhagen.dk/","https://commons.wikimedia.org/wiki/Special:FilePath/Bella%20Sky%201.jpg"),
        hp("Trump International Hotel and Tower Chicago","Riverfront hotel and residential tower in downtown Chicago.","401 North Wabash Avenue","Chicago","United States",5,41.8891,-87.626672222,"+1 312 588 8000","http://www.trumpchicago.com/","https://commons.wikimedia.org/wiki/Special:FilePath/20090518%20Trump%20International%20Hotel%20and%20Tower%2C%20Chicago.jpg"),
        hp("Marina Bay Sands","Integrated resort with the famous three-tower skyline and rooftop SkyPark.","10 Bayfront Avenue","Singapore","Singapore",5,1.2825,103.86,"+65 6688 8888","https://www.marinabaysands.com","https://commons.wikimedia.org/wiki/Special:FilePath/Marina%20Bay%20Sands%20Hotel%203%20%2831345110894%29.jpg"),
        hp("The Westin Warsaw","Modern tower hotel in Warsaw's Wola district.","Aleja Jana Pawla II 21","Warsaw","Poland",5,52.235202,20.996529,"+48 22 450 8000","https://www.marriott.com/en-us/hotels/wawwi-the-westin-warsaw/overview/","https://commons.wikimedia.org/wiki/Special:FilePath/The%20westin%20warsaw.JPG"),
        hp("Caesars Palace Las Vegas","Roman-themed luxury casino resort on the Las Vegas Strip.","3570 Las Vegas Blvd South","Las Vegas","United States",5,36.1165,-115.174,"+1 866 227 5938","http://www.caesarspalace.com","https://commons.wikimedia.org/wiki/Special:FilePath/Caesars%20Palace%20-%20South%20East%20-%202010-12-12.jpg"),
        hp("Steigenberger Golf Resort El Gouna","Lagoon-side Red Sea resort with golf course access in El Gouna.","El Gouna","El Gouna","Egypt",5,27.390373,33.677216,"+20 65 3580140","https://www.steigenberger.com/en/hotels/all-hotels/egypt/el-gouna/steigenberger-golf-resort","https://commons.wikimedia.org/wiki/Special:FilePath/2012-03-05-Hurghada-11.jpg")
    );

    @Bean
    @Order(5)
    CommandLineRunner seedStaticHotels(HotelRepository hotelRepository,
                                      RoomRepository roomRepository,
                                      InventoryRepository inventoryRepository,
                                      AmenityRepository amenityRepository,
                                      JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("Clearing all hotel-related tables to avoid FK conflicts...");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : List.of(
                    "payments", "bookings", "event_reservations", "event_tags", "event_photos",
                    "events", "table_reservations", "cancellation_policies", "room_amenities",
                    "inventory", "pricing_rules", "hotel_gallery", "hotel_amenities", "rooms", "hotels")) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            List<HotelSeed> hotelsToSeed = globallyBalancedHotels();
            Random random = new Random(42); // Seed for reproducibility

            // Load all amenities once; amenities are seeded at @Order(1)
            Map<String, Amenity> amenityMap = amenityRepository.findAll().stream()
                    .collect(Collectors.toMap(Amenity::getName, a -> a));

            log.info("Seeding {} hotels...", hotelsToSeed.size());
            long managerId = 2L;
            int saved = 0;

            for (HotelSeed seed : hotelsToSeed) {
                String hotelPhotoUrl = photoUrlFor(seed);

                // Generate random rating between 4.0 and 5.0
                double randomRating = 4.0 + (random.nextDouble() * 1.0);

                Hotel hotel = Hotel.builder()
                        .name(seed.name())
                        .description(seed.description())
                        .address(seed.address())
                        .city(seed.city())
                        .region(seed.region())
                        .starRating(seed.stars())
                        .averageGuestRating(BigDecimal.valueOf(randomRating).setScale(2, RoundingMode.HALF_UP))
                        .phoneNumber(seed.phone())
                        .websiteUrl(seed.website())
                        .latitude(seed.lat())
                        .longitude(seed.lng())
                        .coverImageUrl(hotelPhotoUrl)
                        .galleryImageUrls(List.of(hotelPhotoUrl))
                        .managerId(managerId)
                        .status(HotelStatus.ACTIVE)
                        .build();

                // Save first so the entity is managed, then attach amenities to avoid
                // "detached entity passed to persist" caused by CascadeType.PERSIST on the ManyToMany
                Hotel savedHotel = hotelRepository.save(hotel);
                savedHotel.setAmenities(amenitiesFor(seed.stars(), amenityMap));
                hotelRepository.save(savedHotel);

                // Create rooms with inventory
                createRoomsWithInventory(savedHotel, roomRepository, inventoryRepository, random);
                
                saved++;
                managerId = managerId == 2L ? 3L : 2L;
            }

            log.info("Seeding complete — {} hotels saved across 60 cities worldwide.", saved);
        };
    }

    private Set<Amenity> amenitiesFor(int stars, Map<String, Amenity> amenityMap) {
        // All hotels get the basics
        List<String> names = new ArrayList<>(List.of(
                "WiFi", "Parking", "Restaurant", "Room Service", "Laundry Service"));
        if (stars >= 5) {
            names.addAll(List.of(
                    "Swimming Pool", "Fitness Center", "Spa",
                    "Bar & Lounge", "Concierge", "Business Center"));
        } else {
            names.addAll(List.of("Swimming Pool", "Fitness Center", "Bar & Lounge"));
        }
        return names.stream()
                .map(amenityMap::get)
                .filter(a -> a != null)
                .collect(Collectors.toSet());
    }

    private record RoomConfig(String name, RoomType type, String bedType, int capacity,
                              double minPrice, double maxPrice, int minRooms, int maxRooms,
                              String description) {}

    private void createRoomsWithInventory(Hotel hotel,
                                          RoomRepository roomRepository,
                                          InventoryRepository inventoryRepository,
                                          Random random) {
        boolean isFiveStar = hotel.getStarRating() >= 5;
        LocalDate today = LocalDate.now();
        int inventoryDays = 90;

        List<RoomConfig> configs = isFiveStar
                ? List.of(
                        new RoomConfig("Classic Room", RoomType.SINGLE, "Twin Beds", 2,
                                200, 450, 12, 20,
                                "Elegantly appointed room with premium bedding, marble bathroom and city or garden views."),
                        new RoomConfig("Deluxe Room", RoomType.DOUBLE, "King Bed", 2,
                                350, 700, 10, 18,
                                "Spacious deluxe room featuring a king bed, luxury toiletries and floor-to-ceiling windows."),
                        new RoomConfig("Junior Suite", RoomType.SUITE, "King Bed", 3,
                                700, 1400, 5, 10,
                                "Generous suite with a separate sitting area, walk-in wardrobe and panoramic views."),
                        new RoomConfig("Premier Suite", RoomType.DELUXE, "King Bed", 4,
                                1500, 3500, 2, 5,
                                "Lavish suite spanning a full floor section with butler service and private dining area.")
                  )
                : List.of(
                        new RoomConfig("Standard Room", RoomType.SINGLE, "Twin Beds", 2,
                                100, 280, 15, 25,
                                "Comfortable standard room with quality furnishings and modern amenities."),
                        new RoomConfig("Superior Room", RoomType.DOUBLE, "Queen Bed", 2,
                                150, 380, 12, 20,
                                "Superior room with a queen bed, work desk and enhanced bathroom amenities."),
                        new RoomConfig("Deluxe Suite", RoomType.SUITE, "King Bed", 3,
                                300, 650, 4, 9,
                                "Comfortable suite with a lounge area, upgraded toiletries and premium views.")
                  );

        for (RoomConfig cfg : configs) {
            double price = cfg.minPrice() + random.nextDouble() * (cfg.maxPrice() - cfg.minPrice());
            int totalRooms = cfg.minRooms() + random.nextInt(cfg.maxRooms() - cfg.minRooms() + 1);

            Room room = Room.builder()
                    .hotel(hotel)
                    .name(cfg.name())
                    .roomType(cfg.type())
                    .description(cfg.description())
                    .maxCapacity(cfg.capacity())
                    .totalRooms(totalRooms)
                    .basePrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP))
                    .bedType(cfg.bedType())
                    .active(true)
                    .build();

            Room savedRoom = roomRepository.save(room);

            List<Inventory> inventoryBatch = new ArrayList<>(inventoryDays);
            for (int d = 0; d < inventoryDays; d++) {
                inventoryBatch.add(Inventory.builder()
                        .room(savedRoom)
                        .date(today.plusDays(d))
                        .totalRooms(totalRooms)
                        .availableRooms(totalRooms)
                        .build());
            }
            inventoryRepository.saveAll(inventoryBatch);
        }
    }

    private static List<HotelSeed> globallyBalancedHotels() {
        Map<String, List<HotelSeed>> hotelsByRegion = new LinkedHashMap<>();
        for (HotelSeed hotel : HOTELS) {
            hotelsByRegion.computeIfAbsent(hotel.region(), ignored -> new ArrayList<>()).add(hotel);
        }
        for (List<HotelSeed> regionHotels : hotelsByRegion.values()) {
            regionHotels.sort((first, second) -> Boolean.compare(
                    second.photoUrl() != null && !second.photoUrl().isBlank(),
                    first.photoUrl() != null && !first.photoUrl().isBlank()));
        }

        List<String> regions = new ArrayList<>(FEATURED_REGION_ORDER);
        for (String region : hotelsByRegion.keySet()) {
            if (!regions.contains(region)) {
                regions.add(region);
            }
        }

        List<HotelSeed> balancedHotels = new ArrayList<>(HOTELS.size());
        boolean addedHotel;
        int index = 0;
        do {
            addedHotel = false;
            for (String region : regions) {
                List<HotelSeed> regionHotels = hotelsByRegion.get(region);
                if (regionHotels != null && index < regionHotels.size()) {
                    balancedHotels.add(regionHotels.get(index));
                    addedHotel = true;
                }
            }
            index++;
        } while (addedHotel);

        return balancedHotels;
    }

    private static String photoUrlFor(HotelSeed seed) {
        if (seed.photoUrl() != null && !seed.photoUrl().isBlank()) {
            return seed.photoUrl();
        }
        return PHOTOS[seed.photoIdx() % PHOTOS.length];
    }
}
