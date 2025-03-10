import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, List, Typography, Spin, message, Empty, Button } from 'antd';
import { 
  UserOutlined, 
  TrophyOutlined, 
  ThunderboltOutlined, 
  StarOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { playerAPI, battleAPI, monsterAPI } from '../services/api';
import { generateMonsterName } from '../utils/nameGenerator';

const { Title, Text } = Typography;

interface PlayerStats {
  level: number;
  experience: number;
  maxExperience: number;
  monstersCount: number;
}

interface BattleRecord {
  id: string;
  monster1Id: string;
  monster2Id: string;
  winnerId: string;
  battleDate: string;
}

interface Monster {
  id: string;
  element: string;
  level: number;
  name?: string;
}

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [playerStats, setPlayerStats] = useState<PlayerStats>({
    level: 0,
    experience: 0,
    maxExperience: 100, // Set default to 100 to avoid division by 0
    monstersCount: 0
  });
  const [recentBattles, setRecentBattles] = useState<BattleRecord[]>([]);
  const [totalBattles, setTotalBattles] = useState<number>(0);
  const [monsters, setMonsters] = useState<Record<string, Monster>>({});

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // Get player info
      const profileResponse = await playerAPI.getProfile();
      
      // Get player's monster list
      const monstersResponse = await playerAPI.getMonsters();
      
      // Get all monster details
      const allMonstersResponse = await monsterAPI.getAllMonsters();
      
      console.log('Profile Response:', profileResponse.data);
      console.log('Monsters IDs Response:', monstersResponse.data);
      console.log('All Monsters Response:', allMonstersResponse.data);
      
      // Create mapping from monster ID to monster details
      const monstersMap: Record<string, Monster> = {};
      if (Array.isArray(allMonstersResponse.data)) {
        allMonstersResponse.data.forEach((monster: any) => {
          if (monster && monster.id) {
            monstersMap[monster.id] = {
              id: monster.id,
              element: monster.element || 'unknown',
              level: monster.level || 0
            };
          }
        });
      }
      setMonsters(monstersMap);
      
      // Update player stats
      if (profileResponse.data) {
        setPlayerStats({
          level: profileResponse.data.level || 0,
          experience: profileResponse.data.experience || 0,
          maxExperience: profileResponse.data.maxExperience || 100,
          monstersCount: Array.isArray(profileResponse.data.monsters) 
            ? profileResponse.data.monsters.length 
            : (Array.isArray(monstersResponse.data) ? monstersResponse.data.length : 0)
        });
      }
      
      // Try to get battle history
      try {
        const battlesResponse = await battleAPI.getAllBattles();
        console.log('Battles Response:', battlesResponse.data);
        
        if (battlesResponse.data && Array.isArray(battlesResponse.data)) {
          setRecentBattles(battlesResponse.data.slice(0, 10)); // Show last 10
          setTotalBattles(battlesResponse.data.length);
        } else {
          setRecentBattles([]);
          setTotalBattles(0);
        }
      } catch (battleError) {
        console.error('Failed to get battle history:', battleError);
        setRecentBattles([]);
        setTotalBattles(0);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
      message.error('Failed to fetch dashboard data. Please check network connection or login again');
    } finally {
      setLoading(false);
    }
  };

  // Calculate experience percentage, ensure no NaN
  const experiencePercent = playerStats.maxExperience > 0 
    ? Math.round((playerStats.experience / playerStats.maxExperience) * 100) 
    : 0;

  // Get monster name
  const getMonsterName = (monsterId: string) => {
    const monster = monsters[monsterId];
    if (monster) {
      // If monster already has a name, use it
      if (monster.name) {
        return monster.name;
      }
      // Otherwise generate a name and save it
      const name = generateMonsterName(monsterId, monster.element);
      monster.name = name;
      return name;
    }
    return `Monster ID: ${monsterId}`;
  };

  // Format date to show only date and time
  const formatDate = (dateString: string) => {
    if (!dateString) return 'Unknown';
    try {
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    } catch (error) {
      console.error('Date formatting error:', error);
      return 'Unknown';
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={2}>Dashboard</Title>
        <Button 
          icon={<ReloadOutlined />} 
          onClick={fetchDashboardData}
          loading={loading}
        >
          Refresh Data
        </Button>
      </div>
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Row gutter={16}>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Player Level"
                  value={playerStats.level}
                  prefix={<UserOutlined />}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Experience"
                  value={experiencePercent}
                  suffix="%"
                  prefix={<StarOutlined />}
                />
                <Text type="secondary">
                  {playerStats.experience} / {playerStats.maxExperience}
                </Text>
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Monster Count"
                  value={playerStats.monstersCount}
                  prefix={<TrophyOutlined />}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Battle Count"
                  value={totalBattles}
                  prefix={<ThunderboltOutlined />}
                />
              </Card>
            </Col>
          </Row>

          <div style={{ marginTop: 24 }}>
            <Card title="Recent Battles">
              {recentBattles.length > 0 ? (
                <List
                  dataSource={recentBattles}
                  renderItem={(battle) => (
                    <List.Item>
                      <List.Item.Meta
                        title={`Battle ID: ${battle.id}`}
                        description={`Date: ${formatDate(battle.battleDate)}`}
                      />
                      <div>
                        Winner: {getMonsterName(battle.winnerId)} | Loser: {getMonsterName(battle.monster1Id === battle.winnerId ? battle.monster2Id : battle.monster1Id)}
                      </div>
                    </List.Item>
                  )}
                />
              ) : (
                <Empty 
                  description="No battle records" 
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              )}
            </Card>
          </div>
        </>
      )}
    </div>
  );
};

export default Dashboard;