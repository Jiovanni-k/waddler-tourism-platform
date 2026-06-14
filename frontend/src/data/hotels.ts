import hotel1 from "@/assets/hotel-1.jpeg";
import hotel2 from "@/assets/hotel-2.jpeg";
import hotel3 from "@/assets/hotel-3.jpeg";
import dest1 from "@/assets/dest-1.jpeg";
import dest2 from "@/assets/dest-2.jpeg";
import dest3 from "@/assets/dest-3.jpeg";
import dest4 from "@/assets/dest-4.jpeg";

export type HotelCategory = "Hotels" | "Resorts" | "Apartments" | "Villas";

export interface Hotel {
  id: string;
  name: string;
  location: string;
  city: string;
  category: HotelCategory;
  tag: string;
  stars: number;
  rating: number;
  reviews: number;
  price: number;
  description: string;
  amenities: string[];
  images: string[];
}

export const hotels: Hotel[] = [
  {
    id: "seaview-resort",
    name: "Seaview Resort & Spa",
    location: "Bali, Indonesia",
    city: "Bali",
    category: "Resorts",
    tag: "Best choice",
    stars: 5,
    rating: 4.8,
    reviews: 256,
    price: 140,
    description:
      "An oceanfront sanctuary with infinity pools, a world-class spa, and sunsets you'll write home about. Perfect for honeymooners and slow travelers.",
    amenities: ["Free Wi-Fi", "Infinity Pool", "Spa", "Beachfront", "Breakfast", "Airport shuttle"],
    images: [hotel1, dest2, dest4],
  },
  {
    id: "royal-ocean",
    name: "The Royal Ocean",
    location: "Maldives",
    city: "Maldives",
    category: "Hotels",
    tag: "Popular",
    stars: 4,
    rating: 4.6,
    reviews: 189,
    price: 115,
    description:
      "Crystal-clear waters and over-water bungalows. The Royal Ocean blends understated luxury with the warmth of island hospitality.",
    amenities: ["Free Wi-Fi", "Private beach", "Pool", "Restaurant", "Bar", "Room service"],
    images: [hotel2, dest2, dest1],
  },
  {
    id: "tropical-paradise",
    name: "Tropical Paradise Hotel",
    location: "Bali, Indonesia",
    city: "Bali",
    category: "Hotels",
    tag: "Budget friendly",
    stars: 4,
    rating: 4.5,
    reviews: 110,
    price: 90,
    description:
      "A lush, tropical hideaway minutes from the beach. Great food, friendly staff, and a pool that calls your name.",
    amenities: ["Free Wi-Fi", "Pool", "Breakfast", "Garden", "Parking"],
    images: [hotel3, dest4, dest1],
  },
  {
    id: "santorini-cliff",
    name: "Santorini Cliff Suites",
    location: "Santorini, Greece",
    city: "Santorini",
    category: "Villas",
    tag: "Top rated",
    stars: 5,
    rating: 4.9,
    reviews: 342,
    price: 220,
    description:
      "Whitewashed suites carved into the caldera. Wake up to the Aegean and end the day with a glass of Assyrtiko at sunset.",
    amenities: ["Caldera view", "Private terrace", "Pool", "Free Wi-Fi", "Concierge"],
    images: [dest1, hotel1, hotel2],
  },
  {
    id: "azure-villa",
    name: "Azure Villa Maldives",
    location: "Maldives",
    city: "Maldives",
    category: "Villas",
    tag: "Luxury",
    stars: 5,
    rating: 4.9,
    reviews: 421,
    price: 320,
    description:
      "An over-water villa with a glass floor, private deck, and direct lagoon access. The kind of place that ruins all other vacations.",
    amenities: ["Over-water deck", "Pool", "Spa", "Breakfast", "Butler service", "Free Wi-Fi"],
    images: [dest2, hotel2, hotel1],
  },
  {
    id: "phuket-beachfront",
    name: "Phuket Beachfront Resort",
    location: "Phuket, Thailand",
    city: "Phuket",
    category: "Resorts",
    tag: "Sea views",
    stars: 4,
    rating: 4.6,
    reviews: 203,
    price: 130,
    description:
      "Nestled along Patong Beach, this resort offers stunning Andaman Sea views with a sprawling pool and authentic Thai cuisine.",
    amenities: ["Free Wi-Fi", "Pool", "Breakfast", "Spa", "Beachfront", "Restaurant"],
    images: [dest3, hotel1, dest1],
  },
  {
    id: "andaman-pearl",
    name: "Andaman Pearl Hotel",
    location: "Phuket, Thailand",
    city: "Phuket",
    category: "Hotels",
    tag: "Great value",
    stars: 3,
    rating: 4.2,
    reviews: 87,
    price: 75,
    description:
      "A charming boutique hotel close to Patong's nightlife and beaches. Clean rooms, warm hospitality, and a great breakfast spread.",
    amenities: ["Free Wi-Fi", "Breakfast", "Airport transfer", "Rooftop bar"],
    images: [hotel3, dest3, dest2],
  },
  {
    id: "aurora-grand",
    name: "Aurora Grand Hotel",
    location: "Reykjavík, Iceland",
    city: "Reykjavík",
    category: "Hotels",
    tag: "Northern lights",
    stars: 4,
    rating: 4.7,
    reviews: 165,
    price: 160,
    description:
      "A sleek Nordic hotel in the heart of Reykjavík. Chase the northern lights, soak in geothermal pools, and return to cozy rooms with volcano views.",
    amenities: ["Free Wi-Fi", "Geothermal Pool", "Breakfast", "Concierge", "Northern lights tours"],
    images: [dest4, hotel2, dest1],
  },
  {
    id: "northern-lights-lodge",
    name: "Northern Lights Lodge",
    location: "Reykjavík, Iceland",
    city: "Reykjavík",
    category: "Apartments",
    tag: "Unique stay",
    stars: 5,
    rating: 4.8,
    reviews: 212,
    price: 240,
    description:
      "Glass-ceiling suites designed to give you a front-row seat to Iceland's famous aurora borealis. An unforgettable bucket-list experience.",
    amenities: ["Free Wi-Fi", "Glass ceiling suite", "Spa", "Breakfast", "Hot tub", "Aurora alerts"],
    images: [dest1, dest4, hotel3],
  },
  {
    id: "kyoto-garden-ryokan",
    name: "Kyoto Garden Ryokan",
    location: "Kyoto, Japan",
    city: "Kyoto",
    category: "Hotels",
    tag: "Cultural gem",
    stars: 4,
    rating: 4.7,
    reviews: 178,
    price: 110,
    description:
      "A traditional Japanese inn surrounded by maple gardens and bamboo groves. Tatami rooms, yukata robes, and kaiseki dinners await.",
    amenities: ["Free Wi-Fi", "Traditional onsen", "Breakfast", "Garden views", "Kimono rental"],
    images: [dest3, hotel1, dest2],
  },
  {
    id: "sakura-boutique",
    name: "Sakura Boutique Inn",
    location: "Kyoto, Japan",
    city: "Kyoto",
    category: "Apartments",
    tag: "Cherry blossoms",
    stars: 3,
    rating: 4.3,
    reviews: 94,
    price: 85,
    description:
      "A charming inn steps from the Philosopher's Path and Ginkaku-ji temple. Ideal for budget-conscious travelers who love cultural immersion.",
    amenities: ["Free Wi-Fi", "Breakfast", "Bicycle rental", "Tour desk"],
    images: [hotel2, dest3, dest1],
  },
  {
    id: "blue-lagoon-santorini",
    name: "Blue Lagoon Suites",
    location: "Santorini, Greece",
    city: "Santorini",
    category: "Hotels",
    tag: "Romantic",
    stars: 5,
    rating: 4.8,
    reviews: 307,
    price: 195,
    description:
      "Perched on the cliffside with sweeping Caldera views, private plunge pools, and a dining experience you'll dream about for years.",
    amenities: ["Free Wi-Fi", "Pool", "Spa", "Breakfast", "Sunset views", "Concierge"],
    images: [hotel1, dest1, dest2],
  },
];

export const categories: HotelCategory[] = ["Hotels", "Resorts", "Apartments", "Villas"];
