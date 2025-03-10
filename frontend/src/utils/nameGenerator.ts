// Monster name prefixes
const prefixes = [
  'Blazing', 'Frozen', 'Savage', 'Mysterious', 'Ancient', 'Ghostly', 'Lightning',
  'Shadow', 'Radiant', 'Chaotic', 'Serene', 'Furious', 'Wise', 'Mighty',
  'Swift', 'Sturdy', 'Deadly', 'Silent', 'Thunder', 'Hurricane', 'Earth',
  'Ocean', 'Stellar', 'Lunar', 'Solar', 'Dark', 'Holy', 'Evil',
  'Good', 'Divine', 'Magical', 'Natural', 'Mechanical', 'Primordial', 'Future'
];

// Monster name suffixes
const suffixes = [
  'Warrior', 'Mage', 'Assassin', 'Guard', 'Beast', 'Dragon', 'Elf', 'Demon', 'Angel',
  'Wizard', 'Knight', 'Hunter', 'Ghost', 'Giant', 'Dwarf', 'Fairy', 'Orc', 'Undead',
  'Overlord', 'Champion', 'Hero', 'Emissary', 'Prophet', 'Guardian', 'Destroyer', 'Creator',
  'Ruler', 'Dominator', 'Explorer', 'Stalker', 'Ranger', 'Warlock', 'Summoner', 'Elementalist',
  'Swordsman', 'Archer', 'Gunner', 'Fighter', 'Samurai', 'Ninja', 'Paladin', 'Reaper'
];

// Generate a deterministic name based on monster ID
export const generateMonsterName = (monsterId: string, element: string): string => {
  // Use monster ID hash to determine prefix and suffix indices
  const hash = hashString(monsterId);
  const prefixIndex = hash % prefixes.length;
  const suffixIndex = (hash * 31) % suffixes.length;
  
  // Choose prefix based on element type
  let elementPrefix = '';
  switch (element) {
    case 'fire':
      elementPrefix = 'Fire';
      break;
    case 'water':
      elementPrefix = 'Water';
      break;
    case 'earth':
      elementPrefix = 'Earth';
      break;
    case 'wind':
      elementPrefix = 'Wind';
      break;
    default:
      elementPrefix = '';
  }
  
  return `${prefixes[prefixIndex]} ${elementPrefix} ${suffixes[suffixIndex]}`;
};

// Simple string hash function
const hashString = (str: string): number => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
};