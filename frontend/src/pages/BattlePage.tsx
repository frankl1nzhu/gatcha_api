import React, { useState, useEffect } from 'react';
import { Card, Select, Button, Typography, Spin, message, Divider, List, Tag, Empty, Alert } from 'antd';
import type { SelectProps } from 'antd/es/select';
import type { ButtonProps } from 'antd/es/button';
import type { AlertProps } from 'antd/es/alert';
import type { TagProps } from 'antd/es/tag';
import type { ListProps } from 'antd/es/list';
import { ThunderboltOutlined, TrophyOutlined, ReloadOutlined } from '@ant-design/icons';
import { monsterAPI, battleAPI, playerAPI } from '../services/api';
import { generateMonsterName } from '../utils/nameGenerator';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

interface Monster {
  id: string;
  element: string;
  level: number;
  hp: number;
  atk: number;
  def: number;
  vit: number;
  name?: string;
}

interface BattleLog {
  id: string;
  monster1Id: string;
  monster2Id: string;
  monster1Element: string;
  monster2Element: string;
  winnerId: string;
  experienceGained: number;
  battleRecords: string[];
  battleDate: string;
}

const BattlePage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [battling, setBattling] = useState(false);
  const [monsters, setMonsters] = useState<Monster[]>([]);
  const [selectedMonster1, setSelectedMonster1] = useState<string>('');
  const [selectedMonster2, setSelectedMonster2] = useState<string>('');
  const [battleResult, setBattleResult] = useState<BattleLog | null>(null);
  const [battleHistory, setBattleHistory] = useState<BattleLog[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      await Promise.all([
        fetchMonsters(),
        fetchBattleHistory()
      ]);
    } catch (error: any) {
      console.error('Failed to fetch data:', error);
      if (error.response) {
        setError(`Failed to fetch data: ${error.response.status} ${error.response.statusText}`);
      } else if (error.request) {
        setError('Unable to connect to server, please check your network connection');
      } else {
        setError(`Request error: ${error.message}`);
      }
      message.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  const fetchMonsters = async () => {
    try {
      // Get the list of monster IDs owned by the player
      const playerMonstersResponse = await playerAPI.getMonsters();
      console.log('Player Monsters IDs Response:', playerMonstersResponse.data);
      
      // Get details of all monsters
      const allMonstersResponse = await monsterAPI.getAllMonsters();
      console.log('All Monsters Response:', allMonstersResponse.data);
      
      if (Array.isArray(playerMonstersResponse.data) && Array.isArray(allMonstersResponse.data)) {
        // Create a mapping from monster ID to monster details
        const monstersMap: Record<string, any> = {};
        allMonstersResponse.data.forEach((monster: any) => {
          if (monster && monster.id) {
            monstersMap[monster.id] = monster;
          }
        });
        
        // Filter out monsters owned by the player
        const playerMonsters = playerMonstersResponse.data
          .map(monsterId => monstersMap[monsterId])
          .filter(monster => monster !== undefined);
        
        console.log('Filtered Player Monsters:', playerMonsters);
        setMonsters(playerMonsters);
      } else {
        console.warn('Monster data format is incorrect:', 
          'Player monsters:', playerMonstersResponse.data, 
          'All monsters:', allMonstersResponse.data);
        setMonsters([]);
      }
    } catch (error) {
      console.error('Failed to fetch monster list:', error);
      setMonsters([]);
      throw error; // Pass the error up for fetchData to handle
    }
  };

  const fetchBattleHistory = async () => {
    try {
      // Try to get battle history
      try {
        const response = await battleAPI.getAllBattles();
        console.log('Battle History Response:', response.data);
        
        if (response.data && Array.isArray(response.data)) {
          setBattleHistory(response.data.slice(0, 20)); // Show the most recent 20
        } else {
          console.warn('Battle history data is not in array format:', response.data);
          setBattleHistory([]);
        }
      } catch (battleError) {
        console.error('Failed to fetch battle history:', battleError);
        setBattleHistory([]);
        // Don't pass up the error, as battle history is not essential
      }
    } catch (error) {
      console.error('Failed to fetch battle history:', error);
      setBattleHistory([]);
      // Don't pass up the error, as battle history is not essential
    }
  };

  const startBattle = async () => {
    if (!selectedMonster1 || !selectedMonster2) {
      message.warning('Please select two monsters for battle');
      return;
    }

    if (selectedMonster1 === selectedMonster2) {
      message.warning('Cannot select the same monster for battle');
      return;
    }

    setBattling(true);
    setError(null);
    try {
      const response = await battleAPI.startBattle(selectedMonster1, selectedMonster2);
      console.log('Battle Response:', response.data);
      
      if (response.data) {
        setBattleResult(response.data);
        message.success('Battle completed!');
        // Refresh monster list and battle history
        fetchData();
      } else {
        setError('Battle failed, server returned empty data');
        message.error('Battle failed');
      }
    } catch (error: any) {
      console.error('Battle failed:', error);
      if (error.response) {
        if (error.response.status === 500 && error.response.data && error.response.data.message) {
          setError(`Battle failed: ${error.response.data.message}`);
        } else {
          setError(`Battle failed: ${error.response.status} ${error.response.statusText}`);
        }
      } else if (error.request) {
        setError('Unable to connect to server, please check your network connection');
      } else {
        setError(`Request error: ${error.message}`);
      }
      message.error('Battle failed');
    } finally {
      setBattling(false);
    }
  };

  const getMonsterById = (id: string) => {
    const monster = monsters.find(monster => monster.id === id);
    if (monster && !monster.name) {
      // If the monster doesn't have a name, generate one and save it
      monster.name = generateMonsterName(id, monster.element);
    }
    return monster;
  };

  const getElementColor = (element: string) => {
    const elementColors: Record<string, string> = {
      fire: '#f5222d',
      water: '#1890ff',
      earth: '#52c41a',
      wind: '#722ed1',
      light: '#faad14',
      dark: '#722ed1'
    };
    return elementColors[element.toLowerCase()] || '#000000';
  };

  const getMonsterName = (monster: Monster) => {
    if (!monster.name) {
      monster.name = generateMonsterName(monster.id, monster.element);
    }
    return monster.name;
  };

  const renderMonsterOption = (monster: Monster) => {
    // Ensure the monster has a name
    if (!monster.name) {
      monster.name = generateMonsterName(monster.id, monster.element);
    }
    
    const tagStyle = { margin: 0 };
    const tagContent = `${monster.name} (Lv.${monster.level})`;
    
    return (
      <Option key={monster.id} value={monster.id}>
        <Tag color={getElementColor(monster.element)} style={tagStyle}>
          {tagContent}
        </Tag>
      </Option>
    );
  };

  const renderBattleResult = () => {
    if (!battleResult) return null;

    const monster1 = getMonsterById(battleResult.monster1Id);
    const monster2 = getMonsterById(battleResult.monster2Id);
    const winnerMonster = getMonsterById(battleResult.winnerId);

    if (!monster1 || !monster2 || !winnerMonster) return null;

    const monster1Content = `${monster1.element} Monster (Lv.${monster1.level})`;
    const monster2Content = `${monster2.element} Monster (Lv.${monster2.level})`;
    const winnerContent = `${winnerMonster.element} Monster (Lv.${winnerMonster.level})`;

    return (
      <Card title="Battle Result" style={{ marginTop: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <div>
            <Tag color={getElementColor(monster1.element)}>
              {monster1Content}
            </Tag>
          </div>
          <div>VS</div>
          <div>
            <Tag color={getElementColor(monster2.element)}>
              {monster2Content}
            </Tag>
          </div>
        </div>
        
        <Divider />
        
        <div style={{ textAlign: 'center', marginBottom: 16 }}>
          <Title level={4}>
            Winner: <span style={{ color: getElementColor(winnerMonster.element) }}>
              {winnerContent}
            </span>
          </Title>
          <Text>Experience gained: {battleResult.experienceGained}</Text>
        </div>
        
        <Divider>Battle Records</Divider>
        
        <div className="battle-log">
          {battleResult.battleRecords && battleResult.battleRecords.length > 0 ? (
            battleResult.battleRecords.map((record, index) => (
              <Paragraph key={index}>{record}</Paragraph>
            ))
          ) : (
            <Empty description="No battle records available" />
          )}
        </div>
      </Card>
    );
  };

  // Format date to show only year, month, day, and time
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

  const renderBattleHistory = () => {
    if (battleHistory.length === 0) {
      return <Empty description="No battle history available" />;
    }

    // Create a cache object to store the mapping of monster IDs to names
    const monsterNameCache: Record<string, string> = {};

    // Function to get monster name, prioritizing cache
    const getMonsterNameById = (monsterId: string): string => {
      // If the name is already in cache, return it directly
      if (monsterNameCache[monsterId]) {
        return monsterNameCache[monsterId];
      }
      
      // Try to find the monster in the current monster list
      const monster = getMonsterById(monsterId);
      if (monster) {
        // If monster is found, get its name and cache it
        const name = getMonsterName(monster);
        monsterNameCache[monsterId] = name;
        return name;
      } else {
        // If the monster has been deleted, generate a name based on ID and element from battle records
        const battle = battleHistory.find(b => b.monster1Id === monsterId || b.monster2Id === monsterId);
        if (battle) {
          const element = battle.monster1Id === monsterId ? battle.monster1Element : battle.monster2Element;
          const name = generateMonsterName(monsterId, element);
          monsterNameCache[monsterId] = name;
          return name;
        }
        // If no battle record is found, use default element
        const name = generateMonsterName(monsterId, 'unknown');
        monsterNameCache[monsterId] = name;
        return name;
      }
    };

    return (
      <List<BattleLog>
        itemLayout="horizontal"
        dataSource={battleHistory}
        renderItem={(battle: BattleLog) => {
          const monster1 = getMonsterById(battle.monster1Id);
          const monster2 = getMonsterById(battle.monster2Id);
          const winner = getMonsterById(battle.winnerId);
          
          return (
            <List.Item>
              <List.Item.Meta
                avatar={<ThunderboltOutlined style={{ fontSize: 24, color: '#1890ff' }} />}
                title={`Battle - ${formatDate(battle.battleDate)}`}
                description={
                  <div>
                    <p>
                      {getMonsterNameById(battle.monster1Id)} vs {getMonsterNameById(battle.monster2Id)}
                    </p>
                    <p>
                      Winner: {getMonsterNameById(battle.winnerId)}
                    </p>
                  </div>
                }
              />
            </List.Item>
          );
        }}
      />
    );
  };

  return (
    <div className="battle-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={2}>Battle</Title>
        <Button 
          icon={<ReloadOutlined />} 
          onClick={fetchData}
          loading={loading}
        >
          Refresh Data
        </Button>
      </div>
      
      {error && (
        <Card style={{ marginBottom: 16, backgroundColor: '#fff2f0', borderColor: '#ffccc7' }}>
          <div style={{ color: '#cf1322' }}>{error}</div>
        </Card>
      )}
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Card title="Select Battle Monsters">
            {monsters.length < 2 ? (
              <Alert
                message="Insufficient Monsters"
                description="You need at least two monsters to battle. Please go to the Summon page to get more monsters."
                type="warning"
                showIcon
                style={{ marginBottom: 16 }}
              />
            ) : (
              <div className="battle-selection">
                <div style={{ flex: 1 }}>
                  <Title level={5}>Select First Monster</Title>
                  <Select
                    style={{ width: '100%' }}
                    placeholder="Select first monster"
                    value={selectedMonster1}
                    onChange={setSelectedMonster1}
                  >
                    {monsters.map(renderMonsterOption)}
                  </Select>
                </div>
                
                <div style={{ flex: 1 }}>
                  <Title level={5}>Select Second Monster</Title>
                  <Select
                    style={{ width: '100%' }}
                    placeholder="Select second monster"
                    value={selectedMonster2}
                    onChange={setSelectedMonster2}
                  >
                    {monsters.map(renderMonsterOption)}
                  </Select>
                </div>
              </div>
            )}
            
            <div style={{ marginTop: 16, textAlign: 'center' }}>
              <Button 
                type="primary" 
                icon={<ThunderboltOutlined />} 
                size="large"
                loading={battling}
                onClick={startBattle}
                disabled={!selectedMonster1 || !selectedMonster2 || selectedMonster1 === selectedMonster2 || monsters.length < 2}
              >
                Start Battle
              </Button>
              
              {selectedMonster1 === selectedMonster2 && selectedMonster1 && selectedMonster2 && (
                <div style={{ marginTop: 8 }}>
                  <Text type="danger">Cannot select the same monster for battle</Text>
                </div>
              )}
            </div>
          </Card>
          
          {renderBattleResult()}
          {renderBattleHistory()}
        </>
      )}
    </div>
  );
};

export default BattlePage; 