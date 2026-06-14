import happy from "@/assets/penguins/Happy_Loved.png";
import traveler from "@/assets/penguins/Traveler.png";
import greeting from "@/assets/penguins/Greeting_Speaking.png";
import music from "@/assets/penguins/Music_Audio.png";
import waving from "@/assets/penguins/Waving.png";
import walking from "@/assets/penguins/Walking_Casual.png";
import listening from "@/assets/penguins/Listening_Sitting.png";
import reward from "@/assets/penguins/Action_Reward.png";
import planning from "@/assets/penguins/Planning.png";
import travelmode from "@/assets/penguins/Travel_Mode_Walking.png";
import celebration from "@/assets/penguins/Celebration.png";
import neutral from "@/assets/penguins/Neutral.png";
import excited from "@/assets/penguins/Excited.png";
import laughing from "@/assets/penguins/Laughing.png";
import winking from "@/assets/penguins/Winking.png";
import thinking from "@/assets/penguins/Thinking.png";
import curious from "@/assets/penguins/Curious.png";
import surprised from "@/assets/penguins/Surprised.png";
import sad from "@/assets/penguins/Sad.png";
import angry from "@/assets/penguins/Angry.png";
import exploring from "@/assets/penguins/Exploring.png";
import adventure from "@/assets/penguins/On_Adventure.png";
import ontheway from "@/assets/penguins/On_the_Way.png";
import photos from "@/assets/penguins/Taking_Photos.png";
import relaxing from "@/assets/penguins/Relaxing.png";
import working from "@/assets/penguins/Working.png";
import peek from "@/assets/penguins/Peek_UI.png";
import sleeping from "@/assets/penguins/Sleeping.png";

const map = {
  happy, traveler, greeting, music, waving, walking,
  listening, reward, planning, travelmode, celebration, neutral,
  excited, laughing, winking, thinking, curious, surprised, sad, angry,
  exploring, adventure, ontheway, photos, relaxing, working, peek, sleeping,
} as const;

export type PenguinMood = keyof typeof map;

interface PenguinProps {
  mood?: PenguinMood;
  className?: string;
  alt?: string;
}

export const Penguin = ({ mood = "happy", className = "h-24 w-24", alt }: PenguinProps) => (
  <img
    src={map[mood]}
    alt={alt ?? `Waddler penguin ${mood}`}
    className={`object-contain select-none pointer-events-none ${className}`}
    draggable={false}
  />
);

export default Penguin;
