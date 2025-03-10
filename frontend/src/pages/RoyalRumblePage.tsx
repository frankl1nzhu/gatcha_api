import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Spin, message, List, Tag, Select, Steps, Collapse, Divider, Empty } from 'antd';
import { TeamOutlined, TrophyOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { monsterAPI, playerAPI, royalRumbleAPI } from '../services/api';
import { generateMonsterName } from '../utils/nameGenerator';
import moment from 'moment';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;
const { Panel } = Collapse;
const { Step } = Steps;

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

interface RumbleRound {
  roundNumber: number;
  actions: {
    monsterId: string;
    skillNum: number;
    damage: number;
    targetId: string;
    remainingHp: number;
  }[];
  remainingMonsterIds: string[];
}

interface RumbleResult {
  id: string;
  participantIds: string[];
  winnerId: string;
  participants: Monster[];
  winner: Monster;
  battleLog: string[];
  battleDate: string;
}

const RoyalRumblePage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [battling, setBattling] = useState(false);
  const [monsters, setMonsters] = useState<Monster[]>([]);
  const [playerMonsters, setPlayerMonsters] = useState<Monster[]>([]);
  const [selectedMonsters, setSelectedMonsters] = useState<string[]>([]);
  const [rumbleResult, setRumbleResult] = useState<RumbleResult | null>(null);
  const [rumbleHistory, setRumbleHistory] = useState<RumbleResult[]>([]);

  useEffect(() => {
    fetchMonsters();
    fetchRumbleHistory();
  }, []);

  const fetchMonsters = async () => {
    setLoading(true);
    try {
      // Get list of player's monster IDs
      const playerMonstersResponse = await playerAPI.getMonsters();
      console.log('Player Monsters IDs Response:', playerMonstersResponse.data);
      
      // Get details of all monsters
      const allMonstersResponse = await monsterAPI.getAllMonsters();
      console.log('All Monsters Response:', allMonstersResponse.data);
      
      if (Array.isArray(playerMonstersResponse.data) && Array.isArray(allMonstersResponse.data)) {
        // Create mapping from monster ID to monster details
        const monstersMap: Record<string, any> = {};
        allMonstersResponse.data.forEach((monster: any) => {
          if (monster && monster.id) {
            monstersMap[monster.id] = monster;
          }
        });
        
        // Filter out player's owned monsters
        const playerMonsters = playerMonstersResponse.data
          .map(monsterId => monstersMap[monsterId])
          .filter(monster => monster !== undefined);
        
        console.log('Filtered Player Monsters:', playerMonsters);
        
        // Save all monster information, not just player-owned monsters
        setMonsters(allMonstersResponse.data);
        
        // Save player's monsters
        setPlayerMonsters(playerMonsters);
        
        // Set selectable monsters for player
        setSelectedMonsters([]);
      } else {
        console.warn('Monster data format incorrect:', 
          'Player monsters:', playerMonstersResponse.data, 
          'All monsters:', allMonstersResponse.data);
        setMonsters([]);
      }
    } catch (error) {
      console.error('Failed to fetch monster list:', error);
      message.error('Failed to fetch monster list');
    } finally {
      setLoading(false);
    }
  };

  const fetchRumbleHistory = async () => {
    try {
      console.log('Starting to fetch Royal Rumble history...');
      const response = await royalRumbleAPI.getAllRumbles();
      console.log('Rumble History Response:', response);
      
      if (response.data && Array.isArray(response.data)) {
        console.log(`Successfully retrieved ${response.data.length} Royal Rumble history records`);
        
        // Enhance history data to ensure participant information is complete
        const enhancedHistory = response.data.map(history => {
          // Ensure date information exists
          if (!history.battleDate) {
            history.battleDate = new Date().toISOString(); // Use current date as default
          }
          
          // If no participants array but has participantIds, create basic participants array
          if ((!history.participants || history.participants.length === 0) && history.participantIds && history.participantIds.length > 0) {
            history.participants = history.participantIds.map((id: string) => {
              // Try to find from monsters list
              const existingMonster = monsters.find(m => m.id === id);
              if (existingMonster) {
                return existingMonster;
              }
              
              // If not found, create basic Monster object
              return {
                id: id,
                element: 'unknown', // Use default element
                level: 1,
                hp: 0,
                atk: 0,
                def: 0,
                vit: 0
              };
            });
          }
          
          return history;
        });
        
        // Sort by date from newest to oldest
        const sortedHistory = enhancedHistory.sort((a, b) => {
          // If no date, put at end
          if (!a.battleDate) return 1;
          if (!b.battleDate) return -1;
          
          // Convert dates to timestamps for comparison
          const dateA = new Date(a.battleDate).getTime();
          const dateB = new Date(b.battleDate).getTime();
          
          // Sort descending (newest to oldest)
          return dateB - dateA;
        });
        
        setRumbleHistory(sortedHistory.slice(0, 10)); // Show latest 10
      } else {
        console.warn('Royal Rumble history data is not in array format:', response.data);
        setRumbleHistory([]);
      }
    } catch (error: any) {
      console.error('Failed to fetch Royal Rumble history:', error);
      if (error.response) {
        console.error('Error response:', error.response.status, error.response.data);
      } else if (error.request) {
        console.error('Request error:', error.request);
      } else {
        console.error('Error message:', error.message);
      }
      message.error('Failed to fetch Royal Rumble history');
    }
  };

  const startRumble = async () => {
    if (selectedMonsters.length < 3) {
      message.warning('Royal Rumble requires at least 3 monsters to participate');
      return;
    }

    setBattling(true);
    try {
      console.log('Starting Royal Rumble, selected monster IDs:', selectedMonsters);
      const response = await royalRumbleAPI.startRumble(selectedMonsters);
      
      console.log('Royal Rumble response:', response);
      
      if (response.data) {
        // Ensure battleLog field exists
        if (!response.data.battleLog) {
          response.data.battleLog = [];
        }
        
        // Ensure participants field exists
        if (!response.data.participants) {
          response.data.participants = [];
          // If participantIds exist, create basic Monster objects from IDs
          if (response.data.participantIds && response.data.participantIds.length > 0) {
            response.data.participants = response.data.participantIds.map((id: string) => {
              const monster = monsters.find(m => m.id === id);
              if (monster) {
                return monster;
              }
              return {
                id: id,
                element: 'unknown',
                level: 1,
                hp: 0,
                atk: 0,
                def: 0,
                vit: 0
              };
            });
          }
        }
        
        setRumbleResult(response.data);
        message.success('Royal Rumble completed!');
        // Refresh monster list and battle history
        fetchMonsters();
        fetchRumbleHistory();
      }
    } catch (error: any) {
      console.error('Royal Rumble failed:', error);
      if (error.response) {
        console.error('Error response:', error.response.status, error.response.data);
        message.error(`Royal Rumble failed: ${error.response.status} ${error.response.statusText || ''}`);
      } else if (error.request) {
        console.error('Request error:', error.request);
        message.error('Royal Rumble request failed, please check network connection');
      } else {
        console.error('Error message:', error.message);
        message.error(`Royal Rumble error: ${error.message}`);
      }
    } finally {
      setBattling(false);
    }
  };

  const getMonsterById = (id: string) => {
    return monsters.find(monster => monster.id === id);
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

  const handleMonsterSelection = (monster: Monster) => {
    setSelectedMonsters(prev => {
      // If monster is already selected, remove it
      if (prev.includes(monster.id)) {
        return prev.filter(id => id !== monster.id);
      }
      // Otherwise add it
      return [...prev, monster.id];
    });
  };

  const getMonsterName = (monster: Monster) => {
    if (!monster) return 'Unknown Monster';
    return generateMonsterName(monster.id, monster.element);
  };

  // Generate monster name from ID and possible element info
  const getMonsterNameById = (monsterId: string, element?: string) => {
    // First try to find from monsters list
    const monster = monsters.find(m => m.id === monsterId);
    if (monster) {
      return getMonsterName(monster);
    }
    
    // If monster not found but element info exists, can still generate name
    if (element) {
      return generateMonsterName(monsterId, element);
    }
    
    // If not enough info, try to generate name using ID
    return generateMonsterName(monsterId, 'unknown');
  };

  // Format date to show only year, month, day and time
  const formatDate = (dateString: string | Date | undefined) => {
    if (!dateString) return 'Unknown';
    try {
      return moment(dateString).format('YYYY-MM-DD HH:mm:ss');
    } catch (error) {
      console.error('Date formatting error:', error);
      return 'Unknown';
    }
  };

  const renderMonsterOption = (monster: Monster) => {
    // Generate monster name
    const monsterName = getMonsterName(monster);
    
    return (
      <Option key={monster.id} value={monster.id}>
        <span style={{ color: getElementColor(monster.element) }}>
          {monsterName} (Lv.{monster.level})
        </span>
        <span style={{ marginLeft: 8 }}>
          HP: {monster.hp} | ATK: {monster.atk} | DEF: {monster.def}
        </span>
      </Option>
    );
  };

  const renderMonsterTag = (monsterId: string) => {
    const monster = getMonsterById(monsterId);
    if (!monster) return null;
    
    // Generate monster name
    const monsterName = getMonsterName(monster);
    
    return (
      <Tag color={getElementColor(monster.element)} style={{ margin: '4px' }}>
        {monsterName} (Lv.{monster.level})
      </Tag>
    );
  };

  const renderMonsterCard = (monster: Monster, isSelected: boolean) => {
    // Generate monster name
    const monsterName = getMonsterName(monster);
    
    return (
      <Card
        key={monster.id}
        hoverable
        style={{
          width: 240,
          margin: '10px',
          border: isSelected ? '2px solid #1890ff' : '1px solid #f0f0f0',
        }}
        onClick={() => handleMonsterSelection(monster)}
      >
        <Card.Meta
          title={
            <span>
              <Tag color={getElementColor(monster.element)}>
                {monster.element}
              </Tag>
              {monsterName} (Lv.{monster.level})
            </span>
          }
          description={
            <div>
              <div>HP: {monster.hp}</div>
              <div>Attack: {monster.atk}</div>
              <div>Defense: {monster.def}</div>
              <div>Vitality: {monster.vit}</div>
            </div>
          }
        />
      </Card>
    );
  };

  const renderRumbleResult = () => {
    if (!rumbleResult) return null;
    
    return (
      <div style={{ marginTop: 20 }}>
        <Title level={3}>Battle Result</Title>
        <Card>
          <Paragraph>
            <Text strong>Winner: </Text>
            {getMonsterName(rumbleResult.winner)}
          </Paragraph>
          <Paragraph>
            <Text strong>Participants: </Text>
            {rumbleResult.participants.map(monster => getMonsterName(monster)).join(', ')}
          </Paragraph>
          <Collapse style={{ marginTop: 16 }}>
            <Panel header="View Detailed Battle Process" key="1">
              <Paragraph>
                <Text strong>Battle Log: </Text>
                <ul>
                  {rumbleResult.battleLog.map((log, index) => (
                    <li key={index}>{log}</li>
                  ))}
                </ul>
              </Paragraph>
            </Panel>
          </Collapse>
        </Card>
      </div>
    );
  };

  const renderRumbleHistory = () => {
    if (rumbleHistory.length === 0) {
      return <Empty description="No Royal Rumble history records" />;
    }
    
    return (
      <List
        itemLayout="horizontal"
        dataSource={rumbleHistory}
        renderItem={history => {
          // Try to get winner info from history
          const winner = history.winner || monsters.find(m => m.id === history.winnerId);
          
          // Ensure battleLog exists
          const battleLog = history.battleLog || [];
          
          return (
            <List.Item>
              <List.Item.Meta
                avatar={<TrophyOutlined style={{ fontSize: 24, color: '#faad14' }} />}
                title={`Royal Rumble - ${formatDate(history.battleDate)}`}
                description={
                  <div>
                    <p>Winner: {winner ? getMonsterName(winner) : getMonsterNameById(history.winnerId)}</p>
                    <p>Participants: {
                      history.participants && history.participants.length > 0 
                        ? history.participants.map(p => getMonsterName(p)).join(', ')
                        : history.participantIds.map(id => getMonsterNameById(id)).join(', ')
                    }</p>
                    <Collapse style={{ marginTop: 8 }}>
                      <Panel header="View Detailed Battle Process" key="1">
                        {battleLog.length > 0 ? (
                          <ul style={{ maxHeight: '200px', overflowY: 'auto', padding: '0 0 0 20px' }}>
                            {battleLog.map((log, index) => (
                              <li key={index}>{log}</li>
                            ))}
                          </ul>
                        ) : (
                          <Empty description="No detailed battle record available" />
                        )}
                      </Panel>
                    </Collapse>
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
    <div>
      <Title level={2}>Royal Rumble</Title>
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Card title="Select Participating Monsters">
            <Paragraph>
              Royal Rumble is a multi-monster battle where all monsters randomly attack other monsters until only one remains.
              The victorious monster will receive generous experience rewards! At least 3 monsters are required to participate.
            </Paragraph>
            
            {playerMonsters.length > 0 ? (
              <>
                <div style={{ marginBottom: 16 }}>
                  <Title level={5}>Select Participating Monsters</Title>
                  <Select
                    mode="multiple"
                    style={{ width: '100%' }}
                    placeholder="Select participating monsters (minimum 3)"
                    value={selectedMonsters}
                    onChange={setSelectedMonsters}
                  >
                    {playerMonsters.map(renderMonsterOption)}
                  </Select>
                </div>
                
                <div style={{ marginTop: 16, textAlign: 'center' }}>
                  <Button 
                    type="primary" 
                    icon={<TeamOutlined />} 
                    size="large"
                    loading={battling}
                    onClick={startRumble}
                    disabled={selectedMonsters.length < 3}
                  >
                    Start Royal Rumble
                  </Button>
                </div>
              </>
            ) : (
              <Empty description="You don't have any monsters yet. Please go to the Summon page to get monsters" />
            )}
          </Card>
          
          {renderRumbleResult()}
          {renderRumbleHistory()}
        </>
      )}
    </div>
  );
};

export default RoyalRumblePage;