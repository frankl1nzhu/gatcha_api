import axios from 'axios';

// Create axios instance
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Unauthorized, clear token and redirect to login page
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Authentication related APIs
export const authAPI = {
  login: (username: string, password: string) => {
    return api.post('/auth/login', { username, password });
  },
  validateToken: () => {
    return api.post('/auth/validate');
  },
};

// Player related APIs
export const playerAPI = {
  getProfile: () => {
    return api.get('/player/profile');
  },
  getMonsters: () => {
    return api.get('/player/monsters');
  },
  getLevel: () => {
    return api.get('/player/level');
  },
  addExperience: (experience: number) => {
    return api.post('/player/experience', { experience });
  },
  levelUp: () => {
    return api.post('/player/levelup');
  },
  addMonster: (monsterId: string) => {
    return api.post(`/player/monsters/${monsterId}`);
  },
  removeMonster: (monsterId: string) => {
    return api.delete(`/player/monsters/${monsterId}`);
  },
};

// Monster related APIs
export const monsterAPI = {
  getAllMonsters: () => {
    return api.get('/monsters');
  },
  getMonsterById: (id: string) => {
    return api.get(`/monsters/${id}`);
  },
  addExperience: (id: string, experience: number) => {
    return api.post(`/monsters/${id}/experience`, { experience });
  },
  upgradeSkill: (id: string, skillNum: number) => {
    return api.post(`/monsters/${id}/skill`, { skillNum });
  },
};

// Summoning related APIs
export const summonAPI = {
  summonMonster: () => {
    return api.post('/summon');
  },
  summonMultiple: (count: number = 10) => {
    return api.post('/summon/multi', { count });
  },
  getSummonHistory: () => {
    return api.get('/summon/history');
  },
  reprocessFailedSummons: () => {
    return api.post('/summon/reprocess');
  },
};

// Battle related APIs
export const battleAPI = {
  startBattle: (monster1Id: string, monster2Id: string) => {
    return api.post('/battles', { monster1Id, monster2Id });
  },
  getBattleById: (battleId: string) => {
    return api.get(`/battles/${battleId}`);
  },
  getMonsterBattles: (monsterId: string) => {
    return api.get(`/battles/monster/${monsterId}`);
  },
  getAllBattles: () => {
    return api.get('/battles/history');
  },
};

// Royal Rumble related APIs
export const royalRumbleAPI = {
  startRumble: (monsterIds: string[]) => {
    return api.post('/royal-rumble', { monsterIds });
  },
  getRumbleExperience: (rumbleId: string) => {
    return api.get(`/royal-rumble/experience/${rumbleId}`);
  },
  getAllRumbles: () => {
    return api.get('/royal-rumble');
  },
};

export default api;